package codedriver.module.process.api.processtask;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class ProcessTaskStepSubtaskListApi extends ApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getToken() {
		return "processtask/step/subtask/list";
	}

	@Override
	public String getName() {
		return "工单步骤子任务列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "步骤id"),
		@Param(name = "id", type = ApiParamType.LONG, desc = "子任务id"),
		@Param(name = "userId", type = ApiParamType.STRING, desc = "子任务处理人"),
		@Param(name = "owner", type = ApiParamType.STRING, desc = "子任务创建人"),
		@Param(name = "status", type = ApiParamType.ENUM, rule = "running,succeed,aborted", desc = "状态")
	})
	@Output({
		@Param(name = "Return", explode = ProcessTaskStepSubtaskVo[].class, desc = "子任务列表")
	})
	@Description(desc = "工单步骤子任务列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessTaskStepSubtaskVo>() {});
		List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskMapper.getProcessTaskStepSubtaskList(processTaskStepSubtaskVo);
		for(ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
			List<ProcessTaskStepSubtaskContentVo> processTaskStepSubtaskContentList = processTaskMapper.getProcessTaskStepSubtaskContentBySubtaskId(processTaskStepSubtask.getId());
			for(ProcessTaskStepSubtaskContentVo processTaskStepSubtaskContentVo : processTaskStepSubtaskContentList) {
				if(processTaskStepSubtaskContentVo != null && processTaskStepSubtaskContentVo.getContentHash() != null) {
					if(ProcessTaskStepAction.CREATESUBTASK.getValue().equals(processTaskStepSubtaskContentVo.getAction())) {
						processTaskStepSubtask.setContent(processTaskStepSubtaskContentVo.getContent());
					}
				}
			}
		}
		return processTaskStepSubtaskList;
	}

}
