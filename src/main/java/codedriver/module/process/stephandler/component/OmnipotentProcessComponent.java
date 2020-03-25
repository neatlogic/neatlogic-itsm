package codedriver.module.process.stephandler.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.attribute.core.AttributeHandlerFactory;
import codedriver.framework.attribute.core.IAttributeHandler;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessStepMode;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
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

		List<ProcessTaskStepWorkerPolicyVo> workerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicyByProcessTaskStepId(currentProcessTaskStepVo.getId());
		if (CollectionUtils.isEmpty(workerPolicyList)) {
			return 1;
		}
		for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : workerPolicyList) {
			IWorkerPolicyHandler workerPolicyHandler = WorkerPolicyHandlerFactory.getHandler(workerPolicyVo.getPolicy());
			if (workerPolicyHandler == null) {
				continue;
			}
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

		String autoStart = workerPolicyConfig.getString("autoStart");
		if ("1".equals(autoStart) && workerList.size() == 1) {
			/** 设置当前步骤状态为处理中 **/
			if (StringUtils.isNotBlank(workerList.get(0).getUserId())) {
				ProcessTaskStepUserVo userVo = new ProcessTaskStepUserVo();
				userVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
				userVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
				userVo.setUserId(workerList.get(0).getUserId());
				UserVo user = userMapper.getUserByUserId(workerList.get(0).getUserId());
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
		JSONObject paramObj = currentProcessTaskStepVo.getParamObj();

		/** 写入当前步骤的表单属性值 **/
		ProcessTaskStepFormAttributeVo formAttributeVo = new ProcessTaskStepFormAttributeVo();
		formAttributeVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
		List<ProcessTaskStepFormAttributeVo> formAttributeList = processTaskMapper.getProcessTaskStepFormAttributeByStepId(formAttributeVo);
		currentProcessTaskStepVo.setFormAttributeList(formAttributeList);
		if (formAttributeList != null && formAttributeList.size() > 0) {
			JSONArray attributeObjList = null;
			if (paramObj != null && paramObj.containsKey("formAttributeValueList") && paramObj.get("formAttributeValueList") instanceof JSONArray) {
				attributeObjList = paramObj.getJSONArray("formAttributeValueList");
			}
			for (ProcessTaskStepFormAttributeVo attribute : formAttributeList) {
				if (attribute.getIsEditable().equals(1)) {
					if (attributeObjList != null && attributeObjList.size() > 0) {
						for (int i = 0; i < attributeObjList.size(); i++) {
							JSONObject attrObj = attributeObjList.getJSONObject(i);
							if (attrObj.getString("uuid").equals(attribute.getAttributeUuid())) {
								ProcessTaskFormAttributeDataVo attributeData = new ProcessTaskFormAttributeDataVo();
								attributeData.setData(attrObj.getString("data"));
								attributeData.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
								attributeData.setAttributeUuid(attribute.getAttributeUuid());
								processTaskMapper.replaceProcessTaskFormAttributeData(attributeData);
								// 放进去方便基类记录日志
								attribute.setAttributeData(attributeData);

								break;
							}
						}
					}
					IAttributeHandler attributeHandler = AttributeHandlerFactory.getHandler(attribute.getHandler());
					if (attributeHandler != null) {
						try {
							attributeHandler.valid(attribute.getAttributeData(), attribute.getConfigObj());
						} catch (Exception ex) {
							throw new ProcessTaskRuntimeException(ex);
						}
					}
				}
			}
		}

		/** 保存内容 **/
		if (paramObj != null && paramObj.containsKey("content") && StringUtils.isNotBlank(paramObj.getString("content"))) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(paramObj.getString("content"));
			processTaskMapper.replaceProcessTaskContent(contentVo);
			processTaskMapper.replaceProcessTaskStepContent(new ProcessTaskStepContentVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), contentVo.getHash()));
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
		DataValid.formAttributeDataValid(currentProcessTaskStepVo);
		return 0;
	}

	@Override
	protected int myRetreat(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 0;
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
			if (StringUtils.isNotBlank(workerList.get(0).getUserId())) {
				ProcessTaskStepUserVo userVo = new ProcessTaskStepUserVo();
				userVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
				userVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
				userVo.setUserId(workerList.get(0).getUserId());
				UserVo user = userMapper.getUserByUserId(workerList.get(0).getUserId());
				userVo.setUserName(user.getUserName());
				userList.add(userVo);
				currentProcessTaskStepVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
			}
		}
		return 1;
	}

	@Override
	public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj) {
		/** 组装通知模板 **/
		JSONArray notifyList = stepConfigObj.getJSONArray("notifyList");
		if (!CollectionUtils.isEmpty(notifyList)) {
			List<String> templateUuidList = new ArrayList<>();
			for (int j = 0; j < notifyList.size(); j++) {
				JSONObject notifyObj = notifyList.getJSONObject(j);
				String template = notifyObj.getString("template");
				if (StringUtils.isNotBlank(template)) {
					templateUuidList.add(template);
				}
			}
			processStepVo.setTemplateUuidList(templateUuidList);
		}
		/** 组装分配策略 **/
		JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
		if (!MapUtils.isEmpty(workerPolicyConfig)) {
			JSONArray policyList = workerPolicyConfig.getJSONArray("policyList");
			if (!CollectionUtils.isEmpty(policyList)) {
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
	protected int mySaveDraft(ProcessTaskStepVo processTaskStepVo) throws ProcessTaskException {
		return 0;
	}

}