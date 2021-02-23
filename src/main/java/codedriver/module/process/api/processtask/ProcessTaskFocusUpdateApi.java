package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.NO_AUTH;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.processtask.ProcessTaskFocusRepeatException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandlerUtil;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = NO_AUTH.class)
public class ProcessTaskFocusUpdateApi extends PrivateApiComponentBase {

	@Resource
	private ProcessTaskMapper processTaskMapper;

	@Resource
	private IProcessStepHandlerUtil IProcessStepHandlerUtil;

	@Override
	public String getToken() {
		return "processtask/focus/update";
	}

	@Override
	public String getName() {
		return "切换工单关注状态";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
			@Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单Id", isRequired = true),
			@Param(name = "isFocus", type = ApiParamType.ENUM, desc = "是否关注工单(1：关注；0：取消关注)", isRequired = true,rule = "0,1")
	})
	@Output({@Param(name="isFocus", type = ApiParamType.INTEGER, desc="是否关注工单")})
	@Description(desc = "切换工单关注状态")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		int isFocus = jsonObj.getIntValue("isFocus");
		String userUuid = UserContext.get().getUserUuid();
		if(processTaskMapper.getProcessTaskById(processTaskId) == null){
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
		processTaskStepVo.setProcessTaskId(processTaskId);
		if(isFocus == 1){
			if(processTaskMapper.checkProcessTaskFocusExists(processTaskId,userUuid) > 0){
				throw new ProcessTaskFocusRepeatException(processTaskId);
			}
			processTaskMapper.insertProcessTaskFocus(processTaskId,userUuid);
			IProcessStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.FOCUSTASK);
		}else{
			processTaskMapper.deleteProcessTaskFocus(processTaskId,userUuid);
			IProcessStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.UNDOFOCUSTASK);
		}
		JSONObject result = new JSONObject();
		result.put("isFocus",isFocus);
		return result;
	}

}
