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

import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alipay.sofa.rpc.bootstrap.ProviderBootstrap;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.common.Version;
import com.alipay.sofa.rpc.common.utils.CommonUtils;
import com.alipay.sofa.rpc.common.utils.StringUtils;
import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.MethodConfig;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import com.alipay.sofa.rpc.ext.Extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provider bootstrap for dubbo
 *
 * @author <a href=mailto:zhanggeng.zg@antfin.com>GengZhang</a>
 */
@Extension("dubbo")
public class DubboProviderBootstrap<T> extends ProviderBootstrap<T> {

    /**
     * 构造函数
     *
     * @param providerConfig 服务发布者配置
     */
    protected DubboProviderBootstrap(ProviderConfig<T> providerConfig) {
        super(providerConfig);
    }

    /**
     * 是否已发布
     */
    protected transient volatile boolean exported;

    /**
     * Dubbo的配置
     */
    private ServiceConfig<T>             serviceConfig;

    @Override
    public void export() {
        if (exported) {
            return;
        }
        serviceConfig = new ServiceConfig<T>();
        covert(providerConfig, serviceConfig);

        serviceConfig.export();
        exported = true;
    }

    private void covert(ProviderConfig<T> providerConfig, ServiceConfig<T> serviceConfig) {
        copyApplication(providerConfig, serviceConfig);
        DubboConvertor.copyRegistries(providerConfig, serviceConfig);
        copyServers(providerConfig, serviceConfig);
        copyProvider(providerConfig, serviceConfig);
        copyMethods(providerConfig, serviceConfig);
    }

    private void copyApplication(ProviderConfig<T> providerConfig, ServiceConfig<T> serviceConfig) {
        ApplicationConfig applicationConfig = providerConfig.getApplication();
        com.alibaba.dubbo.config.ApplicationConfig dubboConfig = new com.alibaba.dubbo.config.ApplicationConfig();
        dubboConfig.setName(applicationConfig.getAppName());
        serviceConfig.setApplication(dubboConfig);
    }

    private void copyServers(ProviderConfig<T> providerConfig, ServiceConfig serviceConfig) {
        List<ServerConfig> serverConfigs = providerConfig.getServer();
        if (CommonUtils.isNotEmpty(serverConfigs)) {
            List<ProtocolConfig> dubboProtocolConfigs = new ArrayList<ProtocolConfig>();
            for (ServerConfig serverConfig : serverConfigs) {
                // 生成并丢到缓存里
                ProtocolConfig protocolConfig = DubboSingleton.SERVER_MAP.get(serverConfig);
                if (protocolConfig == null) {
                    protocolConfig = new ProtocolConfig();
                    copyServerFields(serverConfig, protocolConfig);
                    ProtocolConfig old = DubboSingleton.SERVER_MAP.putIfAbsent(serverConfig, protocolConfig);
                    if (old != null) {
                        protocolConfig = old;
                    }
                }
                dubboProtocolConfigs.add(protocolConfig);
            }
            serviceConfig.setProtocols(dubboProtocolConfigs);
        }
    }

    private void copyServerFields(ServerConfig serverConfig, ProtocolConfig protocolConfig) {
        protocolConfig.setId(serverConfig.getId());
        protocolConfig.setName(serverConfig.getProtocol());
        protocolConfig.setHost(serverConfig.getHost());
        protocolConfig.setPort(serverConfig.getPort());
        protocolConfig.setAccepts(serverConfig.getAccepts());
        protocolConfig.setSerialization(serverConfig.getSerialization());
        if (!StringUtils.CONTEXT_SEP.equals(serverConfig.getContextPath())) {
            protocolConfig.setContextpath(serverConfig.getContextPath());
        }
        protocolConfig.setIothreads(serverConfig.getIoThreads());
        protocolConfig.setThreadpool(serverConfig.getThreadPoolType());
        protocolConfig.setThreads(serverConfig.getMaxThreads());
        protocolConfig.setPayload(serverConfig.getPayload());
        protocolConfig.setQueues(serverConfig.getQueues());

        protocolConfig.setParameters(serverConfig.getParameters());
    }

