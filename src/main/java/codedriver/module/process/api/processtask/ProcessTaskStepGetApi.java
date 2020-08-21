package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepGetApi extends ApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Override
	public String getToken() {
		return "processtask/step/get";
	}

	@Override
	public String getName() {
		return "工单步骤基本信息获取接口";
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
		@Param(name = "processTask", explode = ProcessTaskVo.class, desc = "工单信息")
	})
	@Description(desc = "工单步骤基本信息获取接口，当前步骤名称、激活时间、状态、处理人、协助处理人、处理时效、表单属性显示控制等")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        
		ProcessStepUtilHandlerFactory.getHandler().verifyActionAuthoriy(processTaskId, null, ProcessTaskStepAction.POCESSTASKVIEW);
		
		ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(processTaskId);
        
        processTaskVo.setStartProcessTaskStep(processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskId));

        Map<String, String> formAttributeActionMap = new HashMap<>();
		if(processTaskStepId != null) {
			List<String> verifyActionList = new ArrayList<>();
			verifyActionList.add(ProcessTaskStepAction.VIEW.getValue());
			List<String> actionList = ProcessStepUtilHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskId, processTaskStepId, verifyActionList);
			if(actionList.contains(ProcessTaskStepAction.VIEW.getValue())){	
			    ProcessTaskStepVo currentProcessTaskStepVo = processTaskService.getCurrentProcessTaskStepById(processTaskStepId);
				processTaskVo.setCurrentProcessTaskStep(currentProcessTaskStepVo);
				if(MapUtils.isNotEmpty(currentProcessTaskStepVo.getFormAttributeDataMap())) {
				    processTaskVo.setFormAttributeDataMap(currentProcessTaskStepVo.getFormAttributeDataMap());
				}
				if(StringUtils.isNotBlank(processTaskVo.getFormConfig())) {
	                //表单属性显示控制
	                List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList = processTaskMapper.getProcessTaskStepFormAttributeByProcessTaskStepId(processTaskStepId);
	                for(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo : processTaskStepFormAttributeList) {
	                    formAttributeActionMap.put(processTaskStepFormAttributeVo.getAttributeUuid(), processTaskStepFormAttributeVo.getAction());
	                }
				}
			}			
		}
		if(StringUtils.isNotBlank(processTaskVo.getFormConfig())) {
		    List<String> verifyActionList = new ArrayList<>();
	        verifyActionList.add(ProcessTaskStepAction.WORK.getValue());
	        List<String> actionList = ProcessStepUtilHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskId, processTaskStepId, verifyActionList);
	        processTaskService.setProcessTaskFormAttributeAction(processTaskVo, formAttributeActionMap, actionList.removeAll(verifyActionList) ? 1 : 0);
		}
		
		//TODO 兼容老工单表单（判断是否存在旧表单）
        Map<String,String> oldFormPropMap = processTaskMapper.getProcessTaskOldFormAndPropByTaskId(processTaskId);
        if(oldFormPropMap != null&&oldFormPropMap.size()>0) {
            processTaskVo.setIsHasOldFormProp(1);
        }
		
		JSONObject resultObj = new JSONObject();
        resultObj.put("processTask", processTaskVo);
		return resultObj;
	}

}
