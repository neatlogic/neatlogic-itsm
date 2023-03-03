package neatlogic.module.process.api.processtask;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.crossover.IProcessTaskRetreatableStepListApiCrossoverService;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.process.service.ProcessTaskAgentService;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.module.process.service.ProcessTaskService;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskRetreatableStepListApi extends PrivateApiComponentBase implements IProcessTaskRetreatableStepListApiCrossoverService {
    
    @Resource
    private ProcessTaskService processTaskService;
	@Resource
	private ProcessTaskAgentService processTaskAgentService;
	
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
		new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_RETREAT)
				.build()
				.checkAndNoPermissionThrowException();
		Set<ProcessTaskStepVo> resultList = processTaskService.getRetractableStepListByProcessTask(processTaskVo, UserContext.get().getUserUuid(true));
		/** 如果当前用户接受了其他用户的授权，查出其他用户拥有的权限，叠加当前用户权限里 **/
		List<String> fromUserUUidList = processTaskAgentService.getFromUserUuidListByToUserUuidAndChannelUuid(UserContext.get().getUserUuid(true), processTaskVo.getChannelUuid());
		for (String userUuid : fromUserUUidList) {
			Set<ProcessTaskStepVo> retractableStepList = processTaskService.getRetractableStepListByProcessTask(processTaskVo, userUuid);
			for(ProcessTaskStepVo processTaskStepVo : retractableStepList) {
				if(!resultList.contains(processTaskStepVo)) {
					resultList.add(processTaskStepVo);
				}
			}
		}
//        String userUuid = userMapper.getUserUuidByAgentUuidAndFunc(UserContext.get().getUserUuid(true), "processtask");
//        if(StringUtils.isNotBlank(userUuid)) {
//            Set<ProcessTaskStepVo> retractableStepList = processTaskService.getRetractableStepListByProcessTask(processTaskVo, userUuid);
//            for(ProcessTaskStepVo processTaskStepVo : retractableStepList) {
//                if(!resultList.contains(processTaskStepVo)) {
//                    resultList.add(processTaskStepVo);
//                }
//            }
//        }
        return resultList;
	}

}
