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
 * @author <a href="mailto:lw111072@antfin.com">LiWei.Liangen</a>
 */
public class RpcAbstractMetricsModel {

    protected String  app;

    protected String  service;

    protected String  method;

    protected String  protocol;

    protected String  invokeType;

    protected Long    elapsedTime;

    protected boolean success;

    /**
     * Getter method for property <tt>app</tt>.
     *
     * @return property value of app
     */
    public String getApp() {
        return app;
    }

    /**
     * Setter method for property <tt>app</tt>.
     *
     * @param app value to be assigned to property app
     */
    public void setApp(String app) {
        this.app = app;
    }

    /**
     * Getter method for property <tt>service</tt>.
     *
     * @return property value of service
     */
    public String getService() {
        return service;
    }

    /**
     * Setter method for property <tt>service</tt>.
     *
     * @param service value to be assigned to property service
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * Getter method for property <tt>method</tt>.
     *
     * @return property value of method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Setter method for property <tt>method</tt>.
     *
     * @param method value to be assigned to property method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Getter method for property <tt>protocol</tt>.
     *
     * @return property value of protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Setter method for property <tt>protocol</tt>.
     *
     * @param protocol value to be assigned to property protocol
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Getter method for property <tt>invokeType</tt>.
     *
     * @return property value of invokeType
     */
    public String getInvokeType() {
        return invokeType;
    }

    /**
     * Setter method for property <tt>invokeType</tt>.
     *
     * @param invokeType value to be assigned to property invokeType
     */
    public void setInvokeType(String invokeType) {
        this.invokeType = invokeType;
    }

    /**
     * Getter method for property <tt>elapsedTime</tt>.
     *
     * @return property value of elapsedTime
     */
    public Long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Setter method for property <tt>elapsedTime</tt>.
     *
     * @param elapsedTime value to be assigned to property elapsedTime
     */
    public void setElapsedTime(Long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    /**
     * Getter method for property <tt>success</tt>.
     *
     * @return property value of success
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * Setter method for property <tt>success</tt>.
     *
     * @param success value to be assigned to property success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
}