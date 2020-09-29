package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.processtask.ProcessTaskFocusRepeatException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskFocusUpdateApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;

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
	@Output({})
	@Description(desc = "切换工单关注状态")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		int isFocus = jsonObj.getIntValue("isFocus");
		String userUuid = UserContext.get().getUserUuid();
		if(isFocus == 1){
			if(processTaskMapper.checkProcessTaskFocusExists(processTaskId,userUuid) > 0){
				throw new ProcessTaskFocusRepeatException(processTaskId);
			}
			processTaskMapper.insertProcessTaskFocus(processTaskId,userUuid);
		}else{
			processTaskMapper.deleteProcessTaskFocus(processTaskId,userUuid);
		}
		JSONObject result = new JSONObject();
		result.put("isFocus",isFocus);
		return result;
	}

}
