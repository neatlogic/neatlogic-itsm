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

import neatlogic.framework.process.crossover.IProcessTaskStepDataCrossoverMapper;
import neatlogic.framework.process.dto.ProcessTaskStepDataVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProcessTaskStepDataMapper extends IProcessTaskStepDataCrossoverMapper {
    List<ProcessTaskStepDataVo> getProcessTaskStepDataByProcessTaskIdAndStepId(@Param("processTaskId") Long processTaskId, @Param("processTaskStepId") Long stepId);

    ProcessTaskStepDataVo getProcessTaskStepData(ProcessTaskStepDataVo processTaskStepDataVo);

    ProcessTaskStepDataVo getProcessTaskStepDataById(Long id);

    Long getProcessTaskStepDataId(ProcessTaskStepDataVo processTaskStepDataVo);

    int checkProcessTaskStepDataIdIsExists(Long id);

	List<ProcessTaskStepDataVo> searchProcessTaskStepData(ProcessTaskStepDataVo processTaskStepDataVo);

    int replaceProcessTaskStepData(ProcessTaskStepDataVo processTaskStepDataVo);

    int deleteProcessTaskStepData(ProcessTaskStepDataVo processTaskStepDataVo);

    int deleteProcessTaskStepDataById(Long id);
}
