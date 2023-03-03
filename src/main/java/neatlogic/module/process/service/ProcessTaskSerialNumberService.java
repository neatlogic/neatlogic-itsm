/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.service;

import neatlogic.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.function.Function;

public interface ProcessTaskSerialNumberService {

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
     * @param channelTypeUuid 服务类型
     * @param dateFormat                      日期格式
     * @return
     */
    String genarate(String channelTypeUuid, DateFormat dateFormat);

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

    /**
     * 更新工单号自增值
     * @param channelTypeUuid
     * @param function
     * @return
     */
    Long updateProcessTaskSerialNumberPolicySerialNumberSeedByChannelTypeUuid(String channelTypeUuid, Function<ProcessTaskSerialNumberPolicyVo, Long> function);
}
