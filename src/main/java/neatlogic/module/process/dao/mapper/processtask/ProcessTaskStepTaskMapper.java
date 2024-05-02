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

import neatlogic.framework.process.crossover.IProcessTaskStepTaskCrossoverMapper;
import neatlogic.framework.process.dto.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lvzk
 * @since 2021/9/1 14:18
 **/
public interface ProcessTaskStepTaskMapper extends IProcessTaskStepTaskCrossoverMapper {

    int getInvokedCountByTaskConfigId(Long taskId);

    ProcessTaskStepTaskVo getStepTaskDetailById(Long processTaskStepTaskId);

    ProcessTaskStepTaskVo getStepTaskById(Long processTaskStepTaskId);

    ProcessTaskStepTaskVo getStepTaskLockById(Long processTaskStepTaskId);

    ProcessTaskStepTaskUserVo getStepTaskUserByTaskIdAndTaskUserIdAndUserUuid(@Param("processTaskStepTaskId") Long processtaskStepTaskId, @Param("processTaskStepTaskUserId") Long processTaskStepTaskUserId, @Param("userUuid") String userUuid);

    ProcessTaskStepTaskUserVo getStepTaskUserById(Long id);

    List<ProcessTaskStepTaskVo> getStepTaskByProcessTaskStepId(Long processTaskStepId);

    List<ProcessTaskStepTaskVo> getStepTaskListByProcessTaskStepId(Long processTaskStepId);

    List<ProcessTaskStepTaskUserVo> getStepTaskUserByStepTaskIdList(@Param("stepTaskIdList") List<Long> collect);

    List<ProcessTaskStepTaskUserVo> getStepTaskUserByStepTaskIdListAndUserUuid(@Param("stepTaskIdList") List<Long> collect, @Param("userUuid") String userUuid);

    List<ProcessTaskStepTaskVo> getStepTaskWithUserByProcessTaskStepId(Long processTaskStepId);

    List<ProcessTaskStepTaskUserContentVo> getStepTaskUserContentByStepTaskUserIdList(@Param("stepTaskUserIdList") List<Long> collect);

    ProcessTaskStepTaskUserContentVo getStepTaskUserContentByIdAndUserUuid(@Param("userContentId") Long processTaskStepTaskUserContentId, @Param("userUuid") String userUuid);

    ProcessTaskStepTaskUserContentVo getStepTaskUserContentById(Long id);

    List<ProcessTaskStepTaskUserVo> getStepTaskUserListByTaskIdAndStatus(@Param("processtaskStepTaskId") Long processtaskStepTaskId, @Param("status") String status);

    List<ProcessTaskStepTaskUserVo> getStepTaskUserListByStepTaskId(Long processtaskStepTaskId);

    List<ProcessTaskStepTaskUserVo> getStepTaskUserListByProcessTaskStepId(Long processTaskStepId);

    List<ProcessTaskStepTaskUserVo> getStepTaskUserByTaskIdAndUserUuid(@Param("processTaskStepTaskId") Long processTaskStepTaskId, @Param("userUuid") String userUuid);

    ProcessTaskStepTaskUserContentVo getStepTaskUserContentByStepTaskUserId(Long processTaskStepTaskUserId);

    ProcessTaskStepTaskUserAgentVo getProcessTaskStepTaskUserAgentByStepTaskUserId(Long stepTaskUserId);

    List<ProcessTaskStepTaskUserAgentVo> getProcessTaskStepTaskUserAgentListByStepTaskUserIdList(List<Long> stepTaskUserIdList);

    List<ProcessTaskStepTaskUserAgentVo> getProcessTaskStepTaskUserAgentListByStepTaskIdList(List<Long> stepTaskIdList);

    List<ProcessTaskStepTaskUserFileVo> getStepTaskUserFileListByStepTaskUserIdList(List<Long> stepTaskUserIdList);

    int insertTask(ProcessTaskStepTaskVo processTaskStepTaskVo);

    int insertIgnoreTaskUser(ProcessTaskStepTaskUserVo processTaskStepTaskUserVo);

    int insertTaskUser(ProcessTaskStepTaskUserVo processTaskStepTaskUserVo);

    int insertTaskUserContent(ProcessTaskStepTaskUserContentVo processTaskStepTaskUserContentVo);

    int insertProcessTaskStepTaskUserAgent(ProcessTaskStepTaskUserAgentVo processTaskStepTaskUserAgentVo);

    int insertProcessTaskStepTaskUserFile(ProcessTaskStepTaskUserFileVo processTaskStepTaskUserFileVo);

    int updateTask(ProcessTaskStepTaskVo processTaskStepTaskVo);

    int updateTaskUserByTaskIdAndUserUuid(@Param("status") String status, @Param("processTaskStepTaskId") Long processtaskStepTaskId, @Param("userUuid") String userUuid);

    int updateTaskUserById(ProcessTaskStepTaskUserVo processTaskStepTaskUserVo);

    int updateDeleteTaskUserByUserListAndId(@Param("userList") List<String> userList, @Param("processTaskStepTaskId") Long processTaskStepTaskId, @Param("isDelete") Integer isDelete);

    int updateTaskUserContent(@Param("processTaskStepTaskUserContentId") Long processTaskStepTaskUserContentId, @Param("contentHash") String contentHash, @Param("userUuid") String userUuid);

    int updateTaskUserContentById(ProcessTaskStepTaskUserContentVo userContentVo);

    int updateTaskUserIsDeleteByIdList(@Param("idList") List<Long> idList, @Param("isDelete") Integer isDelete);

    int deleteTaskById(Long processTaskStepTaskId);

    int deleteTaskUserByTaskId(Long processTaskStepTaskId);

    int deleteTaskUserContentByTaskId(Long processTaskStepTaskId);

    int deleteProcessTaskStepTaskUserAgentByStepTaskUserId(Long stepTaskUserId);

    int deleteProcessTaskStepTaskUserAgentByStepTaskUserIdList(List<Long> stepTaskUserIdList);

    int deleteProcessTaskStepTaskUserAgentByStepTaskId(Long processTaskStepTaskId);

    int deleteProcessTaskStepTaskUserFile(ProcessTaskStepTaskUserFileVo processTaskStepTaskUserFileVo);
}
