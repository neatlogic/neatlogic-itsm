package neatlogic.module.process.api.processtask.tmp;

import java.util.ArrayList;
import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskMobileisFitApi extends PrivateApiComponentBase {

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
		blackList.add("changecreate");
		blackList.add("changehandle");
		//blackList.add(ProcessStepHandler.EVENT.getHandler());
		blackList.add(ProcessStepHandlerType.AUTOMATIC.getHandler());
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(jsonObj.getLong("processTaskId"));
		for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
			String handler = processTaskStepVo.getHandler().toLowerCase();
			if(blackList.contains(handler)) {
				result.put("isfit", false);
				result.put("msg", String.format("抱歉！移动端暂时不支持查看/处理含有‘%s’步骤节点的工单", ProcessStepHandlerTypeFactory.getName(handler)));
				break;
			}
		}
		return result;
	}

}
