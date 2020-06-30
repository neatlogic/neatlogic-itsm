package codedriver.module.process.stephandler.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessStepMode;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskGroupSearch;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessTaskStepUserStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.constvalue.WorkerPolicy;
import codedriver.framework.process.dto.ChannelPriorityVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskAssignWorkerVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFileVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.framework.process.workerpolicy.core.IWorkerPolicyHandler;
import codedriver.framework.process.workerpolicy.core.WorkerPolicyHandlerFactory;

@Service
public class OmnipotentProcessComponent extends ProcessStepHandlerBase {
	static Logger logger = LoggerFactory.getLogger(OmnipotentProcessComponent.class);

	@Override
	public String getHandler() {
		return ProcessStepHandler.OMNIPOTENT.getHandler();
	}

	@Override
	public String getType() {
		return ProcessStepHandler.OMNIPOTENT.getType();
	}

	@Override
	public ProcessStepMode getMode() {
		return ProcessStepMode.MT;
	}

	@SuppressWarnings("serial")
	@Override
	public JSONObject getChartConfig() {
		return new JSONObject() {
			{
				this.put("icon", "ts-round-s");
				this.put("shape", "L-rectangle:R-rectangle");
				this.put("width", 68);
				this.put("height", 40);
			}
		};
	}

	@Override
	public String getName() {
		return ProcessStepHandler.OMNIPOTENT.getName();
	}

	@Override
	public int getSort() {
		return 3;
	}

	@Override
	protected int myActive(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {

		return 0;
	}

	protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) throws ProcessTaskException {
		/** 分配处理人 **/
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
		String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());

		JSONObject workerPolicyConfig = null;
		try {
			JSONObject stepConfigObj = JSONObject.parseObject(stepConfig);
			currentProcessTaskStepVo.getParamObj().putAll(stepConfigObj);
			if (MapUtils.isNotEmpty(stepConfigObj)) {
				workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
			}
		} catch (Exception ex) {
			logger.error("hash为" + processTaskStepVo.getConfigHash() + "的processtask_step_config内容不是合法的JSON格式", ex);
		}
		if(workerPolicyConfig == null) {
			workerPolicyConfig = new JSONObject();
		}
		
