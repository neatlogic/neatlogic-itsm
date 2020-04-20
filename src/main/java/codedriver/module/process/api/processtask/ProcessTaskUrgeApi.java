package codedriver.module.process.api.processtask;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class ProcessTaskUrgeApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
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
		List<ProcessTaskStepVo> processTaskStepList = ProcessStepHandlerFactory.getHandler().getUrgeableStepList(processTaskId);
		if(CollectionUtils.isNotEmpty(processTaskStepList)) {
			for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
				List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepVo.getId());
				//TODO linbq增加通知逻辑
				System.out.println("催办成功..");
			}
		}else {
			throw new ProcessTaskNoPermissionException(ProcessTaskStepAction.URGE.getText());
		}
		return null;
	}

}
