/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.process.dao.mapper.processtask;

import neatlogic.framework.process.crossover.IProcessTaskSlaCrossoverMapper;
import neatlogic.framework.process.dto.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author linbq
 * @since 2021/11/28 15:53
 **/
public interface ProcessTaskSlaMapper extends IProcessTaskSlaCrossoverMapper {

    List<ProcessTaskSlaTransferVo> getAllProcessTaskSlaTransfer();

    ProcessTaskSlaTransferVo getProcessTaskSlaTransferById(Long id);

    List<ProcessTaskSlaTransferVo> getProcessTaskSlaTransferBySlaId(Long slaId);

    List<ProcessTaskSlaNotifyVo> getAllProcessTaskSlaNotify();

    ProcessTaskSlaNotifyVo getProcessTaskSlaNotifyById(Long id);

    List<ProcessTaskSlaNotifyVo> getProcessTaskSlaNotifyBySlaId(Long slaId);

    ProcessTaskSlaVo getProcessTaskSlaById(Long id);

    List<Long> getSlaIdListByProcessTaskId(Long processTaskId);

    String getProcessTaskSlaConfigById(Long id);

    ProcessTaskSlaVo getProcessTaskSlaLockById(Long id);

    List<ProcessTaskSlaTimeVo> getProcessTaskSlaTimeByProcessTaskStepIdList(List<Long> processTaskStepIdList);

    List<ProcessTaskSlaTimeVo> getProcessTaskSlaTimeListBySlaIdList(List<Long> slaIdList);

    ProcessTaskSlaTimeVo getProcessTaskSlaTimeBySlaId(Long slaId);

    List<Long> getSlaIdListByProcessTaskStepId(Long processTaskStepId);

    List<Long> getProcessTaskStepIdListBySlaId(Long slaId);

    int getDoingOrPauseSlaIdCountByWorktimeUuid(String worktimeUuid);

    List<Long> getDoingOrPauseSlaIdListByWorktimeUuid(@Param("worktimeUuid") String worktimeUuid, @Param("startNum") int startNum, @Param("pageSize") int pageSize);

    int insertProcessTaskSlaNotify(ProcessTaskSlaNotifyVo processTaskSlaNotifyVo);

    int insertProcessTaskSlaTransfer(ProcessTaskSlaTransferVo processTaskSlaTransferVo);

    int insertProcessTaskSla(ProcessTaskSlaVo processTaskSlaVo);

    int insertProcessTaskSlaTime(ProcessTaskSlaTimeVo processTaskSlaTimeVo);

    int insertProcessTaskStepSla(@Param("processTaskStepId") Long processTaskStepId, @Param("slaId") Long slaId);

    int insertProcessTaskStepSlaTime(ProcessTaskStepSlaTimeVo processTaskStepSlaTimeVo);

    int updateProcessTaskSlaTransfer(ProcessTaskSlaTransferVo processTaskSlaTransferVo);

    int updateProcessTaskSlaNotify(ProcessTaskSlaNotifyVo processTaskNotifyVo);

    int updateProcessTaskSlaTime(ProcessTaskSlaTimeVo processTaskSlaTimeVo);

    int updateProcessTaskSlaIsActiveBySlaId(ProcessTaskSlaVo processTaskSlaVo);

    int deleteProcessTaskSlaNotifyById(Long id);

    int deleteProcessTaskSlaTransferById(Long id);

    int deleteProcessTaskSlaTransferBySlaId(Long slaId);

    int deleteProcessTaskSlaNotifyBySlaId(Long slaId);

    int deleteProcessTaskSlaTimeBySlaId(Long slaId);

    int deleteProcessTaskStepSlaTimeBySlaId(Long slaId);
}
