package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.dto.ProcessTaskSlaTimeVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class ProcessTaskCurrentUserTaskListApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private TeamMapper teamMapper;
	
	@Autowired
	private WorktimeMapper worktimeMapper;

    @Autowired
    private ProcessStepHandlerMapper stepHandlerMapper;
	
	@Override
	public String getToken() {
		return "processtask/currentuser/task/list";
	}

	@Override
	public String getName() {
		return "当前用户任务列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关键字搜索"),
		@Param(name = "currentProcessTaskId", type = ApiParamType.LONG, isRequired = true, desc = "当前工单id"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
	})
	@Output({
		@Param(name = "taskList", type = ApiParamType.JSONARRAY, desc = "任务列表"),
		@Param(explode = BasePageVo.class)
	})
	@Description(desc = "当前用户任务列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long currentProcessTaskId = jsonObj.getLong("currentProcessTaskId");
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(currentProcessTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(currentProcessTaskId.toString());
		}
		JSONObject resultObj = new JSONObject();
		BasePageVo basePageVo = JSON.toJavaObject(jsonObj, BasePageVo.class);
		resultObj.put("currentPage", basePageVo.getCurrentPage());
		resultObj.put("pageSize", basePageVo.getPageSize());
		resultObj.put("rowNum", 0);
		resultObj.put("pageCount", 0);
		resultObj.put("taskList", new ArrayList<>());
		boolean isCurrentProcessTaskTop = false;
		if(StringUtils.isBlank(basePageVo.getKeyword()) && Objects.equal(basePageVo.getCurrentPage(), 1)) {
			isCurrentProcessTaskTop = true;
		}
		String userUuid = UserContext.get().getUserUuid(true);
		List<String> roleUuidList = UserContext.get().getRoleUuidList();
		List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(userUuid);		
		List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = processTaskMapper.getProcessTaskStepWorkerListByUserUuidTeamUuidListRoleUuidList(userUuid, teamUuidList, roleUuidList);
		if(CollectionUtils.isNotEmpty(processTaskStepWorkerList)) {
			Map<Long, List<Long>> processTaskStepIdListMap = new HashMap<>();
			List<Long> processTaskIdList = new ArrayList<>();
			for(ProcessTaskStepWorkerVo processTaskStepWorker : processTaskStepWorkerList) {
				Long processTaskId = processTaskStepWorker.getProcessTaskId();
				if(!processTaskIdList.contains(processTaskId)) {
					processTaskIdList.add(processTaskId);
				}
				List<Long> processTaskStepIdList = processTaskStepIdListMap.get(processTaskId);
				if(processTaskStepIdList == null) {
					processTaskStepIdList = new ArrayList<>();
					processTaskStepIdListMap.put(processTaskId, processTaskStepIdList);
				}
				processTaskStepIdList.add(processTaskStepWorker.getProcessTaskStepId());
			}

			List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskListByKeywordAndIdList(basePageVo.getKeyword(), processTaskIdList);
			Map<Long, ProcessTaskVo> processTaskMap = new HashMap<>();
			List<Long> processTaskStepIdList = new ArrayList<>();
			for(ProcessTaskVo processTask : processTaskList) {
				processTaskMap.put(processTask.getId(), processTask);
				if(StringUtils.isNotBlank(basePageVo.getKeyword()) || !currentProcessTaskId.equals(processTask.getId())) {
					processTaskStepIdList.addAll(processTaskStepIdListMap.get(processTask.getId()));
				}
			}
			processTaskStepIdList.sort((e1, e2) -> -e1.compareTo(e2));
			int rowNum = processTaskStepIdList.size();
			int fromIndex = basePageVo.getStartNum();
			JSONArray taskList = new JSONArray();
			JSONArray currentTaskList = new JSONArray();
			if(fromIndex < rowNum) {
				int toIndex = fromIndex + basePageVo.getPageSize();
				toIndex = toIndex <= rowNum ? toIndex : rowNum;
				processTaskStepIdList = processTaskStepIdList.subList(fromIndex, toIndex);
			}
				
			if(isCurrentProcessTaskTop && processTaskIdList.contains(currentProcessTaskId)) {
				processTaskStepIdList.addAll(processTaskStepIdListMap.get(currentProcessTaskId));
			}
			if(CollectionUtils.isNotEmpty(processTaskStepIdList)) {
				List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByIdList(processTaskStepIdList);
				processTaskStepList.sort((e1, e2) -> -e1.getId().compareTo(e2.getId()));
				Map<Long, ProcessTaskSlaTimeVo> stepSlaTimeMap = new HashMap<>();
				List<ProcessTaskSlaTimeVo> processTaskSlaTimeList = processTaskMapper.getProcessTaskSlaTimeByProcessTaskStepIdList(processTaskStepIdList);
				for(ProcessTaskSlaTimeVo processTaskSlaTimeVo : processTaskSlaTimeList) {
					if(!stepSlaTimeMap.containsKey(processTaskSlaTimeVo.getProcessTaskStepId())) {
						stepSlaTimeMap.put(processTaskSlaTimeVo.getProcessTaskStepId(), processTaskSlaTimeVo);
					}
				}
				Map<String, ProcessStepHandlerVo> handlerConfigMap = new HashMap<>();
		        List<ProcessStepHandlerVo> handlerConfigList = stepHandlerMapper.getProcessStepHandlerConfig();
		        for(ProcessStepHandlerVo handlerConfig : handlerConfigList) {
		        	handlerConfigMap.put(handlerConfig.getHandler(), handlerConfig);
		        }
				for(ProcessTaskStepVo processTaskStep : processTaskStepList) {
					JSONObject task = new JSONObject();
					ProcessTaskVo processTask = processTaskMap.get(processTaskStep.getProcessTaskId());
					task.put("processTaskId", processTaskStep.getProcessTaskId());
					task.put("title", processTask.getTitle());
					task.put("processTaskStepId", processTaskStep.getId());
					task.put("stepName", processTaskStep.getName());
					String config = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStep.getConfigHash());
					processTaskStep.setConfig(config);
					ProcessStepHandlerVo processStepHandlerConfig = handlerConfigMap.get(processTaskStep.getHandler());
					if(processStepHandlerConfig != null) {
						processTaskStep.setGlobalConfig(processStepHandlerConfig.getConfig());							
					}
					task.put("statusVo", processTaskStep.getStatusVo());
					
					ProcessTaskSlaTimeVo processTaskSlaTimeVo = stepSlaTimeMap.get(processTaskStep.getId());
					if(processTaskSlaTimeVo != null) {
						if(processTaskSlaTimeVo.getExpireTime() != null) {
							long timeLeft = 0L;
							long nowTime = System.currentTimeMillis();
							long expireTime = processTaskSlaTimeVo.getExpireTime().getTime();
							if(nowTime < expireTime) {
								timeLeft = worktimeMapper.calculateCostTime(processTask.getWorktimeUuid(), nowTime, expireTime);
							}else if(nowTime > expireTime) {
								timeLeft = -worktimeMapper.calculateCostTime(processTask.getWorktimeUuid(), expireTime, nowTime);
							}
							processTaskSlaTimeVo.setTimeLeft(timeLeft);
//							processTaskSlaTimeVo.setTimeLeftDesc(conversionTimeUnit(timeLeft));
						}
						if(processTaskSlaTimeVo.getRealExpireTime() != null) {
							long realTimeLeft = processTaskSlaTimeVo.getExpireTime().getTime() - System.currentTimeMillis();
							processTaskSlaTimeVo.setRealTimeLeft(realTimeLeft);
//							processTaskSlaTimeVo.setRealTimeLeftDesc(conversionTimeUnit(realTimeLeft));
						}
						task.put("slaTimeVo", processTaskSlaTimeVo);
					}
					if(Objects.equal(processTaskStep.getProcessTaskId(), currentProcessTaskId)) {
						currentTaskList.add(task);
					}else {
						taskList.add(task);
					}
				}
				if(CollectionUtils.isNotEmpty(currentTaskList)) {
					taskList.addAll(0, currentTaskList);
				}
			}

			resultObj.put("rowNum", rowNum);
			resultObj.put("pageCount", PageUtil.getPageCount(rowNum, basePageVo.getPageSize()));
			resultObj.put("taskList", taskList);
		}
				
		return resultObj;
	}
	
//	private String conversionTimeUnit(long milliseconds) {
//		StringBuilder stringBuilder = new StringBuilder();
//		milliseconds = Math.abs(milliseconds);
//		if(milliseconds < 1000) {
//			stringBuilder.append("0秒");
//		} else {
//			if(milliseconds >= (60 * 60 * 1000)) {
//				long hours = milliseconds / (60 * 60 * 1000);
//				stringBuilder.append(hours);
//				stringBuilder.append("小时");
//				milliseconds = milliseconds % (60 * 60 * 1000);
//			}
//			if(milliseconds >= (60 * 1000)) {
//				long minutes = milliseconds / (60 * 1000);
//				stringBuilder.append(minutes);
//				stringBuilder.append("分钟");
//				milliseconds = milliseconds % (60 * 1000);
//			}
//			if(milliseconds >= 1000) {
//				long seconds = milliseconds / 1000;
//				stringBuilder.append(seconds);
//				stringBuilder.append("秒");
//			}
//		}	
//		return stringBuilder.toString();
//	}
}
