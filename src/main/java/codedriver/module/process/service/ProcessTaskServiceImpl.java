package codedriver.module.process.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.common.constvalue.TeamLevel;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.matrix.exception.MatrixExternalException;
import codedriver.framework.process.column.core.ProcessTaskUtil;
import codedriver.framework.process.constvalue.FormAttributeAction;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessStepMode;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskGroupSearch;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.constvalue.WorkerPolicy;
import codedriver.framework.process.constvalue.automatic.CallbackType;
import codedriver.framework.process.constvalue.automatic.FailPolicy;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTagVo;
import codedriver.framework.process.dto.ProcessTaskConfigVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskSlaTimeVo;
import codedriver.framework.process.dto.ProcessTaskSlaVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepFileVo;
import codedriver.framework.process.dto.ProcessTaskStepRemindVo;
import codedriver.framework.process.dto.ProcessTaskStepReplyVo;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskTagVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.automatic.AutomaticConfigVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.integration.handler.ProcessRequestFrom;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.process.stepremind.core.ProcessTaskStepRemindTypeFactory;
import codedriver.framework.scheduler.core.IJob;
import codedriver.framework.scheduler.core.SchedulerManager;
import codedriver.framework.scheduler.dto.JobObject;
import codedriver.framework.scheduler.exception.ScheduleHandlerNotFoundException;
import codedriver.framework.util.ConditionUtil;
import codedriver.framework.util.FreemarkerUtil;
import codedriver.framework.util.TimeUtil;
import codedriver.module.process.schedule.plugin.ProcessTaskAutomaticJob;

@Service
public class ProcessTaskServiceImpl implements ProcessTaskService {

	private final static Logger logger = LoggerFactory.getLogger(ProcessTaskServiceImpl.class);

	private Pattern pattern_html = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private TeamMapper teamMapper;

	@Autowired
	private FileMapper fileMapper;

	@Autowired
	private IntegrationMapper integrationMapper;

	@Autowired
	private PriorityMapper priorityMapper;

	@Autowired
	private ChannelMapper channelMapper;

	@Autowired
	private WorktimeMapper worktimeMapper;

	@Autowired
	ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Autowired
    private CatalogMapper catalogMapper;

    @Autowired
    private SelectContentByHashMapper selectContentByHashMapper;

    @Autowired
    private ProcessMapper processMapper;

