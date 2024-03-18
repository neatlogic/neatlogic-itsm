/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
