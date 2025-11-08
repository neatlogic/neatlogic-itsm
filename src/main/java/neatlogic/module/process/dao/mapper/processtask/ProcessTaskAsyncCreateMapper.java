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

import neatlogic.framework.process.crossover.IProcessTaskAsyncCreateCrossoverMapper;
import neatlogic.framework.process.dto.ProcessTaskAsyncCreateVo;

import java.util.List;

public interface ProcessTaskAsyncCreateMapper extends IProcessTaskAsyncCreateCrossoverMapper {

    ProcessTaskAsyncCreateVo getProcessTaskAsyncCreateById(Long id);

    List<ProcessTaskAsyncCreateVo> getProcessTaskAsyncCreateList(ProcessTaskAsyncCreateVo processTaskAsyncCreateVo);

    int getProcessTaskAsyncCreateCount(ProcessTaskAsyncCreateVo processTaskAsyncCreateVo);

    List<ProcessTaskAsyncCreateVo> getProcessTaskAsyncCreateFailedList(ProcessTaskAsyncCreateVo processTaskAsyncCreateVo);

    int getProcessTaskAsyncCreateFailedCount(ProcessTaskAsyncCreateVo processTaskAsyncCreateVo);

    int insertProcessTaskAsyncCreate(ProcessTaskAsyncCreateVo processTaskAsyncCreateVo);

    int updateProcessTaskAsyncCreate(ProcessTaskAsyncCreateVo processTaskAsyncCreateVo);

    int updateProcessTaskAsyncCreateStatusToDoingById(Long id);

    int updateProcessTaskAsyncCreateForManualIntervention(ProcessTaskAsyncCreateVo processTaskAsyncCreateVo);

    int deleteProcessTaskAsyncCreateById(Long id);

    int deleteProcessTaskAsyncCreateByIdList(List<Long> idList);
}
