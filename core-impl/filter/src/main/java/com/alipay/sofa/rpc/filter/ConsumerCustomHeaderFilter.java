/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.rpc.filter;

import com.alipay.sofa.common.utils.Ordered;
import com.alipay.sofa.rpc.common.utils.CommonUtils;
import com.alipay.sofa.rpc.context.RpcInvokeContext;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.ext.Extension;

import java.util.Map;

/**
 * Set customHeader to  SofaRequest.requestProps
 *
 * @author zhaowang
 * @version : ConsumerCustomHeaderFilter.java, v 0.1 2021年06月23日 2:05 下午 zhaowang
 */
@AutoActive(consumerSide = true)
@Extension(value = "consumerCustomHeader", order = Ordered.LOWEST_PRECEDENCE)
public class ConsumerCustomHeaderFilter extends Filter {

    @Override
    public SofaResponse invoke(FilterInvoker invoker, SofaRequest request) throws SofaRpcException {
        setCustomHeader(request);
        return invoker.invoke(request);
    }

    private void setCustomHeader(SofaRequest sofaRequest) {
        RpcInvokeContext context = RpcInvokeContext.getContext();
        Map customHeader = context.getCustomHeader();
        if (CommonUtils.isNotEmpty(customHeader)) {
            sofaRequest.addRequestProps(customHeader);
        }
        context.clearCustomHeader();
    }
}