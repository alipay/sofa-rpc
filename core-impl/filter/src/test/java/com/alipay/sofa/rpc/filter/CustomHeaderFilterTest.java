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

import com.alipay.sofa.rpc.config.AbstractInterfaceConfig;
import com.alipay.sofa.rpc.context.RpcInvokeContext;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * @author zhaowang
 * @version : CustomHeaderFilterTest.java, v 0.1 2021年06月23日 2:26 下午 zhaowang
 */
public class CustomHeaderFilterTest {

    @Test
    public void testCustomFilter() {
        RpcInvokeContext.getContext().addCustomHeader("a", "b");
        ConsumerCustomHeaderFilter filter = new ConsumerCustomHeaderFilter();
        SofaRequest request = new SofaRequest();
        filter.invoke(new EmptyInvoker(null), request);
        Assert.assertTrue(RpcInvokeContext.getContext().getCustomHeader().isEmpty());
        Assert.assertEquals("b", request.getRequestProp("a"));
    }

    static class EmptyInvoker extends FilterInvoker {

        public Map<String, String> getMetaHolder() {
            return metaHolder;
        }

        private Map<String, String> metaHolder;

        protected EmptyInvoker(AbstractInterfaceConfig config) {
            super(config);
        }

        @Override
        public SofaResponse invoke(SofaRequest request) throws SofaRpcException {
            return null;
        }
    }

}