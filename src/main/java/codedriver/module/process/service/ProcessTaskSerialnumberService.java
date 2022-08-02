/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.service;

import codedriver.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.function.Function;

public interface ProcessTaskSerialnumberService {

    /**
     * 工单号策略属性
     *
     * @param startDigit 工单号位数起始值
     * @param endDigit   工单号位数结束值
     * @return
     */
    JSONArray makeupFormAttributeList(int startDigit, int endDigit);

    /**
     * 工单号策略配置
     * 以日期或其他固定位数的数字串+自增序号的工单号策略，计算自增序号的位数时，需要减去固定串的位数，而后做10的次幂计算才能得到自增序号的位数
     *
     * @param jsonObj
     * @param prefixDigits 固定串位数
     * @return
     */
    JSONObject makeupConfig(JSONObject jsonObj, int prefixDigits);

    /**
     * 生成工单号
     *
     * @param processTaskSerialNumberPolicyVo
     * @param dateFormat                      日期格式
     * @return
     */
    String genarate(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo, DateFormat dateFormat);

    /**
     * 批量更新工单号
     *
     * @param processTaskSerialNumberPolicyVo
     * @param dateFormat                      日期格式
     * @return
     */
    int batchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo, DateFormat dateFormat);

    /**
     * @param processTaskSerialNumberPolicyVo
     * @param startTimeLimit                  是否限定startTime
     * @param startDate                      开始计算的的日期
     * @return
     */
    Long calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo, boolean startTimeLimit, Date startDate);

    /**
     * @param processTaskSerialNumberPolicyVo
     * @param startTimeLimit                  是否限定startTime
     * @return
     */
    Long calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo, boolean startTimeLimit);

    void serialNumberSeedReset(String handlerClassName);

    Long updateProcessTaskSerialNumberPolicySerialNumberSeedByChannelTypeUuid(String channelTypeUuid, Function<ProcessTaskSerialNumberPolicyVo, Long> function);
}
