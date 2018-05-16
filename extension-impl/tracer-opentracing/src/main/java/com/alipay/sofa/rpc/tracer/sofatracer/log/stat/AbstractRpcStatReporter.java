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
package com.alipay.sofa.rpc.tracer.sofatracer.log.stat;

import com.alipay.common.tracer.core.appender.builder.XStringBuilder;
import com.alipay.common.tracer.core.reporter.stat.AbstractSofaTracerStatisticReporter;
import com.alipay.common.tracer.core.reporter.stat.model.StatKey;
import com.alipay.common.tracer.core.span.SofaTracerSpan;
import com.alipay.common.tracer.core.utils.TracerUtils;
import com.alipay.sofa.rpc.tracer.sofatracer.log.tags.RpcSpanTags;

import java.util.Map;

/**
 * AbstractRpcStatReporter
 *
 * @author <a href=mailto:guanchao.ygc@antfin.com>GuanChao Yang</a>
 */
public abstract class AbstractRpcStatReporter extends AbstractSofaTracerStatisticReporter {

    public AbstractRpcStatReporter(String statTracerName, String rollingPolicy, String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    /***
     * 统计一次 span
     * @param sofaTracerSpan 被统计的一次 span
     */
    @Override
    public void doReportStat(SofaTracerSpan sofaTracerSpan) {
        //tags
        Map<String, String> tagsWithStr = sofaTracerSpan.getTagsWithStr();

        StatKey statKey = new StatKey();

        String fromApp = getFromApp(tagsWithStr);
        String toApp = getToApp(tagsWithStr);
        String zone = getZone(tagsWithStr);

        //service name
        String serviceName = tagsWithStr.get(RpcSpanTags.SERVICE);
        //method name
        String methodName = tagsWithStr.get(RpcSpanTags.METHOD);

        statKey.setKey(buildString(new String[] { fromApp, toApp, serviceName, methodName }));
        String resultCode = tagsWithStr.get(RpcSpanTags.RESULT_CODE);
        statKey.setResult(isSuccess(resultCode) ? "Y" : "N");
        statKey.setEnd(buildString(new String[] { getLoadTestMark(sofaTracerSpan), zone }));
        statKey.setLoadTest(TracerUtils.isLoadTest(sofaTracerSpan));
        //次数和耗时，最后一个耗时是单独打印的字段
        long duration = sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime();
        long[] values = new long[] { 1, duration };
        this.addStat(statKey, values);
    }

    /**
     * Get from app
     *
     * @param tagsWithStr tags
     * @return from app
     */
    public abstract String getFromApp(Map<String, String> tagsWithStr);

    /**
     * Get to app
     *
     * @param tagsWithStr tags
     * @return to app
     */
    public abstract String getToApp(Map<String, String> tagsWithStr);

    /**
     * Get zone
     *
     * @param tagsWithStr tags
     * @return zone
     */
    public abstract String getZone(Map<String, String> tagsWithStr);

    @Override
    protected String buildString(String[] keys) {
        XStringBuilder sb = new XStringBuilder();
        int i;
        for (i = 0; i < keys.length - 1; i++) {
            sb.append(keys[i] == null ? "" : keys[i]);
        }
        sb.appendRaw(keys[i] == null ? "" : keys[i]);
        return sb.toString();
    }

    protected boolean isSuccess(String resultCode) {
        //todo 需要判断成功失败的标识
        return "00".equals(resultCode) || "0".equals(resultCode)
            || com.alipay.sofa.rpc.common.utils.StringUtils.isBlank(resultCode);
    }

    protected String getLoadTestMark(SofaTracerSpan span) {
        if (TracerUtils.isLoadTest(span)) {
            return "T";
        } else {
            return "F";
        }
    }
}
