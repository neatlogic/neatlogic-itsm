package codedriver.module.process.stephandler.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.ConditionParamContext;
import codedriver.framework.dto.condition.ConditionConfigVo;
import codedriver.framework.process.column.core.ProcessTaskUtil;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessStepMode;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessTaskStepRelVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.RelExpressionVo;
import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.framework.util.RunScriptUtil;

@Service
public class ConditionProcessComponent extends ProcessStepHandlerBase {
	static Logger logger = LoggerFactory.getLogger(ConditionProcessComponent.class);

	@Override
	public String getName() {
		return ProcessStepHandler.CONDITION.getName();
	}

	@Override
	public String getType() {
		return ProcessStepHandler.CONDITION.getType();
	}

	@SuppressWarnings("serial")
	@Override
	public JSONObject getChartConfig() {
		return new JSONObject() {
			{
				this.put("icon", "tsfont-question");
				this.put("shape", "L-triangle:R-triangle");
				this.put("width", 68);
				this.put("height", 68);
				this.put("rdy", 68 / 4);
			}
		};
	}

	@Override
	public ProcessStepMode getMode() {
		return ProcessStepMode.AT;
	}

	@Override
	public boolean isAsync() {
		return true;
	}

	@Override
	public String getHandler() {
		return ProcessStepHandler.CONDITION.getHandler();
	}

	@Override
	protected int myActive(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myHandle(ProcessTaskStepVo currentProcessTaskStepVo) {
		/** 设置已完成标记位 **/
		currentProcessTaskStepVo.setIsAllDone(true);
		return 0;
	}

	@Override
	protected List<ProcessTaskStepVo> myGetNext(ProcessTaskStepVo currentProcessTaskStepVo) {
		List<ProcessTaskStepVo> nextStepList = new ArrayList<ProcessTaskStepVo>();
		if (CollectionUtils.isNotEmpty(currentProcessTaskStepVo.getRelList())) {
			Map<String, ProcessTaskStepRelVo> toProcessStepUuidMap = new HashMap<>();
			for (ProcessTaskStepRelVo relVo : currentProcessTaskStepVo.getRelList()) {
				toProcessStepUuidMap.put(relVo.getToProcessStepUuid(), relVo);
			}
			ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
			String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
			if (StringUtils.isNotBlank(stepConfig)) {
				JSONObject stepConfigObj = null;
				try {
					stepConfigObj = JSONObject.parseObject(stepConfig);
					currentProcessTaskStepVo.setParamObj(stepConfigObj);
				} catch (Exception ex) {
					logger.error("hash为" + processTaskStepVo.getConfigHash() + "的processtask_step_config内容不是合法的JSON格式", ex);
				}
				if (MapUtils.isNotEmpty(stepConfigObj)) {
					JSONArray moveonConfigList = stepConfigObj.getJSONArray("moveonConfigList");
					if (CollectionUtils.isNotEmpty(moveonConfigList)) {
						JSONArray ruleList = new JSONArray();
						Map<String, String> processStepNameMap = new HashMap<>();
						List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
						for(ProcessTaskStepVo processTaskStep : processTaskStepList) {
							processStepNameMap.put(processTaskStep.getProcessStepUuid(), processTaskStep.getName());
						}
						for (int i = 0; i < moveonConfigList.size(); i++) {
							JSONObject ruleObj = new JSONObject();
							JSONObject moveonConfig = moveonConfigList.getJSONObject(i);
							List<String> targetStepNameList = new ArrayList<>();
							List<String> targetStepList = JSON.parseArray(JSON.toJSONString(moveonConfig.getJSONArray("targetStepList")), String.class);
							for(String targetStepUuid : targetStepList) {
								targetStepNameList.add(processStepNameMap.get(targetStepUuid));
							}
							String type = moveonConfig.getString("type");
							Boolean canRun = false;
							if ("always".equals(type)) {// 直接流转
								canRun = true;
								ruleObj.putAll(moveonConfig);
							} else if ("optional".equals(type)) {// 自定义
								JSONArray conditionGroupList = moveonConfig.getJSONArray("conditionGroupList");
								if (CollectionUtils.isNotEmpty(conditionGroupList)) {
									ProcessTaskVo processTaskVo = ProcessTaskHandlerUtil.getProcessTaskDetailInfoById(currentProcessTaskStepVo.getProcessTaskId());
									JSONObject conditionParamData = ProcessTaskUtil.getProcessFieldData(processTaskVo, true);
//									JSONObject conditionParamTextData = ProcessTaskUtil.getProcessFieldData(processTaskVo, false);
//									JSONObject conditionParamNameData = ProcessTaskUtil.getConditionParamNameData(processTaskVo.getFormConfig());
									try {
										ConditionParamContext.init(conditionParamData).setFormConfig(processTaskVo.getFormConfig());;
										System.out.println(moveonConfig.toJSONString());
										ConditionConfigVo conditionConfigVo = new ConditionConfigVo(moveonConfig);
										String script = conditionConfigVo.buildScript();
										// ((false || true) || (true && false) || (true || false))
										System.out.println(JSON.toJSONString(conditionConfigVo));
										canRun = RunScriptUtil.runScript(script);
										ruleObj.putAll(JSON.parseObject(JSON.toJSONString(conditionConfigVo)));
									} catch (Exception e) {
										logger.error(e.getMessage(), e);
									} finally {
										ConditionParamContext.get().release();
									}
								}
							}else {
								ruleObj.putAll(moveonConfig);
							}
							ruleObj.put("type", type);
							ruleObj.put("result", canRun);
							ruleObj.put("targetStepList", targetStepNameList);
							ruleList.add(ruleObj);
							// 符合条件
							if (canRun) {
								if (CollectionUtils.isNotEmpty(targetStepList)) {
									for (String targetStep : targetStepList) {
										ProcessTaskStepRelVo relVo = toProcessStepUuidMap.get(targetStep);
										ProcessTaskStepVo toStep = new ProcessTaskStepVo();
										toStep.setProcessTaskId(relVo.getProcessTaskId());
										toStep.setId(relVo.getToProcessTaskStepId());
										toStep.setHandler(relVo.getToProcessStepHandler());
										nextStepList.add(toStep);
									}
								}
							}
						}
						currentProcessTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.RULE.getParamName(), ruleList);
					}
				}
			}
		}

		return nextStepList;
	}

