/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.api.task;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.ProcessTaskStepTaskMapper;
import neatlogic.framework.process.dao.mapper.task.TaskMapper;
import neatlogic.framework.process.dto.TaskConfigVo;
import neatlogic.framework.process.exception.processtask.task.TaskConfigIsInvokedException;
import neatlogic.framework.process.exception.processtask.task.TaskConfigNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
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
		return "删除子任务";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "任务id"),
	})
	@Output({

	})
	@Description(desc = "删除子任务接口")
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
