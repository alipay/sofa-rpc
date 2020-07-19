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

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

/**
 * @author zhaowang
 * @version : ThrealPoolCollector.java, v 0.1 2020年04月02日 10:58 上午 zhaowang Exp $
 */
public class ThreadPoolCollector extends Collector {


    private static final List<String> LABEL_NAMES = Collections.singletonList("name");
    private final Map<String, ThreadPoolExecutor> queuedThreadPoolMap = new ConcurrentHashMap<>();


    public ThreadPoolCollector add(ThreadPoolExecutor threadPoolExecutor, String name) {
        queuedThreadPoolMap.put(name, threadPoolExecutor);
        return this;
    }

    public ThreadPoolCollector remove(String name) {
        queuedThreadPoolMap.remove(name);
        return this;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        return Arrays.asList(
                buildGauge("thread_pool_size", "Number of total threads",
                        ThreadPoolExecutor::getPoolSize),
                buildGauge("thread_pool_threads_active", "Number of active threads",
                        ThreadPoolExecutor::getActiveCount),
                buildGauge("thread_pool_max", "Max size of thread pool",
                        ThreadPoolExecutor::getMaximumPoolSize),
                buildGauge("thread_pool_queue_size", "Number of queue size",
                        threadPoolExecutor -> (threadPoolExecutor.getQueue().size())));
    }

    private GaugeMetricFamily buildGauge(String metric, String help,
                                         Function<ThreadPoolExecutor, Integer> metricValueProvider) {
        final GaugeMetricFamily metricFamily = new GaugeMetricFamily(metric, help, LABEL_NAMES);
        queuedThreadPoolMap.forEach((key, value) -> metricFamily.addMetric(
                Collections.singletonList(key),
                metricValueProvider.apply(value)
        ));
        return metricFamily;
    }

    public Map<String, ThreadPoolExecutor> getQueuedThreadPoolMap() {
        return queuedThreadPoolMap;
    }
}