	@Override
	public void setProcessTaskFormAttributeAction(ProcessTaskVo processTaskVo, Map<String, String> formAttributeActionMap, int mode) {
		Map<String, Object> formAttributeDataMap = processTaskVo.getFormAttributeDataMap();
		if(formAttributeDataMap == null) {
			formAttributeDataMap = new HashMap<>();
		}
		String formConfig = processTaskVo.getFormConfig();
		if(StringUtils.isNotBlank(formConfig)) {
			try {
				JSONObject formConfigObj = JSON.parseObject(formConfig);
				if(MapUtils.isNotEmpty(formConfigObj)) {
					JSONArray controllerList = formConfigObj.getJSONArray("controllerList");
					if(CollectionUtils.isNotEmpty(controllerList)) {
						List<String> currentUserProcessUserTypeList = new ArrayList<>();
						List<String> currentUserTeamList = new ArrayList<>();
						if(mode == 0) {
							currentUserProcessUserTypeList.add(UserType.ALL.getValue());
							if(UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner())) {
								currentUserProcessUserTypeList.add(ProcessUserType.OWNER.getValue());
							}
							if(UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
								currentUserProcessUserTypeList.add(ProcessUserType.REPORTER.getValue());
							}
							currentUserTeamList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
						}else if(mode == 1){
							if(formAttributeActionMap == null) {
								formAttributeActionMap = new HashMap<>();
							}
						}

						for(int i = 0; i < controllerList.size(); i++) {
							JSONObject attributeObj = controllerList.getJSONObject(i);
							String action = FormAttributeAction.HIDE.getValue();
							JSONObject config = attributeObj.getJSONObject("config");
							if(mode == 0) {
								if(MapUtils.isNotEmpty(config)) {
									List<String> authorityList = JSON.parseArray(config.getString("authorityConfig"), String.class);
									if(CollectionUtils.isNotEmpty(authorityList)) {
										for(String authority : authorityList) {
											String[] split = authority.split("#");
											if(GroupSearch.COMMON.getValue().equals(split[0])) {
												if(currentUserProcessUserTypeList.contains(split[1])) {
													action = FormAttributeAction.READ.getValue();
													break;
												}
											}else if(ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue().equals(split[0])) {
												if(currentUserProcessUserTypeList.contains(split[1])) {
													action = FormAttributeAction.READ.getValue();
													break;
												}
											}else if(GroupSearch.USER.getValue().equals(split[0])) {
												if(UserContext.get().getUserUuid(true).equals(split[1])) {
													action = FormAttributeAction.READ.getValue();
													break;
												}
											}else if(GroupSearch.TEAM.getValue().equals(split[0])) {
												if(currentUserTeamList.contains(split[1])) {
													action = FormAttributeAction.READ.getValue();
													break;
												}
											}else if(GroupSearch.ROLE.getValue().equals(split[0])) {
												if(UserContext.get().getRoleUuidList().contains(split[1])) {
													action = FormAttributeAction.READ.getValue();
													break;
												}
											}
										}
									}
								}
							}else if(mode == 1){
								action = formAttributeActionMap.get(attributeObj.getString("uuid"));
								if(StringUtils.isBlank(action)) {
								    action = formAttributeActionMap.get("all");
								}
							}
							if(FormAttributeAction.READ.getValue().equals(action)) {
								attributeObj.put("isReadonly", true);
							}else if(FormAttributeAction.HIDE.getValue().equals(action)) {
								attributeObj.put("isHide", true);
								formAttributeDataMap.remove(attributeObj.getString("uuid"));//对于隐藏属性，不返回值
								if(config != null) {
									config.remove("value");
									config.remove("defaultValueList");//对于隐藏属性，不返回默认值
								}
							}
						}
						processTaskVo.setFormConfig(formConfigObj.toJSONString());
					}
				}
			}catch(Exception ex) {
				logger.error("表单配置不是合法的JSON格式", ex);
			}
		}

	}

	@Override
	public void parseProcessTaskStepReply(ProcessTaskStepReplyVo processTaskStepReplyVo) {
		if(StringUtils.isBlank(processTaskStepReplyVo.getContent()) && StringUtils.isNotBlank(processTaskStepReplyVo.getContentHash())) {
		    processTaskStepReplyVo.setContent(selectContentByHashMapper.getProcessTaskContentStringByHash(processTaskStepReplyVo.getContentHash()));
		}
		List<Long> fileIdList = processTaskMapper.getFileIdListByContentId(processTaskStepReplyVo.getId());
		if(CollectionUtils.isNotEmpty(fileIdList)) {
            processTaskStepReplyVo.setFileIdList(fileIdList);
            processTaskStepReplyVo.setFileList(fileMapper.getFileListByIdList(fileIdList));
        }
		if(StringUtils.isNotBlank(processTaskStepReplyVo.getLcu())) {
			UserVo user = userMapper.getUserBaseInfoByUuid(processTaskStepReplyVo.getLcu());
			if(user != null) {
			    processTaskStepReplyVo.setLcuName(user.getUserName());
			    processTaskStepReplyVo.setLcuInfo(user.getUserInfo());
			    processTaskStepReplyVo.setLcuVipLevel(user.getVipLevel());
			}
		}
		UserVo user = userMapper.getUserBaseInfoByUuid(processTaskStepReplyVo.getFcu());
		if(user != null) {
		    processTaskStepReplyVo.setFcuName(user.getUserName());
		    processTaskStepReplyVo.setFcuInfo(user.getUserInfo());
		    processTaskStepReplyVo.setFcuVipLevel(user.getVipLevel());
		}
	}

	@Override
	public Boolean runRequest(AutomaticConfigVo automaticConfigVo,ProcessTaskStepVo currentProcessTaskStepVo) {
		IntegrationResultVo resultVo = null;
		Boolean isUnloadJob = false;
		ProcessTaskStepDataVo auditDataVo = processTaskStepDataMapper.getProcessTaskStepData(new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(),currentProcessTaskStepVo.getId(),ProcessTaskStepDataType.AUTOMATIC.getValue(),SystemUser.SYSTEM.getUserId()));
		JSONObject data = auditDataVo.getData();
		String integrationUuid = automaticConfigVo.getBaseIntegrationUuid();
		JSONObject successConfig = automaticConfigVo.getBaseSuccessConfig();
		String template = automaticConfigVo.getBaseResultTemplate();
		JSONObject failConfig = null;
		JSONObject audit = data.getJSONObject("requestAudit");
		String resultJson = null;
		if(!automaticConfigVo.getIsRequest()) {
			audit = data.getJSONObject("callbackAudit");
			template = automaticConfigVo.getCallbackResultTemplate();
			integrationUuid =automaticConfigVo.getCallbackIntegrationUuid();
			successConfig = automaticConfigVo.getCallbackSuccessConfig();
			failConfig = automaticConfigVo.getCallbackFailConfig();
		}
		audit.put("startTime", System.currentTimeMillis());
		JSONObject auditResult = new JSONObject();
		audit.put("result", auditResult);
		IProcessStepHandler processHandler = ProcessStepHandlerFactory.getHandler(currentProcessTaskStepVo.getHandler());
		try {
			IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(integrationUuid);
			audit.put("integrationName", integrationVo.getName());
			IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
			if (handler == null) {
				throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
			}
	    	integrationVo.getParamObj().putAll(getIntegrationParam(automaticConfigVo,currentProcessTaskStepVo));
			resultVo = handler.sendRequest(integrationVo,ProcessRequestFrom.PROCESS);
			resultJson = resultVo.getTransformedResult();
			if(StringUtils.isBlank(resultVo.getTransformedResult())) {
				resultJson = resultVo.getRawResult();
			}
			audit.put("endTime", System.currentTimeMillis());
			auditResult.put("json", resultJson);
			auditResult.put("template", FreemarkerUtil.transform(JSONObject.parse(resultVo.getTransformedResult()), template));
			if(StringUtils.isNotBlank(resultVo.getError())) {
				logger.error(resultVo.getError());
	    		throw new MatrixExternalException("外部接口访问异常");
	    	}else if(StringUtils.isNotBlank(resultJson)) {
				if(predicate(successConfig,resultVo,true)) {//如果执行成功
					audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.SUCCEED.getValue()));
					if(automaticConfigVo.getIsRequest()&&!automaticConfigVo.getIsHasCallback()||!automaticConfigVo.getIsRequest()) {//第一次请求
						processHandler.complete(currentProcessTaskStepVo);
					}else {//回调请求
						if(CallbackType.WAIT.getValue().equals(automaticConfigVo.getCallbackType())) {
							//等待回调,挂起
							//processHandler.hang(currentProcessTaskStepVo);
						}
						if(CallbackType.INTERVAL.getValue().equals(automaticConfigVo.getCallbackType())) {
							automaticConfigVo.setIsRequest(false);
							automaticConfigVo.setResultJson(JSONObject.parseObject(resultJson));
							data = initProcessTaskStepData(currentProcessTaskStepVo,automaticConfigVo,data,"callback");
							initJob(automaticConfigVo,currentProcessTaskStepVo,data);
						}
					}
					isUnloadJob = true;
				}else if(automaticConfigVo.getIsRequest()||(!automaticConfigVo.getIsRequest()&&predicate(failConfig,resultVo,false))){//失败
					audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.FAILED.getValue()));
					audit.put("failedReason","");
					if(FailPolicy.BACK.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
						List<ProcessTaskStepVo> backStepList = getBackwardNextStepListByProcessTaskStepId(currentProcessTaskStepVo.getId());
						if(backStepList.size() == 1) {
							ProcessTaskStepVo nextProcessTaskStepVo = backStepList.get(0);
							if (processHandler != null) {
								JSONObject jsonParam = new JSONObject();
								jsonParam.put("action", ProcessTaskOperationType.BACK.getValue());
								jsonParam.put("nextStepId", nextProcessTaskStepVo.getId());
								currentProcessTaskStepVo.setParamObj(jsonParam);
								processHandler.complete(currentProcessTaskStepVo);
							}
						}else {//如果存在多个回退线，则挂起
							//processHandler.hang(currentProcessTaskStepVo);
						}
					}else if(FailPolicy.KEEP_ON.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
						processHandler.complete(currentProcessTaskStepVo);
					}else if(FailPolicy.CANCEL.getValue().equals(automaticConfigVo.getBaseFailPolicy())) {
						processHandler.abortProcessTask(new ProcessTaskVo(currentProcessTaskStepVo.getProcessTaskId()));
					}else {//hang
						//processHandler.hang(currentProcessTaskStepVo);
					}
					isUnloadJob = true;
				}else{
					audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.RUNNING.getValue()));
					//continue
				}
	    	}

		}catch(Exception ex) {
			logger.error(ex.getMessage(),ex);
			audit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.FAILED.getValue()));
			if(resultVo != null && StringUtils.isNotEmpty(resultVo.getError())) {
				audit.put("failedReason",resultVo.getError());
			}else {
				StringWriter sw = new StringWriter();
		        PrintWriter pw = new PrintWriter(sw);
		        ex.printStackTrace(pw);
				audit.put("failedReason",sw.toString());
			}
			//processHandler.hang(currentProcessTaskStepVo);
			isUnloadJob = true;
		}finally {
			auditDataVo.setData(data.toJSONString());
			auditDataVo.setFcu(SystemUser.SYSTEM.getUserId());
			processTaskStepDataMapper.replaceProcessTaskStepData(auditDataVo);
		}
		return isUnloadJob;
	}

	@Override
	public JSONObject initProcessTaskStepData(ProcessTaskStepVo currentProcessTaskStepVo,AutomaticConfigVo automaticConfigVo,JSONObject data,String type) {
		JSONObject failConfig = new JSONObject();
		JSONObject successConfig = new JSONObject();
		failConfig.put("default", "默认按状态码判断，4xx和5xx表示失败");
		successConfig.put("default", "默认按状态码判断，2xx和3xx表示成功");
		//init request
		if(type.equals("request")) {
			data = new JSONObject();
			JSONObject requestAudit = new JSONObject();
			data.put("requestAudit", requestAudit);
			requestAudit.put("integrationUuid", automaticConfigVo.getBaseIntegrationUuid());
			requestAudit.put("failPolicy", automaticConfigVo.getBaseFailPolicy());
			requestAudit.put("failPolicyName", FailPolicy.getText(automaticConfigVo.getBaseFailPolicy()));
			requestAudit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.PENDING.getValue()));
			requestAudit.put("successConfig", automaticConfigVo.getBaseSuccessConfig());
			if(automaticConfigVo.getBaseSuccessConfig() == null) {
				requestAudit.put("successConfig",successConfig);
			}
			ProcessTaskStepDataVo auditDataVo = new ProcessTaskStepDataVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), ProcessTaskStepDataType.AUTOMATIC.getValue(),SystemUser.SYSTEM.getUserId());
			auditDataVo.setData(data.toJSONString());
			processTaskStepDataMapper.replaceProcessTaskStepData(auditDataVo);
		}else {//init callback
			JSONObject callbackAudit = new JSONObject();
			callbackAudit.put("integrationUuid", automaticConfigVo.getCallbackIntegrationUuid());
			callbackAudit.put("failPolicy", automaticConfigVo.getBaseFailPolicy());
			callbackAudit.put("failPolicyName", FailPolicy.getText(automaticConfigVo.getBaseFailPolicy()));
			callbackAudit.put("type", automaticConfigVo.getCallbackType());
			callbackAudit.put("typeName", CallbackType.getText(automaticConfigVo.getCallbackType()));
			callbackAudit.put("interval", automaticConfigVo.getCallbackInterval());
			callbackAudit.put("status", ProcessTaskStatus.getJson(ProcessTaskStatus.PENDING.getValue()));
			callbackAudit.put("successConfig", automaticConfigVo.getCallbackSuccessConfig());
			if(automaticConfigVo.getCallbackFailConfig() == null) {
				callbackAudit.put("failConfig",failConfig);
			}
			if(automaticConfigVo.getCallbackSuccessConfig() == null) {
				callbackAudit.put("successConfig",successConfig);
			}
			data.put("callbackAudit", callbackAudit);
		}
		return data;
	}

	@Override
	public void initJob(AutomaticConfigVo automaticConfigVo,ProcessTaskStepVo currentProcessTaskStepVo,JSONObject data) {
		IJob jobHandler = SchedulerManager.getHandler(ProcessTaskAutomaticJob.class.getName());
		if (jobHandler != null) {
			JobObject.Builder jobObjectBuilder = new JobObject.Builder(
					currentProcessTaskStepVo.getProcessTaskId().toString()+"-"+currentProcessTaskStepVo.getId().toString(),
					jobHandler.getGroupName(), jobHandler.getClassName(), TenantContext.get().getTenantUuid()
					).addData("automaticConfigVo", automaticConfigVo)
					 .addData("data", data)
					 .addData("currentProcessTaskStepVo", currentProcessTaskStepVo);
			JobObject jobObject = jobObjectBuilder.build();
			jobHandler.reloadJob(jobObject);
		} else {
			throw new ScheduleHandlerNotFoundException(ProcessTaskAutomaticJob.class.getName());
		}
	}

	/**
	 * @Description: 判断条件是否成立
	 * @Param:
	 * @return: boolean
	 */
	private Boolean predicate(JSONObject config,IntegrationResultVo resultVo,Boolean isSuccess) {
		Boolean result = false;
		if(config==null||config.isEmpty()||!config.containsKey("expression")) {
			String patternStr = "(2|3).*";
			if(!isSuccess) {
				patternStr = "(4|5).*";
			}
			Pattern pattern = Pattern.compile(patternStr);
			if(pattern.matcher(String.valueOf(resultVo.getStatusCode())).matches()) {
				result = true;
			}
		}else {
			String name = config.getString("name");
			if(StringUtils.isNotBlank(name)) {
				String resultValue = null;
				String transformedResult = resultVo.getTransformedResult();
				if(StringUtils.isNotBlank(transformedResult)) {
					JSONObject transformedResultObj = JSON.parseObject(transformedResult);
					if(MapUtils.isNotEmpty(transformedResultObj)) {
						resultValue = transformedResultObj.getString(name);
					}
				}
				if(resultValue == null) {
					String rawResult = resultVo.getRawResult();
					if(StringUtils.isNotEmpty(rawResult)) {
						JSONObject rawResultObj = JSON.parseObject(rawResult);
						if(MapUtils.isNotEmpty(rawResultObj)) {
							resultValue = rawResultObj.getString(name);
						}
					}
				}
				if(resultValue != null) {
					List<String> curentValueList = new ArrayList<>();
					curentValueList.add(resultValue);
					String value = config.getString("value");
					List<String> targetValueList = new ArrayList<>();
					targetValueList.add(value);
					String expression = config.getString("expression");
					result = ConditionUtil.predicate(curentValueList, expression, targetValueList);
				}
			}
		}
		return result;
	}

	/**
	 * 拼装入参数
	 * @param automaticConfigVo
	 * @return
	 * @throws Exception
	 */
	private JSONObject getIntegrationParam(AutomaticConfigVo automaticConfigVo,ProcessTaskStepVo currentProcessTaskStepVo) throws Exception {
		ProcessTaskStepVo stepVo = getProcessTaskStepDetailInfoById(currentProcessTaskStepVo.getId());
		ProcessTaskVo processTaskVo = getProcessTaskDetailById(currentProcessTaskStepVo.getProcessTaskId());
		processTaskVo.setStartProcessTaskStep(getStartProcessTaskStepByProcessTaskId(processTaskVo.getId()));
		processTaskVo.setCurrentProcessTaskStep(stepVo);
		JSONObject processTaskJson = ProcessTaskUtil.getProcessFieldData(processTaskVo,true);
		JSONObject resultJson = automaticConfigVo.getResultJson();
		JSONArray paramList =  automaticConfigVo.getBaseParamList();
		JSONObject integrationParam = new JSONObject();
		if(!automaticConfigVo.getIsRequest()) {
			paramList = automaticConfigVo.getCallbackParamList();
		}
		if(!CollectionUtils.isEmpty(paramList)) {
			for(Object paramObj : paramList) {
				JSONObject param = (JSONObject)paramObj;
				String type = param.getString("type");
				String value = param.getString("value");
				String name = param.getString("name");
				if(type.equals("common")||type.equals("form")) {
					integrationParam.put(name, processTaskJson.get(value));
				}else if(type.equals("integration")){
					integrationParam.put(name, resultJson.get(value));
				}else{//常量
					integrationParam.put(name, value);
				}
			}
		}
		return integrationParam;
	}

	@Override
	public ProcessTaskStepVo getProcessTaskStepDetailInfoById(Long processTaskStepId) {
		//获取步骤信息
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		ProcessStepUtilHandlerFactory.getHandler().setProcessTaskStepConfig(processTaskStepVo);

		//处理人列表
		List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MAJOR.getValue());
		if(CollectionUtils.isNotEmpty(majorUserList)) {
			processTaskStepVo.setMajorUser(majorUserList.get(0));
		}
		List<ProcessTaskStepUserVo> minorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MINOR.getValue());
		processTaskStepVo.setMinorUserList(minorUserList);

		List<ProcessTaskStepWorkerVo> workerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepId);
		processTaskStepVo.setWorkerList(workerList);

		return processTaskStepVo;
	}

	public static void main(String[] args) {
		Pattern pattern = Pattern.compile("(5|4).*");
		System.out.println( pattern.matcher("300").matches());
	}

    @Override
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId, Long processTaskStepId, Long nextStepId) throws Exception {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
        if(processTaskVo == null) {
            throw new ProcessTaskNotFoundException(processTaskId.toString());
        }
        if(processTaskVo.getIsShow() != 1) {
            throw new PermissionDeniedException();
        }
        if(processTaskStepId != null) {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if(processTaskStepVo == null) {
                throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
            }
            IProcessStepUtilHandler processStepUtilHandler = ProcessStepUtilHandlerFactory.getHandler(processTaskStepVo.getHandler());
            if(processStepUtilHandler == null) {
                throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
            }
            if(!processTaskId.equals(processTaskStepVo.getProcessTaskId())) {
                throw new ProcessTaskRuntimeException("步骤：'" + processTaskStepId + "'不是工单：'" + processTaskId + "'的步骤");
            }
            processTaskVo.setCurrentProcessTaskStep(processTaskStepVo);
        }
        if(nextStepId != null) {
            ProcessTaskStepVo nextProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(nextStepId);
            if(nextProcessTaskStepVo == null) {
                throw new ProcessTaskStepNotFoundException(nextStepId.toString());
            }
            IProcessStepUtilHandler processStepUtilHandler = ProcessStepUtilHandlerFactory.getHandler(nextProcessTaskStepVo.getHandler());
            if(processStepUtilHandler == null) {
                throw new ProcessStepUtilHandlerNotFoundException(nextProcessTaskStepVo.getHandler());
            }
            if(!processTaskId.equals(nextProcessTaskStepVo.getProcessTaskId())) {
                throw new ProcessTaskRuntimeException("步骤：'" + nextStepId + "'不是工单：'" + processTaskId + "'的步骤");
            }
        }
        return processTaskVo;
    }

    @Override
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId, Long processTaskStepId) throws Exception {
        return checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId, null);
    }

    @Override
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId) throws Exception {
        return checkProcessTaskParamsIsLegal(processTaskId, null, null);
    }

    @Override
    public ProcessTaskVo getProcessTaskDetailById(Long processTaskId) throws Exception {
      //获取工单基本信息(title、channel_uuid、config_hash、priority_uuid、status、start_time、end_time、expire_time、owner、ownerName、reporter、reporterName)
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
        //判断当前用户是否关注该工单
		if(processTaskVo != null && processTaskMapper.checkProcessTaskFocusExists(processTaskId,UserContext.get().getUserUuid()) > 0){
			processTaskVo.setIsFocus(1);
		}
        //获取工单流程图信息
        ProcessTaskConfigVo processTaskConfig = selectContentByHashMapper.getProcessTaskConfigByHash(processTaskVo.getConfigHash());
        if(processTaskConfig == null) {
            throw new ProcessTaskRuntimeException("没有找到工单：'" + processTaskId + "'的流程图配置信息");
        }
        processTaskVo.setConfig(processTaskConfig.getConfig());
        
        //优先级
        PriorityVo priorityVo = priorityMapper.getPriorityByUuid(processTaskVo.getPriorityUuid());
        if(priorityVo == null) {
            priorityVo = new PriorityVo();
            priorityVo.setUuid(processTaskVo.getPriorityUuid());
        }
        processTaskVo.setPriority(priorityVo);
        //上报服务路径
        ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
        if(channelVo != null) {
            CatalogVo catalogVo = catalogMapper.getCatalogByUuid(channelVo.getParentUuid());
            if(catalogVo != null) {
                List<CatalogVo> catalogList = catalogMapper.getAncestorsAndSelfByLftRht(catalogVo.getLft(), catalogVo.getRht());
                List<String> nameList = catalogList.stream().map(CatalogVo::getName).collect(Collectors.toList());
                nameList.add(channelVo.getName());
                processTaskVo.setChannelPath(String.join("/", nameList));
            }
            ChannelTypeVo channelTypeVo =  channelMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid());
            if(channelTypeVo == null) {
                channelTypeVo = new ChannelTypeVo();
                channelTypeVo.setUuid(channelVo.getChannelTypeUuid());
            }
            processTaskVo.setChannelType(new ChannelTypeVo(channelTypeVo));
        }
        //耗时
        if(processTaskVo.getEndTime() != null) {
            long timeCost = worktimeMapper.calculateCostTime(processTaskVo.getWorktimeUuid(), processTaskVo.getStartTime().getTime(), processTaskVo.getEndTime().getTime());
            processTaskVo.setTimeCost(timeCost);
            processTaskVo.setTimeCostStr(TimeUtil.millisecondsTranferMaxTimeUnit(timeCost));
        }
        
        //获取工单表单信息
        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
        if(processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContentHash())) {
            String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
            if(StringUtils.isNotBlank(formContent)) {
                processTaskVo.setFormConfig(formContent);            
                List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskId);
                for(ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                    processTaskVo.getFormAttributeDataMap().put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
                }
            }
        }
        /** 上报人公司列表 **/
        List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(processTaskVo.getOwner());
        if(CollectionUtils.isNotEmpty(teamUuidList)) {
            List<TeamVo> teamList = teamMapper.getTeamByUuidList(teamUuidList);
            for(TeamVo teamVo : teamList) {
                List<TeamVo> companyList = teamMapper.getAncestorsAndSelfByLftRht(teamVo.getLft(), teamVo.getRht(), TeamLevel.COMPANY.getValue());
                if(CollectionUtils.isNotEmpty(companyList)) {
                    processTaskVo.getOwnerCompanyList().addAll(companyList);
                }
            }
        }
		/** 获取评分信息 */
		String scoreInfo = processTaskMapper.getProcessTaskScoreInfoById(processTaskId);
		processTaskVo.setScoreInfo(scoreInfo);
		
		/** 转报数据 **/
		Long fromProcessTaskId = processTaskMapper.getFromProcessTaskIdByToProcessTaskId(processTaskId);
        if(fromProcessTaskId != null) {
            processTaskVo.getTranferReportProcessTaskList().add(getFromProcessTasById(fromProcessTaskId));
        }
        List<Long> toProcessTaskIdList = processTaskMapper.getToProcessTaskIdListByFromProcessTaskId(processTaskId);
        for(Long toProcessTaskId : toProcessTaskIdList) {
            ProcessTaskVo toProcessTaskVo = processTaskMapper.getProcessTaskBaseInfoById(toProcessTaskId);
            if(toProcessTaskVo != null) {
                toProcessTaskVo.setTranferReportDirection("to");
                ChannelVo channel = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
                if(channel != null) {
                    ChannelTypeVo channelTypeVo =  channelMapper.getChannelTypeByUuid(channel.getChannelTypeUuid());
                    if(channelTypeVo == null) {
                        channelTypeVo = new ChannelTypeVo();
                        channelTypeVo.setUuid(channel.getChannelTypeUuid());
                    }
                    processTaskVo.setChannelType(new ChannelTypeVo(channelTypeVo));
                }
                processTaskVo.getTranferReportProcessTaskList().add(toProcessTaskVo);
            }
        }
        return processTaskVo;
    }

    @Override
    public List<ProcessTaskStepReplyVo> getProcessTaskStepReplyListByProcessTaskStepId(Long processTaskStepId, List<String> typeList) {
        List<ProcessTaskStepReplyVo> processTaskStepReplyList = new ArrayList<>();
        List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(processTaskStepId);
        for(ProcessTaskStepContentVo processTaskStepContentVo : processTaskStepContentList) {
            if(typeList.contains(processTaskStepContentVo.getType())) {
                ProcessTaskStepReplyVo processTaskStepReplyVo = new ProcessTaskStepReplyVo(processTaskStepContentVo);
                parseProcessTaskStepReply(processTaskStepReplyVo);
                processTaskStepReplyList.add(processTaskStepReplyVo);
            }
        }
        processTaskStepReplyList.sort((e1, e2) -> e1.getId().compareTo(e2.getId()));
        return processTaskStepReplyList;
    }

    @Override
    public List<ProcessTaskStepVo> getAssignableWorkerStepList(Long processTaskId, String processStepUuid) {
        ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
        processTaskStepWorkerPolicyVo.setProcessTaskId(processTaskId);
        List<ProcessTaskStepWorkerPolicyVo> processTaskStepWorkerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicy(processTaskStepWorkerPolicyVo);
        if(CollectionUtils.isNotEmpty(processTaskStepWorkerPolicyList)) {
            List<ProcessTaskStepVo> assignableWorkerStepList = new ArrayList<>();
            for(ProcessTaskStepWorkerPolicyVo workerPolicyVo : processTaskStepWorkerPolicyList) {
                if(WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
                    List<String> processStepUuidList = JSON.parseArray(workerPolicyVo.getConfigObj().getString("processStepUuidList"), String.class);
                    for(String stepUuid : processStepUuidList) {
                        if(processStepUuid.equals(stepUuid)) {
                            List<ProcessTaskStepUserVo> majorList = processTaskMapper.getProcessTaskStepUserByStepId(workerPolicyVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
                            if(CollectionUtils.isEmpty(majorList)) {
                                ProcessTaskStepVo assignableWorkerStep = processTaskMapper.getProcessTaskStepBaseInfoById(workerPolicyVo.getProcessTaskStepId());
                                assignableWorkerStep.setIsRequired(workerPolicyVo.getConfigObj().getInteger("isRequired"));
                                assignableWorkerStepList.add(assignableWorkerStep);
                            }
                        }
                    }
                }
            }
            return assignableWorkerStepList;
        }      
        return new ArrayList<>();
    }
    @Override
    public List<ProcessTaskStepVo> getAssignableWorkerStepList(String processUuid, String processStepUuid) {
        List<ProcessStepWorkerPolicyVo> processStepWorkerPolicyList = processMapper.getProcessStepWorkerPolicyListByProcessUuid(processUuid);
        if(CollectionUtils.isNotEmpty(processStepWorkerPolicyList)) {
            List<ProcessTaskStepVo> assignableWorkerStepList = new ArrayList<>();
            for(ProcessStepWorkerPolicyVo workerPolicyVo : processStepWorkerPolicyList) {
                if(WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
                    List<String> processStepUuidList = JSON.parseArray(workerPolicyVo.getConfigObj().getString("processStepUuidList"), String.class);
                    for(String stepUuid : processStepUuidList) {
                        if(processStepUuid.equals(stepUuid)) {
                            ProcessStepVo processStep = processMapper.getProcessStepByUuid(workerPolicyVo.getProcessStepUuid());
                            ProcessTaskStepVo assignableWorkerStep = new ProcessTaskStepVo(processStep);
                            assignableWorkerStep.setIsAutoGenerateId(false);
                            assignableWorkerStep.setIsRequired(workerPolicyVo.getConfigObj().getInteger("isRequired"));
                            assignableWorkerStepList.add(assignableWorkerStep);
                        }
                    }
                }
            }
            return assignableWorkerStepList;
        }        
        return new ArrayList<>();
    }
    @Override
    public List<ProcessTaskSlaTimeVo> getSlaTimeListByProcessTaskStepIdAndWorktimeUuid(Long processTaskStepId,
        String worktimeUuid) {
        List<ProcessTaskSlaTimeVo> slaTimeList = new ArrayList<>();
        List<ProcessTaskSlaVo> processTaskSlaList = processTaskMapper.getProcessTaskSlaByProcessTaskStepId(processTaskStepId);
        for(ProcessTaskSlaVo processTaskSlaVo : processTaskSlaList) {
            ProcessTaskSlaTimeVo processTaskSlaTimeVo = processTaskSlaVo.getSlaTimeVo();
            if(processTaskSlaTimeVo != null) {
                long nowTime = System.currentTimeMillis();
                processTaskSlaTimeVo.setName(processTaskSlaVo.getName());
                processTaskSlaTimeVo.setSlaId(processTaskSlaVo.getId());
                if(processTaskSlaTimeVo.getExpireTime() != null) {
                    long timeLeft = 0L;
                    long expireTime = processTaskSlaTimeVo.getExpireTime().getTime();
                    if(nowTime < expireTime) {
                        timeLeft = worktimeMapper.calculateCostTime(worktimeUuid, nowTime, expireTime);
                    }else if(nowTime > expireTime) {
                        timeLeft = -worktimeMapper.calculateCostTime(worktimeUuid, expireTime, nowTime);
                    }                   
                    processTaskSlaTimeVo.setTimeLeft(timeLeft);
                }
                if(processTaskSlaTimeVo.getRealExpireTime() != null) {
                    long realTimeLeft = processTaskSlaTimeVo.getRealExpireTime().getTime() - nowTime;
                    processTaskSlaTimeVo.setRealTimeLeft(realTimeLeft);
                }
                slaTimeList.add(processTaskSlaTimeVo);
            }
        }
        return slaTimeList;
    }

    @Override
    public List<ProcessTaskStepVo> getForwardNextStepListByProcessTaskStepId(Long processTaskStepId) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        List<ProcessTaskStepVo> nextStepList = processTaskMapper.getToProcessTaskStepByFromIdAndType(processTaskStepId, ProcessFlowDirection.FORWARD.getValue());
        for(ProcessTaskStepVo processTaskStep : nextStepList) {
            if(StringUtils.isNotBlank(processTaskStep.getAliasName())) {
                processTaskStep.setName(processTaskStep.getAliasName());
                processTaskStep.setFlowDirection("");
            }else {
                processTaskStep.setFlowDirection(ProcessFlowDirection.FORWARD.getText());
            }
            resultList.add(processTaskStep);
        }
        return resultList;
    }

    @Override
    public List<ProcessTaskStepVo> getBackwardNextStepListByProcessTaskStepId(Long processTaskStepId) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        List<ProcessTaskStepVo> nextStepList = processTaskMapper.getToProcessTaskStepByFromIdAndType(processTaskStepId, ProcessFlowDirection.BACKWARD.getValue());
        for(ProcessTaskStepVo processTaskStep : nextStepList) {
            if(!Objects.equals(processTaskStep.getIsActive(), 0)){
                if(StringUtils.isNotBlank(processTaskStep.getAliasName())) {
                    processTaskStep.setName(processTaskStep.getAliasName());
                    processTaskStep.setFlowDirection("");
                }else {
                    processTaskStep.setFlowDirection(ProcessFlowDirection.BACKWARD.getText());
                }
                resultList.add(processTaskStep);
            }
        }    
        return resultList;   
    }

    @Override
    public void setProcessTaskStepUser(ProcessTaskStepVo processTaskStepVo) {
        List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
        if(CollectionUtils.isNotEmpty(majorUserList)) {
            processTaskStepVo.setMajorUser(majorUserList.get(0));
        }else {
            processTaskStepVo.setWorkerList(processTaskMapper.getProcessTaskStepWorkerByProcessTaskStepId(processTaskStepVo.getId()));
        }
        processTaskStepVo.setMinorUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MINOR.getValue()));
    }

    @Override
    public ProcessTaskStepReplyVo getProcessTaskStepContentAndFileByProcessTaskStepId(Long processTaskStepId) {
        ProcessTaskStepReplyVo comment = new ProcessTaskStepReplyVo();
        //获取上报描述内容
        List<Long> fileIdList = new ArrayList<>();
        List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(processTaskStepId);
        for(ProcessTaskStepContentVo processTaskStepContent : processTaskStepContentList) {
            if (ProcessTaskOperationType.STARTPROCESS.getValue().equals(processTaskStepContent.getType())) {
                fileIdList = processTaskMapper.getFileIdListByContentId(processTaskStepContent.getId());
                comment.setContent(selectContentByHashMapper.getProcessTaskContentStringByHash(processTaskStepContent.getContentHash()));
                break;
            }
        }
        //附件
        if(CollectionUtils.isNotEmpty(fileIdList)) {
            comment.setFileList(fileMapper.getFileListByIdList(fileIdList));
        }
        return comment;
    }

    @Override
    public boolean saveProcessTaskStepReply(JSONObject jsonObj, ProcessTaskStepReplyVo oldReplyVo) {
        String content = jsonObj.getString("content");
        List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("fileIdList")), Long.class);
        if(content == null && fileIdList == null) {
            return false;
        }
        Long processTaskId = oldReplyVo.getProcessTaskId();
        Long processTaskStepId = oldReplyVo.getProcessTaskStepId();
        boolean isUpdate = false;
      //获取上传附件uuid
        List<Long> oldFileIdList = new ArrayList<>();
        //获取上报描述内容hash
        String oldContentHash = null;
        Long oldContentId = null;
        if(oldReplyVo.getId() != null) {
            parseProcessTaskStepReply(oldReplyVo);
            oldContentId = oldReplyVo.getId();
            oldContentHash = oldReplyVo.getContentHash();
            oldFileIdList = oldReplyVo.getFileIdList();
        }
        /** 保存新附件uuid **/
        if(fileIdList == null) {
            fileIdList = new ArrayList<>();
        }
        
        if(StringUtils.isNotBlank(content)) {
            ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
            if(Objects.equals(oldContentHash, contentVo.getHash())) {
                jsonObj.remove("content");
            }else {
                isUpdate = true;
                jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldContentHash);
                processTaskMapper.replaceProcessTaskContent(contentVo);
                if(oldContentId == null) {
                    processTaskMapper.insertProcessTaskStepContent(new ProcessTaskStepContentVo(processTaskId, processTaskStepId, contentVo.getHash(), ProcessTaskOperationType.STARTPROCESS.getValue()));
                }else {
                    processTaskMapper.updateProcessTaskStepContentById(new ProcessTaskStepContentVo(oldContentId, contentVo.getHash()));
                }
            }
        }else if(oldContentHash != null){
            isUpdate = true;
            jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldContentHash);
            if(CollectionUtils.isEmpty(fileIdList)) {
                processTaskMapper.deleteProcessTaskStepContentById(oldContentId);
            }else {
                processTaskMapper.updateProcessTaskStepContentById(new ProcessTaskStepContentVo(oldContentId, null));
            }
        }else {
            jsonObj.remove("content");
        }
        
        if(Objects.equals(oldFileIdList, fileIdList)) {
            jsonObj.remove("fileIdList");
        }else {
            isUpdate = true;
            processTaskMapper.deleteProcessTaskStepFileByContentId(oldContentId);
            ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(JSON.toJSONString(oldFileIdList));
            processTaskMapper.replaceProcessTaskContent(processTaskContentVo);
            jsonObj.put(ProcessTaskAuditDetailType.FILE.getOldDataParamName(), processTaskContentVo.getHash());
            /** 保存附件uuid **/
            if(CollectionUtils.isNotEmpty(fileIdList)) {
                ProcessTaskStepFileVo processTaskStepFileVo = new ProcessTaskStepFileVo();
                processTaskStepFileVo.setProcessTaskId(processTaskId);
                processTaskStepFileVo.setProcessTaskStepId(processTaskStepId);
                processTaskStepFileVo.setContentId(oldContentId);
                for (Long fileId : fileIdList) {
                    if(fileMapper.getFileById(fileId) == null) {
                        throw new ProcessTaskRuntimeException("上传附件id:'" + fileId + "'不存在");
                    }
                    processTaskStepFileVo.setFileId(fileId);
                    processTaskMapper.insertProcessTaskStepFile(processTaskStepFileVo);
                }
            }
        }
        return isUpdate;
    }
    
    /**
     * 
     * @Time:2020年4月2日
     * @Description: 检查当前用户是否配置该权限
     * @param processTaskStepVo
     * @param operationType 
     * @return boolean
     */
    @Override
    public boolean checkOperationAuthIsConfigured(ProcessTaskStepVo processTaskStepVo, ProcessTaskOperationType operationType) {
        JSONObject configObj = processTaskStepVo.getConfigObj();
        JSONArray authorityList = null;
        if (configObj != null && CollectionUtils.isEmpty(authorityList)) {
            authorityList = configObj.getJSONArray("authorityList");
            JSONObject globalConfig = processTaskStepVo.getGlobalConfig();
            if(MapUtils.isNotEmpty(globalConfig)) {
                authorityList = globalConfig.getJSONArray("authorityList");
            }
        }
        // 如果步骤自定义权限设置为空，则用组件的全局权限设置
        if (CollectionUtils.isNotEmpty(authorityList)) {
            for (int i = 0; i < authorityList.size(); i++) {
                JSONObject authorityObj = authorityList.getJSONObject(i);
                String action = authorityObj.getString("action");
                if(operationType.getValue().equals(action)) {
                    JSONArray acceptList = authorityObj.getJSONArray("acceptList");
                    if (CollectionUtils.isNotEmpty(acceptList)) {
                        List<String> currentUserTeamList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
                        ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
                        processTaskStepUserVo.setProcessTaskId(processTaskStepVo.getProcessTaskId());
                        processTaskStepUserVo.setProcessTaskStepId(processTaskStepVo.getId());
                        processTaskStepUserVo.setUserUuid(UserContext.get().getUserUuid(true));
                        for (int j = 0; j < acceptList.size(); j++) {
                            String accept = acceptList.getString(j);
                            String[] split = accept.split("#");
                            if (GroupSearch.COMMON.getValue().equals(split[0])) {
                                if (UserType.ALL.getValue().equals(split[1])) {
                                    return true;
                                }
                            } else if (ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue().equals(split[0])) {
                                if(processTaskStepVo.getCurrentUserProcessUserTypeList().contains(split[1])) {
                                    return true;
                                }
                            } else if (GroupSearch.USER.getValue().equals(split[0])) {
                                if (UserContext.get().getUserUuid(true).equals(split[1])) {
                                    return true;
                                }
                            } else if (GroupSearch.TEAM.getValue().equals(split[0])) {
                                if (currentUserTeamList.contains(split[1])) {
                                    return true;
                                }
                            } else if (GroupSearch.ROLE.getValue().equals(split[0])) {
                                if (UserContext.get().getRoleUuidList().contains(split[1])) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 
     * @Time:2020年4月3日
     * @Description: 获取工单中当前用户能撤回的步骤列表
     * @param processTaskId
     * @return Set<ProcessTaskStepVo>
     */
    @Override
    public Set<ProcessTaskStepVo> getRetractableStepListByProcessTask(ProcessTaskVo processTaskVo) {
        Set<ProcessTaskStepVo> resultSet = new HashSet<>();
        List<ProcessTaskStepVo> stepVoList = getProcessTaskStepVoListByProcessTask(processTaskVo);
        for (ProcessTaskStepVo stepVo : stepVoList) {
            /** 找到所有已激活步骤 **/
            if (stepVo.getIsActive().equals(1)) {
                resultSet.addAll(getRetractableStepListByProcessTaskStepId(stepVoList, stepVo.getId()));
            }
        }
        return resultSet;
    }
    
    /**
     * 
     * @Author: 14378
     * @Time:2020年4月3日
     * @Description: 获取当前步骤的前置步骤列表中处理人是当前用户的步骤列表
     * @param processTaskStepId 已激活的步骤id
     * @return List<ProcessTaskStepVo>
     */
    @Override
    public List<ProcessTaskStepVo> getRetractableStepListByProcessTaskStepId(List<ProcessTaskStepVo> processTaskStepList, Long processTaskStepId) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        /** 所有前置步骤 **/
        List<ProcessTaskStepVo> fromStepList = processTaskMapper.getFromProcessTaskStepByToId(processTaskStepId);
        /** 找到所有已完成步骤 **/
        for (ProcessTaskStepVo fromStep : fromStepList) {
            IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(fromStep.getHandler());
            if (handler != null) {
                if (ProcessStepMode.MT == handler.getMode()) {// 手动处理节点
                    for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
                        if(processTaskStepVo.getId().equals(fromStep.getId())) {
                            if(checkOperationAuthIsConfigured(processTaskStepVo, ProcessTaskOperationType.RETREATCURRENTSTEP)) {
                                resultList.add(fromStep);
                            }
                        }
                    }
                } else {// 自动处理节点，继续找前置节点
                    resultList.addAll(getRetractableStepListByProcessTaskStepId(processTaskStepList, fromStep.getId()));
                }
            } else {
                throw new ProcessStepHandlerNotFoundException(fromStep.getHandler());
            }
        }
        
        return resultList;
    }

    /**
     * 
     * @Time:2020年4月18日
     * @Description: 获取工单中当前用户能催办的步骤列表
     * @param processTaskId
     * @return List<ProcessTaskStepVo>
     */
    @Override
    public List<ProcessTaskStepVo> getUrgeableStepList(ProcessTaskVo processTaskVo) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        List<ProcessTaskStepVo> processTaskStepList = getProcessTaskStepVoListByProcessTask(processTaskVo);
        for (ProcessTaskStepVo processTaskStep : processTaskStepList) {
            if (processTaskStep.getIsActive().intValue() == 1) {
                if(checkOperationAuthIsConfigured(processTaskStep, ProcessTaskOperationType.URGE)) {
                    resultList.add(processTaskStep);
                }
            }
        }
        return resultList;
    }
    
    @Override
    public List<ProcessTaskStepVo> getProcessTaskStepVoListByProcessTask(ProcessTaskVo processTaskVo){
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskVo.getId());
        for (ProcessTaskStepVo processTaskStep : processTaskStepList) {
            ProcessStepUtilHandlerFactory.getHandler().setProcessTaskStepConfig(processTaskStep);
            ProcessStepUtilHandlerFactory.getHandler().setCurrentUserProcessUserTypeList(processTaskVo, processTaskStep);
        }
        return processTaskStepList;
    }    

    @Override
    public ProcessTaskVo getFromProcessTasById(Long processTaskId) throws Exception {
        ProcessTaskVo processTaskVo = checkProcessTaskParamsIsLegal(processTaskId);
        ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
        if(channelVo != null) {
            ChannelTypeVo channelTypeVo =  channelMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid());
            if(channelTypeVo == null) {
                channelTypeVo = new ChannelTypeVo();
                channelTypeVo.setUuid(channelVo.getChannelTypeUuid());
            }
            processTaskVo.setChannelType(new ChannelTypeVo(channelTypeVo));
        }
        //获取工单表单信息
        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
        if(processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContentHash())) {
            String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
            if(StringUtils.isNotBlank(formContent)) {
                processTaskVo.setFormConfig(formContent);            
                List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskId);
                for(ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                    processTaskVo.getFormAttributeDataMap().put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
                }
            }
        }
        //获取开始步骤id
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
        if(processTaskStepList.size() != 1) {
            throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
        }
        ProcessTaskStepVo startProcessTaskStepVo = processTaskStepList.get(0);
        startProcessTaskStepVo.setComment(getProcessTaskStepContentAndFileByProcessTaskStepId(startProcessTaskStepVo.getId()));
        processTaskVo.setStartProcessTaskStep(startProcessTaskStepVo);
        processTaskVo.setTranferReportDirection("from");
        return processTaskVo;
    }

    @Override
    public List<ProcessTaskStepRemindVo> getProcessTaskStepRemindListByProcessTaskStepId(Long processTaskStepId) {
        List<ProcessTaskStepRemindVo> processTaskStepRemindList = processTaskMapper.getProcessTaskStepRemindListByProcessTaskStepId(processTaskStepId);
        for(ProcessTaskStepRemindVo processTaskStepRemindVo : processTaskStepRemindList) {
            processTaskStepRemindVo.setActionName(ProcessTaskStepRemindTypeFactory.getText(processTaskStepRemindVo.getAction()));
            String contentHash = processTaskStepRemindVo.getContentHash();
            if(StringUtils.isNotBlank(contentHash)) {
                String content = selectContentByHashMapper.getProcessTaskContentStringByHash(contentHash);
                processTaskStepRemindVo.setDetail(content);
                processTaskStepRemindVo.setContent(pattern_html.matcher(content).replaceAll(""));
            }
        }
        return processTaskStepRemindList;
    }
    
    /**
     * 
    * @Author: linbq
    * @Time:2020年8月21日
    * @Description: 获取开始步骤信息 
    * @param processTaskId 工单id
    * @return ProcessTaskStepVo
     */
    @Override
    public ProcessTaskStepVo getStartProcessTaskStepByProcessTaskId(Long processTaskId) {
        //获取开始步骤id
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
        if(processTaskStepList.size() != 1) {
            throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
        }

        ProcessTaskStepVo startProcessTaskStepVo = processTaskStepList.get(0);

        startProcessTaskStepVo.setComment(getProcessTaskStepContentAndFileByProcessTaskStepId(startProcessTaskStepVo.getId()));
        /** 当前步骤特有步骤信息 **/
        IProcessStepUtilHandler startProcessStepUtilHandler = ProcessStepUtilHandlerFactory.getHandler(startProcessTaskStepVo.getHandler());
        if(startProcessStepUtilHandler == null) {
            throw new ProcessStepHandlerNotFoundException(startProcessTaskStepVo.getHandler());
        }
        startProcessStepUtilHandler.setProcessTaskStepConfig(startProcessTaskStepVo);
        startProcessTaskStepVo.setHandlerStepInfo(startProcessStepUtilHandler.getHandlerStepInfo(startProcessTaskStepVo));
        return startProcessTaskStepVo;
    }

