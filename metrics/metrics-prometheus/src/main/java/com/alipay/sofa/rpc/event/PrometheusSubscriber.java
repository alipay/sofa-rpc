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

import com.alipay.sofa.rpc.common.utils.StringUtils;
import com.alipay.sofa.rpc.config.ServerConfig;
import com.alipay.sofa.rpc.metrics.common.MetricsConstants;
import com.alipay.sofa.rpc.metrics.common.MetricsHelper;
import com.alipay.sofa.rpc.metrics.common.RpcClientMetricsModel;
import com.alipay.sofa.rpc.metrics.common.RpcServerMetricsModel;
import com.alipay.sofa.rpc.metrics.prometheus.ThreadPoolCollector;
import io.prometheus.client.Counter;
import io.prometheus.client.Summary;

import java.util.concurrent.ThreadPoolExecutor;

import static com.alipay.sofa.rpc.metrics.common.MetricsConstants.APP;
import static com.alipay.sofa.rpc.metrics.common.MetricsConstants.CALLER_APP;
import static com.alipay.sofa.rpc.metrics.common.MetricsConstants.INVOKE_TYPE;
import static com.alipay.sofa.rpc.metrics.common.MetricsConstants.METHOD;
import static com.alipay.sofa.rpc.metrics.common.MetricsConstants.PROTOCOL;
import static com.alipay.sofa.rpc.metrics.common.MetricsConstants.SERVICE;
import static com.alipay.sofa.rpc.metrics.common.MetricsConstants.SUCCESS;
import static com.alipay.sofa.rpc.metrics.common.MetricsConstants.TARGET_APP;

/**
 * @author zhaowang
 * @version : PrometheusSubscriber.java, v 0.1 2020年04月01日 7:33 下午 zhaowang Exp $
 */
public class PrometheusSubscriber extends Subscriber {

    private static final String[]              CLIENT_LABELS                = { APP, SERVICE, METHOD, PROTOCOL,
                                                                            INVOKE_TYPE, TARGET_APP, SUCCESS };

    private static final String[]              SERVER_LABELS                = { APP, SERVICE, METHOD, PROTOCOL,
                                                                            CALLER_APP, SUCCESS };

    private static final Counter               CLIENT_COUNTER               = Counter
                                                                                .build()
                                                                                .name(MetricsConstants.CLIENT_COUNTER)
                                                                                .help("Client total request")
                                                                                .labelNames(CLIENT_LABELS).register();

    private static final Counter               SERVER_COUNTER               = Counter
                                                                                .build()
                                                                                .name(MetricsConstants.SERVER_COUNTER)
                                                                                .help("Server total request")
                                                                                .labelNames(SERVER_LABELS).register();

    protected static final ThreadPoolCollector THREAD_POOL_COLLECTOR        = new ThreadPoolCollector().register();

    private static final Summary               CLIENT_RT_SUMMARY            = new Summary.Builder()
                                                                                .name(MetricsConstants.CLIENT_RT)
                                                                                .quantile(0.5, 0.05) // Add 50th percentile (= median) with 5% tolerated error
                                                                                .quantile(0.99, 0.001) // Add 99th percentile with 0.1% tolerated error
                                                                                .quantile(0.999, 0.001) // Add 99th percentile with 0.01% tolerated error
                                                                                .help("Measure client rt")
                                                                                .labelNames(CLIENT_LABELS).register();
    private static final Summary               CLIENT_REQUEST_SIZE_SUMMARY  = new Summary.Builder()
                                                                                .name(
                                                                                    MetricsConstants.CLIENT_REQUEST_SIZE)
                                                                                .help("Measure client request size")
                                                                                .labelNames(CLIENT_LABELS).register();
    private static final Summary               CLIENT_RESPONSE_SIZE_SUMMARY = new Summary.Builder()
                                                                                .name(
                                                                                    MetricsConstants.CLIENT_RESPONSE_SIZE)
                                                                                .help("Measure client response size")
                                                                                .labelNames(CLIENT_LABELS).register();

    private static final Summary               SERVER_RT_SUMMARY            = new Summary.Builder()
                                                                                .name(MetricsConstants.SERVER_RT)
                                                                                .quantile(0.5, 0.05) // Add 50th percentile (= median) with 5% tolerated error
                                                                                .quantile(0.99, 0.001) // Add 99th percentile with 0.1% tolerated error
                                                                                .quantile(0.999, 0.001) // Add 99th percentile with 0.01% tolerated error
                                                                                .help("Measure server rt")
                                                                                .labelNames(SERVER_LABELS).register();

