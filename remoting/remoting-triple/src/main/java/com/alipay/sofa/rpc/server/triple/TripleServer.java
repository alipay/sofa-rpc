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
package com.alipay.sofa.rpc.server.triple;

import com.alipay.sofa.rpc.common.struct.NamedThreadFactory;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import com.alipay.sofa.rpc.core.exception.SofaRpcRuntimeException;
import com.alipay.sofa.rpc.event.EventBus;
import com.alipay.sofa.rpc.event.ServerStartedEvent;
import com.alipay.sofa.rpc.ext.Extension;
import com.alipay.sofa.rpc.interceptor.ServerReqHeaderInterceptor;
import com.alipay.sofa.rpc.interceptor.TripleServerInterceptor;
import com.alipay.sofa.rpc.invoke.Invoker;
import com.alipay.sofa.rpc.log.LogCodes;
import com.alipay.sofa.rpc.log.Logger;
import com.alipay.sofa.rpc.log.LoggerFactory;
import com.alipay.sofa.rpc.proxy.ProxyFactory;
import com.alipay.sofa.rpc.server.BusinessPool;
import com.alipay.sofa.rpc.server.Server;
import com.alipay.sofa.rpc.server.SofaRejectedExecutionHandler;
import com.alipay.sofa.rpc.utils.SofaProtoUtils;
import io.grpc.BindableService;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptors;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.channel.EventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.ServerChannel;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollServerSocketChannel;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioServerSocketChannel;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.util.MutableHandlerRegistry;
import triple.Request;
import triple.Response;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * Grpc server base on io.gprc
 *
 * @author LiangEn.LiWei
 * @date 2018.11.20 5:27 PM
 */
@Extension("tri")
public class TripleServer implements Server {

    /**
     * Logger
     */
    private static final Logger                                          LOGGER          = LoggerFactory
                                                                                             .getLogger(TripleServer.class);
    /**
    * The mapping relationship between BindableService and ServerServiceDefinition
    */
    protected ConcurrentHashMap<ProviderConfig, ServerServiceDefinition> oldServiceInfo  = new ConcurrentHashMap<ProviderConfig,
                                                                                                 ServerServiceDefinition>();

    /**
     * server config
     */
    protected ServerConfig                                               serverConfig;

    /**
     * Is it already started
     */
    protected volatile boolean                                           started;

    /**
     * grpc server
     */
    protected io.grpc.Server                                             server;

    /**
     * service registry
     */
    protected MutableHandlerRegistry                                     handlerRegistry = new MutableHandlerRegistry();

    /**
     * The mapping relationship between BindableService and ServerServiceDefinition
     */
    protected ConcurrentHashMap<ProviderConfig, ServerServiceDefinition> serviceInfo     = new ConcurrentHashMap<ProviderConfig,
                                                                                                 ServerServiceDefinition>();
    /**
     * The mapping relationship between service name and unique id invoker
     */
    protected Map<String, UniqueIdInvoker> invokerMap = new ConcurrentHashMap<>();

    /**
     * invoker count
     */
    protected AtomicInteger                                              invokerCnt      = new AtomicInteger();

    /**
     * Thread pool
     * @param serverConfig ServerConfig
     */
    private ThreadPoolExecutor                                           bizThreadPool;

    @Override
    public void init(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        bizThreadPool = initThreadPool(serverConfig);
        server = NettyServerBuilder.forPort(serverConfig.getPort()).
            fallbackHandlerRegistry(handlerRegistry)
            .bossEventLoopGroup(constructBossEventLoopGroup())
            .workerEventLoopGroup(constructWorkerEventLoopGroup())
            .executor(bizThreadPool)
            .channelType(constructChannel())
            .build();
    }

    private Class<? extends ServerChannel> constructChannel() {
        return serverConfig.isEpoll() ?
            EpollServerSocketChannel.class :
            NioServerSocketChannel.class;
    }

    private EventLoopGroup constructWorkerEventLoopGroup() {

        int workerThreads = serverConfig.getIoThreads();
        workerThreads = workerThreads <= 0 ? Runtime.getRuntime().availableProcessors() * 2 : workerThreads;
        NamedThreadFactory threadName =
                new NamedThreadFactory("SEV-WORKER-" + serverConfig.getPort(), serverConfig.isDaemon());
        EventLoopGroup workerGroup = serverConfig.isEpoll() ?
            new EpollEventLoopGroup(workerThreads, threadName) :
            new NioEventLoopGroup(workerThreads, threadName);

        return workerGroup;
    }

    /**
     * default as bolt
     *
     * @return
     */
    private EventLoopGroup constructBossEventLoopGroup() {
        NamedThreadFactory threadName =
                new NamedThreadFactory("SEV-BOSS-" + serverConfig.getPort(), serverConfig.isDaemon());
        EventLoopGroup bossGroup = serverConfig.isEpoll() ?
            new EpollEventLoopGroup(1, threadName) :
            new NioEventLoopGroup(1, threadName);
        return bossGroup;
    }

