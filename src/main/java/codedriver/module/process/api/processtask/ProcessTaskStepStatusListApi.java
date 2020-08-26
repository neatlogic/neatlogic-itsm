package codedriver.module.process.api.processtask;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepStatusListApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
    
    @Autowired
    private ProcessTaskService processTaskService;
	
	@Override
	public String getToken() {
		return "processtask/step/status/list";
	}

	@Override
	public String getName() {
		return "工单全部步骤状态列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")
	})
	@Output({
		@Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "步骤状态列表")
	})
	@Description(desc = "工单全部步骤状态列表接口，用于流程图上显示步骤状态")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
		if(CollectionUtils.isNotEmpty(processTaskStepList)) {
	        for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
	            processTaskService.setProcessTaskStepUser(processTaskStepVo);
	            processTaskService.setProcessTaskStepConfig(processTaskStepVo);
			}
		}	
		return processTaskStepList;
	}

}
