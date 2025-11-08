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
