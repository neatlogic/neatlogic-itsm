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

package neatlogic.module.process.dao.mapper.process;

import neatlogic.framework.process.dto.ProcessCommentTemplateAuthVo;
import neatlogic.framework.process.dto.ProcessCommentTemplateSearchVo;
import neatlogic.framework.process.dto.ProcessCommentTemplateUseCountVo;
import neatlogic.framework.process.dto.ProcessCommentTemplateVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProcessCommentTemplateMapper {

    ProcessCommentTemplateVo getTemplateById(Long id);

    ProcessCommentTemplateVo getTemplateByName(String name);

    ProcessCommentTemplateVo getTemplateByStepUuidAndAuth(@Param("stepUuid") String uuid,@Param("authList") List<String> authList);

    ProcessCommentTemplateUseCountVo getTemplateUseCount(@Param("templateId") Long id, @Param("userUuid") String uuid);

    int checkTemplateNameIsRepeat(ProcessCommentTemplateVo vo);

    int searchCommentTemplateCount(ProcessCommentTemplateSearchVo searchVo);

    List<ProcessCommentTemplateVo> searchCommentTemplateList(ProcessCommentTemplateSearchVo searchVo);

    List<ProcessCommentTemplateAuthVo> getProcessCommentTemplateAuthListByCommentTemplateId(Long id);

    int updateTemplate(ProcessCommentTemplateVo vo);

    int updateTemplateUseCount(@Param("templateId") Long id, @Param("userUuid") String uuid);

    int insertTemplate(ProcessCommentTemplateVo vo);

    int batchInsertAuthority(List<ProcessCommentTemplateAuthVo> list);

    int insertTemplateUseCount(@Param("templateId") Long id, @Param("userUuid") String uuid);

    int deleteTemplate(Long id);

    int deleteTemplateAuthority(Long id);

    int deleteTemplateUsecount(Long id);
}
