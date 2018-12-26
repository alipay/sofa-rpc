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
package com.alipay.sofa.rpc.dynamic;

import com.alipay.sofa.rpc.config.AbstractInterfaceConfig;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.ext.Extensible;
import com.alipay.sofa.rpc.listener.ConfigListener;

import java.util.concurrent.ConcurrentMap;

/**
 * @author bystander
 * @version $Id: DynamicConfiger.java, v 0.1 2018年12月26日 20:29 bystander Exp $
 */
@Extensible(singleton = false)
public interface DynamicConfiger {

    /**
     * 清理cache
     */
    void clearConfigCache();

    /**
     * 订阅接口级别的配置
     * @param config
     * @param listener
     */
    void subscribeInterfaceConfig(final AbstractInterfaceConfig config, ConfigListener listener);

    /**
     * 取消订阅配置
     * @param config
     */
    void unSubscribeConfig(final AbstractInterfaceConfig config);

    /**
     * 取消override订阅
     * @param consumerUrls
     * @param config
     * @param listener
     */
    void subscribeOverride(ConcurrentMap<ConsumerConfig, String> consumerUrls, final ConsumerConfig config,
                           ConfigListener listener);

}