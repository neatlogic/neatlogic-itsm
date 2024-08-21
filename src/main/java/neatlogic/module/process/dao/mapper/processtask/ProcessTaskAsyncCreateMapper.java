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
