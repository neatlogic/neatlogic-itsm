package codedriver.module.process.api.processtask;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.notify.core.TaskNotifyTriggerType;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
@Service
@OperationType(type = OperationTypeEnum.CREATE)
public class ProcessTaskUrgeApi extends PrivateApiComponentBase {
    
    @Autowired
    private ProcessTaskService processTaskService;
    @Autowired
    private UserMapper userMapper;
	
	@Override
	public String getToken() {
		return "processtask/urge";
	}

	@Override
	public String getName() {
		return "催办工单";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id")
	})
	@Description(desc = "催办工单")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
		IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler();
		List<ProcessTaskStepVo> processTaskStepList = processTaskService.getUrgeableStepList(processTaskVo, UserContext.get().getUserUuid(true));
		/** 如果当前用户接受了其他用户的授权，查出其他用户拥有的权限，叠加当前用户权限里 **/
        String userUuid = userMapper.getUserUuidByAgentUuidAndFunc(UserContext.get().getUserUuid(true), "processtask");
        if(StringUtils.isNotBlank(userUuid)) {
            processTaskStepList.addAll(processTaskService.getUrgeableStepList(processTaskVo, userUuid));
        }
		if(CollectionUtils.isNotEmpty(processTaskStepList)) {
			for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
				/** 触发通知 **/
				handler.notify(processTaskStepVo, TaskNotifyTriggerType.URGE);
			}
		}else {
			try {
	            throw new ProcessTaskNoPermissionException(ProcessTaskOperationType.URGE.getText());
	        }catch(ProcessTaskNoPermissionException e) {
	            throw new PermissionDeniedException();
	        }
		}
		/*生成催办活动*/
		ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
		processTaskStepVo.setProcessTaskId(processTaskId);
		handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.URGE);
		return null;
	}

}
