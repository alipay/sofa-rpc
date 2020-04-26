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
package com.alipay.sofa.rpc.common;

import java.nio.charset.Charset;

import static com.alipay.sofa.rpc.common.RpcConfigs.getStringValue;

/**
 * Rpc Constants
 *
 * @author <a href=mailto:zhanggeng.zg@antfin.com>GengZhang</a>
 */
public class RpcConstants {

    /*--------Config配置值相关开始---------*/
    /**
     * zookeeper注册中心
     */
    public static final String  REGISTRY_PROTOCOL_ZK               = "zookeeper";

    /**
     * mesh注册中心
     */
    public static final String  REGISTRY_PROTOCOL_MESH             = "mesh";

    /**
     * sofa 注册中心
     */
    public static final String  REGISTRY_PROTOCOL_SOFA             = "sofa";

    /**
     * xml文件注册中心
     */
    public static final String  REGISTRY_PROTOCOL_LOCAL            = "local";

    /**
     * 线程池类型：固定线程池
     */
    public static final String  THREADPOOL_TYPE_FIXED              = "fixed";

    /**
     * 线程池类型：伸缩线程池
     */
    public static final String  THREADPOOL_TYPE_CACHED             = "cached";

    /**
     * 事件分发类型：all 所有消息都派发到业务线程池，包括请求，响应，连接事件，断开事件，心跳等。
     */
    public static final String  DISPATCHER_ALL                     = "all";

    /**
     * 事件分发类型：direct 所有消息都不派发到线程池，全部在IO线程上直接执行。
     */
    public static final String  DISPATCHER_DIRECT                  = "direct";

    /**
     * 事件分发类型：message 只有请求响应消息派发到线程池，其它连接断开事件，心跳等消息，直接在IO线程上执行。
     */
    public static final String  DISPATCHER_MESSAGE                 = "message";

    /**
     * 事件分发类型：execution 只请求消息派发到线程池，不含响应，响应和其它连接断开事件，心跳等消息，直接在IO线程上执行。
     */
    public static final String  DISPATCHER_EXECUTION               = "execution";

    /**
     * 事件分发类型：connection 在IO线程上，将连接断开事件放入队列，有序逐个执行，其它消息派发到线程池。
     */
    public static final String  DISPATCHER_CONNECTION              = "connection";

    /**
     * 队列类型：普通队列
     */
    public static final String  QUEUE_TYPE_NORMAL                  = "normal";

    /**
     * 队列类型：优先级队列
     */
    public static final String  QUEUE_TYPE_PRIORITY                = "priority";

    /**
     * 默认字符集 utf-8
     */
    public static final Charset DEFAULT_CHARSET                    = Charset
                                                                       .forName(getStringValue(RpcOptions.DEFAULT_CHARSET));

    /**
     * 调用方式：同步调用
     */
    public static final String  INVOKER_TYPE_SYNC                  = "sync";

    /**
     * 调用方式：单向
     */
    public static final String  INVOKER_TYPE_ONEWAY                = "oneway";
    /**
     * 调用方式：回调
     */
    public static final String  INVOKER_TYPE_CALLBACK              = "callback";
    /**
     * 调用方式：future
     */
    public static final String  INVOKER_TYPE_FUTURE                = "future";

    /**
     * Hessian序列化 [不推荐]
     *
     * @deprecated Use {@link #SERIALIZE_HESSIAN2}
     */
    public static final String  SERIALIZE_HESSIAN                  = "hessian";
    /**
     * Hessian2序列化
     */
    public static final String  SERIALIZE_HESSIAN2                 = "hessian2";
    /**
     * Java序列化
     */
    public static final String  SERIALIZE_JAVA                     = "java";
    /**
     * protobuf序列化
     */
    public static final String  SERIALIZE_PROTOBUF                 = "protobuf";
    /**
     * json序列化
     */
    public static final String  SERIALIZE_JSON                     = "json";

