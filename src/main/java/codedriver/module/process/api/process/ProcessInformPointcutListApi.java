package codedriver.module.process.api.process;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.dto.ProcessInformPointcutVo;

@Service
public class ProcessInformPointcutListApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "process/inform/pointcut/list";
	}

	@Override
	public String getName() {
		return "通知触发点列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Description(desc = "通知触发点列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<ProcessInformPointcutVo> processInformPointcutList = new ArrayList<>();;
		for(ProcessTaskStepAction processTaskStepAction : ProcessTaskStepAction.values()) {
			processInformPointcutList.add(new ProcessInformPointcutVo(processTaskStepAction.getValue(), processTaskStepAction.getText()));
		}
		return processInformPointcutList;
	}

}
