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

package neatlogic.module.process.sla.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.process.constvalue.SlaStatus;
import neatlogic.framework.process.dao.mapper.ProcessTaskSlaMapper;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.sla.SlaCalculateHandlerNotFoundException;
import neatlogic.framework.process.sla.core.ISlaCalculateHandler;
import neatlogic.framework.process.sla.core.SlaCalculateHandlerFactory;
import neatlogic.framework.sla.core.ISlaRecalculateHandler;
import neatlogic.module.process.sla.service.SlaService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Component
public class ItsmSlaRecalculateHandler implements ISlaRecalculateHandler {

    private Logger logger = LoggerFactory.getLogger(ItsmSlaRecalculateHandler.class);
    @Resource
    private ProcessTaskSlaMapper processTaskSlaMapper;

    @Resource
    private SlaService slaService;

    @Override
    public void execute(String worktimeUuid) {
        int rowNum = processTaskSlaMapper.getDoingOrPauseSlaIdCountByWorktimeUuid(worktimeUuid);
        if (rowNum == 0) {
            return;
        }
        BasePageVo searchVo = new BasePageVo();
        searchVo.setRowNum(rowNum);
        searchVo.setPageSize(100);
        int pageCount = searchVo.getPageCount();
        for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
            searchVo.setCurrentPage(currentPage);
            List<Long> slaIdList = processTaskSlaMapper.getDoingOrPauseSlaIdListByWorktimeUuid(worktimeUuid, searchVo.getStartNum(), searchVo.getPageSize());
            for (Long slaId : slaIdList) {
                recalculateSla(slaId, worktimeUuid);
            }
        }
    }

    /**
     * @param slaId 时效id
     */
    private void recalculateSla(Long slaId, String worktimeUuid) {
        try {
            ProcessTaskSlaVo processTaskSlaVo = processTaskSlaMapper.getProcessTaskSlaLockById(slaId);
            if (processTaskSlaVo == null) {
                return;
            }
            JSONObject slaConfigObj = processTaskSlaVo.getConfigObj();
            if (MapUtils.isEmpty(slaConfigObj)) {
                return;
            }
            ProcessTaskSlaTimeVo oldSlaTimeVo = processTaskSlaMapper.getProcessTaskSlaTimeBySlaId(slaId);
            System.out.println("oldSlaTimeVo=" + oldSlaTimeVo);
            if (oldSlaTimeVo == null) {
                return;
            }
            Long timeSum = oldSlaTimeVo.getTimeSum();

            // 修正最终超时日期
            String calculateHandler = slaConfigObj.getString("calculateHandler");
            if (StringUtils.isBlank(calculateHandler)) {
                calculateHandler = DefaultSlaCalculateHandler.class.getSimpleName();
            }
            ISlaCalculateHandler handler = SlaCalculateHandlerFactory.getHandler(calculateHandler);
            if (handler == null) {
                throw new SlaCalculateHandlerNotFoundException(calculateHandler);
            }
            long currentTimeMillis = System.currentTimeMillis();
            ProcessTaskSlaTimeCostVo timeCostVo = handler.calculateTimeCost(slaId, currentTimeMillis, worktimeUuid);
            ProcessTaskSlaTimeVo slaTimeVo = slaService.createSlaTime(slaId, timeSum, currentTimeMillis, timeCostVo);
            slaService.recalculateExpireTime(slaTimeVo, currentTimeMillis, worktimeUuid);
            slaTimeVo.setStatus(oldSlaTimeVo.getStatus());

            System.out.println("newSlaTimeVo=" + slaTimeVo);
            processTaskSlaMapper.updateProcessTaskSlaTime(slaTimeVo);
//        processTaskSlaMapper.deleteProcessTaskStepSlaTimeBySlaId(slaId);
            if (!Objects.equals(oldSlaTimeVo.getExpireTimeLong(), slaTimeVo.getExpireTimeLong())
                    && SlaStatus.DOING.toString().toLowerCase().equals(slaTimeVo.getStatus())) {
                slaService.loadJobNotifyAndTransfer(slaId, slaConfigObj);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