    /**
     * 协议类型：(tr+bolt) v==4.0?bolt:tr
     *
     * @since 5.1.0
     */
    public static final String  PROTOCOL_TYPE_TR                   = "tr";
    /**
     * 协议类型：bolt
     *
     * @since 5.1.0
     */
    public static final String  PROTOCOL_TYPE_BOLT                 = "bolt";
    /**
     * 协议类型：GRPC
     *
     * @since 5.1.0
     */
    public static final String  PROTOCOL_TYPE_GRPC                 = "grpc";
    /**
     * 协议类型：xfire
     *
     * @since 5.1.0
     */
    public static final String  PROTOCOL_TYPE_XFIRE                = "xfire";
    /**
     * 协议类型：rest
     *
     * @since 5.2.0
     */
    public static final String  PROTOCOL_TYPE_REST                 = "rest";
    /**
     * 协议类型：http (http/1.1)
     *
     * @since 5.4.0
     */
    public static final String  PROTOCOL_TYPE_HTTP                 = "http";
    /**
     * 协议类型：https
     *
     * @since 5.4.0
     */
    public static final String  PROTOCOL_TYPE_HTTPS                = "https";
    /**
     * 协议类型：http2 clear text
     *
     * @since 5.4.0
     */
    public static final String  PROTOCOL_TYPE_H2C                  = "h2c";
    /**
     * 协议类型：http2 
     *
     * @since 5.4.0
     */
    public static final String  PROTOCOL_TYPE_H2                   = "h2";

    /**
     * rest allow origins key
     *
     * @since 5.5.0
     */
    public static final String  ALLOWED_ORIGINS                    = "allowedOrigins";

    /**
     * bolt server process in io thread
     *
     * @since 5.5.6
     */
    public static final String  PROCESS_IN_IOTHREAD                = "processInIOThread";

    /**
     * bolt server process timeout discard in process
     *
     * @since 5.6.0
     */
    public static final String  TIMEOUT_DISCARD_IN_SERVER          = "timeoutDiscard";

    /*--------Config配置值相关结束---------*/

    /*--------上下文KEY相关开始---------*/
    /**
     * 隐藏的key前缀，防止和自定义key冲突，隐藏的key不会被关闭上下文传递功能
     */
    public static final char    HIDE_KEY_PREFIX                    = '.';
    /**
     * 内部使用的key前缀，防止和自定义key冲突，这种key可能被关闭上下文传递功能。
     */
    public static final char    INTERNAL_KEY_PREFIX                = '_';

    /**
     * 隐藏的key：.async_context 异步调用上下文
     */
    public static final String  HIDDEN_KEY_ASYNC_CONTEXT           = HIDE_KEY_PREFIX + "async_context";
    /**
     * 隐藏的key：.async_req 异步调用请求
     */
    public static final String  HIDDEN_KEY_ASYNC_REQUEST           = HIDE_KEY_PREFIX + "async_req";
    /**
     * 隐藏的key：.pinpoint 指定远程调用地址
     */
    public static final String  HIDDEN_KEY_PINPOINT                = HIDE_KEY_PREFIX + "pinpoint";
    /**
     * 隐藏的key：.token 指定调用Token
     */
    public static final String  HIDDEN_KEY_TOKEN                   = HIDE_KEY_PREFIX + "token";
    /**
     * 隐藏的key：.invoke_ctx 业务调用上下文
     */
    public static final String  HIDDEN_KEY_INVOKE_CONTEXT          = HIDE_KEY_PREFIX + "invoke_ctx";
    /**
     * 隐藏属性的key：consumer是否自动销毁（例如Registry和Monitor不需要自动销毁）
     */
    public static final String  HIDDEN_KEY_DESTROY                 = HIDE_KEY_PREFIX + "destroy";

