package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepActionListApi extends PrivateApiComponentBase {
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Override
	public String getToken() {
		return "processtask/step/action/list";
	}

	@Override
	public String getName() {
		return "工单步骤当前用户操作权限列表获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "工单步骤id")
	})
	@Output({
		@Param(name = "Return", explode = ValueTextVo[].class, desc = "当前用户操作权限列表")
	})
	@Description(desc = "工单步骤当前用户操作权限列表获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler();
		ProcessTaskStepVo processTaskStepVo = processTaskVo.getCurrentProcessTaskStep();
		Map<String, String> customButtonMap = new HashMap<>();
		if(processTaskStepVo != null) {
			processTaskService.setProcessTaskStepConfig(processTaskStepVo);
			customButtonMap = processTaskStepVo.getCustomButtonMap();
			handler = ProcessStepUtilHandlerFactory.getHandler(processTaskStepVo.getHandler());
		}
		List<ValueTextVo> resultList = new ArrayList<>();
//		List<String> actionList = ProcessStepUtilHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskId, processTaskStepId);
//		Long start = System.currentTimeMillis();
		List<ProcessTaskOperationType> operateList = handler.getOperateList(processTaskVo, processTaskStepVo);
//        System.out.println("aaaaaaaaaaa:" + (System.currentTimeMillis() - start));
		//TODO automatic ，临时处理，重构后删去
//		if(processTaskStepVo != null && ProcessStepHandler.AUTOMATIC.getHandler().equals(processTaskStepVo.getHandler())) {
////			actionList = Arrays.asList("view", "transfer", "pocesstaskview", "work", "complete");
//			actionList.remove(ProcessTaskStepAction.STARTPROCESS.getValue());
//			actionList.remove(ProcessTaskStepAction.START.getValue());
//			actionList.remove(ProcessTaskStepAction.ACTIVE.getValue());
//			actionList.remove(ProcessTaskStepAction.RETREAT.getValue());
//			actionList.remove(ProcessTaskStepAction.ACCEPT.getValue());
//			actionList.remove(ProcessTaskStepAction.WORK.getValue());
//			actionList.remove(ProcessTaskStepAction.ABORT.getValue());
//			actionList.remove(ProcessTaskStepAction.RECOVER.getValue());
//			actionList.remove(ProcessTaskStepAction.UPDATE.getValue());
//			actionList.remove(ProcessTaskStepAction.COMMENT.getValue());
//			actionList.remove(ProcessTaskStepAction.CREATESUBTASK.getValue());
//			actionList.remove(ProcessTaskStepAction.URGE.getValue());
//		}

		for(ProcessTaskOperationType operationType : operateList) {
		    String text = customButtonMap.get(operationType.getValue());
		    if(StringUtils.isBlank(text)) {
		        text = operationType.getText();
		    }
		    if(StringUtils.isNotBlank(text)) {
		        ValueTextVo valueText = new ValueTextVo();
		        valueText.setValue(operationType.getValue());
		        valueText.setText(text);
		        resultList.add(valueText);
		    }
		}
		return resultList;
	}

}
