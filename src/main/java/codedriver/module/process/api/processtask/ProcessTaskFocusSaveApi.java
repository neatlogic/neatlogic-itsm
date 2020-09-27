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
@OperationType(type = OperationTypeEnum.CREATE)
public class ProcessTaskFocusSaveApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getToken() {
		return "processtask/focus/save";
	}

	@Override
	public String getName() {
		return "关注工单";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({@Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单Id", isRequired = true)})
	@Output({})
	@Description(desc = "关注工单")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		String userUuid = UserContext.get().getUserUuid();
		if(processTaskMapper.checkProcessTaskFocusExists(processTaskId,userUuid) > 0){
			throw new ProcessTaskFocusRepeatException(processTaskId);
		}
		processTaskMapper.insertProcessTaskFocus(processTaskId,userUuid);
		return null;
	}

}
