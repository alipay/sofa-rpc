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
import com.alipay.sofa.rpc.metrics.common.RpcAbstractMetricsModel;
import com.alipay.sofa.rpc.metrics.common.RpcClientMetricsModel;
import com.alipay.sofa.rpc.metrics.common.RpcServerMetricsModel;
import com.alipay.sofa.rpc.metrics.prometheus.ThreadPoolCollector;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author zhaowang
 * @version : MockPrometheusSubscriber.java, v 0.1 2020年04月04日 3:10 下午 zhaowang Exp $
 */
public class MockPrometheusSubscriber extends PrometheusSubscriber {

    public static final String      HANDLER_CLIENT_METRICS    = "handleClientMetrics";
    public static final String      HANDLER_SERVER_METRICS    = "handleServerMetrics";
    public static final String      HANDLE_SERVER_THREAD_POOL = "handleServerThreadPool";
    public static final String      REMOVE_THREAD_POOL        = "removeThreadPool";
    private String                  lastMassage;
    private RpcAbstractMetricsModel model;
    private String                  serverConfig;

    public String getLastMassage() {
        return lastMassage;
    }

    public void setLastMassage(String lastMassage) {
        this.lastMassage = lastMassage;
    }

    public RpcAbstractMetricsModel getModel() {
        return model;
    }

    public void setModel(RpcAbstractMetricsModel model) {
        this.model = model;
    }

    public String getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(String serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    protected void handleClientMetrics(RpcClientMetricsModel rpcClientMetricsModel) {
        this.lastMassage = HANDLER_CLIENT_METRICS;
        this.model = rpcClientMetricsModel;

    }

    @Override
    protected void handleServerMetrics(RpcServerMetricsModel rpcServerMetricsModel) {
        this.lastMassage = HANDLER_SERVER_METRICS;
    }

    @Override
    protected void handleServerThreadPool(ServerConfig serverConfig, ThreadPoolExecutor threadPoolExecutor) {
        this.lastMassage = HANDLE_SERVER_THREAD_POOL;
    }

    @Override
    protected void removeThreadPool(ServerConfig serverConfig) {
        this.lastMassage = REMOVE_THREAD_POOL;
    }

    public String[] testExtractClientLabels(RpcClientMetricsModel model) {
        return super.extractClientLabels(model);
    }

    public String[] testExtractServerLabels(RpcServerMetricsModel model) {
        return super.extractServerLabels(model);
    }

    public static ThreadPoolCollector getThreadCollector() {
        return THREAD_POOL_COLLECTOR;
    }

}