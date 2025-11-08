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

package neatlogic.module.process.dao.mapper.score;

import neatlogic.framework.process.crossover.IProcessTaskScoreCrossoverMapper;
import neatlogic.framework.process.dto.score.ProcessTaskAutoScoreVo;
import neatlogic.framework.process.dto.score.ProcessTaskScoreVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProcessTaskScoreMapper extends IProcessTaskScoreCrossoverMapper {

    List<ProcessTaskScoreVo> getProcessTaskScoreByProcesstaskId(Long processtaskId);

    List<ProcessTaskScoreVo> getProcessTaskScoreWithContentHashByProcessTaskId(Long processtaskId);

    List<Long> getAllProcessTaskAutoScoreProcessTaskIdList();
    
    String getProcessTaskAutoScoreConfigByProcessTaskId(Long processTaskId);

    String getProcessTaskScoreContentHashByProcessTaskId(Long processTaskId);

    void insertProcessTaskScore(ProcessTaskScoreVo vo);

    void insertProcessTaskScoreContent(ProcessTaskScoreVo vo);
    
    int insertProcessTaskAutoScore(ProcessTaskAutoScoreVo processTaskAutoScoreVo);

    int updateProcessTaskAutoScoreByProcessTaskId(ProcessTaskAutoScoreVo processTaskAutoScoreVo);

    int updateProcessTaskAutoScoreErrorByProcessTaskId(@Param("processTaskId") Long processTaskId, @Param("error") String error);
    
    int deleteProcessTaskByProcessTaskId(Long processTaskId);

    int deleteProcessTaskAutoScoreByProcessTaskId(Long processTaskId);
}
