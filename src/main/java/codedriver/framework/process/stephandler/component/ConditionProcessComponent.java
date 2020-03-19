package codedriver.framework.process.stephandler.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.module.process.constvalue.ProcessStepHandler;
import codedriver.module.process.constvalue.ProcessStepMode;
import codedriver.module.process.dto.ProcessStepVo;
import codedriver.module.process.dto.ProcessTaskStepRelVo;
import codedriver.module.process.dto.ProcessTaskStepUserVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;
import codedriver.module.process.dto.RelExpressionVo;

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

	@Override
	public String getIcon() {
		return "ts-shunt";
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

	private static boolean runScriptOld(Long flowJobId, String expression) throws ScriptException, NoSuchMethodException {
		Pattern pattern = null;
		Matcher matcher = null;
		StringBuffer temp = new StringBuffer();
		String regex = "\\$\\{([^}]+)\\}";
		pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(expression);
		List<String> stepAndKeyList = new ArrayList<String>();
		while (matcher.find()) {
			matcher.appendReplacement(temp, "map[\"" + matcher.group(1) + "\"]");
			stepAndKeyList.add(matcher.group(1));
		}
		matcher.appendTail(temp);

		StringBuilder script = new StringBuilder();
		script.append("function run(){");
		script.append("var map = new Object();");

		if (stepAndKeyList.size() > 0) {
			for (String stepAndKey : stepAndKeyList) {
				if (stepAndKey.indexOf(".") > -1 && stepAndKey.split("\\.").length == 2) {
					String stepUid = stepAndKey.split("\\.")[0];
					String key = stepAndKey.split("\\.")[1];
					List<String> valueList = new ArrayList<>();// flowJobMapper.getFlowJobStepNodeParamValueByFlowJobIdUidKey(flowJobId,
																// stepUid,
																// key);
					if (valueList.size() > 0) {
						if (valueList.size() > 1) {
							script.append("map[\"" + stepUid + "." + key + "\"] = [");
							for (int i = 0; i < valueList.size(); i++) {
								String value = valueList.get(i);
								script.append("\"" + value + "\"");
								if (i < valueList.size() - 1) {
									script.append(",");
								}
							}
							script.append("];");
						} else {
							script.append("map[\"" + stepUid + "." + key + "\"] = \"" + valueList.get(0) + "\";");
						}
					}
				}
			}
		}
		script.append("return " + temp.toString() + ";");
		script.append("}");
		ScriptEngineManager sem = new ScriptEngineManager();
		ScriptEngine se = sem.getEngineByName("nashorn");
		se.eval(script.toString());
		Invocable invocableEngine = (Invocable) se;
		Object callbackvalue = invocableEngine.invokeFunction("run");
		return Boolean.parseBoolean(callbackvalue.toString());
	}

	private static boolean runScript(Long flowJobId, String expression) throws ScriptException, NoSuchMethodException {
		Pattern pattern = null;
		Matcher matcher = null;
		StringBuffer temp = new StringBuffer();
		String regex = "\\$\\{([^}]+)\\}";
		pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(expression);
		List<String> stepAndKeyList = new ArrayList<String>();
		while (matcher.find()) {
			String key = matcher.group(1);
			String[] keys = key.split("\\.");
			String newkey = "";
			for (String k : keys) {
				newkey += "[\"" + k + "\"]";
			}
			matcher.appendReplacement(temp, "json" + newkey);
			stepAndKeyList.add(matcher.group(1));
		}
		matcher.appendTail(temp);

		StringBuilder script = new StringBuilder();
		script.append("function run(){");
		script.append("return " + temp + ";\n");
		script.append("}");

		JSONObject jsonObj = new JSONObject();
		if (stepAndKeyList.size() > 0) {
			for (String stepAndKey : stepAndKeyList) {
				if (stepAndKey.indexOf(".") > -1 && stepAndKey.split("\\.").length == 2) {
					String stepUid = stepAndKey.split("\\.")[0];
					String key = stepAndKey.split("\\.")[1];
					List<String> valueList = new ArrayList<>();// flowJobMapper.getFlowJobStepNodeParamValueByFlowJobIdUidKey(flowJobId,
																// stepUid,
																// key);
					JSONObject valueObj = new JSONObject();
					if (valueList.size() > 0) {
						if (valueList.size() > 1) {
							valueObj.put(key, valueList);
						} else {
							valueObj.put(key, valueList.get(0));
						}
					}
					if (!valueObj.isEmpty()) {
						if (!jsonObj.containsKey(stepUid)) {
							jsonObj.put(stepUid, valueObj);
						} else {
							JSONObject tmpV = jsonObj.getJSONObject(stepUid);
							// tmpV.accumulate(key, valueObj.get(key));
							jsonObj.put(stepUid, tmpV);
						}
					}
				}
			}
		}
		ScriptEngineManager sem = new ScriptEngineManager();
		ScriptEngine se = sem.getEngineByName("nashorn");
		se.put("json", jsonObj);
		se.eval(script.toString());
		Invocable invocableEngine = (Invocable) se;
		Object callbackvalue = invocableEngine.invokeFunction("run");
		return Boolean.parseBoolean(callbackvalue.toString());
	}

	@Override
	protected List<ProcessTaskStepVo> myGetNext(ProcessTaskStepVo currentProcessTaskStepVo) {
		List<ProcessTaskStepVo> nextStepList = new ArrayList<ProcessTaskStepVo>();
//		if (currentProcessTaskStepVo.getRelList() != null && currentProcessTaskStepVo.getRelList().size() > 0) {
//			for (ProcessTaskStepRelVo relVo : currentProcessTaskStepVo.getRelList()) {
//				if (relVo.getCondition() != null) {
//					Boolean result = false;
//					try {
//						result = runScript(currentProcessTaskStepVo.getProcessTaskId(), relVo.getCondition());
//					} catch (NoSuchMethodException e) {
//						logger.error(e.getMessage(), e);
//					} catch (ScriptException e) {
//						logger.error(e.getMessage(), e);
//					}
//					if (result) {
//						nextStepList.add(new ProcessTaskStepVo() {
//							{
//								this.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
//								this.setId(relVo.getToProcessTaskStepId());
//								this.setHandler(relVo.getToProcessStepHandler());
//							}
//						});
//					}
//				}
//			}
//		}
		if(CollectionUtils.isEmpty(currentProcessTaskStepVo.getRelList())) {
			return nextStepList;
		}
		Map<String, ProcessTaskStepRelVo> toProcessStepUuidMap = new HashMap<>();
		for(ProcessTaskStepRelVo relVo : currentProcessTaskStepVo.getRelList()) {
			toProcessStepUuidMap.put(relVo.getToProcessStepUuid(), relVo);
		}
		if(CollectionUtils.isEmpty(currentProcessTaskStepVo.getRelList())) {
			return nextStepList;
		}
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
		String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
		System.out.println("stepConfig:" + stepConfig);
		//stepConfig:{"moveonConfigList":[{"targetStepList":["YfsOrBMvn0ZoU9gPuNFBL4xV36FgQ5dZ"],"conditionGroupList":[],"type":"always","conditionGroupRelList":[]},{"targetStepList":["oTIzMKifba6oHQJeDZum4pbRfs6EzgF9"],"conditionGroupList":[],"type":"negative","conditionGroupRelList":[]},{"targetStepList":["X4MKW27ND367chRKCURfojLJAz38JY1O","XyJUPc3GJWjoBO06wd9OpA5xkkgrPRsE"],"conditionGroupList":[{"conditionList":[{"expression":"include","valueList":["user#chenqw","user#lvzk"],"uuid":"de6918c535504518b3d070939583d6a8","key":"common.owner"},{"expression":"like","valueList":"114","uuid":"ab8d46c3850d45babc545bcf356b3b04","key":"common.id"},{"expression":"include","valueList":["41ba513c4433462cb17a2974d9f09eeb"],"uuid":"0a8664b12ee741e5ad97504b45f26ef0","key":"common.priority"},{"expression":"exclude","valueList":["running"],"uuid":"b4e6f9ebe14c4ea09a82c0dff94c8c3f","key":"common.status"},{"expression":"between","valueList":1584547200000,"uuid":"bf8ba3d8ff75491a96d3e268e04738d9","key":"common.startTime"}],"conditionRelList":[{"joinType":"and","to":"ab8d46c3850d45babc545bcf356b3b04"},{"joinType":"or","from":"ab8d46c3850d45babc545bcf356b3b04","to":"0a8664b12ee741e5ad97504b45f26ef0"},{"joinType":"and","from":"0a8664b12ee741e5ad97504b45f26ef0","to":"b4e6f9ebe14c4ea09a82c0dff94c8c3f"},{"joinType":"and","from":"b4e6f9ebe14c4ea09a82c0dff94c8c3f","to":"bf8ba3d8ff75491a96d3e268e04738d9"}],"uuid":"2cea68abf04c4489acda4f63cb193203"}],"type":"optional","conditionGroupRelList":[]}]}
		if (StringUtils.isBlank(stepConfig)) {
			return nextStepList;
		}
		JSONObject stepConfigObj = null;
		try {
			stepConfigObj = JSONObject.parseObject(stepConfig);
			currentProcessTaskStepVo.setParamObj(stepConfigObj);
		} catch (Exception ex) {
			logger.error("条件步骤设置配置失败，" + ex.getMessage(), ex);
		}
		if (CollectionUtils.isEmpty(stepConfigObj)) {
			return nextStepList;
		}
		JSONArray moveonConfigList = stepConfigObj.getJSONArray("moveonConfigList");
		if(CollectionUtils.isEmpty(moveonConfigList)) {
			return nextStepList;
		}
		for(int i = 0; i < moveonConfigList.size(); i++) {
			JSONObject moveonConfig = moveonConfigList.getJSONObject(i);
			String type = moveonConfig.getString("type");
			if("always".equals(type)) {//直接流转
				List<String> targetStepList = JSON.parseArray(moveonConfig.getString("targetStepList"), String.class);
				if(CollectionUtils.isEmpty(targetStepList)) {
					continue;
				}
				for(String targetStep : targetStepList) {
					ProcessTaskStepRelVo relVo = toProcessStepUuidMap.get(targetStep);
					ProcessTaskStepVo toStep = new ProcessTaskStepVo();
					toStep.setProcessTaskId(relVo.getProcessTaskId());
					toStep.setId(relVo.getToProcessTaskStepId());
					toStep.setHandler(relVo.getToProcessStepHandler());
					nextStepList.add(toStep);
				}
			}else if("negative".equals(type)) {//不流转
				continue;
			}else if("optional".equals(type)) {//自定义
				
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
						stepAndKeyList.add(matcher.group(1));
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
		return 0;
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

}
