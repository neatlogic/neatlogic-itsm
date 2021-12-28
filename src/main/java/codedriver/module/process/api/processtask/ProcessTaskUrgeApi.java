package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import codedriver.framework.process.service.ProcessTaskAgentService;
import codedriver.framework.process.stephandler.core.IProcessStepHandlerUtil;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskUrgeApi extends PrivateApiComponentBase {
    
    @Resource
    private ProcessTaskService processTaskService;
    @Resource
    private IProcessStepHandlerUtil IProcessStepHandlerUtil;
	@Resource
	private ProcessTaskAgentService processTaskAgentService;

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
		List<ProcessTaskStepVo> processTaskStepList = processTaskService.getUrgeableStepList(processTaskVo, UserContext.get().getUserUuid(true));
		/** 如果当前用户接受了其他用户的授权，查出其他用户拥有的权限，叠加当前用户权限里 **/
		List<String> fromUserUUidList = processTaskAgentService.getFromUserUuidListByToUserUuidAndChannelUuid(UserContext.get().getUserUuid(true), processTaskVo.getChannelUuid());
		for (String userUuid : fromUserUUidList) {
			processTaskStepList.addAll(processTaskService.getUrgeableStepList(processTaskVo, userUuid));
		}
//        String userUuid = userMapper.getUserUuidByAgentUuidAndFunc(UserContext.get().getUserUuid(true), "processtask");
//        if(StringUtils.isNotBlank(userUuid)) {
//            processTaskStepList.addAll(processTaskService.getUrgeableStepList(processTaskVo, userUuid));
//        }
		if(CollectionUtils.isNotEmpty(processTaskStepList)) {
			for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
				/** 触发通知 **/
				IProcessStepHandlerUtil.notify(processTaskStepVo, ProcessTaskNotifyTriggerType.URGE);
				IProcessStepHandlerUtil.action(processTaskStepVo, ProcessTaskNotifyTriggerType.URGE);
			}
		}else {
			try {
	            throw new ProcessTaskNoPermissionException(ProcessTaskOperationType.PROCESSTASK_URGE.getText());
	        }catch(ProcessTaskNoPermissionException e) {
	            throw new PermissionDeniedException();
	        }
		}
		/*生成催办活动*/
		ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
		processTaskStepVo.setProcessTaskId(processTaskId);
		IProcessStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.URGE);
		return null;
	}

}
