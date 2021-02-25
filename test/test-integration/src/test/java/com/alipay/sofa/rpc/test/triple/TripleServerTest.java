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
package com.alipay.sofa.rpc.test.triple;

import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import com.alipay.sofa.rpc.context.RpcInternalContext;
import com.alipay.sofa.rpc.context.RpcInvokeContext;
import com.alipay.sofa.rpc.context.RpcRunningState;
import com.alipay.sofa.rpc.context.RpcRuntimeContext;
import com.alipay.sofa.rpc.core.exception.SofaTimeOutException;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.examples.helloworld.SofaGreeterTriple;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href=mailto:leizhiyuan@gmail.com>leizhiyuan</a>
 */
public class TripleServerTest {

    @Test
    //同步调用,直连
    public void testSync() {

        ApplicationConfig applicationConfig = new ApplicationConfig().setAppName("triple-server");

        int port = 50052;

        ServerConfig serverConfig = new ServerConfig()
            .setProtocol(RpcConstants.PROTOCOL_TYPE_TRIPLE)
            .setPort(port);

        ProviderConfig<SofaGreeterTriple.IGreeter> providerConfig = new ProviderConfig<SofaGreeterTriple.IGreeter>()
            .setApplication(applicationConfig)
            .setBootstrap(RpcConstants.PROTOCOL_TYPE_TRIPLE)
            .setInterfaceId(SofaGreeterTriple.IGreeter.class.getName())
            .setRef(new GreeterImpl())
            .setServer(serverConfig);

        providerConfig.export();

        ConsumerConfig<SofaGreeterTriple.IGreeter> consumerConfig = new ConsumerConfig<SofaGreeterTriple.IGreeter>();
        consumerConfig.setInterfaceId(SofaGreeterTriple.IGreeter.class.getName())
            .setProtocol(RpcConstants.PROTOCOL_TYPE_TRIPLE)
            .setDirectUrl("tri://127.0.0.1:" + port);

        SofaGreeterTriple.IGreeter greeterBlockingStub = consumerConfig.refer();

        HelloRequest.DateTime dateTime = HelloRequest.DateTime.newBuilder().setDate("2018-12-28").setTime("11:13:00")
            .build();
        HelloReply reply = null;
        HelloRequest request = HelloRequest.newBuilder().setName("world").setDateTime(dateTime).build();
        reply = greeterBlockingStub.sayHello(request);

        Assert.assertNotNull(reply);

    }

    @Test
    //同步调用,直连 有uniqueId
    public void testSyncWithUniqueId() {

        ApplicationConfig applicationConfig = new ApplicationConfig().setAppName("triple-server");

        int port = 50052;

        ServerConfig serverConfig = new ServerConfig()
            .setProtocol(RpcConstants.PROTOCOL_TYPE_TRIPLE)
            .setPort(port);

        ProviderConfig<SofaGreeterTriple.IGreeter> providerConfig = new ProviderConfig<SofaGreeterTriple.IGreeter>()
            .setApplication(applicationConfig)
            .setUniqueId("abc")
            .setBootstrap(RpcConstants.PROTOCOL_TYPE_TRIPLE)
            .setInterfaceId(SofaGreeterTriple.IGreeter.class.getName())
            .setRef(new GreeterImpl())
            .setServer(serverConfig);

        providerConfig.export();

        ConsumerConfig<SofaGreeterTriple.IGreeter> consumerConfig = new ConsumerConfig<SofaGreeterTriple.IGreeter>();
        consumerConfig.setInterfaceId(SofaGreeterTriple.IGreeter.class.getName())
            .setProtocol(RpcConstants.PROTOCOL_TYPE_TRIPLE)
            .setUniqueId("abc")
            .setDirectUrl("tri://127.0.0.1:" + port);

        SofaGreeterTriple.IGreeter greeterBlockingStub = consumerConfig.refer();

        HelloRequest.DateTime dateTime = HelloRequest.DateTime.newBuilder().setDate("2018-12-28").setTime("11:13:00")
            .build();
        HelloReply reply = null;
        HelloRequest request = HelloRequest.newBuilder().setName("world").setDateTime(dateTime).build();
        reply = greeterBlockingStub.sayHello(request);

        Assert.assertNotNull(reply);

        consumerConfig = new ConsumerConfig<SofaGreeterTriple.IGreeter>();
        consumerConfig.setInterfaceId(SofaGreeterTriple.IGreeter.class.getName())
            .setProtocol(RpcConstants.PROTOCOL_TYPE_TRIPLE)
            .setDirectUrl("tri://127.0.0.1:" + port);

        greeterBlockingStub = consumerConfig.refer();

        dateTime = HelloRequest.DateTime.newBuilder().setDate("2018-12-28").setTime("11:13:00")
            .build();
        request = HelloRequest.newBuilder().setName("world").setDateTime(dateTime).build();
        reply = greeterBlockingStub.sayHello(request);

        Assert.assertNotNull(reply);

    }

