/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.dao.mapper.task;

import com.alibaba.fastjson.JSONArray;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.process.crossover.ITaskCrossoverMapper;
import neatlogic.framework.process.dto.TaskConfigVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author lvzk
 * @since 2021/9/1 14:18
 **/
public interface TaskMapper extends ITaskCrossoverMapper {

    int searchTaskConfigCount(TaskConfigVo taskConfigVo);

    List<TaskConfigVo> searchTaskConfig(TaskConfigVo taskConfigVo);

    int checkTaskConfigNameIsRepeat(TaskConfigVo taskConfigVo);

    TaskConfigVo getTaskConfigById(Long taskId);

    TaskConfigVo getTaskConfigByName(String name);

    List<TaskConfigVo> getTaskConfigByIdList(JSONArray stepTaskIdList);

    List<Map<String,Long>> getTaskConfigReferenceCountMap(List<Long> idList);

    List<ValueTextVo> getTaskConfigReferenceProcessList(@Param("taskConfigId") Long taskConfigId,@Param("basePageVo") BasePageVo basePageVo);

    int getTaskConfigReferenceProcessCount(Long taskConfigId);

    int updateTaskConfig(TaskConfigVo taskConfigVo);

    int insertTaskConfig(TaskConfigVo taskConfigVo);

    int deleteTaskConfigById(Long taskId);

}
