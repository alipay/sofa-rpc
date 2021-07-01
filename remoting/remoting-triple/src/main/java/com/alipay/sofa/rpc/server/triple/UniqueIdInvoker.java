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
package com.alipay.sofa.rpc.server.triple;

import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.context.RpcInvokeContext;
import com.alipay.sofa.rpc.core.exception.RpcErrorType;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.invoke.Invoker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * unique id invoker
 *
 * @author huicha
 * @version : UniqueIdInvoker.java 2020/12/24
 */
public class UniqueIdInvoker implements Invoker {

    private Map<String, Invoker> uniqueIdInvokerMap;

    public UniqueIdInvoker() {
        this.uniqueIdInvokerMap = new ConcurrentHashMap<>();
    }

    /**
     * register invoker with unique id from provider config
     *
     * @param providerConfig provider config
     * @param invoker        invoker
     * @return register success or not
     */
    public boolean registerInvoker(ProviderConfig providerConfig, Invoker invoker) {
        String uniqueId = this.getUniqueId(providerConfig);
        return this.uniqueIdInvokerMap.putIfAbsent(uniqueId, invoker) == null;
    }

    /**
     * unregister invoker with unique id from provider config
     *
     * @param providerConfig provider config
     * @return unregister success or not
     */
    public boolean unRegisterInvoker(ProviderConfig providerConfig) {
        String uniqueId = this.getUniqueId(providerConfig);
        return this.uniqueIdInvokerMap.remove(uniqueId) != null;
    }

    /**
     * get unique id from provider config, this function won't return null
     *
     * @param providerConfig provider config
     * @return unique id in provider config
     */
    private String getUniqueId(ProviderConfig providerConfig) {
        String uniqueId = providerConfig.getUniqueId();
        return null == uniqueId ? "" : uniqueId;
    }

    /**
     * get unique id from rpc invoke context
     *
     * @return unique id from rpc invoke context
     */
    private String getUniqueIdFromInvokeContext() {
        String uniqueId = (String) RpcInvokeContext.getContext().get(TripleContants.SOFA_UNIQUE_ID);
        return uniqueId == null ? "" : uniqueId;
    }

    @Override
    public SofaResponse invoke(SofaRequest request) throws SofaRpcException {
        String interfaceName = request.getInterfaceName();
        String uniqueId = this.getUniqueIdFromInvokeContext();

        if (this.uniqueIdInvokerMap.isEmpty()) {
            // invoker map is empty, can not find any invoker
            throw new SofaRpcException(RpcErrorType.SERVER_NOT_FOUND_INVOKER, "Cannot find service of [" +
                interfaceName + "] with uniqueId [" + uniqueId + "]");
        }

        // find unique id, try find invoker
        Invoker invoker = this.uniqueIdInvokerMap.get(uniqueId);
        if (null == invoker) {
            // can not find invoker
            throw new SofaRpcException(RpcErrorType.SERVER_NOT_FOUND_INVOKER, "Cannot find service of [" +
                interfaceName + "] with uniqueId [" + uniqueId + "]");
        }

        // find invoker, invoke
        return invoker.invoke(request);
    }

}
