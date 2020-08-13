package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskFormApi extends ApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Autowired
	private ProcessTaskStepDataMapper processTaskStepDataMapper;

	@Override
	public String getToken() {
		return "processtask/step/form";
	}

	@Override
	public String getName() {
		return "查询工单步骤表单数据";
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
		@Param(name = "formAttributeDataMap", type = ApiParamType.JSONOBJECT, desc = "工单信息"),
		@Param(name = "formConfig", type = ApiParamType.JSONOBJECT, desc = "工单信息")
	})
	@Description(desc = "查询工单步骤表单数据")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessStepUtilHandlerFactory.getHandler().verifyActionAuthoriy(processTaskId, null, ProcessTaskStepAction.POCESSTASKVIEW);
		/** 检查工单id是否合法 **/
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		/** 检查工单是否存在表单 **/
		ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
		if(processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContent())) {
			processTaskVo.setFormConfig(processTaskFormVo.getFormContent());			
			List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskId);
			if(CollectionUtils.isNotEmpty(processTaskFormAttributeDataList)) {
				Map<String, Object> formAttributeDataMap = new HashMap<>();
				for(ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
					formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
				}
				processTaskVo.setFormAttributeDataMap(formAttributeDataMap);
			}
			
			Long processTaskStepId = jsonObj.getLong("processTaskStepId");
			if(processTaskStepId != null) {
				ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
				if(processTaskStepVo == null) {
					throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
				}
				if(!processTaskId.equals(processTaskStepVo.getProcessTaskId())) {
					throw new ProcessTaskRuntimeException("步骤：'" + processTaskStepId + "'不是工单：'" + processTaskId + "'的步骤");
				}
				List<String> verifyActionList = new ArrayList<>();
				verifyActionList.add(ProcessTaskStepAction.VIEW.getValue());
				List<String> actionList = ProcessStepUtilHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskId, processTaskStepId, verifyActionList);
				if(actionList.contains(ProcessTaskStepAction.VIEW.getValue())){
					/** 查出暂存数据中的表单数据**/				
					ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
					processTaskStepDataVo.setProcessTaskId(processTaskId);
					processTaskStepDataVo.setProcessTaskStepId(processTaskStepId);
					processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
					processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
					ProcessTaskStepDataVo stepDraftSaveData = processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
					if(stepDraftSaveData != null) {
						JSONObject dataObj = stepDraftSaveData.getData();
						if(MapUtils.isNotEmpty(dataObj)) {
							JSONArray formAttributeDataList = dataObj.getJSONArray("formAttributeDataList");
							if(CollectionUtils.isNotEmpty(formAttributeDataList)) {
								Map<String, Object> formAttributeDataMap = new HashMap<>();
								for(int i = 0; i < formAttributeDataList.size(); i++) {
									JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
									formAttributeDataMap.put(formAttributeDataObj.getString("attributeUuid"), formAttributeDataObj.get("dataList"));
								}
								processTaskVo.setFormAttributeDataMap(formAttributeDataMap);
							}
						}
					}
				}
			}
			
			
			List<String> verifyActionList = new ArrayList<>();
			verifyActionList.add(ProcessTaskStepAction.WORK.getValue());
			List<String> actionList = ProcessStepUtilHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskId, processTaskStepId, verifyActionList);
			if(actionList.removeAll(verifyActionList)) {
				/** 当前用户有处理权限，根据当前步骤表单属性显示设置控制表单属性展示 **/
				Map<String, String> formAttributeActionMap = new HashMap<>();
				List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList = processTaskMapper.getProcessTaskStepFormAttributeByProcessTaskStepId(processTaskStepId);
				if(processTaskStepFormAttributeList.size() > 0) {
					for(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo : processTaskStepFormAttributeList) {
						formAttributeActionMap.put(processTaskStepFormAttributeVo.getAttributeUuid(), processTaskStepFormAttributeVo.getAction());
					}
				}
				processTaskService.setProcessTaskFormAttributeAction(processTaskVo, formAttributeActionMap, 1);
			}else {
				processTaskService.setProcessTaskFormAttributeAction(processTaskVo, null, 0);
			}
			
			resultObj.put("formAttributeDataMap", processTaskVo.getFormAttributeDataMap());
			resultObj.put("formConfig", JSON.parseObject(processTaskVo.getFormConfig()));
		}
		
		return resultObj;
	}

}
