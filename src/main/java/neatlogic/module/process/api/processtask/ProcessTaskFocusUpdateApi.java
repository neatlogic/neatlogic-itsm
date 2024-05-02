package neatlogic.module.process.api.processtask;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskFocusRepeatException;
import neatlogic.framework.process.exception.processtask.ProcessTaskNotFoundException;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskFocusUpdateApi extends PrivateApiComponentBase {

	@Resource
	private ProcessTaskMapper processTaskMapper;

	@Resource
	private IProcessStepHandlerUtil processStepHandlerUtil;

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
			@Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源"),
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
		processTaskStepVo.getParamObj().put("source", jsonObj.getString("source"));
		if(isFocus == 1){
			if(processTaskMapper.checkProcessTaskFocusExists(processTaskId,userUuid) > 0){
				throw new ProcessTaskFocusRepeatException(processTaskId);
			}
			processTaskMapper.insertProcessTaskFocus(processTaskId,userUuid);
			processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.FOCUSTASK);
		}else{
			processTaskMapper.deleteProcessTaskFocus(processTaskId,userUuid);
			processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.UNDOFOCUSTASK);
		}
		JSONObject result = new JSONObject();
		result.put("isFocus",isFocus);
		return result;
	}

}
