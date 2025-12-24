package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskTransferApi extends PrivateApiComponentBase {
    
    @Autowired
    private ProcessTaskService processTaskService;
	
	@Override
	public String getToken() {
		return "processtask/transfer";
	}

	@Override
	public String getName() {
		return "nmpap.processtasktransferapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
			@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
			@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskstepid"),
			@Param(name = "workerList", type = ApiParamType.NOAUTH, isRequired = true, desc = "nmpap.processtasktransferapi.input.param.desc.workerlist", help = "[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]"),
			@Param(name = "isSaveData", type = ApiParamType.ENUM, rule = "0,1", desc = "nmpap.processtasktransferapi.input.param.desc.issavedata"),
			@Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "common.source"),
			@Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "common.content")
	})
	@Output({})
	@Description(desc = "nmpap.processtasktransferapi.getname")
	@ResubmitInterval(3)
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");

		List<String> workerList = new ArrayList<>();
		Object workerListObj = jsonObj.get("workerList");
		if(workerListObj instanceof JSONArray) {
			workerList = JSON.parseArray(JSON.toJSONString(workerListObj), String.class);
		}else if(workerListObj instanceof String) {
			workerList.add((String)workerListObj);
		}
		Integer isSaveData = jsonObj.getInteger("isSaveData");
		String source = jsonObj.getString("source");
		String content = jsonObj.getString("content");
		processTaskService.transferProcessTaskStep(processTaskId, processTaskStepId, workerList, isSaveData, content, source);
		return null;
	}

}
