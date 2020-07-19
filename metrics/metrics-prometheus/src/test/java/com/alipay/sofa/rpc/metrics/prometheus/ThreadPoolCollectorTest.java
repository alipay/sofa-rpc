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
package com.alipay.sofa.rpc.metrics.prometheus;

import com.alipay.sofa.rpc.event.MockPrometheusSubscriber;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author <a href=mailto:orezsilence@163.com>zhangchengxi</a>
 */
public class ThreadPoolCollectorTest {

    @Test
    public void test() {
        // 加载 PrometheusSubscriber 类,初始化 ThreadPoolCollector
        ThreadPoolCollector collector = MockPrometheusSubscriber.getThreadCollector();
        ThreadPoolExecutor threadPoolExecutor = ((ThreadPoolExecutor) Executors.newFixedThreadPool(10));
        collector.add(threadPoolExecutor, "name_a");
        Map<String, ThreadPoolExecutor> queuedThreadPoolMap = collector.getQueuedThreadPoolMap();
        Assert.assertFalse(queuedThreadPoolMap.isEmpty());

        List<Collector.MetricFamilySamples> collect = collector.collect();
        System.out.println(collect);
        String[] labelNames = {"name"};
        String[] labelValues = {"name_a"};
        double threadPoolSize = CollectorRegistry.defaultRegistry.getSampleValue("thread_pool_size", labelNames, labelValues);
        Assert.assertEquals(0.0, threadPoolSize, 0.001);

        threadPoolExecutor.execute(() -> System.out.println("hello"));
        threadPoolSize = CollectorRegistry.defaultRegistry.getSampleValue("thread_pool_size", labelNames, labelValues);
        CollectorRegistry.defaultRegistry.getSampleValue("thread_pool_size", labelNames, labelValues);
        Assert.assertEquals(1.0, threadPoolSize, 0.001);
        Assert.assertEquals(10.0, CollectorRegistry.defaultRegistry.getSampleValue("thread_pool_max", labelNames, labelValues), 0.01);
        Assert.assertEquals(0.0, CollectorRegistry.defaultRegistry.getSampleValue("thread_pool_queue_size", labelNames, labelValues), 0.01);

    }
}