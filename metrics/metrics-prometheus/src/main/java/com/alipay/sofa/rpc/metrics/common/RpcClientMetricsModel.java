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
 * The model for lookout info of client
 *
 * @author <a href="mailto:lw111072@antfin.com">LiWei.Liangen</a>
 */
public class RpcClientMetricsModel extends RpcAbstractMetricsModel {

    protected String targetApp;

    protected Long   requestSize;

    protected Long   responseSize;

    /**
     * Getter method for property <tt>targetApp</tt>.
     *
     * @return property value of targetApp
     */
    public String getTargetApp() {
        return targetApp;
    }

    /**
     * Setter method for property <tt>targetApp</tt>.
     *
     * @param targetApp value to be assigned to property targetApp
     */
    public void setTargetApp(String targetApp) {
        this.targetApp = targetApp;
    }

    /**
     * Getter method for property <tt>requestSize</tt>.
     *
     * @return property value of requestSize
     */
    public Long getRequestSize() {
        return requestSize;
    }

    /**
     * Setter method for property <tt>requestSize</tt>.
     *
     * @param requestSize value to be assigned to property requestSize
     */
    public void setRequestSize(Long requestSize) {
        this.requestSize = requestSize;
    }

    /**
     * Getter method for property <tt>responseSize</tt>.
     *
     * @return property value of responseSize
     */
    public Long getResponseSize() {
        return responseSize;
    }

    /**
     * Setter method for property <tt>responseSize</tt>.
     *
     * @param responseSize value to be assigned to property responseSize
     */
    public void setResponseSize(Long responseSize) {
        this.responseSize = responseSize;
    }

    @Override
    public String toString() {
        return "RpcClientLookoutModel{" +
            "targetApp='" + targetApp + '\'' +
            ", requestSize=" + requestSize +
            ", responseSize=" + responseSize +
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