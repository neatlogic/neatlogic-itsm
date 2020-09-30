package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskProcessableStepList extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
    
    @Autowired
    private ProcessTaskService processTaskService;
    
    @Autowired
    private TeamMapper teamMapper;
    
    @Autowired
    private UserMapper userMapper;

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
		processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
		List<ProcessTaskStepVo> processableStepList = getProcessableStepList(processTaskId);
		/** 如果当前用户接受了其他用户的授权，查出其他用户拥有的权限，叠加当前用户权限里 **/
        String userUuid = userMapper.getUserUuidByAgentUuidAndFunc(UserContext.get().getUserUuid(true), "processtask");
        if(StringUtils.isNotBlank(userUuid)) {
            List<String> roleUuidList = userMapper.getRoleUuidListByUserUuid(userUuid);
            String currentUserUuid = UserContext.get().getUserUuid(true);
            String currentUserId = UserContext.get().getUserId(true);
            String currentUserName = UserContext.get().getUserName();
            List<String> currentRoleUuidList = UserContext.get().getRoleUuidList();
            UserContext.get().setUserUuid(userUuid);
            UserContext.get().setUserId(null);
            UserContext.get().setUserName(null);
            UserContext.get().setRoleUuidList(roleUuidList);
            for(ProcessTaskStepVo processTaskStepVo : getProcessableStepList(processTaskId)) {
                if(!processableStepList.contains(processTaskStepVo)) {
                    processableStepList.add(processTaskStepVo);
                }
            }
            UserContext.get().setUserUuid(currentUserUuid);
            UserContext.get().setUserId(currentUserId);
            UserContext.get().setUserName(currentUserName);
            UserContext.get().setRoleUuidList(currentRoleUuidList);
        }
		String action = jsonObj.getString("action");
		if(StringUtils.isNotBlank(action)) {
			Iterator<ProcessTaskStepVo> iterator = processableStepList.iterator();
			while(iterator.hasNext()) {
				ProcessTaskStepVo processTaskStepVo = iterator.next();
				List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
				if(ProcessTaskOperationType.ACCEPT.getValue().equals(action)) {
					if(CollectionUtils.isNotEmpty(majorUserList)) {
						iterator.remove();
					}
				}else if(ProcessTaskOperationType.START.getValue().equals(action)) {
					if(CollectionUtils.isEmpty(majorUserList)) {
						iterator.remove();
					}else if(ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())){
						iterator.remove();
					}
				}else if(ProcessTaskOperationType.COMPLETE.getValue().equals(action)) {
					if(CollectionUtils.isEmpty(majorUserList)) {
						iterator.remove();
					}else if(ProcessTaskStatus.PENDING.getValue().equals(processTaskStepVo.getStatus())){
						iterator.remove();
					}
				}
			}
		}
		return processableStepList;
	}
	
	/**
     * 
     * @Time:2020年4月3日
     * @Description: 获取工单中当前用户能处理的步骤列表
     * @param processTaskId
     * @return List<ProcessTaskStepVo>
     */
	private List<ProcessTaskStepVo> getProcessableStepList(Long processTaskId) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        List<String> currentUserTeamList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskId);
        for (ProcessTaskStepVo stepVo : processTaskStepList) {
            /** 找到所有已激活未处理的步骤 **/
            if (stepVo.getIsActive().equals(1)) {
                if(processTaskMapper.checkIsWorker(processTaskId, stepVo.getId(), null, UserContext.get().getUserUuid(true), currentUserTeamList, UserContext.get().getRoleUuidList()) > 0) {
                    resultList.add(stepVo);
                }
            }
        }
        return resultList;
    }
}
