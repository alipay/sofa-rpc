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
package com.alipay.sofa.rpc.metrics.common;

/**
 * @author zhaowang
 * @version : MetricsConstants.java, v 0.1 2020年04月01日 9:21 下午 zhaowang Exp $
 */
public class MetricsConstants {

    // metric name
    public static final String CLIENT_COUNTER       = "client_counter";
    public static final String SERVER_COUNTER       = "server_counter";
    public static final String CLIENT_RT            = "client_rt";
    public static final String SERVER_RT            = "server_rt";
    public static final String CLIENT_REQUEST_SIZE  = "client_request_size";
    public static final String CLIENT_RESPONSE_SIZE = "client_response_size";

    // label key -- common
    public static final String APP                  = "app";
    public static final String SERVICE              = "service";
    public static final String METHOD               = "method";
    public static final String PROTOCOL             = "protocol";
    public static final String SUCCESS              = "success";

    // label key -- client
    public static final String INVOKE_TYPE          = "invoke_type";
    public static final String TARGET_APP           = "target_app";

    // label key -- server
    public static final String CALLER_APP           = "caller_app";

}