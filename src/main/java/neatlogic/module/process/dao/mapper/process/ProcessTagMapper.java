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