    protected ThreadPoolExecutor initThreadPool(ServerConfig serverConfig) {
        ThreadPoolExecutor threadPool = BusinessPool.initPool(serverConfig);
        threadPool.setThreadFactory(new NamedThreadFactory(
            "SEV-TRIPLE-BIZ-" + serverConfig.getPort(), serverConfig.isDaemon()));
        threadPool.setRejectedExecutionHandler(new SofaRejectedExecutionHandler());
        if (serverConfig.isPreStartCore()) { // 初始化核心线程池
            threadPool.prestartAllCoreThreads();
        }
        return threadPool;
    }

    @Override
    public void start() {
        if (started) {
            return;
        }
        synchronized (this) {
            if (started) {
                return;
            }
            try {
                server.start();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Start the triple server at port {}", serverConfig.getPort());
                }
                if (EventBus.isEnable(ServerStartedEvent.class)) {
                    EventBus.post(new ServerStartedEvent(serverConfig, bizThreadPool));
                }
            } catch (SofaRpcRuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_START_SERVER_WITH_PORT, "grpc",
                    serverConfig.getPort()), e);
            }
            started = true;
        }
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean hasNoEntry() {
        return false;
    }

    @Override
    public void stop() {
        if (!started) {
            return;
        }
        try {
            // 关闭端口，不关闭线程池
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Stop the triple server at port {}", serverConfig.getPort());
            }
            server.shutdown();
        } catch (Exception e) {
            LOGGER.error("Stop the triple server at port " + serverConfig.getPort() + " error !", e);
        }
        started = false;
    }

    @Override
    public void registerProcessor(ProviderConfig providerConfig, Invoker instance) {
        Object ref = providerConfig.getRef();
        try {
            // wrap invoker to support unique id
            UniqueIdInvoker uniqueIdInvoker = this.invokerMap.computeIfAbsent(providerConfig.getInterfaceId(), interfaceId -> new UniqueIdInvoker());
            uniqueIdInvoker.registerInvoker(providerConfig, instance);

            // create service definition
            ServerServiceDefinition serviceDef;
            if (SofaProtoUtils.isProtoClass(ref)) {
                // refer is BindableService
                this.setBindableProxiedImpl(providerConfig, uniqueIdInvoker);
                BindableService bindableService = (BindableService) providerConfig.getRef();
                serviceDef = bindableService.bindService();
            } else {
                // normal class
                Object obj = ProxyFactory.buildProxy(providerConfig.getProxy(), providerConfig.getProxyClass(), uniqueIdInvoker);
                GenericServiceImpl genericService = new GenericServiceImpl(obj, providerConfig.getProxyClass());
                genericService.setProxiedImpl(genericService);
                serviceDef = buildSofaServiceDef(genericService, providerConfig);
            }

            List<TripleServerInterceptor> interceptorList = buildInterceptorChain(serviceDef);
            ServerServiceDefinition serviceDefinition = ServerInterceptors.intercept(
                serviceDef, interceptorList);
            serviceInfo.put(providerConfig, serviceDefinition);
            ServerServiceDefinition ssd = handlerRegistry.addService(serviceDefinition);
            if (ssd != null) {
                throw new IllegalStateException("Can not expose service with same name:" +
                    serviceDefinition.getServiceDescriptor().getName());
            }
            invokerCnt.incrementAndGet();
        } catch (Exception e) {
            String msg = "Register triple service error";
            LOGGER.error(msg, e);
            serviceInfo.remove(providerConfig);
            throw new SofaRpcRuntimeException(msg, e);
        }

    }

    private void setBindableProxiedImpl(ProviderConfig providerConfig, Invoker invoker) {
        Class<?> implClass = providerConfig.getRef().getClass();
        try {
            Method method = implClass.getMethod("setProxiedImpl", providerConfig.getProxyClass());
            Object obj = ProxyFactory.buildProxy(providerConfig.getProxy(), providerConfig.getProxyClass(), invoker);
            method.invoke(providerConfig.getRef(), obj);
        } catch (NoSuchMethodException e) {
            LOGGER
                    .info(
                            "{} don't hava method setProxiedImpl, will treated as origin provider service instead of grpc service.",
                            implClass);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to set sofa proxied service impl to stub, please make sure your stub "
                            + "was generated by the sofa-protoc-compiler.", e);
        }
    }

    private ServerServiceDefinition appendUniqueIdToServiceDef(String uniqueId, ServerServiceDefinition serviceDef) {
        final String newServiceName = serviceDef.getServiceDescriptor().getName() + "." + uniqueId;

        ServerServiceDefinition.Builder builder = ServerServiceDefinition.builder(newServiceName);

        Collection<ServerMethodDefinition<?, ?>> methods = serviceDef.getMethods();
        for (ServerMethodDefinition method : methods) {
            MethodDescriptor<?, ?> methodDescriptor = method.getMethodDescriptor();
            String fullMethodName = methodDescriptor.getFullMethodName();
            MethodDescriptor<?, ?> methodDescriptorWithUniqueId = methodDescriptor
                .toBuilder()
                .setFullMethodName(fullMethodName)
                .build();
            ServerMethodDefinition<?, ?> newServerMethodDefinition = ServerMethodDefinition.create(
                methodDescriptorWithUniqueId, method.getServerCallHandler());
            builder.addMethod(newServerMethodDefinition);
        }
        serviceDef = builder.build();
        return serviceDef;
    }

    private ServerServiceDefinition buildSofaServiceDef(GenericServiceImpl genericService,
                                                        ProviderConfig providerConfig) {
        ServerServiceDefinition templateDefinition = genericService.bindService();
        ServerCallHandler<Request, Response> templateHandler = (ServerCallHandler<Request, Response>) templateDefinition
            .getMethods().iterator().next().getServerCallHandler();
        List<MethodDescriptor<Request, Response>> methodDescriptor = getMethodDescriptor(providerConfig);
        List<ServerMethodDefinition<Request, Response>> methodDefs = getMethodDefinitions(templateHandler,
            methodDescriptor);
        ServerServiceDefinition.Builder builder = ServerServiceDefinition.builder(getServiceDescriptor(
            templateDefinition, providerConfig, methodDescriptor));
        for (ServerMethodDefinition<Request, Response> methodDef : methodDefs) {
            builder.addMethod(methodDef);

        }
        return builder.build();

    }

    private List<ServerMethodDefinition<Request, Response>> getMethodDefinitions(ServerCallHandler<Request, Response> templateHandler,List<MethodDescriptor<Request, Response>> methodDescriptors) {
        List<ServerMethodDefinition<Request, Response>> result = new ArrayList<>();

        for (MethodDescriptor<Request, Response> methodDescriptor : methodDescriptors) {
            ServerMethodDefinition<Request, Response> serverMethodDefinition = ServerMethodDefinition.create(methodDescriptor, templateHandler);
            result.add(serverMethodDefinition);
        }
        return result;
    }

    private ServiceDescriptor getServiceDescriptor(ServerServiceDefinition template, ProviderConfig providerConfig,
                                                   List<MethodDescriptor<Request, Response>> methodDescriptors) {
        String serviceName = providerConfig.getInterfaceId();
        ServiceDescriptor.Builder builder = ServiceDescriptor.newBuilder(serviceName)
            .setSchemaDescriptor(template.getServiceDescriptor().getSchemaDescriptor());
        for (MethodDescriptor<Request, Response> methodDescriptor : methodDescriptors) {
            builder.addMethod(methodDescriptor);
        }
        return builder.build();

    }

    private List<MethodDescriptor<Request, Response>> getMethodDescriptor( ProviderConfig providerConfig) {
        List<MethodDescriptor<Request, Response>> result = new ArrayList<>();

        Set<String> methodNames = SofaProtoUtils.getMethodNames(providerConfig.getInterfaceId());
        for (String name : methodNames) {
            MethodDescriptor<Request, Response> methodDescriptor = MethodDescriptor.<Request, Response>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName(generateFullMethodName(providerConfig.getInterfaceId(), name))
                    .setSampledToLocalTracing(true)
                    .setRequestMarshaller(ProtoUtils.marshaller(
                            Request.getDefaultInstance()))
                    .setResponseMarshaller(ProtoUtils.marshaller(
                            Response.getDefaultInstance()))
                    .build();
            result.add(methodDescriptor);
        }

        return result;
    }

    /**
     * build chain
     *
     * @param serviceDef
     * @return
     */
    protected List<TripleServerInterceptor> buildInterceptorChain(ServerServiceDefinition serviceDef) {
        List<TripleServerInterceptor> interceptorList = new ArrayList<>();
        interceptorList.add(new ServerReqHeaderInterceptor(serviceDef));
        return interceptorList;
    }

    @Override
    public void unRegisterProcessor(ProviderConfig providerConfig, boolean closeIfNoEntry) {
        try {
            ServerServiceDefinition serverServiceDefinition = serviceInfo.get(providerConfig);
            handlerRegistry.removeService(serverServiceDefinition);
            UniqueIdInvoker uniqueIdInvoker = this.invokerMap.get(providerConfig.getInterfaceId());
            if (null != uniqueIdInvoker) {
                uniqueIdInvoker.unRegisterInvoker(providerConfig);
            }
            invokerCnt.decrementAndGet();
        } catch (Exception e) {
            LOGGER.error("Unregister triple service error", e);
        }
        if (closeIfNoEntry && invokerCnt.get() == 0) {
            stop();
        }
    }

    @Override
    public void destroy() {
        stop();
        server = null;
    }

    @Override
    public void destroy(DestroyHook hook) {
        if (hook != null) {
            hook.preDestroy();
        }
        destroy();
        if (hook != null) {
            hook.postDestroy();
        }
    }

    /**
     * 得到业务线程池
     *
     * @return 业务线程池
     */
    public ThreadPoolExecutor getBizThreadPool() {
        return bizThreadPool;
    }

}