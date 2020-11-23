package codedriver.module.process.api.processtask;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.module.process.service.ProcessTaskService;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskRetreatableStepListApi extends PrivateApiComponentBase {
    
    @Autowired
    private ProcessTaskService processTaskService;
    @Autowired
    private UserMapper userMapper;
	
	@Override
	public String getToken() {
		return "processtask/retreatablestep/list";
	}

	@Override
	public String getName() {
		return "当前用户可撤回的步骤列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单Id", isRequired = true)
	})
	@Output({
		@Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "步骤信息列表")
	})
	@Description(desc = "当前用户可撤回的步骤列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
		Set<ProcessTaskStepVo> resultList = processTaskService.getRetractableStepListByProcessTask(processTaskVo);
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
            Set<ProcessTaskStepVo> retractableStepList = processTaskService.getRetractableStepListByProcessTask(processTaskVo);
            for(ProcessTaskStepVo processTaskStepVo : retractableStepList) {
                if(!resultList.contains(processTaskStepVo)) {
                    resultList.add(processTaskStepVo);
                }
            }
            UserContext.get().setUserUuid(currentUserUuid);
            UserContext.get().setUserId(currentUserId);
            UserContext.get().setUserName(currentUserName);
            UserContext.get().setRoleUuidList(currentRoleUuidList);
        }
        return resultList;
	}

}
