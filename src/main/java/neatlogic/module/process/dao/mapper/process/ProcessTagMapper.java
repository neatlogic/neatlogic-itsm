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
import neatlogic.framework.process.dto.ProcessTagVo;

import java.util.List;

/**
 * @author linbq
 * @since 2021/10/15 14:35
 **/
public interface ProcessTagMapper {

    List<ValueTextVo> getProcessTagForSelect(ProcessTagVo processTagVo);

    List<ProcessTagVo> getProcessTagByNameList(List<String> tagNameList);

    List<ProcessTagVo> getProcessTagByIdList(List<Long> tagIdList);

    Long getProcessTagIdByName(String Name);

    int getProcessTagCount(ProcessTagVo processTagVo);

    int insertProcessTag(ProcessTagVo processTagVo);
}
