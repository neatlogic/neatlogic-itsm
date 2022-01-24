package codedriver.module.process.api.processtask;

import java.util.*;
import java.util.concurrent.TimeUnit;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dto.AuthenticationInfoVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dto.ProcessTaskStepInOperationVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.service.ProcessTaskAgentService;
import codedriver.framework.service.AuthenticationInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskProcessableStepList extends PrivateApiComponentBase {

	@Resource
	private ProcessTaskMapper processTaskMapper;
    
    @Resource
    private ProcessTaskService processTaskService;

	@Resource
	private AuthenticationInfoService authenticationInfoService;
	@Resource
	private ProcessTaskAgentService processTaskAgentService;

	@Override
	public String getToken() {
		return "processtask/processablestep/list";
	}

	@Override
	public String getName() {
		return "当前用户可处理的步骤列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id"),
		@Param(name = "action", type = ApiParamType.ENUM, rule = "accept,start,complete", desc = "操作类型")
	})
	@Output({
		@Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "步骤信息列表")
	})
	@Description(desc = "当前用户可处理的步骤列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
//		for(int i = 0; i < 10; i++) {
//		    if(processTaskMapper.getProcessTaskStepInOperationCountByProcessTaskId(processTaskId) == 0) {
//		        break;
//		    }
//            TimeUnit.MILLISECONDS.sleep(30);
//		}
		JSONObject resultObj = new JSONObject();
		resultObj.put("await", -1);
		List<ProcessTaskStepInOperationVo> processTaskStepInOperationList = processTaskMapper.getProcessTaskStepInOperationListByProcessTaskId(processTaskId);
		if (CollectionUtils.isNotEmpty(processTaskStepInOperationList)) {
			boolean needAwait = false;
			long await = 0;
			for (ProcessTaskStepInOperationVo processTaskStepInOperationVo : processTaskStepInOperationList) {
				Date expireTime = processTaskStepInOperationVo.getExpireTime();
				if (expireTime == null) {
					needAwait = true;
					continue;
				}
				long after = System.currentTimeMillis() - expireTime.getTime();
				if (after > 0) {
					needAwait = true;
					await = after > await ? after : await;
				}
			}
			if (needAwait) {
				resultObj.put("await", await);
				return resultObj;
			}
		}
		List<ProcessTaskStepVo> processableStepList = getProcessableStepList(processTaskId, UserContext.get().getUserUuid(true));
		/** 如果当前用户接受了其他用户的授权，查出其他用户拥有的权限，叠加当前用户权限里 **/
		List<String> fromUserUUidList = processTaskAgentService.getFromUserUuidListByToUserUuidAndChannelUuid(UserContext.get().getUserUuid(true), processTaskVo.getChannelUuid());
		for (String userUuid : fromUserUUidList) {
			for(ProcessTaskStepVo processTaskStepVo : getProcessableStepList(processTaskId, userUuid)) {
				if(!processableStepList.contains(processTaskStepVo)) {
					processableStepList.add(processTaskStepVo);
				}
			}
		}

		String action = jsonObj.getString("action");
		if(StringUtils.isNotBlank(action)) {
			Iterator<ProcessTaskStepVo> iterator = processableStepList.iterator();
			while(iterator.hasNext()) {
				ProcessTaskStepVo processTaskStepVo = iterator.next();
				List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
				if(ProcessTaskOperationType.STEP_ACCEPT.getValue().equals(action)) {
					if(CollectionUtils.isNotEmpty(majorUserList)) {
						iterator.remove();
					}
				}else if(ProcessTaskOperationType.STEP_START.getValue().equals(action)) {
					if(CollectionUtils.isEmpty(majorUserList)) {
						iterator.remove();
					}else if(ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())){
						iterator.remove();
					}
				}else if(ProcessTaskOperationType.STEP_COMPLETE.getValue().equals(action)) {
					if(CollectionUtils.isEmpty(majorUserList)) {
						iterator.remove();
					}else if(ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())){
						iterator.remove();
					}
				}
			}
		}

		resultObj.put("tbodyList", processableStepList);
		return resultObj;
	}
	
	/**
     * 
     * @Time:2020年4月3日
     * @Description: 获取工单中当前用户能处理的步骤列表
     * @param processTaskId
     * @return List<ProcessTaskStepVo>
     */
	private List<ProcessTaskStepVo> getProcessableStepList(Long processTaskId, String userUuid) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
		AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userUuid);
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskId);
        for (ProcessTaskStepVo stepVo : processTaskStepList) {
            /** 找到所有已激活未处理的步骤 **/
            if (stepVo.getIsActive().equals(1)) {
                if(processTaskMapper.checkIsWorker(processTaskId, stepVo.getId(), null, authenticationInfoVo) > 0) {
                    resultList.add(stepVo);
                }
            }
        }
        return resultList;
    }
}
