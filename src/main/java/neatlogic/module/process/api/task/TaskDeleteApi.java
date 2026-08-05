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

package neatlogic.module.process.api.task;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.TaskConfigVo;
import neatlogic.framework.process.exception.processtask.task.TaskConfigIsInvokedException;
import neatlogic.framework.process.exception.processtask.task.TaskConfigNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepTaskMapper;
import neatlogic.module.process.dao.mapper.task.TaskMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service

@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class TaskDeleteApi extends PrivateApiComponentBase{
	@Resource
	TaskMapper taskMapper;
	@Resource
	ProcessTaskStepTaskMapper processTaskStepTaskMapper;
	@Override
	public String getToken() {
		return "task/delete";
	}

	@Override
	public String getName() {
		return "nmpat.taskdeleteapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "nmpat.taskdeleteapi.input.param.desc.id"),
	})
	@Output({

	})
	@Description(desc = "nmpat.taskdeleteapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long taskId = jsonObj.getLong("id");
		TaskConfigVo taskConfigTmp = taskMapper.getTaskConfigById(taskId);
		if (taskConfigTmp == null) {
			throw new TaskConfigNotFoundException(taskId.toString());
		}
		//判断依赖能否删除
		if(processTaskStepTaskMapper.getInvokedCountByTaskConfigId(taskId)>0){
			throw new TaskConfigIsInvokedException(taskConfigTmp.getName());
		}
		taskMapper.deleteTaskConfigById(taskId);
		return null;
	}

}
