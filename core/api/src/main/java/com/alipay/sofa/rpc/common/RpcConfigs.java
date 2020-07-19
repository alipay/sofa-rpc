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

import com.alipay.sofa.rpc.base.Sortable;
import com.alipay.sofa.rpc.common.annotation.JustForTest;
import com.alipay.sofa.rpc.common.json.JSON;
import com.alipay.sofa.rpc.common.struct.OrderedComparator;
import com.alipay.sofa.rpc.common.utils.ClassLoaderUtils;
import com.alipay.sofa.rpc.common.utils.CommonUtils;
import com.alipay.sofa.rpc.common.utils.CompatibleTypeUtils;
import com.alipay.sofa.rpc.common.utils.FileUtils;
import com.alipay.sofa.rpc.core.exception.SofaRpcRuntimeException;
import com.alipay.sofa.rpc.log.LogCodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 配置加载器和操作入口
 *
 * @author <a href=mailto:zhanggeng.zg@antfin.com>GengZhang</a>
 */
public class RpcConfigs {

    /**
     * 全部配置
     */
    private final static ConcurrentMap<String, Object>                  CFG          = new ConcurrentHashMap<String, Object>();
    /**
     * 配置变化监听器
     */
    private final static ConcurrentMap<String, List<RpcConfigListener>> CFG_LISTENER = new ConcurrentHashMap<String,
                                                                                             List<RpcConfigListener>>();

    static {
        init(); // 加载配置文件
    }

