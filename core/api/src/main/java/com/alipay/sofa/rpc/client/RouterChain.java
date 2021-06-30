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
package com.alipay.sofa.rpc.client;

import com.alipay.sofa.rpc.bootstrap.ConsumerBootstrap;
import com.alipay.sofa.rpc.common.struct.OrderedComparator;
import com.alipay.sofa.rpc.common.utils.CommonUtils;
import com.alipay.sofa.rpc.common.utils.StringUtils;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.ext.ExtensionClass;
import com.alipay.sofa.rpc.ext.ExtensionLoader;
import com.alipay.sofa.rpc.ext.ExtensionLoaderFactory;
import com.alipay.sofa.rpc.ext.ExtensionLoaderListener;
import com.alipay.sofa.rpc.filter.AutoActive;
import com.alipay.sofa.rpc.log.Logger;
import com.alipay.sofa.rpc.log.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Chain of routers
 *
 * @author <a href=mailto:zhanggeng.zg@antfin.com>GengZhang</a>
 */
public class RouterChain {

    /**
     * LOGGER
     */
    private static final Logger                              LOGGER                = LoggerFactory
                                                                                       .getLogger(RouterChain.class);

    /**
     * 服务端自动激活的 {"alias":ExtensionClass}
     */
    private final static Map<String, ExtensionClass<Router>> PROVIDER_AUTO_ACTIVES = Collections
                                                                                       .synchronizedMap(new ConcurrentHashMap<String, ExtensionClass<Router>>());

    /**
     * 调用端自动激活的 {"alias":ExtensionClass}
     */
    private final static Map<String, ExtensionClass<Router>> CONSUMER_AUTO_ACTIVES = Collections
                                                                                       .synchronizedMap(new ConcurrentHashMap<String, ExtensionClass<Router>>());

    /**
     * 扩展加载器
     */
    private final static ExtensionLoader<Router>             EXTENSION_LOADER      = buildLoader();

    private static ExtensionLoader<Router> buildLoader() {
        ExtensionLoader<Router> extensionLoader = ExtensionLoaderFactory.getExtensionLoader(Router.class);
        extensionLoader.addListener(new ExtensionLoaderListener<Router>() {
            @Override
            public void onLoad(ExtensionClass<Router> extensionClass) {
                Class<? extends Router> implClass = extensionClass.getClazz();
                // 读取自动加载的类列表。
                AutoActive autoActive = implClass.getAnnotation(AutoActive.class);
                if (autoActive != null) {
                    String alias = extensionClass.getAlias();
                    if (autoActive.providerSide()) {
                        PROVIDER_AUTO_ACTIVES.put(alias, extensionClass);
                    }
                    if (autoActive.consumerSide()) {
                        CONSUMER_AUTO_ACTIVES.put(alias, extensionClass);
                    }
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Extension of interface " + Router.class + ", " + implClass + "(" + alias +
                            ") will auto active");
                    }
                }
            }
        });
        return extensionLoader;
    }

    /**
     * 调用链
     */
    private final List<Router> routers;

    public RouterChain(List<Router> actualRouters, ConsumerBootstrap consumerBootstrap) {
        this.routers = new ArrayList<Router>();
        if (CommonUtils.isNotEmpty(actualRouters)) {
            for (Router router : actualRouters) {
                if (router.needToLoad(consumerBootstrap)) {
                    router.init(consumerBootstrap);
                    routers.add(router);
                }
            }
        }
    }

    /**
     * 筛选Provider
     *
     * @param request       本次调用（可以得到类名，方法名，方法参数，参数值等）
     * @param providerInfos providers（<b>当前可用</b>的服务Provider列表）
     * @return 路由匹配的服务Provider列表
     */
    public List<ProviderInfo> route(SofaRequest request, List<ProviderInfo> providerInfos) {
        for (Router router : routers) {
            providerInfos = router.route(request, providerInfos);
        }
        return providerInfos;
    }

    /**
     * 构建Router链
     *
     * @param consumerBootstrap 服务端订阅者配置
     * @return 路由链
     */
    public static RouterChain buildConsumerChain(ConsumerBootstrap consumerBootstrap) {
        ConsumerConfig<?> consumerConfig = consumerBootstrap.getConsumerConfig();
        List<Router> customRouters = consumerConfig.getRouterRef() == null ? new ArrayList<Router>()
            : new CopyOnWriteArrayList<Router>(consumerConfig.getRouterRef());
        // 先解析是否有特殊处理
        HashSet<String> excludes = parseExcludeRouter(customRouters);

        // 准备数据：用户通过别名的方式注入的router，需要解析
        List<ExtensionClass<Router>> extensionRouters = new ArrayList<ExtensionClass<Router>>();
        List<String> routerAliases = consumerConfig.getRouter();
        if (CommonUtils.isNotEmpty(routerAliases)) {
            for (String routerAlias : routerAliases) {
                if (startsWithExcludePrefix(routerAlias)) { // 排除用的特殊字符
                    excludes.add(routerAlias.substring(1));
                } else {
                    ExtensionClass<Router> extensionRouter = EXTENSION_LOADER.getExtensionClass(routerAlias);
                    if (extensionRouter != null) {
                        extensionRouters.add(extensionRouter);
                    }
                }
            }
        }
        // 解析自动加载的router
        if (!excludes.contains(StringUtils.ALL) && !excludes.contains(StringUtils.DEFAULT)) { // 配了-*和-default表示不加载内置
            for (Map.Entry<String, ExtensionClass<Router>> entry : CONSUMER_AUTO_ACTIVES.entrySet()) {
                if (!excludes.contains(entry.getKey())) {
                    extensionRouters.add(entry.getValue());
                }
            }
        }
        excludes = null; // 不需要了
        // 按order从小到大排序
        if (extensionRouters.size() > 1) {
            Collections.sort(extensionRouters, new OrderedComparator<ExtensionClass>());
        }
        List<Router> actualRouters = new ArrayList<Router>();
        for (ExtensionClass<Router> extensionRouter : extensionRouters) {
            Router actualRoute = extensionRouter.getExtInstance();
            actualRouters.add(actualRoute);
        }
        // 加入自定义的过滤器
        actualRouters.addAll(customRouters);
        return new RouterChain(actualRouters, consumerBootstrap);
    }

    /**
     * 判断是否需要排除系统过滤器
     *
     * @param customRouters 自定义Router
     * @return 是否排除
     */
    private static HashSet<String> parseExcludeRouter(List<Router> customRouters) {
        HashSet<String> excludeKeys = new HashSet<String>();
        if (CommonUtils.isNotEmpty(customRouters)) {
            for (Router router : customRouters) {
                if (router instanceof ExcludeRouter) {
                    // 存在需要排除的过滤器
                    ExcludeRouter excludeRouter = (ExcludeRouter) router;
                    String excludeName = excludeRouter.getExcludeName();
                    if (StringUtils.isNotEmpty(excludeName)) {
                        String excludeRouterName = startsWithExcludePrefix(excludeName) ? excludeName.substring(1)
                            : excludeName;
                        if (StringUtils.isNotEmpty(excludeRouterName)) {
                            excludeKeys.add(excludeRouterName);
                        }
                    }
                    customRouters.remove(router);
                }
            }
        }
        if (!excludeKeys.isEmpty()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Find exclude routers: {}", excludeKeys);
            }
        }
        return excludeKeys;
    }

    private static boolean startsWithExcludePrefix(String excludeName) {
        char c = excludeName.charAt(0);
        return c == '-' || c == '!';
    }
}
