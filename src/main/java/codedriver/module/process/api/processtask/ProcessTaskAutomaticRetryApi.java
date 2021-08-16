package codedriver.module.process.api.processtask;


import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.automatic.AutomaticConfigVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskAutomaticService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskAutomaticRetryApi extends PrivateApiComponentBase {

	@Resource
	private ProcessTaskMapper processTaskMapper;
	
	@Resource
	ProcessTaskAutomaticService processTaskAutomaticService;
	
	@Resource
	ProcessTaskStepDataMapper processTaskStepDataMapper;
	
	@Resource
	SelectContentByHashMapper selectContentByHashMapper;

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
		ProcessTaskStepDataVo data = processTaskStepDataMapper.getProcessTaskStepData(new ProcessTaskStepDataVo(processTaskStepVo.getProcessTaskId(),processTaskStepVo.getId(),ProcessTaskStepDataType.AUTOMATIC.getValue(),SystemUser.SYSTEM.getUserId()));
		JSONObject dataObject = data.getData();
		Boolean isRetry = false;
		AutomaticConfigVo automaticConfigVo = null;
		if(dataObject != null && dataObject.containsKey("requestAudit")) {
			JSONObject requestStatus = dataObject.getJSONObject("requestAudit").getJSONObject("status");
			//load第一次请求job
			if(!ProcessTaskStatus.SUCCEED.getValue().equals(requestStatus.getString("value"))) {
				String config = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
				automaticConfigVo = new AutomaticConfigVo(JSONObject.parseObject(config).getJSONObject("automaticConfig"));
				automaticConfigVo.setIsRequest(true);
				isRetry = true;
			}
			//load回调job
			if( dataObject.containsKey("callbackAudit")&&ProcessTaskStatus.SUCCEED.getValue().equals(requestStatus.getString("value"))){
				JSONObject callbackStatus = dataObject.getJSONObject("callbackAudit").getJSONObject("status");
				if(!ProcessTaskStatus.SUCCEED.getValue().equals(callbackStatus.getString("value"))
						&& ProcessTaskStatus.FAILED.getValue().equals(callbackStatus.getString("value"))) {
					String config = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
					JSONObject stepConfig = JSONObject.parseObject(config);
					if(MapUtils.isNotEmpty(stepConfig)) {
						JSONObject automaticConfig = stepConfig.getJSONObject("automaticConfig");
						if(MapUtils.isNotEmpty(automaticConfig)) {
							automaticConfigVo = new AutomaticConfigVo(automaticConfig);
							automaticConfigVo.setIsRequest(false);
							isRetry = true;
						}
					}
				}
			}
			
		}
		if(isRetry) {
			processTaskAutomaticService.runRequest(automaticConfigVo, processTaskStepVo);
		}else {}
		return null;
	}

}
