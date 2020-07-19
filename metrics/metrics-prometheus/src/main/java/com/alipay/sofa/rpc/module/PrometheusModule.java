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
package com.alipay.sofa.rpc.module;

import com.alipay.sofa.rpc.common.RpcConfigs;
import com.alipay.sofa.rpc.event.ClientEndInvokeEvent;
import com.alipay.sofa.rpc.event.EventBus;
import com.alipay.sofa.rpc.event.PrometheusSubscriber;
import com.alipay.sofa.rpc.event.ServerSendEvent;
import com.alipay.sofa.rpc.event.ServerStartedEvent;
import com.alipay.sofa.rpc.event.ServerStoppedEvent;
import com.alipay.sofa.rpc.ext.Extension;
import com.alipay.sofa.rpc.log.Logger;
import com.alipay.sofa.rpc.log.LoggerFactory;
import io.prometheus.client.exporter.HTTPServer;

import static com.alipay.sofa.rpc.common.RpcOptions.METRICS_PROMETHEUS_ENABLE;
import static com.alipay.sofa.rpc.common.RpcOptions.METRICS_PROMETHEUS_PORT;

/**
 * @author zhaowang
 * @version : PrometheusModule.java, v 0.1 2020年04月01日 7:34 下午 zhaowang Exp $
 */
@Extension("prometheus")
public class PrometheusModule implements Module {

    private static final Logger  LOGGER = LoggerFactory.getLogger(PrometheusModule.class);

    private PrometheusSubscriber subscriber;
    private HTTPServer           httpServer;

    @Override
    public boolean needLoad() {
        return prometheusClientExist();
    }

    private static boolean prometheusClientExist() {
        try {
            Class.forName("io.prometheus.client.CollectorRegistry");
            return RpcConfigs.getBooleanValue(METRICS_PROMETHEUS_ENABLE);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void install() {
        subscriber = getSubscriber();
        EventBus.register(ClientEndInvokeEvent.class, subscriber);
        EventBus.register(ServerSendEvent.class, subscriber);
        EventBus.register(ServerStartedEvent.class, subscriber);
        EventBus.register(ServerStoppedEvent.class, subscriber);

        try {
            httpServer = new HTTPServer(RpcConfigs.getIntValue(METRICS_PROMETHEUS_PORT));
        } catch (Exception e) {
            LOGGER.error("Filed to start prometheus exporter http server.", e);
            throw new RuntimeException(e);
        }
    }

    protected PrometheusSubscriber getSubscriber() {
        return new PrometheusSubscriber();
    }

    @Override
    public void uninstall() {
        EventBus.unRegister(ClientEndInvokeEvent.class, subscriber);
        EventBus.unRegister(ServerSendEvent.class, subscriber);
        EventBus.unRegister(ServerStartedEvent.class, subscriber);
        EventBus.unRegister(ServerStoppedEvent.class, subscriber);
        httpServer.stop();
        httpServer = null;
    }
}