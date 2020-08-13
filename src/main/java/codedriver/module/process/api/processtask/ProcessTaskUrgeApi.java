package codedriver.module.process.api.processtask;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.notify.core.NotifyTriggerType;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
@Service
@OperationType(type = OperationTypeEnum.CREATE)
public class ProcessTaskUrgeApi extends ApiComponentBase {
	
	@Override
	public String getToken() {
		return "processtask/urge";
	}

	@Override
	public String getName() {
		return "工单催办接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id")
	})
	@Description(desc = "工单完成接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler();
		List<ProcessTaskStepVo> processTaskStepList = handler.getUrgeableStepList(processTaskId);
		if(CollectionUtils.isNotEmpty(processTaskStepList)) {
			for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
				/** 触发通知 **/
				handler.notify(processTaskStepVo, NotifyTriggerType.URGE);
			}
		}else {
			throw new ProcessTaskNoPermissionException(ProcessTaskStepAction.URGE.getText());
		}
		/*生成催办活动*/
		ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
		processTaskStepVo.setProcessTaskId(processTaskId);
		handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.URGE);
		return null;
	}

}
