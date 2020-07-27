package codedriver.module.process.api.processtask;


import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.automatic.AutomaticConfigVo;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskAutomaticRetryApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	ProcessTaskService processTaskService;
	
	@Autowired
	ProcessTaskStepDataMapper processTaskStepDataMapper;

	@Override
	public String getToken() {
		return "processtask/automatic/retry";
	}

	@Override
	public String getName() {
		return "工单automatic步骤重试接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
			@Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "工单步骤Id", isRequired = true)
	})
	@Output({})
	@Description(desc = "工单automatic步骤重试接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessTaskStepVo  processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(jsonObj.getLong("processTaskStepId"));
		ProcessTaskStepDataVo data = processTaskStepDataMapper.getProcessTaskStepData(new ProcessTaskStepDataVo(processTaskStepVo.getProcessTaskId(),processTaskStepVo.getId(),ProcessTaskStepDataType.AUTOMATIC.getValue()));
		JSONObject dataObject = data.getData();
		Boolean isRetry = false;
		AutomaticConfigVo automaticConfigVo = null;
		if(dataObject != null && dataObject.containsKey("requestAudit")) {
			JSONObject requestStatus = dataObject.getJSONObject("requestAudit").getJSONObject("status");
			//load第一次请求job
			if(!ProcessTaskStatus.SUCCEED.getValue().equals(requestStatus.getString("value"))
					&&!ProcessTaskStatus.FAILED.getValue().equals(requestStatus.getString("value"))) {
				String config = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
				automaticConfigVo = new AutomaticConfigVo(JSONObject.parseObject(config));
				automaticConfigVo.setIsRequest(true);
				isRetry = true;
			}
			//load回调job
			if( dataObject.containsKey("callbackAudit")&&ProcessTaskStatus.SUCCEED.getValue().equals(requestStatus.getString("value"))){
				JSONObject callbackStatus = dataObject.getJSONObject("callbackAudit").getJSONObject("status");
				if(!ProcessTaskStatus.SUCCEED.getValue().equals(callbackStatus.getString("value"))
						&&!ProcessTaskStatus.FAILED.getValue().equals(callbackStatus.getString("value"))) {
					String config = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
					automaticConfigVo = new AutomaticConfigVo(JSONObject.parseObject(config));
					automaticConfigVo.setIsRequest(false);
					isRetry = true;
				}
			}
			
		}
		if(isRetry) {
			processTaskService.runRequest(automaticConfigVo, processTaskStepVo);
		}else {}
		return null;
	}

}