    private void copyProvider(ProviderConfig<T> providerConfig, ServiceConfig<T> serviceConfig) {
        serviceConfig.setId(providerConfig.getId());
        serviceConfig.setInterface(providerConfig.getInterfaceId());
        serviceConfig.setRef(providerConfig.getRef());
        serviceConfig.setGroup(providerConfig.getUniqueId());
        String dubboVersion = providerConfig.getParameter(RpcConstants.CONFIG_KEY_DUBBO_VERSION);
        serviceConfig.setVersion(StringUtils.isNotBlank(dubboVersion) ? dubboVersion : "1.0");
        serviceConfig.setActives(providerConfig.getConcurrents());
        serviceConfig.setDelay(providerConfig.getDelay());
        serviceConfig.setDynamic(providerConfig.isDynamic());
        serviceConfig.setRegister(providerConfig.isRegister());
        serviceConfig.setProxy(providerConfig.getProxy());
        serviceConfig.setWeight(providerConfig.getWeight());
        if (providerConfig.getTimeout() > 0) {
            serviceConfig.setTimeout(providerConfig.getTimeout());
        }
        serviceConfig.setParameters(providerConfig.getParameters());
    }

    private void copyMethods(ProviderConfig<T> providerConfig, ServiceConfig<T> serviceConfig) {
        Map<String, MethodConfig> methodConfigs = providerConfig.getMethods();
        if (CommonUtils.isNotEmpty(methodConfigs)) {
            List<com.alibaba.dubbo.config.MethodConfig> dubboMethodConfigs =
                    new ArrayList<com.alibaba.dubbo.config.MethodConfig>();
            for (Map.Entry<String, MethodConfig> entry : methodConfigs.entrySet()) {
                MethodConfig methodConfig = entry.getValue();
                com.alibaba.dubbo.config.MethodConfig dubboMethodConfig = new com.alibaba.dubbo.config.MethodConfig();
                dubboMethodConfig.setName(methodConfig.getName());
                dubboMethodConfig.setParameters(methodConfig.getParameters());
                dubboMethodConfigs.add(dubboMethodConfig);
            }
            serviceConfig.setMethods(dubboMethodConfigs);
        }
    }

    /**
     * 取消发布（从server里取消注册）
     */
    @Override
    public synchronized void unExport() {
        if (!exported) {
            return;
        }
        serviceConfig.unexport();
        exported = false;
    }

    /**
     * 得到已发布的全部list
     *
     * @return urls urls
     */
    public List<String> buildUrls() {
        if (exported) {
            List<ServerConfig> servers = providerConfig.getServer();
            if (servers != null && !servers.isEmpty()) {
                List<String> urls = new ArrayList<String>();
                for (ServerConfig server : servers) {
                    StringBuilder sb = new StringBuilder(200);
                    sb.append(server.getProtocol()).append("://").append(server.getHost())
                        .append(":").append(server.getPort()).append(server.getContextPath())
                        .append(providerConfig.getInterfaceId())
                        .append("?uniqueId=").append(providerConfig.getUniqueId())
                        .append(getKeyPairs("version", serviceConfig.getVersion()))
                        .append(getKeyPairs("delay", providerConfig.getDelay()))
                        .append(getKeyPairs("weight", providerConfig.getWeight()))
                        .append(getKeyPairs("register", providerConfig.isRegister()))
                        .append(getKeyPairs("maxThreads", server.getMaxThreads()))
                        .append(getKeyPairs("ioThreads", server.getIoThreads()))
                        .append(getKeyPairs("threadPoolType", server.getThreadPoolType()))
                        .append(getKeyPairs("accepts", server.getAccepts()))
                        .append(getKeyPairs("dynamic", providerConfig.isDynamic()))
                        .append(getKeyPairs(RpcConstants.CONFIG_KEY_RPC_VERSION, Version.RPC_VERSION));
                    urls.add(sb.toString());
                }
                return urls;
            }
        }
        return null;
    }

    /**
     * Gets key pairs.
     *
     * @param key   the key
     * @param value the value
     * @return the key pairs
     */
    private String getKeyPairs(String key, Object value) {
        if (value != null) {
            return "&" + key + "=" + value.toString();
        } else {
            return "";
        }
    }
}
