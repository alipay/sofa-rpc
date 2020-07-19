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
package com.alipay.sofa.rpc.metrics.common;

import com.alipay.sofa.rpc.common.RemotingConstants;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.context.RpcInternalContext;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href=mailto:orezsilence@163.com>zhangchengxi</a>
 */
public class MetricsHelperTest {

    public static final String  APP           = "App_1";
    public static final String  SERVICE       = "Service_1";
    public static final String  METHOD        = "Method_1";
    public static final String  PROTOCOL      = "Protocol_1";
    public static final String  INVOKE_TYPE   = "InvokeType_1";
    public static final String  TARGET_APP    = "TargetApp_1";
    public static final String  CALLER_APP    = "CallerApp_1";
    public static final Long    REQUEST_SIZE  = 12345L;
    public static final Long    RESPONSE_SIZE = 54321L;
    public static final Long    ELAPSED_TIME  = 123L;
    public static final boolean SUCCESS       = true;

    @Test
    public void createClientMetricsModel() {
        // test null
        RpcClientMetricsModel clientMetricsModel = MetricsHelper.createClientMetricsModel(null, null);
        Assert.assertNull(clientMetricsModel.getApp());
        Assert.assertNull(clientMetricsModel.getService());

        // test Request
        RpcInternalContext context = RpcInternalContext.getContext();
        context.setAttachment(RpcConstants.INTERNAL_KEY_APP_NAME, APP);
        context.setAttachment(RpcConstants.INTERNAL_KEY_PROTOCOL_NAME, PROTOCOL);
        context.setAttachment(RpcConstants.INTERNAL_KEY_REQ_SIZE, REQUEST_SIZE);
        context.setAttachment(RpcConstants.INTERNAL_KEY_RESP_SIZE, RESPONSE_SIZE);
        context.setAttachment(RpcConstants.INTERNAL_KEY_CLIENT_ELAPSE, ELAPSED_TIME);

        SofaRequest sofaRequest = new SofaRequest();
        sofaRequest.setTargetServiceUniqueName(SERVICE);
        sofaRequest.setMethodName(METHOD);
        sofaRequest.setInvokeType(INVOKE_TYPE);
        sofaRequest.setTargetAppName(TARGET_APP);

        SofaResponse sofaResponse = new SofaResponse();

        clientMetricsModel = MetricsHelper.createClientMetricsModel(sofaRequest, sofaResponse);

        Assert.assertEquals(APP, clientMetricsModel.getApp());
        Assert.assertEquals(SERVICE, clientMetricsModel.getService());
        Assert.assertEquals(METHOD, clientMetricsModel.getMethod());
        Assert.assertEquals(PROTOCOL, clientMetricsModel.getProtocol());
        Assert.assertEquals(INVOKE_TYPE, clientMetricsModel.getInvokeType());
        Assert.assertEquals(TARGET_APP, clientMetricsModel.getTargetApp());
        Assert.assertEquals(REQUEST_SIZE, clientMetricsModel.getRequestSize());
        Assert.assertEquals(RESPONSE_SIZE, clientMetricsModel.getResponseSize());
        Assert.assertEquals(ELAPSED_TIME, clientMetricsModel.getElapsedTime());
        Assert.assertEquals(SUCCESS, clientMetricsModel.getSuccess());

        clientMetricsModel = MetricsHelper.createClientMetricsModel(sofaRequest, null);
        assertFalse(clientMetricsModel.getSuccess());
        sofaResponse.setErrorMsg("error");
        clientMetricsModel = MetricsHelper.createClientMetricsModel(sofaRequest, sofaResponse);
        assertFalse(clientMetricsModel.getSuccess());

        sofaResponse = new SofaResponse();
        clientMetricsModel = MetricsHelper.createClientMetricsModel(sofaRequest, sofaResponse);
        assertTrue(clientMetricsModel.getSuccess());

        sofaResponse.setAppResponse(new Exception());
        clientMetricsModel = MetricsHelper.createClientMetricsModel(sofaRequest, sofaResponse);
        assertFalse(clientMetricsModel.getSuccess());

    }

    @Test
    public void createServerMetricsModel() {
        // test null
        RpcServerMetricsModel rpcServerModel = MetricsHelper.createServerMetricsModel(null, null);
        Assert.assertNull(rpcServerModel.getApp());
        Assert.assertNull(rpcServerModel.getService());

        // test Request
        RpcInternalContext context = RpcInternalContext.getContext();
        context.setAttachment(RpcConstants.INTERNAL_KEY_REQ_SIZE, REQUEST_SIZE);
        context.setAttachment(RpcConstants.INTERNAL_KEY_RESP_SIZE, RESPONSE_SIZE);
        context.setAttachment(RpcConstants.INTERNAL_KEY_IMPL_ELAPSE, ELAPSED_TIME);

        SofaRequest sofaRequest = new SofaRequest();
        sofaRequest.setTargetAppName(APP);
        sofaRequest.setTargetServiceUniqueName(SERVICE);
        sofaRequest.setMethodName(METHOD);
        sofaRequest.setInvokeType(INVOKE_TYPE);
        sofaRequest.addRequestProp(RemotingConstants.HEAD_APP_NAME, CALLER_APP);
        sofaRequest.addRequestProp(RemotingConstants.HEAD_PROTOCOL, PROTOCOL);

        SofaResponse sofaResponse = new SofaResponse();

        rpcServerModel = MetricsHelper.createServerMetricsModel(sofaRequest, sofaResponse);

        Assert.assertEquals(APP, rpcServerModel.getApp());
        Assert.assertEquals(SERVICE, rpcServerModel.getService());
        Assert.assertEquals(METHOD, rpcServerModel.getMethod());
        Assert.assertEquals(PROTOCOL, rpcServerModel.getProtocol());
        Assert.assertEquals(INVOKE_TYPE, rpcServerModel.getInvokeType());
        Assert.assertEquals(CALLER_APP, rpcServerModel.getCallerApp());
        Assert.assertEquals(ELAPSED_TIME, rpcServerModel.getElapsedTime());
        Assert.assertEquals(SUCCESS, rpcServerModel.getSuccess());

        rpcServerModel = MetricsHelper.createServerMetricsModel(sofaRequest, null);
        assertFalse(rpcServerModel.getSuccess());
        sofaResponse.setErrorMsg("error");
        rpcServerModel = MetricsHelper.createServerMetricsModel(sofaRequest, sofaResponse);
        assertFalse(rpcServerModel.getSuccess());

        sofaResponse = new SofaResponse();
        rpcServerModel = MetricsHelper.createServerMetricsModel(sofaRequest, sofaResponse);
        assertTrue(rpcServerModel.getSuccess());

        sofaResponse.setAppResponse(new Exception());
        rpcServerModel = MetricsHelper.createServerMetricsModel(sofaRequest, sofaResponse);
        assertFalse(rpcServerModel.getSuccess());

    }
}