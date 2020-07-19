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
package com.alipay.sofa.rpc.event;

import com.alipay.sofa.rpc.config.ServerConfig;
import com.alipay.sofa.rpc.metrics.common.RpcClientMetricsModel;
import com.alipay.sofa.rpc.metrics.common.RpcServerMetricsModel;
import com.alipay.sofa.rpc.metrics.prometheus.ThreadPoolCollector;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * import org.junit.Assert;
 *
 * @author <a href=mailto:orezsilence@163.com>zhangchengxi</a>
 */
public class PrometheusSubscriberTest {

    private MockPrometheusSubscriber mock   = new MockPrometheusSubscriber();
    private PrometheusSubscriber     origin = new PrometheusSubscriber();

    @Test
    public void testOnEvent() {
        Event clientEndInvokeEvent = new ClientEndInvokeEvent(null, null, null);
        Event serverSendEvent = new ServerSendEvent(null, null, null);
        Event serverStartedEvent = new ServerStartedEvent(null, null);
        Event serverStoppedEvent = new ServerStoppedEvent(null);
        mock.onEvent(clientEndInvokeEvent);
        Assert.assertEquals(MockPrometheusSubscriber.HANDLER_CLIENT_METRICS, mock.getLastMassage());
        mock.onEvent(serverSendEvent);
        Assert.assertEquals(MockPrometheusSubscriber.HANDLER_SERVER_METRICS, mock.getLastMassage());
        mock.onEvent(serverStartedEvent);
        Assert.assertEquals(MockPrometheusSubscriber.HANDLE_SERVER_THREAD_POOL, mock.getLastMassage());
        mock.onEvent(serverStoppedEvent);
        Assert.assertEquals(MockPrometheusSubscriber.REMOVE_THREAD_POOL, mock.getLastMassage());
    }

    @Test
    public void testExtractInfoFromModule() {
        RpcClientMetricsModel rpcClientMetricsModel = new RpcClientMetricsModel();

        rpcClientMetricsModel.setApp("app");
        rpcClientMetricsModel.setService("service");
        rpcClientMetricsModel.setMethod("method");
        rpcClientMetricsModel.setProtocol("protocol");
        rpcClientMetricsModel.setInvokeType("invokeType");
        rpcClientMetricsModel.setTargetApp("targetApp");
        rpcClientMetricsModel.setSuccess(true);

        String[] labels = mock.testExtractClientLabels(rpcClientMetricsModel);
        Assert.assertEquals(7, labels.length);
        Assert.assertEquals("app", labels[0]);
        Assert.assertEquals("service", labels[1]);
        Assert.assertEquals("method", labels[2]);
        Assert.assertEquals("protocol", labels[3]);
        Assert.assertEquals("invokeType", labels[4]);
        Assert.assertEquals("targetApp", labels[5]);
        Assert.assertEquals("true", labels[6]);

        RpcServerMetricsModel rpcServerMetricsModel = new RpcServerMetricsModel();

        rpcServerMetricsModel.setApp("app");
        rpcServerMetricsModel.setService("service");
        rpcServerMetricsModel.setMethod("method");
        rpcServerMetricsModel.setProtocol("protocol");
        rpcServerMetricsModel.setCallerApp("callerApp");
        rpcServerMetricsModel.setSuccess(true);

        labels = mock.testExtractServerLabels(rpcServerMetricsModel);
        Assert.assertEquals("app", labels[0]);
        Assert.assertEquals("service", labels[1]);
        Assert.assertEquals("method", labels[2]);
        Assert.assertEquals("protocol", labels[3]);
        Assert.assertEquals("callerApp", labels[4]);
        Assert.assertEquals("true", labels[5]);
    }

    @Test
    public void testHandleServerThreadPool() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setProtocol("protocolA");
        serverConfig.setPort(9999);

        ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        origin.handleServerThreadPool(serverConfig, executorService);
        ThreadPoolCollector threadCollector = PrometheusSubscriber.THREAD_POOL_COLLECTOR;
        Map<String, ThreadPoolExecutor> queuedThreadPoolMap = threadCollector.getQueuedThreadPoolMap();
        Assert.assertTrue(queuedThreadPoolMap.containsKey("protocolA:9999"));
        Assert.assertEquals(executorService, queuedThreadPoolMap.get("protocolA:9999"));

        origin.removeThreadPool(serverConfig);
        Assert.assertFalse(queuedThreadPoolMap.containsKey("protocolA:9999"));
    }

}