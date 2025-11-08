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

import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.process.crossover.IScoreTemplateCrossoverMapper;
import neatlogic.framework.process.dto.score.ProcessScoreTemplateVo;
import neatlogic.framework.process.dto.score.ScoreTemplateDimensionVo;
import neatlogic.framework.process.dto.score.ScoreTemplateVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScoreTemplateMapper extends IScoreTemplateCrossoverMapper {

    List<ScoreTemplateVo> searchScoreTemplate(ScoreTemplateVo scoreTemplateVo);

    List<ValueTextVo> searchScoreTemplateForSelect(ScoreTemplateVo scoreTemplateVo);

    int searchScoreTemplateCount(ScoreTemplateVo scoreTemplateVo);

    ScoreTemplateVo getScoreTemplateById(@Param("id") Long id);

    ScoreTemplateVo getScoreTemplateByName(String name);

    int checkScoreTemplateNameIsRepeat(ScoreTemplateVo scoreTemplateVo);

    ScoreTemplateVo checkScoreTemplateExistsById(@Param("id") Long id);

    List<ValueTextVo> getRefProcessList(ScoreTemplateVo scoreTemplateVo);

    int getRefProcessCount(@Param("scoreTemplateId") Long scoreTemplateId);

    List<ScoreTemplateVo> getProcessCountByIdList(List<Long> scoreTemplateIdList);

    ProcessScoreTemplateVo getProcessScoreTemplateByProcessUuid(String processUuid);

    List<ScoreTemplateDimensionVo> getScoreTemplateDimensionListByScoreTemplateId(Long scoreTemplateId);

    void updateScoreTemplate(ScoreTemplateVo scoreTemplateVo);

    void updateScoreTemplateStatus(ScoreTemplateVo scoreTemplateVo);

    void insertScoreTemplate(ScoreTemplateVo scoreTemplateVo);

    void insertScoreTemplateDimension(ScoreTemplateDimensionVo scoreTemplateDimensionVo);

    void insertProcessScoreTemplate(ProcessScoreTemplateVo processScoreTemplateVo);

    void deleteScoreTemplate(@Param("scoreTemplateId") Long scoreTemplateId);

    void deleteScoreTemplateDimension(@Param("scoreTemplateId") Long scoreTemplateId);

    void deleteProcessScoreTemplateByProcessUuid(String processUuid);

}
