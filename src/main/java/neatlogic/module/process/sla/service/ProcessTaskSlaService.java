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

package neatlogic.module.process.sla.service;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.dto.ProcessTaskSlaTimeCostVo;
import neatlogic.framework.process.dto.ProcessTaskSlaTimeVo;
import org.springframework.stereotype.Service;

@Service
public interface ProcessTaskSlaService {
    /**
     * 创建ProcessTaskSlaTimeVo
     * @param slaId 时效ID
     * @param timeSum 总时效长度
     * @param currentTimeMillis 这次计算时效的时间点
     * @param timeCostVo 时效耗时信息
     * @return
     */
    ProcessTaskSlaTimeVo createSlaTime(Long slaId, Long timeSum, long currentTimeMillis, ProcessTaskSlaTimeCostVo timeCostVo);

    /**
     * 根据时间窗口和时效耗时数据，计算超时时间点
     * @param slaTimeVo
     * @param currentTimeMillis
     * @param worktimeUuid
     */
    void recalculateExpireTime(ProcessTaskSlaTimeVo slaTimeVo, long currentTimeMillis, String worktimeUuid);

    void loadJobNotifyAndTransfer(Long slaId, JSONObject slaConfigObj);
}
