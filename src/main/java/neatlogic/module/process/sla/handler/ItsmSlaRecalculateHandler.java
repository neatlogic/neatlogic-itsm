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

package neatlogic.module.process.sla.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.process.constvalue.SlaStatus;
import neatlogic.framework.process.dao.mapper.ProcessTaskSlaMapper;
import neatlogic.framework.process.dto.ProcessTaskSlaTimeCostVo;
import neatlogic.framework.process.dto.ProcessTaskSlaTimeVo;
import neatlogic.framework.process.dto.ProcessTaskSlaVo;
import neatlogic.framework.process.exception.sla.SlaCalculateHandlerNotFoundException;
import neatlogic.framework.process.sla.core.ISlaCalculateHandler;
import neatlogic.framework.process.sla.core.SlaCalculateHandlerFactory;
import neatlogic.framework.sla.core.ISlaRecalculateHandler;
import neatlogic.module.process.sla.service.ProcessTaskSlaService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Component
public class ItsmSlaRecalculateHandler implements ISlaRecalculateHandler {

    private Logger logger = LoggerFactory.getLogger(ItsmSlaRecalculateHandler.class);
    @Resource
    private ProcessTaskSlaMapper processTaskSlaMapper;

    @Resource
    private ProcessTaskSlaService processTaskSlaService;

    @Override
    @Transactional
    public void execute(String worktimeUuid) {
        // 查询出所有与worktimeUuid服务窗口关联的未完成的slaId，且相关工单的状态为进行中，且未删除
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
            ProcessTaskSlaTimeVo slaTimeVo = processTaskSlaService.createSlaTime(slaId, timeSum, currentTimeMillis, timeCostVo);
            processTaskSlaService.recalculateExpireTime(slaTimeVo, currentTimeMillis, worktimeUuid);
            slaTimeVo.setStatus(oldSlaTimeVo.getStatus());

            processTaskSlaMapper.updateProcessTaskSlaTime(slaTimeVo);
            if (!Objects.equals(oldSlaTimeVo.getExpireTimeLong(), slaTimeVo.getExpireTimeLong())
                    && SlaStatus.DOING.toString().toLowerCase().equals(slaTimeVo.getStatus())) {
                processTaskSlaService.loadJobNotifyAndTransfer(slaId, slaConfigObj);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
