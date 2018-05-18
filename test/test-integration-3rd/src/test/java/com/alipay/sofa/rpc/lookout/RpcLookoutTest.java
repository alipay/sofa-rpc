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
package com.alipay.sofa.rpc.lookout;

import com.alipay.lookout.api.Lookout;
import com.alipay.lookout.api.Measurement;
import com.alipay.lookout.api.Metric;
import com.alipay.lookout.api.Registry;
import com.alipay.lookout.api.Tag;
import com.alipay.lookout.core.DefaultRegistry;
import com.alipay.sofa.rpc.api.future.SofaResponseFuture;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.MethodConfig;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.RegistryConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.invoke.SofaResponseCallback;
import com.alipay.sofa.rpc.core.request.RequestBase;
import com.alipay.sofa.rpc.event.LookoutSubscriber;
import com.alipay.sofa.rpc.test.ActivelyDestroyTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author <a href="mailto:lw111072@antfin.com">LiWei.Liangen</a>
 */
public class RpcLookoutTest extends ActivelyDestroyTest {

    static Field corePoolSize;
    static Field maxPoolSize;
    static Field queueSize;

    @BeforeClass
    public static void beforeClass() throws IllegalAccessException, NoSuchFieldException {
        try {
            Class clazz = RpcLookout.class;
            Class[] innerClazzs = clazz.getDeclaredClasses();
            for (Class cls : innerClazzs) {
                if (cls.getName().contains("ThreadPoolConfig")) {
                    corePoolSize = cls.getDeclaredField("corePoolSize");
                    corePoolSize.setAccessible(true);

                    maxPoolSize = cls.getDeclaredField("maxPoolSize");
                    maxPoolSize.setAccessible(true);

                    queueSize = cls.getDeclaredField("queueSize");
                    queueSize.setAccessible(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Registry registry = new DefaultRegistry();
        Lookout.setRegistry(registry);

        LookoutSubscriber.setLookoutCollectDisable(false);
    }

    @Test
    public void testAll() throws Exception {

        invoke();

        Thread.sleep(5000);

        for (Metric metric : Lookout.registry()) {
            if (metric.id().name().equals("rpc.bolt.threadpool.config")) {
                testThreadPoolConfig(metric);
            } else if (metric.id().name().equals("rpc.bolt.threadpool.active.count")) {
                testThreadPoolActiveCount(metric);
            } else if (metric.id().name().equals("rpc.bolt.threadpool.idle.count")) {
                testThreadPoolIdleCount(metric);
            } else if (metric.id().name().equals("rpc.bolt.threadpool.queue.size")) {
                testThreadPoolQueueSize(metric);
            } else if (metric.id().name().equals("rpc.provider.service.stats")) {
                testProviderServiceStats(metric);
            } else if (metric.id().name().equals("rpc.consumer.service.stats")) {
                testConsumerServiceStats(metric);
            }
        }
    }

    /**
     * invoke
     */
    private void invoke() throws InterruptedException {
        RegistryConfig registryConfig = new RegistryConfig()
            .setProtocol("zookeeper")
            .setAddress("127.0.0.1:2181");

        ServerConfig serverConfig = new ServerConfig()
            .setPort(12200)
            .setCoreThreads(30)
            .setMaxThreads(500)
            .setQueues(600)
            .setProtocol(RpcConstants.PROTOCOL_TYPE_BOLT);

        ProviderConfig<LookoutService> providerConfig = new ProviderConfig<LookoutService>()
            .setInterfaceId(LookoutService.class.getName())
            .setRef(new LookoutServiceImpl())
            .setServer(serverConfig)
            .setBootstrap("bolt")
            .setRegister(true)
            .setRegistry(registryConfig)
            .setApplication(new ApplicationConfig().setAppName("TestLookOutServer"));
        providerConfig.export();

        MethodConfig methodConfigFuture = new MethodConfig()
            .setName("sayFuture")
            .setInvokeType("future");
        MethodConfig methodConfigCallback = new MethodConfig()
            .setName("sayCallback")
            .setInvokeType("callback")
            .setOnReturn(new SofaResponseCallback() {
                @Override
                public void onAppResponse(Object appResponse, String methodName, RequestBase request) {

                }

                @Override
                public void onAppException(Throwable throwable, String methodName, RequestBase request) {

                }

                @Override
                public void onSofaException(SofaRpcException sofaException, String methodName, RequestBase request) {

                }
            });
        MethodConfig methodConfigOneway = new MethodConfig()
            .setName("sayOneway")
            .setInvokeType("oneway");
        List<MethodConfig> methodConfigs = new ArrayList<MethodConfig>();
        methodConfigs.add(methodConfigFuture);
        methodConfigs.add(methodConfigCallback);
        methodConfigs.add(methodConfigOneway);

        ConsumerConfig<LookoutService> consumerConfig = new ConsumerConfig<LookoutService>()
            .setInterfaceId(LookoutService.class.getName())
            .setProtocol("bolt")
            .setBootstrap("bolt")
            .setMethods(methodConfigs)
            .setTimeout(3000)
            .setRegister(true)
            .setRegistry(registryConfig)
            .setApplication(new ApplicationConfig().setAppName("TestLookOutClient"));
        LookoutService lookoutService = consumerConfig.refer();

        //sync
        for (int i = 0; i < 3; i++) {
            try {
                lookoutService.saySync("lookout_sync");
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        //future
        for (int i = 0; i < 4; i++) {
            try {
                lookoutService.sayFuture("lookout_future");
                SofaResponseFuture.getResponse(3000, true);
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        //callback
        for (int i = 0; i < 5; i++) {
            try {
                lookoutService.sayCallback("lookout_callback");
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        //oneway
        for (int i = 0; i < 6; i++) {
            try {
                lookoutService.sayOneway("lookout_oneway");
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    /**
     * test thread pool config
     * @param metric
     * @throws Exception
     */
    private void testThreadPoolConfig(Metric metric) throws Exception {
        Collection<Measurement> measurements = metric.measure().measurements();
        assertTrue(measurements.size() == 1);
        for (Measurement measurement : measurements) {

            // 判断ThreadPool启动配置
            Object obj = measurement.value();

            assertEquals(30, corePoolSize.get(obj));
            assertEquals(500, maxPoolSize.get(obj));
            assertEquals(600, queueSize.get(obj));
        }
    }

    /**
     * test thread pool active count
     * @param metric
     * @throws Exception
     */
    private void testThreadPoolActiveCount(Metric metric) throws Exception {
        Thread.sleep(3500);

        Collection<Measurement> measurements = metric.measure().measurements();
        assertTrue(measurements.size() == 1);
        for (Measurement measurement : measurements) {
            assertEquals(0, ((Number) measurement.value()).intValue());
        }
    }

    /**
     * test thread pool idle count
     * @param metric
     */
    private void testThreadPoolIdleCount(Metric metric) {
        Collection<Measurement> measurements = metric.measure().measurements();
        assertTrue(measurements.size() == 1);
        for (Measurement measurement : measurements) {
            assertEquals(3 + 4 + 5 + 6, ((Number) measurement.value()).intValue());
        }
    }

    /**
     * test thread pool queue size
     * @param metric
     */
    private void testThreadPoolQueueSize(Metric metric) {
        Collection<Measurement> measurements = metric.measure().measurements();
        assertTrue(measurements.size() == 1);
        for (Measurement measurement : measurements) {
            assertEquals(0, ((Number) measurement.value()).intValue());
        }
    }

    /**
     * test provider service stats
     * @param metric
     * @throws InterruptedException
     */
    private void testProviderServiceStats(Metric metric) throws InterruptedException {

        for (Tag tag : metric.id().tags()) {
            if (tag.key() == "method") {
                String methodName = tag.value();

                if (methodName.equals("saySync")) {
                    assertMethod(metric, true, 3, "saySync", 0, 0);

                } else if (methodName.equals("sayFuture")) {
                    assertMethod(metric, true, 4, "sayFuture", 0, 0);

                } else if (methodName.equals("sayCallback")) {
                    assertMethod(metric, true, 5, "sayCallback", 0, 0);

                } else if (methodName.equals("sayOneway")) {
                    assertMethod(metric, true, 6, "sayOneway", 0, 0);

                }
            }
        }
    }

    /**
     * test consumer service stats
     * @param metric
     * @throws InterruptedException
     */
    private void testConsumerServiceStats(Metric metric) throws InterruptedException {

        for (Tag tag : metric.id().tags()) {
            if (tag.key() == "method") {
                String methodName = tag.value();

                if (methodName.equals("saySync")) {
                    assertMethod(metric, false, 3, "saySync", 1227, 352);

                } else if (methodName.equals("sayFuture")) {
                    assertMethod(metric, false, 4, "sayFuture", 1652, 534);

                } else if (methodName.equals("sayCallback")) {
                    assertMethod(metric, false, 5, "sayCallback", 2085, 720);

                } else if (methodName.equals("sayOneway")) {
                    assertMethod(metric, false, 6, "sayOneway", 2478, 0);

                }
            }
        }
    }

    /**
     * assert method
     * @param metric the metric
     * @param isProvider is it the provider
     * @param totalCount the total invoke count
     * @param method the method name
     * @param requestSize the request size
     * @param responseSize the response size
     */
    private void assertMethod(Metric metric, boolean isProvider, int totalCount, String method, int requestSize,
                              int responseSize) {
        // tag
        boolean tagAssert = false;
        for (Tag tag : metric.id().tags()) {

            String key = tag.key();
            String value = tag.value();
            if (key.equals("service")) {
                assertEquals("com.alipay.sofa.rpc.lookout.LookoutService:1.0", value);
                tagAssert = true;
            }
            if (key.equals("protocol")) {
                assertEquals("bolt", value);
                tagAssert = true;
            }
            if (key.equals("method")) {
                assertEquals(method, value);
                tagAssert = true;
            }
            if (isProvider) {
                if (key.equals("app")) {
                    assertEquals("TestLookOutServer", value);
                    tagAssert = true;
                }
                if (key.equals("caller_app")) {
                    assertEquals("TestLookOutClient", value);
                    tagAssert = true;
                }
            } else {
                if (key.equals("app")) {
                    assertEquals("TestLookOutClient", value);
                    tagAssert = true;
                }
                if (key.equals("target_app")) {
                    assertEquals("TestLookOutServer", value);
                    tagAssert = true;
                }
                if (key.equals("invoke_type")) {
                    assertEquals(method.substring(3).toLowerCase(), value);

                }
            }
        }
        if (!tagAssert) {
            Assert.fail();
        }

        // invoke info
        Collection<Measurement> measurements = metric.measure().measurements();
        if (isProvider) {
            assertEquals(6, measurements.size());
        } else {
            if (method.equals("sayOneway")) {
                assertEquals(5, measurements.size());
            } else {
                assertEquals(10, measurements.size());
            }
        }

        boolean invokeInfoAssert = false;
        for (Measurement measurement : measurements) {
            String name = measurement.name();
            int value = ((Long) measurement.value()).intValue();

            if (name.equals("total_count")) {
                assertEquals(totalCount, value);
                invokeInfoAssert = true;
            }
            if (name.equals("total_time.totalTime")) {
                if (method.equals("sayOneway") && !isProvider) {
                    assertTrue(value < 3000);
                } else {
                    assertTrue(value > 3000);
                }
                invokeInfoAssert = true;
            }
            if (name.equals("total_time.count")) {
                assertEquals(totalCount, value);
                invokeInfoAssert = true;
            }
            if (name.equals("fail_count")) {
                assertEquals(1, value);
                invokeInfoAssert = true;
            }
            if (name.equals("fail_time.totalTime")) {
                assertTrue(value > 3000);
                invokeInfoAssert = true;
            }
            if (name.equals("fail_time.count")) {
                assertEquals(1, value);
                invokeInfoAssert = true;
            }
            if (!isProvider) {
                if (name.equals("request_size.count")) {
                    assertTrue(value == requestSize || value == totalCount);
                    invokeInfoAssert = true;
                }
                if (name.equals("response_size.count")) {
                    assertTrue(value == responseSize || value == totalCount - 1);
                    invokeInfoAssert = true;
                }
            }
        }
        if (!invokeInfoAssert) {
            Assert.fail();
        }
    }
}