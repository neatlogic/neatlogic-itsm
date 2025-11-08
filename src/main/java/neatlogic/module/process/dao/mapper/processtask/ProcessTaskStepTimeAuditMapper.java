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

import neatlogic.framework.process.crossover.IProcessTaskStepTimeAuditCrossoverMapper;
import neatlogic.framework.process.dto.ProcessTaskStepCostVo;
import neatlogic.framework.process.dto.ProcessTaskStepCostWorkerVo;
import neatlogic.framework.process.dto.ProcessTaskStepTimeAuditVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProcessTaskStepTimeAuditMapper extends IProcessTaskStepTimeAuditCrossoverMapper {
	List<ProcessTaskStepTimeAuditVo> getProcessTaskStepTimeAuditBySlaId(Long slaId);

	ProcessTaskStepTimeAuditVo getLastProcessTaskStepTimeAuditByStepId(Long processTaskStepId);

	ProcessTaskStepCostVo getLastProcessTaskStepCostByProcessTaskStepId(Long processTaskStepId);

	int getProcessTaskStepCostCountByProcessTaskStepIdAndStartOperateList(@Param("processTaskStepId") Long processTaskStepId, @Param("startOperateList") List<String> startOperateList);

	int updateProcessTaskStepTimeAudit(ProcessTaskStepTimeAuditVo processTaskStepTimeAuditVo);

	int insertProcessTaskStepTimeAudit(ProcessTaskStepTimeAuditVo processTaskStepTimeAuditVo);

	int insertProcessTaskStepCost(ProcessTaskStepCostVo processTaskStepCostVo);

	int insertProcessTaskStepCostWorker(ProcessTaskStepCostWorkerVo processTaskStepCostWorkerVo);

	int updateProcessTaskStepCost(ProcessTaskStepCostVo processTaskStepCostVo);
}