		/** 如果已经存在过处理人，则继续使用旧处理人，否则启用分派 **/
		List<ProcessTaskStepUserVo> oldUserList = processTaskMapper.getProcessTaskStepUserByStepId(currentProcessTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
		if (oldUserList.size() > 0) {
			ProcessTaskStepUserVo oldUserVo = oldUserList.get(0);
			workerList.add(new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), GroupSearch.USER.getValue(), oldUserVo.getUserUuid()));
		} else {
			/** 分配处理人 **/
			ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
			processTaskStepWorkerPolicyVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
			List<ProcessTaskStepWorkerPolicyVo> workerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicy(processTaskStepWorkerPolicyVo);
			if (CollectionUtils.isNotEmpty(workerPolicyList)) {
				for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : workerPolicyList) {
					IWorkerPolicyHandler workerPolicyHandler = WorkerPolicyHandlerFactory.getHandler(workerPolicyVo.getPolicy());
					if (workerPolicyHandler != null) {
						List<ProcessTaskStepWorkerVo> tmpWorkerList = workerPolicyHandler.execute(workerPolicyVo, currentProcessTaskStepVo);
						/** 顺序分配处理人 **/
						if ("sort".equals(workerPolicyConfig.getString("executeMode")) && tmpWorkerList.size() > 0) {
							// 找到处理人，则退出
							workerList.addAll(tmpWorkerList);
							break;
						} else if ("batch".equals(workerPolicyConfig.getString("executeMode"))) {
							// 去重取并集
							tmpWorkerList.removeAll(workerList);
							workerList.addAll(tmpWorkerList);
						}
					}
				}
			}
		}
		if (workerList.size() == 1) {
			String autoStart = workerPolicyConfig.getString("autoStart");
			/** 设置当前步骤状态为处理中 **/
			if ("1".equals(autoStart) && StringUtils.isNotBlank(workerList.get(0).getUuid()) && GroupSearch.USER.getValue().equals(workerList.get(0).getType())) {
				ProcessTaskStepUserVo userVo = new ProcessTaskStepUserVo();
				userVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
				userVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
				userVo.setUserUuid(workerList.get(0).getUuid());
				UserVo user = userMapper.getUserBaseInfoByUuid(workerList.get(0).getUuid());
				userVo.setUserName(user.getUserName());
				userList.add(userVo);
				currentProcessTaskStepVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
			}
		}
		return 1;
	}

	@Override
	protected int myStart(ProcessTaskStepVo processTaskStepVo) {
		return 0;
	}

	@Override
	public Boolean isAllowStart() {
		return true;
	}

	@Override
	public List<ProcessTaskStepVo> myGetNext(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		List<ProcessTaskStepVo> returnNextStepList = new ArrayList<>();
		List<ProcessTaskStepVo> nextStepList = processTaskMapper.getToProcessTaskStepByFromId(currentProcessTaskStepVo.getId());
		if (nextStepList.size() == 1) {
			return nextStepList;
		} else if (nextStepList.size() > 1) {
			JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
			if (paramObj != null && paramObj.containsKey("nextStepId")) {
				Long nextStepId = paramObj.getLong("nextStepId");
				for (ProcessTaskStepVo processTaskStepVo : nextStepList) {
					if (processTaskStepVo.getId().equals(nextStepId)) {
						returnNextStepList.add(processTaskStepVo);
						break;
					}
				}
			} else {
				throw new ProcessTaskException("找到多个后续节点");
			}
		}
		return returnNextStepList;
	}

	@Override
	protected int myStartProcess(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		baseInfoValid(currentProcessTaskStepVo);
		JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
		//前置步骤指派处理人
//		"assignWorkerList": [
//		             		{
//		             			"processTaskStepId": 1,
//								"processStepUuid": "abc",
//		             			"workerList": [
//		             				"user#xxx",
//		             				"team#xxx",
//		             				"role#xxx"
//		             			]
//		             		}
//		             	]
		Map<Long, List<String>> assignWorkerMap = new HashMap<>();
		JSONArray assignWorkerList = paramObj.getJSONArray("assignWorkerList");
		if(CollectionUtils.isNotEmpty(assignWorkerList)) {
			for(int i = 0; i < assignWorkerList.size(); i++) {
				JSONObject assignWorker = assignWorkerList.getJSONObject(i);
				Long processTaskStepId = assignWorker.getLong("processTaskStepId");
				if(processTaskStepId == null) {
					String processStepUuid = assignWorker.getString("processStepUuid");
					if(processStepUuid != null) {
						ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskIdAndProcessStepUuid(currentProcessTaskStepVo.getProcessTaskId(), processStepUuid);
						if(processTaskStepVo != null) {
							processTaskStepId = processTaskStepVo.getId();
						}
					}
				}
				if(processTaskStepId != null) {
					assignWorkerMap.put(processTaskStepId, JSON.parseArray(assignWorker.getString("workerList"), String.class));					
				}
			}
		}
		
		//获取可分配处理人的步骤列表				
		ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
		processTaskStepWorkerPolicyVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
		List<ProcessTaskStepWorkerPolicyVo> processTaskStepWorkerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicy(processTaskStepWorkerPolicyVo);
		if(CollectionUtils.isNotEmpty(processTaskStepWorkerPolicyList)) {
			for(ProcessTaskStepWorkerPolicyVo workerPolicyVo : processTaskStepWorkerPolicyList) {
				if(WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
					List<String> processStepUuidList = JSON.parseArray(workerPolicyVo.getConfigObj().getString("processStepUuidList"), String.class);
					for(String processStepUuid : processStepUuidList) {
						if(currentProcessTaskStepVo.getProcessStepUuid().equals(processStepUuid)) {
							List<ProcessTaskStepUserVo> majorList = processTaskMapper.getProcessTaskStepUserByStepId(workerPolicyVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
							if(CollectionUtils.isEmpty(majorList)) {
								ProcessTaskAssignWorkerVo assignWorkerVo = new ProcessTaskAssignWorkerVo();
								assignWorkerVo.setProcessTaskId(workerPolicyVo.getProcessTaskId());
								assignWorkerVo.setProcessTaskStepId(workerPolicyVo.getProcessTaskStepId());
								assignWorkerVo.setFromProcessTaskStepId(currentProcessTaskStepVo.getId());
								assignWorkerVo.setFromProcessStepUuid(currentProcessTaskStepVo.getProcessStepUuid());
								processTaskMapper.deleteProcessTaskAssignWorker(assignWorkerVo);
								List<String> workerList = assignWorkerMap.get(workerPolicyVo.getProcessTaskStepId());
								if(CollectionUtils.isNotEmpty(workerList)) {
									for(String worker : workerList) {
										String[] split = worker.split("#");
										assignWorkerVo.setType(split[0]);
										assignWorkerVo.setUuid(split[1]);
										processTaskMapper.insertProcessTaskAssignWorker(assignWorkerVo);
									}
								}else {
									Integer isRequired = workerPolicyVo.getConfigObj().getInteger("isRequired");
									if(isRequired != null && isRequired.intValue() == 1) {
										ProcessTaskStepVo assignableWorkerStep = processTaskMapper.getProcessTaskStepBaseInfoById(workerPolicyVo.getProcessTaskStepId());
										throw new ProcessTaskRuntimeException("指派：" + assignableWorkerStep.getName() + "步骤处理人是必填");
									}
								}
							}
						}
					}
				}
			}
		}
		return 1;
	}

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	protected int myHandle(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myComplete(ProcessTaskStepVo currentProcessTaskStepVo) {
		JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
		//找出当前用户再当前步骤的所有暂存活动，一般只有一个
//		ProcessTaskStepAuditVo auditVo = new ProcessTaskStepAuditVo();
//		auditVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
//		auditVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
//		auditVo.setAction(ProcessTaskStepAction.SAVE.getValue());
//		auditVo.setUserUuid(UserContext.get().getUserUuid(true));
//		List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(auditVo);
//		if(CollectionUtils.isNotEmpty(processTaskStepAuditList)) {
//			//找出最后一次暂存活动
//			ProcessTaskStepAuditVo processTaskStepAuditVo = processTaskStepAuditList.get(processTaskStepAuditList.size() - 1);
//			List<ProcessTaskStepAuditDetailVo> processTaskStepAuditDetailList = processTaskStepAuditVo.getAuditDetailList();
//			for(ProcessTaskStepAuditDetailVo processTaskStepAuditDetail : processTaskStepAuditDetailList) {
//				ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepAuditDetail.getNewContent());
//				if(processTaskContentVo != null) {
//					paramObj.put(ProcessTaskAuditDetailType.getParamName(processTaskStepAuditDetail.getType()), processTaskContentVo.getContent());
//				}
//			}
//			//删除暂存活动
//			for(ProcessTaskStepAuditVo processTaskStepAudit : processTaskStepAuditList) {
//				processTaskMapper.deleteProcessTaskStepAuditById(processTaskStepAudit.getId());
//			}
//		}
		ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
		processTaskStepDataVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
		processTaskStepDataVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
		processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
		processTaskStepDataVo.setType("stepDraftSave");
		ProcessTaskStepDataVo stepDraftSaveData = processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
		if(stepDraftSaveData != null) {
			JSONObject dataObj = stepDraftSaveData.getData();
			if(MapUtils.isNotEmpty(dataObj)) {
				paramObj.putAll(dataObj);
			}
		}
		/** 保存描述内容 **/
		String content = paramObj.getString("content");
		if (StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
//			processTaskMapper.replaceProcessTaskContent(contentVo);
			processTaskMapper.replaceProcessTaskStepContent(new ProcessTaskStepContentVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), contentVo.getHash()));
//			paramObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), contentVo.getHash());
		}

		if(ProcessTaskStepAction.COMPLETE.getValue().equals(paramObj.getString("action"))) {		
//			前置步骤指派处理人
//			"assignWorkerList": [
//			             		{
//			             			"processTaskStepId": 1,
//			             			"workerList": [
//			             				"user#xxx",
//			             				"team#xxx",
//			             				"role#xxx"
//			             			]
//			             		}
//			             	]
			Map<Long, List<String>> assignWorkerMap = new HashMap<>();
			JSONArray assignWorkerList = paramObj.getJSONArray("assignWorkerList");
			if(CollectionUtils.isNotEmpty(assignWorkerList)) {
				for(int i = 0; i < assignWorkerList.size(); i++) {
					JSONObject assignWorker = assignWorkerList.getJSONObject(i);
					assignWorkerMap.put(assignWorker.getLong("processTaskStepId"), JSON.parseArray(assignWorker.getString("workerList"), String.class));
				}
			}
			
			//获取可分配处理人的步骤列表				
			ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
			processTaskStepWorkerPolicyVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
			List<ProcessTaskStepWorkerPolicyVo> processTaskStepWorkerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicy(processTaskStepWorkerPolicyVo);
			if(CollectionUtils.isNotEmpty(processTaskStepWorkerPolicyList)) {
				for(ProcessTaskStepWorkerPolicyVo workerPolicyVo : processTaskStepWorkerPolicyList) {
					if(WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
						List<String> processStepUuidList = JSON.parseArray(workerPolicyVo.getConfigObj().getString("processStepUuidList"), String.class);
						for(String processStepUuid : processStepUuidList) {
							if(currentProcessTaskStepVo.getProcessStepUuid().equals(processStepUuid)) {
								List<ProcessTaskStepUserVo> majorList = processTaskMapper.getProcessTaskStepUserByStepId(workerPolicyVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
								if(CollectionUtils.isEmpty(majorList)) {
									ProcessTaskAssignWorkerVo assignWorkerVo = new ProcessTaskAssignWorkerVo();
									assignWorkerVo.setProcessTaskId(workerPolicyVo.getProcessTaskId());
									assignWorkerVo.setProcessTaskStepId(workerPolicyVo.getProcessTaskStepId());
									assignWorkerVo.setFromProcessTaskStepId(currentProcessTaskStepVo.getId());
									assignWorkerVo.setFromProcessStepUuid(currentProcessTaskStepVo.getProcessStepUuid());
									processTaskMapper.deleteProcessTaskAssignWorker(assignWorkerVo);
									List<String> workerList = assignWorkerMap.get(workerPolicyVo.getProcessTaskStepId());
									if(CollectionUtils.isNotEmpty(workerList)) {
										for(String worker : workerList) {
											String[] split = worker.split("#");
											assignWorkerVo.setType(split[0]);
											assignWorkerVo.setUuid(split[1]);
											processTaskMapper.insertProcessTaskAssignWorker(assignWorkerVo);
										}
									}else {
										Integer isRequired = workerPolicyVo.getConfigObj().getInteger("isRequired");
										if(isRequired != null && isRequired.intValue() == 1) {
											ProcessTaskStepVo assignableWorkerStep = processTaskMapper.getProcessTaskStepBaseInfoById(workerPolicyVo.getProcessTaskStepId());
											throw new ProcessTaskRuntimeException("指派：" + assignableWorkerStep.getName() + "步骤处理人是必填");
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return 1;
	}

	@Override
	protected int myRetreat(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		/** 保存描述内容 **/
		JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
		String content = paramObj.getString("content");
		if (StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
//			processTaskMapper.replaceProcessTaskContent(contentVo);
			processTaskMapper.replaceProcessTaskStepContent(new ProcessTaskStepContentVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), contentVo.getHash()));
//			paramObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), contentVo.getHash());
		}
		return 1;
	}

	@Override
	protected int myAbort(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myBack(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myHang(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myRecover(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myTransfer(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) throws ProcessTaskException {
		/** 保存描述内容 **/
		JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
		String content = paramObj.getString("content");
		if (StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
//			processTaskMapper.replaceProcessTaskContent(contentVo);
			processTaskMapper.replaceProcessTaskStepContent(new ProcessTaskStepContentVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), contentVo.getHash()));
//			paramObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), contentVo.getHash());
		}
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
		String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());

		if (StringUtils.isBlank(stepConfig)) {
			return 1;
		}
		JSONObject stepConfigObj = null;
		try {
			stepConfigObj = JSONObject.parseObject(stepConfig);
			currentProcessTaskStepVo.setParamObj(stepConfigObj);
		} catch (Exception ex) {
			logger.error("hash为" + processTaskStepVo.getConfigHash() + "的processtask_step_config内容不是合法的JSON格式", ex);
		}
		if (MapUtils.isEmpty(stepConfigObj)) {
			return 1;
		}
		JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
		if (MapUtils.isEmpty(workerPolicyConfig)) {
			return 1;
		}
		String autoStart = workerPolicyConfig.getString("autoStart");
		if ("1".equals(autoStart) && workerList.size() == 1) {
			/** 设置当前步骤状态为处理中 **/
			if (StringUtils.isNotBlank(workerList.get(0).getUuid()) && GroupSearch.USER.getValue().equals(workerList.get(0).getType())) {
				ProcessTaskStepUserVo userVo = new ProcessTaskStepUserVo();
				userVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
				userVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
				userVo.setUserUuid(workerList.get(0).getUuid());
				UserVo user = userMapper.getUserBaseInfoByUuid(workerList.get(0).getUuid());
				userVo.setUserName(user.getUserName());
				userList.add(userVo);
				currentProcessTaskStepVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
			}
		}
		return 1;
	}

	@Override
	public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj) {
		/** 组装通知模板 **/ //TODO linbq 这里要删除
//		JSONArray notifyList = stepConfigObj.getJSONArray("notifyList");
//		if (CollectionUtils.isNotEmpty(notifyList)) {
//			List<String> templateUuidList = new ArrayList<>();
//			for (int j = 0; j < notifyList.size(); j++) {
//				JSONObject notifyObj = notifyList.getJSONObject(j);
//				String template = notifyObj.getString("template");
//				if (StringUtils.isNotBlank(template)) {
//					templateUuidList.add(template);
//				}
//			}
//			processStepVo.setTemplateUuidList(templateUuidList);
//		}
		/** 组装通知策略id **/
		JSONObject notifyPolicyConfig = stepConfigObj.getJSONObject("notifyPolicyConfig");
		if(MapUtils.isNotEmpty(notifyPolicyConfig)) {
	        Long policyId = notifyPolicyConfig.getLong("policyId");
	        if(policyId != null) {
	        	processStepVo.setNotifyPolicyId(policyId);
	        }
		}
		/** 组装分配策略 **/
		JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
		if (MapUtils.isNotEmpty(workerPolicyConfig)) {
			JSONArray policyList = workerPolicyConfig.getJSONArray("policyList");
			if (CollectionUtils.isNotEmpty(policyList)) {
				List<ProcessStepWorkerPolicyVo> workerPolicyList = new ArrayList<>();
				for (int k = 0; k < policyList.size(); k++) {
					JSONObject policyObj = policyList.getJSONObject(k);
					if (!"1".equals(policyObj.getString("isChecked"))) {
						continue;
					}
					ProcessStepWorkerPolicyVo processStepWorkerPolicyVo = new ProcessStepWorkerPolicyVo();
					processStepWorkerPolicyVo.setProcessUuid(processStepVo.getProcessUuid());
					processStepWorkerPolicyVo.setProcessStepUuid(processStepVo.getUuid());
					processStepWorkerPolicyVo.setPolicy(policyObj.getString("type"));
					processStepWorkerPolicyVo.setSort(k + 1);
					processStepWorkerPolicyVo.setConfig(policyObj.getString("config"));
					workerPolicyList.add(processStepWorkerPolicyVo);
				}
				processStepVo.setWorkerPolicyList(workerPolicyList);
			}
		}
	}

	@Override
	protected int mySaveDraft(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		JSONObject paramObj = currentProcessTaskStepVo.getParamObj();

		/** 更新工单信息 **/
		ProcessTaskVo processTaskVo = new ProcessTaskVo();
		processTaskVo.setId(currentProcessTaskStepVo.getProcessTaskId());
		processTaskVo.setTitle(paramObj.getString("title"));
		processTaskVo.setOwner(paramObj.getString("owner"));
		processTaskVo.setPriorityUuid(paramObj.getString("priorityUuid"));
		processTaskMapper.updateProcessTaskTitleOwnerPriorityUuid(processTaskVo);

		/** 保存描述内容 **/
		String content = paramObj.getString("content");
		if (StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
			processTaskMapper.replaceProcessTaskContent(contentVo);
			processTaskMapper.replaceProcessTaskStepContent(new ProcessTaskStepContentVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), contentVo.getHash()));
		}

		/** 保存附件uuid **/
		ProcessTaskFileVo processTaskFileVo = new ProcessTaskFileVo();
		processTaskFileVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
		processTaskFileVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
		processTaskMapper.deleteProcessTaskFile(processTaskFileVo);
		String fileUuidListStr = paramObj.getString("fileUuidList");
		if (StringUtils.isNotBlank(fileUuidListStr)) {
			List<String> fileUuidList = JSON.parseArray(fileUuidListStr, String.class);
			for (String fileUuid : fileUuidList) {
				processTaskFileVo.setFileUuid(fileUuid);
				processTaskMapper.insertProcessTaskFile(processTaskFileVo);
			}
		}
		return 1;
	}
	
	@Override
	public void updateProcessTaskStepUserAndWorker(List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) {
		
		for(ProcessTaskStepUserVo processTaskStepUserVo : userList) {
			//查出userUuid在当前步骤拥有的子任务
			ProcessTaskStepSubtaskVo stepSubtaskVo = new ProcessTaskStepSubtaskVo();
			stepSubtaskVo.setProcessTaskId(processTaskStepUserVo.getProcessTaskId());
			stepSubtaskVo.setProcessTaskStepId(processTaskStepUserVo.getProcessTaskStepId());
			stepSubtaskVo.setUserUuid(processTaskStepUserVo.getUserUuid());
			List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskMapper.getProcessTaskStepSubtaskList(stepSubtaskVo);
			//子任务状态列表
			List<String> stepSubtaskStatusList = processTaskStepSubtaskList.stream().map(ProcessTaskStepSubtaskVo::getStatus).collect(Collectors.toList());
			
			if(stepSubtaskStatusList.contains(ProcessTaskStatus.RUNNING.getValue())) {
				processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DOING.getValue());
			}else if(stepSubtaskStatusList.contains(ProcessTaskStatus.SUCCEED.getValue())) {
				processTaskStepUserVo.setStatus(ProcessTaskStepUserStatus.DONE.getValue());
			}else {//userUuid不是任何子任务处理人
				processTaskStepUserVo.setStatus(null);
			}
			String minorUserStatus = null;//userUuid是子任务处理人时的状态，null代表userUuid不是子任务处理人
			List<ProcessTaskStepUserVo> processTaskStepUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepUserVo.getProcessTaskStepId(), ProcessUserType.MINOR.getValue());
			for(ProcessTaskStepUserVo stepUser : processTaskStepUserList) {
				if(processTaskStepUserVo.getUserUuid().equals(stepUser.getUserUuid())) {
					minorUserStatus = stepUser.getStatus();
				}
			}
			if(minorUserStatus == null && processTaskStepUserVo.getStatus() == null) {
				//processtask_step_subtask表和processtask_step_user表都没有数据
				//不增不减不更新
			}else if(minorUserStatus == null && processTaskStepUserVo.getStatus() != null) {
				processTaskMapper.insertProcessTaskStepUser(processTaskStepUserVo);
			}else if(minorUserStatus != null && processTaskStepUserVo.getStatus() == null){
				processTaskMapper.deleteProcessTaskStepUser(processTaskStepUserVo);
			}else if(!processTaskStepUserVo.getStatus().equals(minorUserStatus)){
				processTaskMapper.updateProcessTaskStepUserStatus(processTaskStepUserVo);
			}
		}
		
		for(ProcessTaskStepWorkerVo processTaskStepWorkerVo : workerList) {
			//查出userUuid在当前步骤拥有的子任务
			ProcessTaskStepSubtaskVo stepSubtaskVo = new ProcessTaskStepSubtaskVo();
			stepSubtaskVo.setProcessTaskId(processTaskStepWorkerVo.getProcessTaskId());
			stepSubtaskVo.setProcessTaskStepId(processTaskStepWorkerVo.getProcessTaskStepId());
			stepSubtaskVo.setUserUuid(processTaskStepWorkerVo.getUuid());
			List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskMapper.getProcessTaskStepSubtaskList(stepSubtaskVo);
			//子任务状态列表
			List<String> stepSubtaskStatusList = processTaskStepSubtaskList.stream().map(ProcessTaskStepSubtaskVo::getStatus).collect(Collectors.toList());
			String minorUserStatus = null;//userUuid是子任务处理人时的状态，null代表userUuid不是子任务处理人
			if(stepSubtaskStatusList.contains(ProcessTaskStatus.RUNNING.getValue())) {
				minorUserStatus = ProcessTaskStepUserStatus.DOING.getValue();
			}else if(stepSubtaskStatusList.contains(ProcessTaskStatus.SUCCEED.getValue())) {
				minorUserStatus = ProcessTaskStepUserStatus.DONE.getValue();
			}
			
			String majorUserStatus = null;//userUuid是主处理人时的状态，null代表userUuid不是主处理人
			List<ProcessTaskStepUserVo> stepMajorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepWorkerVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
			for(ProcessTaskStepUserVo stepUser : stepMajorUserList) {
				if(processTaskStepWorkerVo.getUuid().equals(stepUser.getUserUuid())) {
					majorUserStatus = stepUser.getStatus();
				}
			}
			
			List<ProcessTaskStepWorkerVo> processTaskWorkerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepWorkerVo.getProcessTaskStepId());
			List<String> userUuidList = processTaskWorkerList.stream().filter(e -> GroupSearch.USER.getValue().equals(e.getType())).map(ProcessTaskStepWorkerVo::getUuid).collect(Collectors.toList());
			
			if(ProcessTaskStepUserStatus.DOING.getValue().equals(majorUserStatus) 
					|| ProcessTaskStepUserStatus.DOING.getValue().equals(minorUserStatus)) {//如果userUuid是主处理人或子任务处理人，且状态时doing
				if(!userUuidList.contains(processTaskStepWorkerVo.getUuid())) {//processtask_step_worker不存在userUuid数据
					//插入processTaskStepWorker
					processTaskMapper.insertProcessTaskStepWorker(processTaskStepWorkerVo);
				}
			}else {
				if(userUuidList.contains(processTaskStepWorkerVo.getUuid())) {//processtask_step_worker存在userUuid数据
					//删除processTaskStepWorker
					processTaskMapper.deleteProcessTaskStepWorker(processTaskStepWorkerVo);
				}
			}
		}
	}
	
	private boolean baseInfoValid(ProcessTaskStepVo currentProcessTaskStepVo) {
		JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
		if(processTaskVo.getTitle() == null) {
			throw new ProcessTaskRuntimeException("工单标题格式不能为空");
		}
		Pattern titlePattern = Pattern.compile("^[A-Za-z_\\d\\u4e00-\\u9fa5]+$");
		if (!titlePattern.matcher(processTaskVo.getTitle()).matches()) {
			throw new ProcessTaskRuntimeException("工单标题格式不对");
		}
		paramObj.put(ProcessTaskAuditDetailType.TITLE.getParamName(), processTaskVo.getTitle());
		if (StringUtils.isBlank(processTaskVo.getOwner())) {
			throw new ProcessTaskRuntimeException("工单请求人不能为空");
		}
		if (userMapper.getUserBaseInfoByUuid(processTaskVo.getOwner()) == null) {
			throw new ProcessTaskRuntimeException("工单请求人账号:'" + processTaskVo.getOwner() + "'不存在");
		}
		if (StringUtils.isBlank(processTaskVo.getPriorityUuid())) {
			throw new ProcessTaskRuntimeException("工单优先级不能为空");
		}
		List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(processTaskVo.getChannelUuid());
		List<String> priorityUuidlist = new ArrayList<>(channelPriorityList.size());
		for (ChannelPriorityVo channelPriorityVo : channelPriorityList) {
			priorityUuidlist.add(channelPriorityVo.getPriorityUuid());
		}
		if (!priorityUuidlist.contains(processTaskVo.getPriorityUuid())) {
			throw new ProcessTaskRuntimeException("工单优先级与服务优先级级不匹配");
		}
		paramObj.put(ProcessTaskAuditDetailType.PRIORITY.getParamName(), processTaskVo.getPriorityUuid());

		// 获取上报描述内容
		List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentProcessTaskStepId(currentProcessTaskStepVo.getId());
		if (CollectionUtils.isNotEmpty(processTaskStepContentList)) {
			ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepContentList.get(0).getContentHash());
			if(processTaskContentVo != null) {
				paramObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), processTaskContentVo.getContent());
			}
		}
		ProcessTaskFileVo processTaskFileVo = new ProcessTaskFileVo();
		processTaskFileVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
		processTaskFileVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
		List<ProcessTaskFileVo> processTaskFileList = processTaskMapper.searchProcessTaskFile(processTaskFileVo);
		if (processTaskFileList.size() > 0) {
			List<String> fileUuidList = new ArrayList<>();
			for (ProcessTaskFileVo processTaskFile : processTaskFileList) {
				if (fileMapper.getFileByUuid(processTaskFile.getFileUuid()) == null) {
					throw new ProcessTaskRuntimeException("上传附件uuid:'" + processTaskFile.getFileUuid() + "'不存在");
				}
				fileUuidList.add(processTaskFile.getFileUuid());
			}
			paramObj.put(ProcessTaskAuditDetailType.FILE.getParamName(), JSON.toJSONString(fileUuidList));
		}
		currentProcessTaskStepVo.setParamObj(paramObj);
		return true;
	}
	
	@SuppressWarnings("serial")
	@Override
	public JSONObject makeupConfig(JSONObject configObj) {				
		if(configObj == null) {
			configObj = new JSONObject();
		}
		JSONObject resultObj = new JSONObject();
		
		/** 授权 **/
		JSONArray authorityArray = new JSONArray();
		authorityArray.add(new JSONObject() {{this.put("action", ProcessTaskStepAction.VIEW.getValue());this.put("text", ProcessTaskStepAction.VIEW.getText());this.put("acceptList", Arrays.asList(GroupSearch.COMMON.getValuePlugin() + UserType.ALL.getValue()));this.put("groupList", Arrays.asList(GroupSearch.COMMON.getValue(), GroupSearch.USER.getValue(), GroupSearch.TEAM.getValue(), GroupSearch.ROLE.getValue()));}});
		authorityArray.add(new JSONObject() {{this.put("action", ProcessTaskStepAction.ABORT.getValue());this.put("text", ProcessTaskStepAction.ABORT.getText());this.put("acceptList", Arrays.asList(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValuePlugin() + ProcessUserType.MAJOR.getValue()));this.put("groupList", Arrays.asList(GroupSearch.COMMON.getValue(), ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue(), GroupSearch.USER.getValue(), GroupSearch.TEAM.getValue(), GroupSearch.ROLE.getValue()));}});
		authorityArray.add(new JSONObject() {{this.put("action", ProcessTaskStepAction.TRANSFER.getValue());this.put("text", ProcessTaskStepAction.TRANSFER.getText());this.put("acceptList", Arrays.asList(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValuePlugin() + ProcessUserType.MAJOR.getValue()));this.put("groupList", Arrays.asList(GroupSearch.COMMON.getValue(), ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue(), GroupSearch.USER.getValue(), GroupSearch.TEAM.getValue(), GroupSearch.ROLE.getValue()));}});
		authorityArray.add(new JSONObject() {{this.put("action", ProcessTaskStepAction.UPDATE.getValue());this.put("text", ProcessTaskStepAction.UPDATE.getText());this.put("acceptList", Arrays.asList(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValuePlugin() + ProcessUserType.MAJOR.getValue()));this.put("groupList", Arrays.asList(GroupSearch.COMMON.getValue(), ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue(), GroupSearch.USER.getValue(), GroupSearch.TEAM.getValue(), GroupSearch.ROLE.getValue()));}});
		authorityArray.add(new JSONObject() {{this.put("action", ProcessTaskStepAction.URGE.getValue());this.put("text", ProcessTaskStepAction.URGE.getText());this.put("acceptList", Arrays.asList(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValuePlugin() + ProcessUserType.MAJOR.getValue()));this.put("groupList", Arrays.asList(GroupSearch.COMMON.getValue(), ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue(), GroupSearch.USER.getValue(), GroupSearch.TEAM.getValue(), GroupSearch.ROLE.getValue()));}});

		JSONArray authorityList = configObj.getJSONArray("authorityList");
		if(CollectionUtils.isNotEmpty(authorityList)) {
			Map<String, JSONArray> authorityMap = new HashMap<>();
			for(int i = 0; i < authorityList.size(); i++) {
				JSONObject authority = authorityList.getJSONObject(i);
				authorityMap.put(authority.getString("action"), authority.getJSONArray("acceptList"));
			}
			for(int i = 0; i < authorityArray.size(); i++) {
				JSONObject authority = authorityArray.getJSONObject(i);
				JSONArray acceptList = authorityMap.get(authority.getString("action"));
				if(acceptList != null) {
					authority.put("acceptList", acceptList);
				}
			}
		}
		resultObj.put("authorityList", authorityArray);
		
		/** 按钮映射 **/
		JSONArray customButtonArray = new JSONArray();
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.COMPLETE.getValue());this.put("customText", ProcessTaskStepAction.COMPLETE.getText());this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.BACK.getValue());this.put("customText", ProcessTaskStepAction.BACK.getText());this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.COMMENT.getValue());this.put("customText", ProcessTaskStepAction.COMMENT.getText());this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.TRANSFER.getValue());this.put("customText", ProcessTaskStepAction.TRANSFER.getText());this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.START.getValue());this.put("customText", ProcessTaskStepAction.START.getText());this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.ABORT.getValue());this.put("customText", ProcessTaskStepAction.ABORT.getText());this.put("value", "");}});
		customButtonArray.add(new JSONObject() {{this.put("name", ProcessTaskStepAction.RECOVER.getValue());this.put("customText", ProcessTaskStepAction.RECOVER.getText());this.put("value", "");}});
		
		JSONArray customButtonList = configObj.getJSONArray("customButtonList");
		if(CollectionUtils.isNotEmpty(customButtonList)) {
			Map<String, String> customButtonMap = new HashMap<>();
			for(int i = 0; i < customButtonList.size(); i++) {
				JSONObject customButton = customButtonList.getJSONObject(i);
				customButtonMap.put(customButton.getString("name"), customButton.getString("value"));
			}
			for(int i = 0; i < customButtonArray.size(); i++) {
				JSONObject customButton = customButtonArray.getJSONObject(i);
				String value = customButtonMap.get(customButton.getString("name"));
				if(StringUtils.isNotBlank(value)) {
					customButton.put("value", value);
				}
			}
		}
		resultObj.put("customButtonList", customButtonArray);
		
		/** 通知 **/
		JSONObject notifyPolicyObj = new JSONObject();
		JSONObject notifyPolicyConfig = configObj.getJSONObject("notifyPolicyConfig");
		if(MapUtils.isNotEmpty(notifyPolicyConfig)) {
			notifyPolicyObj.putAll(notifyPolicyConfig);
		}
		resultObj.put("notifyPolicyConfig", notifyPolicyObj);
		
		return resultObj;
	}
}