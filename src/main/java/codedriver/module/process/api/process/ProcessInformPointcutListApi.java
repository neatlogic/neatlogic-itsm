package codedriver.module.process.api.process;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessTaskStepAction;

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
	
	@Output({
		@Param(name="Return", explode=ValueTextVo[].class, desc = "通知触发点列表")
	})
	@Description(desc = "通知触发点列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<ValueTextVo> processInformPointcutList = new ArrayList<>();;
		for(ProcessTaskStepAction processTaskStepAction : ProcessTaskStepAction.values()) {
			processInformPointcutList.add(new ValueTextVo(processTaskStepAction.getValue(), processTaskStepAction.getText()));
		}
		return processInformPointcutList;
	}

}
