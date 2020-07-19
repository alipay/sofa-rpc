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
import com.alipay.sofa.rpc.event.Event;
import com.alipay.sofa.rpc.event.EventBus;
import com.alipay.sofa.rpc.event.PrometheusSubscriber;
import com.alipay.sofa.rpc.event.ServerSendEvent;
import com.alipay.sofa.rpc.event.ServerStartedEvent;
import com.alipay.sofa.rpc.event.ServerStoppedEvent;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href=mailto:orezsilence@163.com>zhangchengxi</a>
 */
public class PrometheusModuleTest {

    private PrometheusModule prometheusModule = new PrometheusModule();

    @Test
    public void testNeedLoad() {
        Assert.assertTrue(prometheusModule.needLoad());
        System.setProperty("metrics.prometheus.enable", "false");
        RpcConfigs.reloadSystemProperties();
        Assert.assertFalse(prometheusModule.needLoad());
        System.setProperty("metrics.prometheus.enable", "true");
        RpcConfigs.reloadSystemProperties();
        Assert.assertTrue(prometheusModule.needLoad());
        System.setProperty("metrics.prometheus.enable", "xxx");
        RpcConfigs.reloadSystemProperties();
        Assert.assertFalse(prometheusModule.needLoad());
    }

    @Test
    public void testInstall() {
        System.setProperty("metrics.prometheus.port", "18999");
        RpcConfigs.reloadSystemProperties();
        PrometheusModuleForTest prometheusModuleForTest = new PrometheusModuleForTest();
        PrometheusSubscriberForTest subscriber = new PrometheusSubscriberForTest();
        prometheusModuleForTest.setPrometheusSubscriber(subscriber);

        Event clientEndInvokeEvent = new ClientEndInvokeEvent(null, null, null);
        Event serverSendEvent = new ServerSendEvent(null, null, null);
        Event serverStartedEvent = new ServerStartedEvent(null, null);
        Event serverStoppedEvent = new ServerStoppedEvent(null);
        EventBus.post(clientEndInvokeEvent);
        Assert.assertNull(subscriber.getEvent());
        EventBus.post(serverSendEvent);
        Assert.assertNull(subscriber.getEvent());
        EventBus.post(serverStartedEvent);
        Assert.assertNull(subscriber.getEvent());
        EventBus.post(serverStoppedEvent);
        Assert.assertNull(subscriber.getEvent());
        EventBus.post(new Event() {
        });
        Assert.assertNull(subscriber.getEvent());

        prometheusModuleForTest.install();
        EventBus.post(clientEndInvokeEvent);
        Assert.assertEquals(clientEndInvokeEvent, subscriber.getEvent());
        EventBus.post(serverSendEvent);
        Assert.assertEquals(serverSendEvent, subscriber.getEvent());
        EventBus.post(serverStartedEvent);
        Assert.assertEquals(serverStartedEvent, subscriber.getEvent());
        EventBus.post(serverStoppedEvent);
        Assert.assertEquals(serverStoppedEvent, subscriber.getEvent());
        subscriber.reset();
        EventBus.post(new Event() {
        });
        Assert.assertNull(subscriber.getEvent());

        prometheusModuleForTest.uninstall();
        subscriber.reset();
        EventBus.post(clientEndInvokeEvent);
        Assert.assertNull(subscriber.getEvent());
        EventBus.post(serverSendEvent);
        Assert.assertNull(subscriber.getEvent());
        EventBus.post(serverStartedEvent);
        Assert.assertNull(subscriber.getEvent());
        EventBus.post(serverStoppedEvent);
        Assert.assertNull(subscriber.getEvent());
        EventBus.post(new Event() {
        });
        Assert.assertNull(subscriber.getEvent());

    }

}