	@Override
	protected int myComplete(ProcessTaskStepVo currentFlowJobStepVo) {
		return 1;
	}

	public void makeupFlowJobStepVo(ProcessTaskStepVo flowJobStepVo) {
		if (flowJobStepVo.getRelList() != null && flowJobStepVo.getRelList().size() > 0) {
			for (ProcessTaskStepRelVo relVo : flowJobStepVo.getRelList()) {
				if (!StringUtils.isBlank(relVo.getCondition())) {
					Pattern pattern = null;
					Matcher matcher = null;
					StringBuffer temp = new StringBuffer();
					String regex = "\\$\\{([^}]+)\\}";
					pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
					matcher = pattern.matcher(relVo.getCondition());
					List<String> stepAndKeyList = new ArrayList<String>();
					while (matcher.find()) {
						matcher.appendReplacement(temp, "map[\"" + matcher.group(1) + "\"]");
						if (!stepAndKeyList.contains(matcher.group(1))) {
							stepAndKeyList.add(matcher.group(1));
						}
					}
					matcher.appendTail(temp);

					StringBuilder script = new StringBuilder();
					script.append("function run(){");
					script.append("var map = new Object();");

					if (stepAndKeyList.size() > 0) {
						List<RelExpressionVo> relExpressionList = new ArrayList<>();
						for (String stepAndKey : stepAndKeyList) {
							if (stepAndKey.indexOf(".") > -1 && stepAndKey.split("\\.").length == 2) {
								String stepUid = stepAndKey.split("\\.")[0];
								String key = stepAndKey.split("\\.")[1];
								RelExpressionVo relExpressionVo = new RelExpressionVo();
								relExpressionVo.setExpression("${" + stepUid + "." + key + "}");
								List<String> valueList = new ArrayList<>(); // flowJobMapper.getFlowJobStepNodeParamValueByFlowJobIdUidKey(flowJobStepVo.getFlowJobId(),
																			// stepUid,
																			// key);
								if (valueList.size() > 0) {
									if (valueList.size() > 1) {
										script.append("map[\"" + stepUid + "." + key + "\"] = [");
										String v = "[";
										for (int i = 0; i < valueList.size(); i++) {
											String value = valueList.get(i);
											script.append("\"" + value + "\"");
											v += "\"" + value + "\"";
											if (i < valueList.size() - 1) {
												script.append(",");
												v += ",";
											}
										}
										v += "]";
										script.append("];");
										relExpressionVo.setValue(v);
									} else {
										script.append("map[\"" + stepUid + "." + key + "\"] = \"" + valueList.get(0) + "\";");
										relExpressionVo.setValue("\"" + valueList.get(0) + "\"");
									}
								}
								relExpressionList.add(relExpressionVo);
							}
						}
						relVo.setRelExpressionList(relExpressionList);
					}
					script.append("return " + temp.toString() + ";");
					script.append("}");
					ScriptEngineManager sem = new ScriptEngineManager();
					ScriptEngine se = sem.getEngineByName("nashorn");
					try {
						se.eval(script.toString());
						Invocable invocableEngine = (Invocable) se;
						Object callbackvalue = invocableEngine.invokeFunction("run");
						relVo.setScriptResult(callbackvalue.toString());
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						relVo.setError(ex.getMessage());
					}
				}
			}
		}
	}

	@Override
	public int getSort() {
		return 2;
	}

	@Override
	public Boolean isAllowStart() {
		return null;
	}

	@Override
	protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myHang(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myStart(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
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
	protected int myStartProcess(ProcessTaskStepVo processTaskStepVo) throws ProcessTaskException {
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

	@Override
	public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj) {
		// TODO Auto-generated method stub

	}

	@Override
	protected int mySaveDraft(ProcessTaskStepVo processTaskStepVo) throws ProcessTaskException {
		return 0;
	}

	@Override
	public void updateProcessTaskStepUserAndWorker(List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject makeupConfig(JSONObject configObj) {
		// TODO Auto-generated method stub
		return null;
	}
}