//    @Override
//    public List<ProcessTaskStepReplyVo> getProcessTaskStepReplyListByProcessTaskId(Long processTaskId, List<String> typeList) {
//        List<ProcessTaskStepReplyVo> processTaskStepReplyList = new ArrayList<>();
//        Map<Long, String> processTaskStepNameMap = new HashMap<>();
//        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
//        for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
//            processTaskStepNameMap.put(processTaskStepVo.getId(), processTaskStepVo.getName());
//        }
//        List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskId(processTaskId);
//        for(ProcessTaskStepContentVo processTaskStepContentVo : processTaskStepContentList) {
//            if(typeList.contains(processTaskStepContentVo.getType())) {
//                ProcessTaskStepReplyVo processTaskStepReplyVo = new ProcessTaskStepReplyVo(processTaskStepContentVo);
//                parseProcessTaskStepReply(processTaskStepReplyVo);
//                processTaskStepReplyVo.setProcessTaskStepName(processTaskStepNameMap.get(processTaskStepReplyVo.getProcessTaskStepId()));
//                processTaskStepReplyList.add(processTaskStepReplyVo);
//            }
//        }
//        return processTaskStepReplyList;
//    }
    
    @Transactional
    @Override
    public void updateTag(Long processTaskId,Long processTaskStepId,JSONObject jsonObj) throws PermissionDeniedException {
        JSONArray tagArray = jsonObj.getJSONArray("tagList");
        List<ProcessTagVo>  oldTagList = processTaskMapper.getProcessTaskTagListByProcessTaskId(processTaskId);
        processTaskMapper.deleteProcessTaskTagByProcessTaskId(processTaskId);
        if(CollectionUtils.isNotEmpty(tagArray)) {
            List<String> tagNameList = JSONObject.parseArray(tagArray.toJSONString(), String.class);
            List<ProcessTagVo> existTagList = processMapper.getProcessTagByNameList(tagNameList);
            List<String> notExistTagList = tagNameList.stream().filter(a->!existTagList.stream().map(b -> b.getName()).collect(Collectors.toList()).contains(a)).collect(Collectors.toList());
            List<ProcessTagVo> notExistTagVoList = new ArrayList<ProcessTagVo>();
            for(String tagName : notExistTagList) {
                notExistTagVoList.add(new ProcessTagVo(tagName));
            }
            if(CollectionUtils.isNotEmpty(notExistTagVoList)) {
                processMapper.insertProcessTag(notExistTagVoList);
                existTagList.addAll(notExistTagVoList);
            }
            List<ProcessTaskTagVo> processTaskTagVoList = new ArrayList<ProcessTaskTagVo>();
            for(ProcessTagVo processTagVo : existTagList) {
                processTaskTagVoList.add(new ProcessTaskTagVo(processTaskId,processTagVo.getId()));
            }
            processTaskMapper.insertProcessTaskTag(processTaskTagVoList);
            
            //生成活动
            IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler();
            try {
                handler.verifyOperationAuthoriy(processTaskId, ProcessTaskOperationType.UPDATE, true);
            }catch(ProcessTaskNoPermissionException e) {
                throw new PermissionDeniedException();
            }
            List<String> oldTagNameList = new ArrayList<String>();
            for(ProcessTagVo tag:oldTagList) {
                oldTagNameList.add(tag.getName());
            }
            ProcessTaskContentVo oldTagContentVo = new ProcessTaskContentVo(String.join(",", oldTagNameList));
            processTaskMapper.replaceProcessTaskContent(oldTagContentVo);
            if(StringUtils.isNotBlank(oldTagContentVo.getHash())) {
                jsonObj.put(ProcessTaskAuditDetailType.TAGLIST.getOldDataParamName(), oldTagContentVo.getHash());
                jsonObj.put(ProcessTaskAuditDetailType.TAGLIST.getParamName(), String.join(",", tagNameList));
            }
            ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
            processTaskStepVo.setProcessTaskId(processTaskId);
            processTaskStepVo.setId(processTaskStepId);
            processTaskStepVo.setParamObj(jsonObj);
            handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.UPDATE);
        }   
    }
}