    @Override
    public void onEvent(Event event) {

        Class eventClass = event.getClass();

        if (eventClass == ClientEndInvokeEvent.class) {
            ClientEndInvokeEvent clientEndInvokeEvent = (ClientEndInvokeEvent) event;

            RpcClientMetricsModel rpcClientMetricsModel = MetricsHelper.createClientMetricsModel(
                clientEndInvokeEvent.getRequest(),
                clientEndInvokeEvent.getResponse());

            handleClientMetrics(rpcClientMetricsModel);
        } else if (eventClass == ServerSendEvent.class) {
            ServerSendEvent serverSendEvent = (ServerSendEvent) event;

            RpcServerMetricsModel rpcServerMetricsModel = MetricsHelper.createServerMetricsModel(
                serverSendEvent.getRequest(),
                serverSendEvent.getResponse());

            handleServerMetrics(rpcServerMetricsModel);
        } else if (eventClass == ServerStartedEvent.class) {
            ServerStartedEvent serverStartedEvent = (ServerStartedEvent) event;

            handleServerThreadPool(serverStartedEvent.getServerConfig(),
                serverStartedEvent.getThreadPoolExecutor());
        } else if (eventClass == ServerStoppedEvent.class) {
            ServerStoppedEvent serverStartedEvent = (ServerStoppedEvent) event;

            removeThreadPool(serverStartedEvent.getServerConfig());
        }
    }

    protected void handleClientMetrics(RpcClientMetricsModel rpcClientMetricsModel) {
        // count
        String[] labels = extractClientLabels(rpcClientMetricsModel);
        CLIENT_COUNTER.labels(labels).inc();
        // time
        CLIENT_RT_SUMMARY.labels(labels).observe(rpcClientMetricsModel.getElapsedTime());
        //size
        CLIENT_REQUEST_SIZE_SUMMARY.labels(labels).observe(rpcClientMetricsModel.getRequestSize());
        CLIENT_RESPONSE_SIZE_SUMMARY.labels(labels).observe(rpcClientMetricsModel.getResponseSize());
    }

    protected void handleServerMetrics(RpcServerMetricsModel rpcServerMetricsModel) {
        // count
        String[] labels = extractServerLabels(rpcServerMetricsModel);
        SERVER_COUNTER.labels(labels).inc();
        // time
        SERVER_RT_SUMMARY.labels(labels).observe(rpcServerMetricsModel.getElapsedTime());
    }

    protected String[] extractClientLabels(RpcClientMetricsModel model) {
        String[] labels = new String[7];

        labels[0] = StringUtils.defaultString(model.getApp());
        labels[1] = StringUtils.defaultString(model.getService());
        labels[2] = StringUtils.defaultString(model.getMethod());
        labels[3] = StringUtils.defaultString(model.getProtocol());
        labels[4] = StringUtils.defaultString(model.getInvokeType());
        labels[5] = StringUtils.defaultString(model.getTargetApp());
        labels[6] = StringUtils.defaultString(model.getSuccess());

        return labels;
    }

    protected String[] extractServerLabels(RpcServerMetricsModel model) {
        String[] labels = new String[6];
        labels[0] = StringUtils.defaultString(model.getApp());
        labels[1] = StringUtils.defaultString(model.getService());
        labels[2] = StringUtils.defaultString(model.getMethod());
        labels[3] = StringUtils.defaultString(model.getProtocol());
        labels[4] = StringUtils.defaultString(model.getCallerApp());
        labels[5] = StringUtils.defaultString(model.getSuccess());
        return labels;
    }

    protected void handleServerThreadPool(ServerConfig serverConfig, ThreadPoolExecutor threadPoolExecutor) {
        String name = extractThreadPoolName(serverConfig);
        THREAD_POOL_COLLECTOR.add(threadPoolExecutor, name);
    }

    private String extractThreadPoolName(ServerConfig serverConfig) {
        int port = serverConfig.getPort();
        String protocol = serverConfig.getProtocol();
        return protocol + ":" + port;
    }

    protected void removeThreadPool(ServerConfig serverConfig) {
        THREAD_POOL_COLLECTOR.remove(extractThreadPoolName(serverConfig));
    }
}