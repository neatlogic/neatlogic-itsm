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
