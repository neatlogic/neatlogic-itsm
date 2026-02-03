/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.dao.mapper.processtask;

import neatlogic.framework.process.crossover.IProcessTaskSlaCrossoverMapper;
import neatlogic.framework.process.dto.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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

    List<ProcessTaskSlaVo> getProcessTaskSlaListByProcessTaskId(Long processTaskId);

    ProcessTaskSlaVo getProcessTaskSlaById(Long id);

    List<Long> getSlaIdListByProcessTaskId(Long processTaskId);

    String getProcessTaskSlaConfigById(Long id);

    ProcessTaskSlaVo getProcessTaskSlaLockById(Long id);

    List<ProcessTaskSlaTimeVo> getProcessTaskSlaTimeByProcessTaskStepIdList(List<Long> processTaskStepIdList);

    List<ProcessTaskSlaTimeVo> getProcessTaskSlaTimeListBySlaIdList(List<Long> slaIdList);

    ProcessTaskSlaTimeVo getProcessTaskSlaTimeBySlaId(Long slaId);

    List<Long> getSlaIdListByProcessTaskStepId(Long processTaskStepId);

    List<Map<String, Long>> getProcessTaskStepSlaListByProcessTaskStepIdList(List<Long> processTaskStepIdList);

    List<Long> getProcessTaskStepIdListBySlaId(Long slaId);

    int getDoingOrPauseSlaIdCountByWorktimeUuid(String worktimeUuid);

    List<Long> getDoingOrPauseSlaIdListByWorktimeUuid(@Param("worktimeUuid") String worktimeUuid, @Param("startNum") int startNum, @Param("pageSize") int pageSize);

    List<ProcessTaskStepSlaDelayVo> getProcessTaskStepSlaDelayListBySlaId(Long slaId);

    List<ProcessTaskStepSlaDelayVo> getProcessTaskStepSlaDelayListBySlaIdList(List<Long> slaIdList);

    int insertProcessTaskSlaNotify(ProcessTaskSlaNotifyVo processTaskSlaNotifyVo);

    int insertProcessTaskSlaTransfer(ProcessTaskSlaTransferVo processTaskSlaTransferVo);

    int insertProcessTaskSla(ProcessTaskSlaVo processTaskSlaVo);

    int insertProcessTaskSlaTime(ProcessTaskSlaTimeVo processTaskSlaTimeVo);

    int insertProcessTaskStepSla(@Param("processTaskStepId") Long processTaskStepId, @Param("slaId") Long slaId);

    int insertProcessTaskStepSlaTime(ProcessTaskStepSlaTimeVo processTaskStepSlaTimeVo);

    int insertProcessTaskStepSlaDelay(ProcessTaskStepSlaDelayVo processTaskStepSlaDelayVo);

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

    int deleteProcessTaskStepSlaDelayByTargetProcessTaskStepId(Long targetProcessTaskStepId);

    int deleteProcessTaskSlaById(Long id);

    int deleteProcessTaskStepSlaBySlaId(Long slaId);

    void deleteProcessTaskStepSla(@Param("processTaskStepId") Long processTaskStepId, @Param("slaId") Long slaId);
}
