package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepActionListApi extends PrivateApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
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
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
		ProcessTaskStepVo processTaskStepVo = null;
		Map<String, String> customButtonMap = new HashMap<>();
		if(processTaskStepId != null) {
			processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
			processTaskService.setProcessTaskStepConfig(processTaskStepVo);
			customButtonMap = processTaskStepVo.getCustomButtonMap();
		}
		List<ValueTextVo> resultList = new ArrayList<>();
		List<String> actionList = ProcessStepUtilHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskId, processTaskStepId);
		List<codedriver.framework.process.constvalue.ProcessTaskOperationType> operateList = ProcessStepUtilHandlerFactory.getHandler().getOperateList(processTaskId, processTaskStepId);
		if(!Objects.equals(actionList, operateList)) {
		    //TODO
		}
		//TODO automatic ，临时处理，重构后删去
		if(processTaskStepVo != null && ProcessStepHandler.AUTOMATIC.getHandler().equals(processTaskStepVo.getHandler())) {
//			actionList = Arrays.asList("view", "transfer", "pocesstaskview", "work", "complete");
			actionList.remove(ProcessTaskStepAction.STARTPROCESS.getValue());
			actionList.remove(ProcessTaskStepAction.START.getValue());
			actionList.remove(ProcessTaskStepAction.ACTIVE.getValue());
			actionList.remove(ProcessTaskStepAction.RETREAT.getValue());
			actionList.remove(ProcessTaskStepAction.ACCEPT.getValue());
			actionList.remove(ProcessTaskStepAction.WORK.getValue());
			actionList.remove(ProcessTaskStepAction.ABORT.getValue());
			actionList.remove(ProcessTaskStepAction.RECOVER.getValue());
			actionList.remove(ProcessTaskStepAction.UPDATE.getValue());
			actionList.remove(ProcessTaskStepAction.COMMENT.getValue());
			actionList.remove(ProcessTaskStepAction.CREATESUBTASK.getValue());
			actionList.remove(ProcessTaskStepAction.URGE.getValue());
		}
		//
		
		for(String action : actionList) {
			String text = customButtonMap.get(action);//自定义优先
			if(StringUtils.isBlank(text)) {
				text = ProcessTaskStepAction.getText(action);
			}
			if(StringUtils.isNotBlank(text)) {
				ValueTextVo valueText = new ValueTextVo();
				valueText.setValue(action);
				valueText.setText(text);
				resultList.add(valueText);
			}
		}
		return resultList;
	}

}
