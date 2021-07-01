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
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.invoke.Invoker;
import org.junit.Assert;
import org.junit.Test;

public class UniqueIdInvokerTest {

    private static final String INTERFACE_ID  = "com.alipay.test.UniqueIdFacade";

    private static final String UNIQUE_ID_ONE = "unique-1";

    private static final String UNIQUE_ID_TWO = "unique-2";

    @Test(expected = SofaRpcException.class)
    public void test() {
        try {
            UniqueIdInvoker uniqueIdInvoker = new UniqueIdInvoker();

            ProviderConfig uniqueOne = new ProviderConfig();
            uniqueOne.setInterfaceId(INTERFACE_ID);
            uniqueOne.setUniqueId(UNIQUE_ID_ONE);
            uniqueIdInvoker.registerInvoker(uniqueOne, new UniqueIdTestInvoker(UNIQUE_ID_ONE));

            ProviderConfig uniqueTwo = new ProviderConfig();
            uniqueTwo.setInterfaceId(INTERFACE_ID);
            uniqueTwo.setUniqueId(UNIQUE_ID_TWO);
            uniqueIdInvoker.registerInvoker(uniqueTwo, new UniqueIdTestInvoker(UNIQUE_ID_TWO));

            SofaRequest sofaRequest = new SofaRequest();
            sofaRequest.setInterfaceName(INTERFACE_ID);
            RpcInvokeContext.getContext().put(TripleContants.SOFA_UNIQUE_ID, UNIQUE_ID_ONE);
            SofaResponse sofaResponse = uniqueIdInvoker.invoke(sofaRequest);
            String appResponse = (String) sofaResponse.getAppResponse();
            Assert.assertEquals(appResponse, UNIQUE_ID_ONE);

            RpcInvokeContext.getContext().put(TripleContants.SOFA_UNIQUE_ID, UNIQUE_ID_TWO);
            sofaResponse = uniqueIdInvoker.invoke(sofaRequest);
            appResponse = (String) sofaResponse.getAppResponse();
            Assert.assertEquals(appResponse, UNIQUE_ID_TWO);

            uniqueIdInvoker.unRegisterInvoker(uniqueTwo);

            RpcInvokeContext.getContext().put(TripleContants.SOFA_UNIQUE_ID, UNIQUE_ID_TWO);
            // except exception
            uniqueIdInvoker.invoke(sofaRequest);
        } finally {
            RpcInvokeContext.removeContext();
        }
    }

    private class UniqueIdTestInvoker implements Invoker {

        private String uniqueId;

        public UniqueIdTestInvoker(String uniqueId) {
            this.uniqueId = uniqueId;
        }

        @Override
        public SofaResponse invoke(SofaRequest request) throws SofaRpcException {
            SofaResponse sofaResponse = new SofaResponse();
            sofaResponse.setAppResponse(this.uniqueId);
            return sofaResponse;
        }

        public void setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
        }

        public String getUniqueId() {
            return uniqueId;
        }

        @Override
        public String toString() {
            return "UniqueIdInvoker{" +
                "uniqueId='" + uniqueId + '\'' +
                '}';
        }

    }

}