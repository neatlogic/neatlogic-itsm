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