    /**
     * 内部使用的key：_app_name，string
     */
    public static final String  INTERNAL_KEY_APP_NAME              = INTERNAL_KEY_PREFIX + "app_name";
    /**
     * 内部使用的key：_protocol_name，string
     */
    public static final String  INTERNAL_KEY_PROTOCOL_NAME         = INTERNAL_KEY_PREFIX + "protocol_name";
    /**
     * 内部使用的key：_req_size， int
     */
    public static final String  INTERNAL_KEY_REQ_SIZE              = INTERNAL_KEY_PREFIX + "req_size";
    /**
     * 内部使用的key：_req_serialize_time， int
     */
    public static final String  INTERNAL_KEY_REQ_SERIALIZE_TIME    = INTERNAL_KEY_PREFIX + "req_ser_time";
    /**
     * 内部使用的key：_req_deserialize_time， int
     */
    public static final String  INTERNAL_KEY_REQ_DESERIALIZE_TIME  = INTERNAL_KEY_PREFIX + "req_des_time";
    /**
     * 内部使用的key：_resp_size， int
     */
    public static final String  INTERNAL_KEY_RESP_SIZE             = INTERNAL_KEY_PREFIX + "resp_size";
    /**
     * 内部使用的key：_resp_serialized_time， int
     */
    public static final String  INTERNAL_KEY_RESP_SERIALIZE_TIME   = INTERNAL_KEY_PREFIX + "resp_ser_time";
    /**
     * 内部使用的key：_resp_deserialize_time， int
     */
    public static final String  INTERNAL_KEY_RESP_DESERIALIZE_TIME = INTERNAL_KEY_PREFIX + "resp_des_time";
    /**
     * 内部使用的key：_process_wait_time 在业务线程池里等待时间，long
     */
    public static final String  INTERNAL_KEY_PROCESS_WAIT_TIME     = INTERNAL_KEY_PREFIX + "process_wait_time";
    /**
     * 内部使用的key：_conn_create_time 长连接建立时间，long
     */
    public static final String  INTERNAL_KEY_CONN_CREATE_TIME      = INTERNAL_KEY_PREFIX + "conn_create_time";
    /**
     * 内部使用的key：_impl_elapse 业务代码执行耗时，long
     */
    public static final String  INTERNAL_KEY_IMPL_ELAPSE           = INTERNAL_KEY_PREFIX + "impl_elapse";
    /**
     * 内部使用的key：_client_elapse 客户端总耗时，long
     */
    public static final String  INTERNAL_KEY_CLIENT_ELAPSE         = INTERNAL_KEY_PREFIX + "client_elapse";
    /**
     * 内部使用的key：_client_send_time 客户端发送时间戳，long
     * 
     * @since 5.4.0
     */
    public static final String  INTERNAL_KEY_CLIENT_SEND_TIME      = INTERNAL_KEY_PREFIX + "client_send_time";

    /**
     * 内部使用的key：_server_receive_time 服务端接收时间戳，long
     *
     * @since 5.4.8
     */
    public static final String  INTERNAL_KEY_SERVER_RECEIVE_TIME   = INTERNAL_KEY_PREFIX + "server_receive_time";

    /**
     * 内部使用的key：_router_record 路由记录，string
     */
    public static final String  INTERNAL_KEY_ROUTER_RECORD         = INTERNAL_KEY_PREFIX + "router_record";
    /**
     * 内部使用的key：_invoke_times 调用次数，int
     */
    public static final String  INTERNAL_KEY_INVOKE_TIMES          = INTERNAL_KEY_PREFIX + "invoke_times";
    /**
     * 内部使用的key：_result_code，结果码
     *
     * @since 5.1.1
     */
    public static final String  INTERNAL_KEY_RESULT_CODE           = INTERNAL_KEY_PREFIX + "result_code";
    /**
     * 内部使用的key： _trace_id 
     *
     * @since 5.1.1
     */
    public static final String  INTERNAL_KEY_TRACE_ID              = INTERNAL_KEY_PREFIX + "trace_id";
    /**
     * 内部使用的key： _span_id 
     *
     * @since 5.1.1
     */
    public static final String  INTERNAL_KEY_SPAN_ID               = INTERNAL_KEY_PREFIX + "span_id";

    /**
     * 内部使用的key：_tracer_span
     */
    public static final String  INTERNAL_KEY_TRACER_SPAN           = INTERNAL_KEY_PREFIX + "tracer_span";

    /*--------上下文KEY相关结束---------*/

