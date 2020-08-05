package codedriver.module.process.api.processtask.tmp;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskMobileisFitApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getToken() {
		return "processtask/mobile/isfit";
	}

	@Override
	public String getName() {
		return "查看工单是否支持移动端";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")
	})
	@Output({
		@Param(name = "Return", type = ApiParamType.BOOLEAN, desc = "工单是否支持移动端，1：支持，0：不支持;不支持则移动端提示不支持")
	})
	@Description(desc = "临时屏蔽移动端工单查看处理接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		//TODO 临时屏蔽移动端工单
		JSONObject result = new JSONObject();
		result.put("isfit", true);
		//屏蔽节点
		List<String> blackList = new ArrayList<>();
		blackList.add(ProcessStepHandler.CHANGECREATE.getHandler());
		blackList.add(ProcessStepHandler.CHANGEHANDLE.getHandler());
		blackList.add(ProcessStepHandler.AUTOMATIC.getHandler());
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(jsonObj.getLong("processTaskId"));
		for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
			String handler = processTaskStepVo.getHandler().toLowerCase();
			if(blackList.contains(handler)) {
				result.put("isfit", false);
				result.put("msg", String.format("抱歉！移动端暂时不支持查看/处理含有‘%s’步骤节点的工单", ProcessStepHandler.getName(handler)));
				break;
			}
		}
		return result;
	}

}
