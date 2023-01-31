package neatlogic.module.process.api.processtask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.ProcessTaskStepSubtaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import neatlogic.framework.process.dto.ProcessTaskStepSubtaskVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
//@Service
@Deprecated
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepSubtaskListApi extends PrivateApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
    
    @Autowired
    private ProcessTaskStepSubtaskMapper processTaskStepSubtaskMapper;

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
			Map<String, String> customButtonMap = ProcessStepInternalHandlerFactory.getHandler().getCustomButtonMapByConfigHashAndHandler(processTaskStepVo.getConfigHash(), processTaskStepVo.getHandler());
			List<ProcessTaskStepSubtaskVo> subtaskList = new ArrayList<>();
			for (ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
				String currentUser = UserContext.get().getUserUuid(true);
				if ((currentUser.equals(processTaskStepSubtask.getMajorUser()) && !ProcessTaskStatus.ABORTED.getValue().equals(processTaskStepSubtask.getStatus()))
						|| (currentUser.equals(processTaskStepSubtask.getUserUuid()) && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepSubtask.getStatus()))) {
					List<ProcessTaskStepSubtaskContentVo> processTaskStepSubtaskContentList = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskContentBySubtaskId(processTaskStepSubtask.getId());
					Iterator<ProcessTaskStepSubtaskContentVo> iterator = processTaskStepSubtaskContentList.iterator();
					while (iterator.hasNext()) {
						ProcessTaskStepSubtaskContentVo processTaskStepSubtaskContentVo = iterator.next();
						if (processTaskStepSubtaskContentVo != null && processTaskStepSubtaskContentVo.getContentHash() != null) {
//							if(ProcessTaskOperationType.SUBTASK_CREATE.getValue().equals(processTaskStepSubtaskContentVo.getAction())) {
//								processTaskStepSubtask.setContent(processTaskStepSubtaskContentVo.getContent());
//								iterator.remove();
//							}
						}
					}
					processTaskStepSubtask.setContentList(processTaskStepSubtaskContentList);
//					if(processTaskStepSubtask.getIsAbortable() == 1) {
//						String value = ProcessTaskOperationType.SUBTASK_ABORT.getValue();
//						String text = customButtonMap.get(value);
//						if(StringUtils.isBlank(text)) {
//							text = ProcessTaskOperationType.SUBTASK_ABORT.getText();
//						}
//						processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
//					}
//					if(processTaskStepSubtask.getIsCommentable() == 1) {
//						String value = ProcessTaskOperationType.SUBTASK_COMMENT.getValue();
//						String text = customButtonMap.get(value);
//						if(StringUtils.isBlank(text)) {
//							text = ProcessTaskOperationType.SUBTASK_COMMENT.getText();
//						}
//						processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
//					}
//					if(processTaskStepSubtask.getIsCompletable() == 1) {
//						String value = ProcessTaskOperationType.SUBTASK_COMPLETE.getValue();
//						String text = customButtonMap.get(value);
//						if(StringUtils.isBlank(text)) {
//							text = ProcessTaskOperationType.SUBTASK_COMPLETE.getText();
//						}
//						processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
//					}
//					if(processTaskStepSubtask.getIsEditable() == 1) {
//						String value = ProcessTaskOperationType.SUBTASK_EDIT.getValue();
//						String text = customButtonMap.get(value);
//						if(StringUtils.isBlank(text)) {
//							text = ProcessTaskOperationType.SUBTASK_EDIT.getText();
//						}
//						processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
//					}
//					if(processTaskStepSubtask.getIsRedoable() == 1) {
//						String value = ProcessTaskOperationType.SUBTASK_REDO.getValue();
//						String text = customButtonMap.get(value);
//						if(StringUtils.isBlank(text)) {
//							text = ProcessTaskOperationType.SUBTASK_REDO.getText();
//						}
//						processTaskStepSubtask.getActionList().add(new ValueTextVo(value, text));
//					}
					subtaskList.add(processTaskStepSubtask);
				}
			}
			return subtaskList;
		}
		return new ArrayList<>();
	}

}