    @Test
    public void testExposeTwice() {
        ApplicationConfig applicationConfig = new ApplicationConfig().setAppName("triple-server");

        int port = 50052;

        ServerConfig serverConfig = new ServerConfig()
            .setProtocol(RpcConstants.PROTOCOL_TYPE_TRIPLE)
            .setPort(port);

        ProviderConfig<SofaGreeterTriple.IGreeter> providerConfig = new ProviderConfig<SofaGreeterTriple.IGreeter>()
            .setApplication(applicationConfig)
            .setUniqueId("abc")
            .setBootstrap(RpcConstants.PROTOCOL_TYPE_TRIPLE)
            .setInterfaceId(SofaGreeterTriple.IGreeter.class.getName())
            .setRef(new GreeterImpl())
            .setServer(serverConfig);

        providerConfig.export();
        applicationConfig = new ApplicationConfig().setAppName("triple-server");

        serverConfig = new ServerConfig()
            .setProtocol(RpcConstants.PROTOCOL_TYPE_TRIPLE)
            .setPort(port);
        providerConfig = new ProviderConfig<SofaGreeterTriple.IGreeter>()
            .setApplication(applicationConfig)
            .setUniqueId("abcd")
            .setBootstrap(RpcConstants.PROTOCOL_TYPE_TRIPLE)
            .setInterfaceId(SofaGreeterTriple.IGreeter.class.getName())
            .setRef(new GreeterImpl())
            .setServer(serverConfig);

        try {
            providerConfig.export();
            Assert.fail();
        } catch (Exception e) {

        }

    }

    @Test
    //同步调用,直连
    public void testSyncTimeout() {

        ApplicationConfig applicationConfig = new ApplicationConfig().setAppName("triple-server");

        int port = 50052;

        ServerConfig serverConfig = new ServerConfig()
            .setProtocol(RpcConstants.PROTOCOL_TYPE_TRIPLE)
            .setPort(port);

        ProviderConfig<SofaGreeterTriple.IGreeter> providerConfig = new ProviderConfig<SofaGreeterTriple.IGreeter>()
            .setApplication(applicationConfig)
            .setBootstrap(RpcConstants.PROTOCOL_TYPE_TRIPLE)
            .setInterfaceId(SofaGreeterTriple.IGreeter.class.getName())
            .setRef(new GreeterImpl())
            .setServer(serverConfig);

        providerConfig.export();

        ConsumerConfig<SofaGreeterTriple.IGreeter> consumerConfig = new ConsumerConfig<SofaGreeterTriple.IGreeter>();
        consumerConfig.setInterfaceId(SofaGreeterTriple.IGreeter.class.getName())
            .setProtocol(RpcConstants.PROTOCOL_TYPE_TRIPLE)
            .setDirectUrl("tri://127.0.0.1:" + port)
            .setTimeout(1);

        SofaGreeterTriple.IGreeter greeterBlockingStub = consumerConfig.refer();

        HelloRequest.DateTime dateTime = HelloRequest.DateTime.newBuilder().setDate("2018-12-28").setTime("11:13:00")
            .build();
        HelloReply reply = null;
        HelloRequest request = HelloRequest.newBuilder().setName("world").setDateTime(dateTime).build();

        boolean exp = false;
        try {
            reply = greeterBlockingStub.sayHello(request);
        } catch (SofaTimeOutException e) {
            exp = true;
        }
        Assert.assertTrue(exp);

    }

    @BeforeClass
    public static void adBeforeClass() {
        RpcRunningState.setUnitTestMode(true);
    }

    @After
    public void afterMethod() {
        RpcRuntimeContext.destroy();
        RpcInternalContext.removeAllContext();
        RpcInvokeContext.removeContext();
    }
}