    /*--------配置项相关开始---------*/
    /**
     * 配置key:generic
     */
    public static final String  CONFIG_KEY_GENERIC                 = "generic";
    /**
     * 配置key:invokeType
     */
    public static final String  CONFIG_KEY_INVOKE_TYPE             = "invokeType";
    /**
     * 配置key:retries
     */
    public static final String  CONFIG_KEY_RETRIES                 = "retries";

    /**
     * 配置key:timeout
     */
    public static final String  CONFIG_KEY_TIMEOUT                 = "timeout";

    /**
     * 配置key:concurrents
     */
    public static final String  CONFIG_KEY_CONCURRENTS             = "concurrents";

    /**
     * 配置key:parameters
     */
    public static final String  CONFIG_KEY_PARAMS                  = "parameters";

    /**
     * 配置key:onReturn
     */
    public static final String  CONFIG_KEY_ONRETURN                = "onReturn";

    /**
     * 配置key:weight
     */
    public static final String  CONFIG_KEY_WEIGHT                  = "weight";

    /**
     * 配置key:interface | interfaceId
     */
    public static final String  CONFIG_KEY_INTERFACE               = "interface";

    /**
     * 配置key:alias
     */
    public static final String  CONFIG_KEY_UNIQUEID                = "uniqueId";

    /**
     * 配置key:dynamic
     */
    public static final String  CONFIG_KEY_DYNAMIC                 = "dynamic";

    /**
     * 配置key:validation
     */
    public static final String  CONFIG_KEY_VALIDATION              = "validation";

    /**
     * 配置key:mock
     */
    public static final String  CONFIG_KEY_MOCK                    = "mock";

    /**
     * 配置key:cache
     */
    public static final String  CONFIG_KEY_CACHE                   = "cache";

    /**
     * 配置key:compress
     */
    public static final String  CONFIG_KEY_COMPRESS                = "compress";

    /**
     * 配置key:priority
     */
    public static final String  CONFIG_KEY_PRIORITY                = "priority";

    /**
     * 配置key:rpcVersion
     */
    public static final String  CONFIG_KEY_RPC_VERSION             = "rpcVer";

    /**
     * 配置key:serialization
     */
    public static final String  CONFIG_KEY_SERIALIZATION           = "serialization";

    /**
     * 配置key:appName
     */
    public static final String  CONFIG_KEY_APP_NAME                = "appName";

    /**
     * 配置key:loadBalancer
     */
    public static final String  CONFIG_KEY_LOADBALANCER            = "loadBalancer";

    /**
     * 配置key:delay
     */
    public static final String  CONFIG_KEY_DELAY                   = "delay";

    /**
     * 配置key:id
     */
    public static final String  CONFIG_KEY_ID                      = "id";

    /**
     * 配置key:accepts
     */
    public static final String  CONFIG_KEY_ACCEPTS                 = "accepts";

    /**
     * 配置key:pid
     */
    public static final String  CONFIG_KEY_PID                     = "pid";

    /**
     * 配置key:language
     */
    public static final String  CONFIG_KEY_LANGUAGE                = "language";

    /**
     * 配置key:protocol
     */
    public static final String  CONFIG_KEY_PROTOCOL                = "protocol";

    /**
     * 配置key:protocol
     */
    public static final String  CONFIG_KEY_DUBBO_VERSION           = "dubboVersion";
    /*--------配置项相关结束---------*/

    /*--------客户端相关开始---------*/

    /**
     * 默认分组
     *
     * @since 5.1.0
     */
    public static final String  ADDRESS_DEFAULT_GROUP              = "_DEFAULT";

    /**
     * 默认直连分组
     *
     * @since 5.2.0
     */
    public static final String  ADDRESS_DIRECT_GROUP               = "_DIRECT";

    /*--------客户端相关结束---------*/

    /*--------系统参数相关开始---------*/
    /**
     * 全局配置的key
     */
    public static final String  GLOBAL_SETTING                     = "global_setting";

    /* --------系统参数相关结束---------*/

    public static final String  SOFA_REQUEST_HEADER_KEY            = "request_header_key";
}
