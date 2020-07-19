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
 * The model for lookout info of server
 *
 * @author <a href="mailto:lw111072@antfin.com">LiWei.Liangen</a>
 */
public class RpcServerMetricsModel extends RpcAbstractMetricsModel {

    protected String callerApp;

    /**
     * Getter method for property <tt>callerApp</tt>.
     *
     * @return property value of callerApp
     */
    public String getCallerApp() {
        return callerApp;
    }

    /**
     * Setter method for property <tt>callerApp</tt>.
     *
     * @param callerApp value to be assigned to property callerApp
     */
    public void setCallerApp(String callerApp) {
        this.callerApp = callerApp;
    }

    @Override
    public String toString() {
        return "RpcServerLookoutModel{" +
            "callerApp='" + callerApp + '\'' +
            ", app='" + app + '\'' +
            ", service='" + service + '\'' +
            ", method='" + method + '\'' +
            ", protocol='" + protocol + '\'' +
            ", invokeType='" + invokeType + '\'' +
            ", elapsedTime=" + elapsedTime +
            ", success=" + success +
            '}';
    }
}