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
package com.alipay.sofa.rpc.proxy.javassist;

import com.alipay.sofa.rpc.common.utils.ClassLoaderUtils;
import com.alipay.sofa.rpc.common.utils.ClassTypeUtils;
import com.alipay.sofa.rpc.common.utils.ReflectUtils;
import com.alipay.sofa.rpc.core.exception.RpcErrorType;
import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.exception.SofaRpcRuntimeException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.ext.Extension;
import com.alipay.sofa.rpc.invoke.Invoker;
import com.alipay.sofa.rpc.log.LogCodes;
import com.alipay.sofa.rpc.log.Logger;
import com.alipay.sofa.rpc.log.LoggerFactory;
import com.alipay.sofa.rpc.message.MessageBuilder;
import com.alipay.sofa.rpc.proxy.Proxy;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Proxy implement base on javassist
 *
 * @author <a href=mailto:zhanggeng.zg@antfin.com>GengZhang</a>
 */
@Extension("javassist")
public class JavassistProxy implements Proxy {

    /**
     * Logger for this class
     */
    private static final Logger            LOGGER          = LoggerFactory.getLogger(JavassistProxy.class);

    private static AtomicInteger           counter         = new AtomicInteger();

    /**
     * 原始类和代理类的映射
     */
    private static final Map<Class, Class> PROXY_CLASS_MAP = new ConcurrentHashMap<Class, Class>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> interfaceClass, Invoker proxyInvoker) {
        StringBuilder debug = null;
        if (LOGGER.isDebugEnabled()) {
            debug = new StringBuilder();
        }
        try {
            Class clazz = PROXY_CLASS_MAP.get(interfaceClass);
            if (clazz == null) {
                //生成代理类
                String interfaceName = ClassTypeUtils.getTypeStr(interfaceClass);
                ClassPool mPool = ClassPool.getDefault();
                mPool.appendClassPath(new LoaderClassPath(ClassLoaderUtils.getClassLoader(JavassistProxy.class)));
                CtClass mCtc = mPool.makeClass(interfaceName + "_proxy_" + counter.getAndIncrement());
                if (interfaceClass.isInterface()) {
                    mCtc.addInterface(mPool.get(interfaceName));
                } else {
                    throw new IllegalArgumentException(interfaceClass.getName() + " is not an interface");
                }

                // 继承 java.lang.reflect.Proxy
                mCtc.setSuperclass(mPool.get(java.lang.reflect.Proxy.class.getName()));
                CtConstructor constructor = new CtConstructor(null, mCtc);
                constructor.setModifiers(Modifier.PUBLIC);
                constructor.setBody("{super(new " + UselessInvocationHandler.class.getName() + "());}");
                mCtc.addConstructor(constructor);

                List<String> fieldList = new ArrayList<>();
                List<String> methodList = new ArrayList<>();

                fieldList.add("public " + Invoker.class.getCanonicalName() + " proxyInvoker = null;");
                createMethod(interfaceClass, fieldList, methodList);

                for (String fieldStr : fieldList) {
                    if (LOGGER.isDebugEnabled()) {
                        debug.append(fieldStr).append("\n");
                    }
                    mCtc.addField(CtField.make(fieldStr, mCtc));
                }
                for (String methodStr : methodList) {
                    if (LOGGER.isDebugEnabled()) {
                        debug.append(methodStr).append("\n");
                    }
                    mCtc.addMethod(CtMethod.make(methodStr, mCtc));
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("javassist proxy of interface: {} \r\n{}", interfaceClass,
                        debug != null ? debug.toString() : "");
                }
                clazz = mCtc.toClass();
                PROXY_CLASS_MAP.put(interfaceClass, clazz);
            }
            Object instance = clazz.newInstance();
            clazz.getField("proxyInvoker").set(instance, proxyInvoker);
            return (T) instance;
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("javassist proxy of interface: {} \r\n{}", interfaceClass,
                    debug != null ? debug.toString() : "");
            }
            throw new SofaRpcRuntimeException(LogCodes.getLog(LogCodes.ERROR_PROXY_CONSTRUCT, "javassist"), e);
        }
    }

    private void createMethod(Class<?> interfaceClass, List<String> fieldList, List<String> resultList) {
        Method[] methodAry = interfaceClass.getMethods();
        StringBuilder sb = new StringBuilder(512);
        int mi = 0;
        for (Method m : methodAry) {
            mi++;
            if (Modifier.isNative(m.getModifiers()) || Modifier.isFinal(m.getModifiers()) || Modifier.isStatic(m.getModifiers())) {
                continue;
            }
            Class<?>[] mType = m.getParameterTypes();
            Class<?> returnType = m.getReturnType();

            sb.append(Modifier.toString(m.getModifiers()).replace("abstract", ""))
                .append(" ").append(ClassTypeUtils.getTypeStr(returnType)).append(" ").append(m.getName())
                .append("( ");
            int c = 0;

            for (Class<?> mp : mType) {
                sb.append(" ").append(mp.getCanonicalName()).append(" arg").append(c).append(" ,");
                c++;
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(")");
            Class<?>[] exceptions = m.getExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (Class<?> exception : exceptions) {
                    sb.append(exception.getCanonicalName()).append(" ,");
                }
                sb = sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("{");

            sb.append(" Class clazz = ").append(interfaceClass.getCanonicalName()).append(".class;");
            sb.append(" ").append(Method.class.getCanonicalName()).append(" method =  method_").append(mi).append(";");
            sb.append(" Class[] paramTypes = new Class[").append(c).append("];");
            sb.append(" Object[] paramValues = new Object[").append(c).append("];");
            StringBuilder methodSig = new StringBuilder();
            for (int i = 0; i < c; i++) {
                sb.append("paramValues[").append(i).append("] = ($w)$").append(i + 1).append(";");
                sb.append("paramTypes[").append(i).append("] = ").append(mType[i].getCanonicalName()).append(".class;");
                methodSig.append("," + mType[i].getCanonicalName() + ".class");
            }

            fieldList.add("private " + Method.class.getCanonicalName() + " method_" + mi + " = "
                + ReflectUtils.class.getCanonicalName() + ".getMethod("
                + interfaceClass.getCanonicalName() + ".class, \"" + m.getName() + "\", "
                + (c > 0 ? "new Class[]{" + methodSig.toString().substring(1) + "}" : "new Class[0]") + ");"
                );

            sb.append(SofaRequest.class.getCanonicalName()).append(" request = ")
                .append(MessageBuilder.class.getCanonicalName())
                .append(".buildSofaRequest(clazz, method, paramTypes, paramValues);");
            sb.append(SofaResponse.class.getCanonicalName()).append(" response = ")
                .append("proxyInvoker.invoke(request);");
            sb.append("if(response.isError()){");
            sb.append("  throw new ").append(SofaRpcException.class.getName()).append("(")
                .append(RpcErrorType.class.getName())
                .append(".SERVER_UNDECLARED_ERROR,").append(" response.getErrorMsg());");
            sb.append("}");

            sb.append("Object ret = response.getAppResponse();");
            sb.append("if (ret instanceof Throwable) {");
            sb.append("    throw (Throwable) ret;");
            sb.append("} else {");
            if (returnType.equals(void.class)) {
                sb.append(" return;");
            } else {
                sb.append(" return ").append(asArgument(returnType, "ret")).append(";");
            }
            sb.append("}");

            sb.append("}");

            resultList.add(sb.toString());
            sb.delete(0, sb.length());
        }

        // toString()
        sb.append("public String toString() {");
        sb.append("  return proxyInvoker.toString();");
        sb.append("}");
        resultList.add(sb.toString());
        // hashCode()
        sb.delete(0, sb.length());
        sb.append("public int hashCode() {");
        sb.append("  return proxyInvoker.hashCode();");
        sb.append("}");
        resultList.add(sb.toString());
        // equals()
        sb.delete(0, sb.length());
        sb.append("public boolean equals(Object obj) {");
        sb.append("  return this == obj || (getClass().isInstance($1) && proxyInvoker.equals(")
            .append(JavassistProxy.class.getName()).append(".parseInvoker($1)));");
        sb.append("}");
        resultList.add(sb.toString());
    }

    private String asArgument(Class<?> cl, String name) {
        if (cl.isPrimitive()) {
            if (Boolean.TYPE == cl) {
                return name + "==null?false:((Boolean)" + name + ").booleanValue()";
            }
            if (Byte.TYPE == cl) {
                return name + "==null?(byte)0:((Byte)" + name + ").byteValue()";
            }
            if (Character.TYPE == cl) {
                return name + "==null?(char)0:((Character)" + name + ").charValue()";
            }
            if (Double.TYPE == cl) {
                return name + "==null?(double)0:((Double)" + name + ").doubleValue()";
            }
            if (Float.TYPE == cl) {
                return name + "==null?(float)0:((Float)" + name + ").floatValue()";
            }
            if (Integer.TYPE == cl) {
                return name + "==null?(int)0:((Integer)" + name + ").intValue()";
            }
            if (Long.TYPE == cl) {
                return name + "==null?(long)0:((Long)" + name + ").longValue()";
            }
            if (Short.TYPE == cl) {
                return name + "==null?(short)0:((Short)" + name + ").shortValue()";
            }
            throw new RuntimeException(name + " is unknown primitive type.");
        }
        return "(" + ClassTypeUtils.getTypeStr(cl) + ")" + name;
    }

    @Override
    public Invoker getInvoker(Object proxyObject) {
        return parseInvoker(proxyObject);
    }

    /**
     * Parse proxy invoker from proxy object
     *
     * @param proxyObject Proxy object
     * @return proxy invoker
     */
    public static Invoker parseInvoker(Object proxyObject) {
        try {
            Field field = proxyObject.getClass().getField("proxyInvoker");
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return (Invoker) field.get(proxyObject);
        } catch (Exception e) {
            return null;
        }
    }
}
