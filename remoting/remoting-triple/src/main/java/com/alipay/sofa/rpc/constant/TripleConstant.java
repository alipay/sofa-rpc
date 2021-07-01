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
package com.alipay.sofa.rpc.constant;

import com.alipay.sofa.common.config.SofaConfigs;
import com.alipay.sofa.rpc.common.config.RpcConfigKeys;
import io.grpc.CallOptions;

/**
 * @author zhaowang
 * @version : TripleConstant.java, v 0.1 2020年09月01日 8:05 下午 zhaowang Exp $
 */
public class TripleConstant {
    public static final CallOptions.Key<String> UNIQUE_ID                    = CallOptions.Key.createWithDefault(
                                                                                 "uniqueId", "");
    public static final String                  TRIPLE_EXPOSE_OLD            = "triple.use.old.path";
    public static final Boolean                 EXPOSE_OLD_UNIQUE_ID_SERVICE = SofaConfigs
                                                                                 .getOrDefault(RpcConfigKeys.TRIPLE_EXPOSE_OLD_UNIQUE_ID_SERVICE);
}