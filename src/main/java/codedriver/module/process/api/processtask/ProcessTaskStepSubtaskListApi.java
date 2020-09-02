package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepSubtaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepSubtaskListApi extends PrivateApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
    
    @Autowired
    private ProcessTaskStepSubtaskMapper processTaskStepSubtaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;

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
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "步骤id")
	})
	@Output({
		@Param(name = "Return", explode = ProcessTaskStepSubtaskVo[].class, desc = "子任务列表")
	})
	@Description(desc = "工单步骤子任务列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
	    Long processTaskStepId = jsonObj.getLong("processTaskStepId");
	    ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
	    if(processTaskStepVo == null) {
	        throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
	    }
		List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskListByProcessTaskStepId(processTaskStepId);
		if(CollectionUtils.isNotEmpty(processTaskStepSubtaskList)) {
		    processTaskService.setProcessTaskStepConfig(processTaskStepVo);
			Map<String, String> customButtonMap = processTaskStepVo.getCustomButtonMap();
			List<ProcessTaskStepSubtaskVo> subtaskList = new ArrayList<>();
			for(ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
				String currentUser = UserContext.get().getUserUuid(true);
				if((currentUser.equals(processTaskStepSubtask.getMajorUser()) && !ProcessTaskStatus.ABORTED.getValue().equals(processTaskStepSubtask.getStatus()))
						|| (currentUser.equals(processTaskStepSubtask.getUserUuid()) && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepSubtask.getStatus()))) {
					List<ProcessTaskStepSubtaskContentVo> processTaskStepSubtaskContentList = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskContentBySubtaskId(processTaskStepSubtask.getId());
					Iterator<ProcessTaskStepSubtaskContentVo> iterator = processTaskStepSubtaskContentList.iterator();
					while(iterator.hasNext()) {
						ProcessTaskStepSubtaskContentVo processTaskStepSubtaskContentVo = iterator.next();
						if(processTaskStepSubtaskContentVo != null && processTaskStepSubtaskContentVo.getContentHash() != null) {
							if(ProcessTaskStepAction.CREATESUBTASK.getValue().equals(processTaskStepSubtaskContentVo.getAction())) {
								processTaskStepSubtask.setContent(processTaskStepSubtaskContentVo.getContent());
								iterator.remove();
							}
						}
					}
					processTaskStepSubtask.setContentList(processTaskStepSubtaskContentList);
					if(processTaskStepSubtask.getIsAbortable() == 1) {
						String value = ProcessTaskStepAction.ABORTSUBTASK.getValue();
						String text = customButtonMap.get(value);
						if(StringUtils.isBlank(text)) {
							text = ProcessTaskStepAction.ABORTSUBTASK.getText();
						}
						processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
					}
					if(processTaskStepSubtask.getIsCommentable() == 1) {
						String value = ProcessTaskStepAction.COMMENTSUBTASK.getValue();
						String text = customButtonMap.get(value);
						if(StringUtils.isBlank(text)) {
							text = ProcessTaskStepAction.COMMENTSUBTASK.getText();
						}
						processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
					}
					if(processTaskStepSubtask.getIsCompletable() == 1) {
						String value = ProcessTaskStepAction.COMPLETESUBTASK.getValue();
						String text = customButtonMap.get(value);
						if(StringUtils.isBlank(text)) {
							text = ProcessTaskStepAction.COMPLETESUBTASK.getText();
						}
						processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
					}
					if(processTaskStepSubtask.getIsEditable() == 1) {
						String value = ProcessTaskStepAction.EDITSUBTASK.getValue();
						String text = customButtonMap.get(value);
						if(StringUtils.isBlank(text)) {
							text = ProcessTaskStepAction.EDITSUBTASK.getText();
						}
						processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
					}
					if(processTaskStepSubtask.getIsRedoable() == 1) {
						String value = ProcessTaskStepAction.REDOSUBTASK.getValue();
						String text = customButtonMap.get(value);
						if(StringUtils.isBlank(text)) {
							text = ProcessTaskStepAction.REDOSUBTASK.getText();
						}
						processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
					}
					subtaskList.add(processTaskStepSubtask);
				}
			}
			return subtaskList;
		}
		return new ArrayList<>();
	}

}
