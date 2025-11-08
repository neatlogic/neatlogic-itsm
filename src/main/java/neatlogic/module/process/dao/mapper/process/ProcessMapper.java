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

package neatlogic.module.process.dao.mapper.process;

import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.process.crossover.IProcessCrossoverMapper;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.dto.score.ProcessScoreTemplateVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProcessMapper extends IProcessCrossoverMapper {
    int checkProcessIsExists(String processUuid);

    List<String> getProcessStepUuidBySlaUuid(String slaUuid);

    ProcessFormVo getProcessFormByProcessUuid(String processUuid);

    List<ProcessStepRelVo> getProcessStepRelByProcessUuid(String processUuid);

    ProcessStepRelVo getProcessStepRelByUuid(String uuid);

    List<ProcessSlaVo> getProcessSlaByProcessUuid(String processUuid);

    List<String> getSlaUuidListByProcessUuid(String processUuid);

    List<ProcessStepVo> getProcessStepDetailByProcessUuid(String processUuid);

    List<ProcessStepVo> getProcessStepDetailByProcessUuidAndType(@Param("processUuid") String processUuid, @Param("type") String type);

    List<String> getProcessStepUuidListByProcessUuid(String processUuid);

    List<String> getAllProcessUuidList();

    ProcessVo getProcessByUuid(String processUuid);

    ProcessVo getProcessByName(String processName);

    ProcessVo getProcessBaseInfoByUuid(String processUuid);

    List<ProcessStepVo> searchProcessStep(ProcessStepVo processStepVo);

    List<ProcessStepVo> getProcessStepListByUuidList(List<String> uuidList);

    List<ProcessTypeVo> getAllProcessType();

    int checkProcessNameIsRepeat(ProcessVo processVo);

    int searchProcessCount(ProcessVo processVo);

    List<ProcessVo> searchProcessList(ProcessVo processVo);

    List<ValueTextVo> searchProcessListForSelect(ProcessVo processVo);

    int getProcessReferenceCount(String processUuid);

    List<String> getProcessReferenceUuidList(String processUuid);

    int checkProcessDraftIsExists(ProcessDraftVo processDraftVo);

    ProcessDraftVo getProcessDraftByUuid(String uuid);

    List<ProcessDraftVo> getProcessDraftList(ProcessDraftVo processDraftVo);

    String getEarliestProcessDraft(ProcessDraftVo processDraftVo);

    List<ProcessStepWorkerPolicyVo> getProcessStepWorkerPolicyListByProcessUuid(String processUuid);

    ProcessStepVo getProcessStepByUuid(String processStepUuid);

    ProcessScoreTemplateVo getProcessScoreTemplateByProcessUuid(String processUuid);

    ProcessStepVo getStartProcessStepByProcessUuid(String processUuid);

//	int getFormReferenceCount(String formUuid);

//	List<ProcessVo> getFormReferenceList(ProcessFormVo processFormVo);

    ProcessSlaVo getProcessSlaByUuid(String caller);

    Long getNotifyPolicyIdByProcessStepUuid(String processStepUuid);

    List<ProcessVo> getProcessListByUuidList(List<String> uuidList);

    List<Long> getProcessStepTagIdListByProcessStepUuid(String processStepUuid);

    List<ProcessStepTagVo> getProcessStepTagListByProcessUuid(String processUuid);

    int insertProcess(ProcessVo processVo);

    int insertProcessStep(ProcessStepVo processStepVo);

    int insertProcessStepList(List<ProcessStepVo> processStepVo);

//    int insertProcessStepFormAttribute(ProcessStepFormAttributeVo processStepFormAttributeVo);

    int insertProcessStepRel(ProcessStepRelVo processStepRelVo);

    int insertProcessStepRelList(List<ProcessStepRelVo> processStepRelList);

//	int insertProcessStepTeam(ProcessStepTeamVo processStepTeamVo);

    int insertProcessStepWorkerPolicy(ProcessStepWorkerPolicyVo processStepWorkerPolicyVo);

    int insertProcessForm(ProcessFormVo processFormVo);

    int insertProcessStepSla(@Param("stepUuid") String stepUuid, @Param("slaUuid") String slaUuid);

    int insertProcessSla(ProcessSlaVo processSlaVo);

    int insertProcessDraft(ProcessDraftVo processDraftVo);

    int insertProcessStepTag(ProcessStepTagVo processStepTagVo);

    int insertProcessStepCommentTemplate(@Param("processStepUuid") String processStepUuid, @Param("commentTemplateId") Long commentTemplateId);

    int insertProcessStepTask(ProcessStepTaskConfigVo tmpVo);

    int updateProcess(ProcessVo processVo);

    int updateProcessNameByUuid(ProcessVo processVo);

    int deleteProcessStepByProcessUuid(String processUuid);

    int deleteProcessStepRelByProcessUuid(String processUuid);

    //	int deleteProcessStepTeamByProcessUuid(String processUuid);
    int deleteProcessStepWorkerPolicyByProcessUuid(String processUuid);

    int deleteProcessStepWorkerPolicyByProcessStepUuid(String processUuid);

//    int deleteProcessStepFormAttributeByProcessUuid(String processUuid);

    int deleteProcessByUuid(String uuid);

    int deleteProcessFormByProcessUuid(String processUuid);

    int deleteProcessDraft(ProcessDraftVo processDraftVo);

    int deleteProcessDraftByUuid(String uuid);

    int deleteProcessSlaByProcessUuid(String uuid);

    int deleteProcessStepCommentTemplate(String stepUuid);

//    int deleteProcessStepTagByProcessUuid(String processUuid);

    int deleteProcessStepTagByProcessStepUuid(String processStepUuid);

    int deleteProcessStepTaskByProcessStepUuid(String processStepUuid);

}
