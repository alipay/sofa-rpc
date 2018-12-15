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
package com.alipay.sofa.rpc.hystrix;

import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.filter.FilterInvoker;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixObservableCommand;

/**
 * Control the properties of a hystrix command, such as group key, command key...
 *
 * @author <a href=mailto:scienjus@gmail.com>ScienJus</a>
 */
public interface SetterFactory {

    /**
     * Create a {@link HystrixObservableCommand.Setter} with the given invoker and request
     * @param invoker
     * @param request
     * @return
     */
    HystrixCommand.Setter createSetter(FilterInvoker invoker, SofaRequest request);

    /**
     * Create a {@link HystrixObservableCommand.Setter} with the given invoker and request
     * @param invoker
     * @param request
     * @return
     */
    HystrixObservableCommand.Setter createObservableSetter(FilterInvoker invoker, SofaRequest request);

}
