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

import com.alipay.sofa.rpc.event.PrometheusSubscriber;

/**
 * @author zhaowang
 * @version : PrometheusModuleForTest.java, v 0.1 2020年04月06日 4:09 下午 zhaowang Exp $
 */
public class PrometheusModuleForTest extends PrometheusModule {

    private PrometheusSubscriber prometheusSubscriber;

    @Override
    protected PrometheusSubscriber getSubscriber() {
        return prometheusSubscriber;
    }

    public PrometheusSubscriber getPrometheusSubscriber() {
        return prometheusSubscriber;
    }

    public void setPrometheusSubscriber(PrometheusSubscriber prometheusSubscriber) {
        this.prometheusSubscriber = prometheusSubscriber;
    }
}