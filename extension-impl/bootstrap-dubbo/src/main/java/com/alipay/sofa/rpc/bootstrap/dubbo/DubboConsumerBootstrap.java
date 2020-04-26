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
package com.alipay.sofa.rpc.bootstrap.dubbo;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.alipay.sofa.rpc.bootstrap.ConsumerBootstrap;
import com.alipay.sofa.rpc.client.Cluster;
import com.alipay.sofa.rpc.client.ProviderGroup;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.common.utils.CommonUtils;
import com.alipay.sofa.rpc.common.utils.StringUtils;
import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.MethodConfig;
import com.alipay.sofa.rpc.core.exception.SofaRpcRuntimeException;
import com.alipay.sofa.rpc.ext.Extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.alipay.sofa.rpc.bootstrap.dubbo.DubboConvertor.copyRegistries;

/**
 * Consumer bootstrap for dubbo
 *
 * @author <a href=mailto:zhanggeng.zg@antfin.com>GengZhang</a>
 */
@Extension("dubbo")
public class DubboConsumerBootstrap<T> extends ConsumerBootstrap<T> {

    /**
     * Dubbo的配置
     */
    private ReferenceConfig<T>     referenceConfig;

    /**
     * 代理实现类
     */
    protected transient volatile T proxyIns;

    /**
     * 构造函数
     *
     * @param consumerConfig 服务消费者配置
     */
    protected DubboConsumerBootstrap(ConsumerConfig<T> consumerConfig) {
        super(consumerConfig);
    }

    /**
     * Refer t.
     *
     * @return the t
     * @throws SofaRpcRuntimeException the init error exception
     */
    @Override
    public synchronized T refer() {
        if (proxyIns != null) {
            return proxyIns;
        }

        referenceConfig = new ReferenceConfig<T>();
        covert(consumerConfig, referenceConfig);

        proxyIns = referenceConfig.get();

        return proxyIns;
    }

    private void covert(ConsumerConfig<T> consumerConfig, ReferenceConfig<T> referenceConfig) {
        copyCommon(consumerConfig, referenceConfig);
        copyApplication(consumerConfig, referenceConfig);
        copyRegistries(consumerConfig, referenceConfig);
        copyConsumer(consumerConfig, referenceConfig);
        copyMethods(consumerConfig, referenceConfig);
    }

    private void copyCommon(ConsumerConfig<T> consumerConfig, ReferenceConfig<T> referenceConfig) {
        referenceConfig.setInjvm(consumerConfig.isInJVM());
    }

    private void copyApplication(ConsumerConfig<T> consumerConfig, ReferenceConfig<T> referenceConfig) {
        ApplicationConfig applicationConfig = consumerConfig.getApplication();
        com.alibaba.dubbo.config.ApplicationConfig dubboConfig = new com.alibaba.dubbo.config.ApplicationConfig();
        dubboConfig.setName(applicationConfig.getAppName());
        referenceConfig.setApplication(dubboConfig);
    }

    private void copyConsumer(ConsumerConfig<T> consumerConfig, ReferenceConfig<T> referenceConfig) {
        referenceConfig.setId(consumerConfig.getId());
        referenceConfig.setInterface(consumerConfig.getInterfaceId());
        referenceConfig.setGroup(consumerConfig.getUniqueId());
        String dubboVersion = consumerConfig.getParameter(RpcConstants.CONFIG_KEY_DUBBO_VERSION);
        referenceConfig.setVersion(StringUtils.isNotBlank(dubboVersion) ? dubboVersion : "1.0");
        referenceConfig.setActives(consumerConfig.getConcurrents());
        referenceConfig.setCluster(consumerConfig.getCluster());
        referenceConfig.setConnections(consumerConfig.getConnectionNum());
        referenceConfig.setRetries(consumerConfig.getRetries());
        referenceConfig.setProxy(consumerConfig.getProxy());
        referenceConfig.setTimeout(consumerConfig.getTimeout());
        referenceConfig.setUrl(consumerConfig.getDirectUrl());
        referenceConfig.setCheck(consumerConfig.isCheck());
        referenceConfig.setLazy(consumerConfig.isLazy());
        referenceConfig.setGeneric(consumerConfig.isGeneric());
        String invokeType = consumerConfig.getInvokeType();
        if (invokeType != null) {
            if (RpcConstants.INVOKER_TYPE_ONEWAY.equals(invokeType)) {
                referenceConfig.setSent(false);
            }
            if (RpcConstants.INVOKER_TYPE_CALLBACK.equals(invokeType)
                || RpcConstants.INVOKER_TYPE_FUTURE.equals(invokeType)) {
                referenceConfig.setAsync(true);
            }
        }
        referenceConfig.setParameters(consumerConfig.getParameters());
    }

    private void copyMethods(ConsumerConfig<T> consumerConfig, ReferenceConfig<T> referenceConfig) {
        Map<String, MethodConfig> methodConfigs = consumerConfig.getMethods();
        if (CommonUtils.isNotEmpty(methodConfigs)) {
            List<com.alibaba.dubbo.config.MethodConfig> dubboMethodConfigs =
                    new ArrayList<com.alibaba.dubbo.config.MethodConfig>();
            for (Map.Entry<String, MethodConfig> entry : methodConfigs.entrySet()) {
                MethodConfig methodConfig = entry.getValue();
                com.alibaba.dubbo.config.MethodConfig dubboMethodConfig = new com.alibaba.dubbo.config.MethodConfig();
                dubboMethodConfig.setName(methodConfig.getName());
                dubboMethodConfig.setParameters(methodConfig.getParameters());
                dubboMethodConfig.setTimeout(methodConfig.getTimeout());
                dubboMethodConfig.setRetries(methodConfig.getRetries());
                String invokeType = methodConfig.getInvokeType();
                if (invokeType != null) {
                    if (RpcConstants.INVOKER_TYPE_ONEWAY.equals(invokeType)) {
                        dubboMethodConfig.setReturn(false);
                    }
                    if (RpcConstants.INVOKER_TYPE_CALLBACK.equals(invokeType)
                        || RpcConstants.INVOKER_TYPE_FUTURE.equals(invokeType)) {
                        dubboMethodConfig.setAsync(true);
                    }
                }
                dubboMethodConfigs.add(dubboMethodConfig);
            }
            referenceConfig.setMethods(dubboMethodConfigs);
        }
    }

    /**
     * unRefer void.
     */
    @Override
    public synchronized void unRefer() {
        if (proxyIns == null) {
            return;
        }
        referenceConfig.destroy();
        proxyIns = null;
    }

    @Override
    public Cluster getCluster() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public List<ProviderGroup> subscribe() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isSubscribed() {
        return proxyIns != null;
    }

    /**
     * 得到实现代理类
     *
     * @return 实现代理类 proxy ins
     */
    @Override
    public T getProxyIns() {
        return proxyIns;
    }
}