    private static void init() {
        try {
            // loadDefault
            String json = FileUtils.file2String(RpcConfigs.class, "rpc-config-default.json", "UTF-8");
            Map map = JSON.parseObject(json, Map.class);
            CFG.putAll(map);

            // loadCustom
            loadCustom("sofa-rpc/rpc-config.json");
            loadCustom("META-INF/sofa-rpc/rpc-config.json");

            // load system properties
            CFG.putAll(new HashMap(System.getProperties())); // 注意部分属性可能被覆盖为字符串
        } catch (Exception e) {
            throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_LOAD_RPC_CONFIGS), e);
        }
    }

    /**
     * 加载自定义配置文件
     *
     * @param fileName 文件名
     * @throws IOException 加载异常
     */
    private static void loadCustom(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoaderUtils.getClassLoader(RpcConfigs.class);
        Enumeration<URL> urls = classLoader != null ? classLoader.getResources(fileName)
            : ClassLoader.getSystemResources(fileName);
        if (urls != null) { // 可能存在多个文件
            List<CfgFile> allFile = new ArrayList<CfgFile>();
            while (urls.hasMoreElements()) {
                // 读取每一个文件
                URL url = urls.nextElement();
                InputStreamReader input = null;
                BufferedReader reader = null;
                try {
                    input = new InputStreamReader(url.openStream(), "utf-8");
                    reader = new BufferedReader(input);
                    StringBuilder context = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        context.append(line).append("\n");
                    }
                    Map map = JSON.parseObject(context.toString(), Map.class);
                    Integer order = (Integer) map.get(RpcOptions.RPC_CFG_ORDER);
                    allFile.add(new CfgFile(url, order == null ? 0 : order, map));
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                }
            }
            Collections.sort(allFile, new OrderedComparator<CfgFile>()); // 从小到大排下序
            for (CfgFile file : allFile) {
                CFG.putAll(file.getMap()); // 顺序加载，越大越后加载
            }
        }
    }

    /**
     * Put value.
     *
     * @param key      the key
     * @param newValue the new value
     */
    public static void putValue(String key, Object newValue) {
        Object oldValue = CFG.get(key);
        if (changed(oldValue, newValue)) {
            CFG.put(key, newValue);
            List<RpcConfigListener> rpcConfigListeners = CFG_LISTENER.get(key);
            if (CommonUtils.isNotEmpty(rpcConfigListeners)) {
                for (RpcConfigListener rpcConfigListener : rpcConfigListeners) {
                    rpcConfigListener.onChange(oldValue, newValue);
                }
            }
        }
    }

    /**
     * Remove value
     *
     * @param key Key
     */
    @JustForTest
    synchronized static void removeValue(String key) {
        Object oldValue = CFG.get(key);
        if (oldValue != null) {
            CFG.remove(key);
            List<RpcConfigListener> rpcConfigListeners = CFG_LISTENER.get(key);
            if (CommonUtils.isNotEmpty(rpcConfigListeners)) {
                for (RpcConfigListener rpcConfigListener : rpcConfigListeners) {
                    rpcConfigListener.onChange(oldValue, null);
                }
            }
        }
    }

    /**
     * Gets boolean value.
     *
     * @param primaryKey the primary key
     * @return the boolean value
     */
    public static boolean getBooleanValue(String primaryKey) {
        Object val = CFG.get(primaryKey);
        if (val == null) {
            throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_NOT_FOUND_KEY, primaryKey));
        } else {
            return Boolean.valueOf(val.toString());
        }
    }

    /**
     * Gets boolean value.
     *
     * @param primaryKey   the primary key
     * @param secondaryKey the secondary key
     * @return the boolean value
     */
    public static boolean getBooleanValue(String primaryKey, String secondaryKey) {
        Object val = CFG.get(primaryKey);
        if (val == null) {
            val = CFG.get(secondaryKey);
            if (val == null) {
                throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_NOT_FOUND_KEY, primaryKey + "/" +
                    secondaryKey));
            }
        }
        return Boolean.valueOf(val.toString());
    }

    /**
     * Gets int value.
     *
     * @param primaryKey the primary key
     * @return the int value
     */
    public static int getIntValue(String primaryKey) {
        Object val = CFG.get(primaryKey);
        if (val == null) {
            throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_NOT_FOUND_KEY, primaryKey));
        } else {
            return Integer.parseInt(val.toString());
        }
    }

    /**
     * Gets int value.
     *
     * @param primaryKey   the primary key
     * @param secondaryKey the secondary key
     * @return the int value
     */
    public static int getIntValue(String primaryKey, String secondaryKey) {
        Object val = CFG.get(primaryKey);
        if (val == null) {
            val = CFG.get(secondaryKey);
            if (val == null) {
                throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_NOT_FOUND_KEY, primaryKey + "/" +
                    secondaryKey));
            }
        }
        return Integer.parseInt(val.toString());
    }

    /**
     * Gets enum value.
     *
     * @param <T>        the type parameter
     * @param primaryKey the primary key
     * @param enumClazz  the enum clazz
     * @return the enum value
     */
    public static <T extends Enum<T>> T getEnumValue(String primaryKey, Class<T> enumClazz) {
        String val = (String) CFG.get(primaryKey);
        if (val == null) {
            throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_NOT_FOUND_KEY, primaryKey));
        } else {
            return Enum.valueOf(enumClazz, val);
        }
    }

    /**
     * Gets string value.
     *
     * @param primaryKey the primary key
     * @return the string value
     */
    public static String getStringValue(String primaryKey) {
        String val = (String) CFG.get(primaryKey);
        if (val == null) {
            throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_NOT_FOUND_KEY, primaryKey));
        } else {
            return val;
        }
    }

    /**
     * Gets string value.
     *
     * @param primaryKey   the primary key
     * @param secondaryKey the secondary key
     * @return the string value
     */
    public static String getStringValue(String primaryKey, String secondaryKey) {
        String val = (String) CFG.get(primaryKey);
        if (val == null) {
            val = (String) CFG.get(secondaryKey);
            if (val == null) {
                throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_NOT_FOUND_KEY, primaryKey + "/" +
                    secondaryKey));
            } else {
                return val;
            }
        } else {
            return val;
        }
    }

    /**
     * Gets list value.
     *
     * @param primaryKey the primary key
     * @return the list value
     */
    public static List getListValue(String primaryKey) {
        List val = (List) CFG.get(primaryKey);
        if (val == null) {
            throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_NOT_FOUND_KEY, primaryKey));
        } else {
            return val;
        }
    }

    /**
     * Gets or default value.
     *
     * @param <T>          the type parameter
     * @param primaryKey   the primary key
     * @param defaultValue the default value
     * @return the or default value
     */
    public static <T> T getOrDefaultValue(String primaryKey, T defaultValue) {
        Object val = CFG.get(primaryKey);
        if (val == null) {
            return defaultValue;
        } else {
            Class<?> type = defaultValue == null ? null : defaultValue.getClass();
            return (T) CompatibleTypeUtils.convert(val, type);
        }
    }

    /**
     * 订阅配置变化
     *
     * @param key      关键字
     * @param listener 配置监听器
     * @see RpcOptions
     */
    public static synchronized void subscribe(String key, RpcConfigListener listener) {
        List<RpcConfigListener> listeners = CFG_LISTENER.get(key);
        if (listeners == null) {
            listeners = new ArrayList<RpcConfigListener>();
            CFG_LISTENER.put(key, listeners);
        }
        listeners.add(listener);
    }

    /**
     * 取消订阅配置变化
     *
     * @param key      关键字
     * @param listener 配置监听器
     * @see RpcOptions
     */
    public static synchronized void unSubscribe(String key, RpcConfigListener listener) {
        List<RpcConfigListener> listeners = CFG_LISTENER.get(key);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.size() == 0) {
                CFG_LISTENER.remove(key);
            }
        }
    }

    /**
     * 值是否发生变化
     *
     * @param oldObj 旧值
     * @param newObj 新值
     * @return 是否变化 boolean
     */
    protected static boolean changed(Object oldObj, Object newObj) {
        return oldObj == null ?
            newObj != null :
            !oldObj.equals(newObj);
    }

    /**
     * 用于排序的一个类
     */
    private static class CfgFile implements Sortable {
        private final URL url;
        private final int order;
        private final Map map;

        /**
         * Instantiates a new Cfg file.
         *
         * @param url   the url
         * @param order the order
         * @param map   the map
         */
        public CfgFile(URL url, int order, Map map) {
            this.url = url;
            this.order = order;
            this.map = map;
        }

        /**
         * Gets url.
         *
         * @return the url
         */
        public URL getUrl() {
            return url;
        }

        @Override
        public int getOrder() {
            return order;
        }

        /**
         * Gets map.
         *
         * @return the map
         */
        public Map getMap() {
            return map;
        }
    }

    /**
     * 配置变更会拿到通知
     *
     * @param <T> the type parameter
     */
    public interface RpcConfigListener<T> {
        /**
         * On change.
         *
         * @param oldValue the old value
         * @param newValue the new value
         */
        public void onChange(T oldValue, T newValue);
    }

    /**
     * reload system properties for test
     */
    public static void reloadSystemProperties() {
        CFG.putAll(new HashMap(System.getProperties())); // 注意部分属性可能被覆盖为字符串
    }
}