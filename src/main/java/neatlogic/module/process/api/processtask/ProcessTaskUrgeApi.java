package neatlogic.module.process.api.processtask;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyTriggerType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.process.service.ProcessTaskAgentService;
import neatlogic.framework.process.stephandler.core.IProcessStepHandlerUtil;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskUrgeApi extends PrivateApiComponentBase {
    
    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private ProcessTaskService processTaskService;
    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;
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
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id"),
		@Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源")
	})
	@Description(desc = "催办工单")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
		new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_URGE)
				.build()
				.checkAndNoPermissionThrowException();
		List<ProcessTaskStepVo> processTaskStepList = processTaskService.getUrgeableStepList(processTaskVo, UserContext.get().getUserUuid(true));
		/** 如果当前用户接受了其他用户的授权，查出其他用户拥有的权限，叠加当前用户权限里 **/
		List<String> fromUserUUidList = processTaskAgentService.getFromUserUuidListByToUserUuidAndChannelUuid(UserContext.get().getUserUuid(true), processTaskVo.getChannelUuid());
		for (String userUuid : fromUserUUidList) {
			processTaskStepList.addAll(processTaskService.getUrgeableStepList(processTaskVo, userUuid));
		}
		for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
			/** 触发通知 **/
			processStepHandlerUtil.notify(processTaskStepVo, ProcessTaskStepNotifyTriggerType.URGE);
			processStepHandlerUtil.action(processTaskStepVo, ProcessTaskNotifyTriggerType.URGE);
		}
		// 催办记录
		processTaskMapper.insertProcessTaskUrge(processTaskId, UserContext.get().getUserUuid(true));
		/*生成催办活动*/
		ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
		processTaskStepVo.setProcessTaskId(processTaskId);
		processTaskStepVo.getParamObj().put("source", jsonObj.getString("source"));
		processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.URGE);
		return null;
	}

}
