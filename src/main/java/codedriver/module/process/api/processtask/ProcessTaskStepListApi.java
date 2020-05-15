package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepAuditVo;
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class ProcessTaskStepListApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getToken() {
		return "processtask/step/list";
	}

	@Override
	public String getName() {
		return "工单步骤列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")
	})
	@Output({
		@Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "步骤信息列表")
	})
	@Description(desc = "工单步骤列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessStepHandlerFactory.getHandler().verifyActionAuthoriy(processTaskId, null, ProcessTaskStepAction.POCESSTASKVIEW);
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		
		List<ProcessTaskStepVo> resultList = new ArrayList<>();
		//开始步骤
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
		if(processTaskStepList.size() != 1) {
			throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
		}
		ProcessTaskStepVo startStepVo = processTaskStepList.get(0);
		List<ProcessTaskStepUserVo> startStepMajorUserList = processTaskMapper.getProcessTaskStepUserByStepId(startStepVo.getId(), ProcessUserType.MAJOR.getValue());
		if(CollectionUtils.isNotEmpty(startStepMajorUserList)) {
			startStepVo.setMajorUser(startStepMajorUserList.get(0));
		}
		startStepVo.setAgentUserList(processTaskMapper.getProcessTaskStepUserByStepId(startStepVo.getId(), ProcessUserType.AGENT.getValue()));
		//上报描述内容和附件
		ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
		processTaskStepAuditVo.setProcessTaskId(processTaskId);
		processTaskStepAuditVo.setProcessTaskStepId(startStepVo.getId());
		processTaskStepAuditVo.setAction(ProcessTaskStepAction.STARTPROCESS.getValue());
		List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(processTaskStepAuditVo);
		if(CollectionUtils.isNotEmpty(processTaskStepAuditList)) {
			ProcessTaskStepCommentVo comment = new ProcessTaskStepCommentVo(processTaskStepAuditList.get(0));
			startStepVo.setComment(comment);
		}
		startStepVo.setIsView(1);
		resultList.add(startStepVo);
		//其他处理步骤
		processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.PROCESS.getValue());
		if(CollectionUtils.isNotEmpty(processTaskStepList)) {
			for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
				if(ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())) {
					continue;
				}
				//判断当前用户是否有权限查看该节点信息
				List<String> verifyActionList = new ArrayList<>();
				verifyActionList.add(ProcessTaskStepAction.VIEW.getValue());
				List<String> actionList = ProcessStepHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), verifyActionList);
				if(actionList.contains(ProcessTaskStepAction.VIEW.getValue())){
					processTaskStepVo.setIsView(1);
					List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
					if(CollectionUtils.isNotEmpty(majorUserList)) {
						processTaskStepVo.setMajorUser(majorUserList.get(0));
					}
					processTaskStepVo.setMinorUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MINOR.getValue()));
					processTaskStepVo.setAgentUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.AGENT.getValue()));
					processTaskStepVo.setWorkerList(processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepVo.getId()));
					//步骤评论列表
					processTaskStepAuditVo = new ProcessTaskStepAuditVo();
					processTaskStepAuditVo.setProcessTaskId(processTaskId);
					processTaskStepAuditVo.setProcessTaskStepId(processTaskStepVo.getId());
					processTaskStepAuditVo.setAction(ProcessTaskStepAction.COMMENT.getValue());
					processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(processTaskStepAuditVo);
					if(CollectionUtils.isNotEmpty(processTaskStepAuditList)) {
//						for(ProcessTaskStepAuditVo processTaskStepAudit : processTaskStepAuditList) {
//							List<ProcessTaskStepAuditDetailVo> processTaskStepAuditDetailList = processTaskStepAudit.getAuditDetailList();
//							processTaskStepAuditDetailList.sort(ProcessTaskStepAuditDetailVo::compareTo);
//							for(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo : processTaskStepAuditDetailList) {
//								IProcessTaskStepAuditDetailHandler auditDetailHandler = ProcessTaskStepAuditDetailHandlerFactory.getHandler(processTaskStepAuditDetailVo.getType());
//								if(auditDetailHandler != null) {
//									auditDetailHandler.handle(processTaskStepAuditDetailVo);
//								}
//							}
//						}
//						processTaskStepVo.setProcessTaskStepAuditList(processTaskStepAuditList);
						for(ProcessTaskStepAuditVo processTaskStepAudit : processTaskStepAuditList) {
							processTaskStepVo.getCommentList().add(new ProcessTaskStepCommentVo(processTaskStepAudit));
						}
					}
					//子任务列表
					ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = new ProcessTaskStepSubtaskVo();
					processTaskStepSubtaskVo.setProcessTaskId(processTaskStepVo.getProcessTaskId());
					processTaskStepSubtaskVo.setProcessTaskStepId(processTaskStepVo.getId());
					List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskMapper.getProcessTaskStepSubtaskList(processTaskStepSubtaskVo);
					for(ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
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
						processTaskStepSubtask.setIsAbortable(0);
						processTaskStepSubtask.setIsCompletable(0);
						processTaskStepSubtask.setIsEditable(0);
						processTaskStepSubtask.setIsRedoable(0);
						
					}
					processTaskStepVo.setProcessTaskStepSubtaskList(processTaskStepSubtaskList);
				}else {
					processTaskStepVo.setIsView(0);
				}
				String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
				processTaskStepVo.setConfig(stepConfig);
				resultList.add(processTaskStepVo);
			}
		}
		//按开始时间正序排序
		resultList.sort((step1, step2) -> (int)(step1.getStartTime().getTime() - step2.getStartTime().getTime()));
		return resultList;
	}

}
