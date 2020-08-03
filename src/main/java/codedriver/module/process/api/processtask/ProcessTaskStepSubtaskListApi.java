package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepSubtaskListApi extends ApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
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
		@Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "步骤id"),
		@Param(name = "id", type = ApiParamType.LONG, desc = "子任务id"),
		@Param(name = "userUuid", type = ApiParamType.STRING, desc = "子任务处理人"),
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
		if(CollectionUtils.isNotEmpty(processTaskStepSubtaskList)) {
			Map<String, String> customButtonMap = processTaskService.getCustomButtonTextMap(processTaskStepSubtaskVo.getProcessTaskStepId());
			List<ProcessTaskStepSubtaskVo> subtaskList = new ArrayList<>();
			for(ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
				String currentUser = UserContext.get().getUserUuid(true);
				if((currentUser.equals(processTaskStepSubtask.getMajorUser()) && !ProcessTaskStatus.ABORTED.getValue().equals(processTaskStepSubtask.getStatus()))
						|| (currentUser.equals(processTaskStepSubtask.getUserUuid()) && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepSubtask.getStatus()))) {
					List<ProcessTaskStepSubtaskContentVo> processTaskStepSubtaskContentList = processTaskMapper.getProcessTaskStepSubtaskContentBySubtaskId(processTaskStepSubtask.getId());
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
