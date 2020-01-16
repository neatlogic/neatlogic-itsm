package codedriver.framework.process.stephandler.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.attribute.core.AttributeHandlerFactory;
import codedriver.framework.attribute.core.IAttributeHandler;
import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.framework.process.workerpolicy.handler.IWorkerPolicyHandler;
import codedriver.framework.process.workerpolicy.handler.WorkerPolicyHandlerFactory;
import codedriver.module.process.constvalue.ProcessStepHandler;
import codedriver.module.process.constvalue.ProcessStepMode;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.constvalue.ProcessTaskStatus;
import codedriver.module.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.module.process.dto.ProcessTaskContentVo;
import codedriver.module.process.dto.ProcessTaskStepContentVo;
import codedriver.module.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepUserVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;
import codedriver.module.process.dto.ProcessTaskVo;

@Service
public class OmnipotentProcessComponent extends ProcessStepHandlerBase {
	static Logger logger = LoggerFactory.getLogger(OmnipotentProcessComponent.class);

	@Override
	public String getType() {
		return ProcessStepHandler.OMNIPOTENT.getType();
	}

	@Override
	public ProcessStepMode getMode() {
		return ProcessStepMode.MT;
	}

	@Override
	public String getIcon() {
		return "ts-round-s";
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
		/** 如果是开始节点，则处理人为上报人或代报人 **/
		if (processTaskStepVo.getType().equals(ProcessStepType.START.getValue())) {
			ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(currentProcessTaskStepVo.getProcessTaskId());
			workerList.add(new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), processTaskVo.getReporter()));
			userList.add(new ProcessTaskStepUserVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), processTaskVo.getReporter()));
		} else if (processTaskStepVo.getType().equals(ProcessStepType.PROCESS.getValue())) {
			JSONObject stepConfigObj = null;
			if (StringUtils.isNotBlank(stepConfig)) {
				try {
					stepConfigObj = JSONObject.parseObject(stepConfig);
					currentProcessTaskStepVo.setParamObj(stepConfigObj);
				} catch (Exception ex) {
					logger.error("转换步骤设置配置失败，" + ex.getMessage(), ex);
				}
			}
			if (stepConfigObj != null) {
				if (stepConfigObj.containsKey("assignPolicy")) {
					/** 顺序分配处理人 **/
					if ("serial".equals(stepConfigObj.getString("assignPolicy"))) {
						List<ProcessTaskStepWorkerPolicyVo> workerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicyByProcessTaskStepId(currentProcessTaskStepVo.getId());
						if (workerPolicyList != null && workerPolicyList.size() > 0) {
							for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : workerPolicyList) {
								IWorkerPolicyHandler workerPolicyHandler = WorkerPolicyHandlerFactory.getHandler(workerPolicyVo.getPolicy());
								if (workerPolicyHandler != null) {
									workerList = workerPolicyHandler.execute(workerPolicyVo, currentProcessTaskStepVo);
									if (workerList.size() > 0) {
										// 找到处理人，则退出
										break;
									}
								}
							}
						}
					} else if ("parallel".equals(stepConfigObj.getString("assignPolicy"))) {
						List<ProcessTaskStepWorkerPolicyVo> workerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicyByProcessTaskStepId(currentProcessTaskStepVo.getId());
						if (workerPolicyList != null && workerPolicyList.size() > 0) {
							for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : workerPolicyList) {
								IWorkerPolicyHandler workerPolicyHandler = WorkerPolicyHandlerFactory.getHandler(workerPolicyVo.getPolicy());
								if (workerPolicyHandler != null) {
									List<ProcessTaskStepWorkerVo> tmpWorkerList = workerPolicyHandler.execute(workerPolicyVo, currentProcessTaskStepVo);
									// 去重取并集
									tmpWorkerList.removeAll(workerList);
									workerList.addAll(tmpWorkerList);
								}
							}
						}
					}
				}

				if (stepConfigObj.containsKey("isAutoStart") && stepConfigObj.getString("isAutoStart").equals("1") && workerList.size() == 1) {
					/** 设置当前步骤状态为处理中 **/
					if (StringUtils.isNotBlank(workerList.get(0).getUserId())) {
						ProcessTaskStepUserVo userVo = new ProcessTaskStepUserVo();
						userVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
						userVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
						userVo.setUserId(workerList.get(0).getUserId());
						userList.add(userVo);
						currentProcessTaskStepVo.setStatus(ProcessTaskStatus.RUNNING.getValue());
					}
				}
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
			processTaskMapper.insertProcessTaskStepContent(new ProcessTaskStepContentVo(currentProcessTaskStepVo.getId(), contentVo.getHash()));
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
		return 0;
	}

}