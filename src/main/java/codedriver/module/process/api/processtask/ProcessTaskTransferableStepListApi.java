package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.service.ProcessTaskAgentService;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.module.process.service.ProcessTaskService;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskTransferableStepListApi extends PrivateApiComponentBase {
    
    @Resource
    private ProcessTaskService processTaskService;
	@Resource
	private ProcessTaskAgentService processTaskAgentService;
	
	@Override
	public String getToken() {
		return "processtask/transferablestep/list";
	}

	@Override
	public String getName() {
		return "当前用户可转交的步骤列表接口";
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
	@Description(desc = "当前用户可转交的步骤列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
		Set<ProcessTaskStepVo> resultList = processTaskService.getTransferableStepListByProcessTask(processTaskVo, UserContext.get().getUserUuid(true));
		/** 如果当前用户接受了其他用户的授权，查出其他用户拥有的权限，叠加当前用户权限里 **/
		List<String> fromUserUUidList = processTaskAgentService.getFromUserUuidListByToUserUuidAndChannelUuid(UserContext.get().getUserUuid(true), processTaskVo.getChannelUuid());
		for (String userUuid : fromUserUUidList) {
			Set<ProcessTaskStepVo> retractableStepList = processTaskService.getTransferableStepListByProcessTask(processTaskVo, userUuid);
			for(ProcessTaskStepVo processTaskStepVo : retractableStepList) {
				if(!resultList.contains(processTaskStepVo)) {
					resultList.add(processTaskStepVo);
				}
			}
		}
//        String userUuid = userMapper.getUserUuidByAgentUuidAndFunc(UserContext.get().getUserUuid(true), "processtask");
//        if(StringUtils.isNotBlank(userUuid)) {
//            Set<ProcessTaskStepVo> retractableStepList = processTaskService.getTransferableStepListByProcessTask(processTaskVo, userUuid);
//            for(ProcessTaskStepVo processTaskStepVo : retractableStepList) {
//                if(!resultList.contains(processTaskStepVo)) {
//                    resultList.add(processTaskStepVo);
//                }
//            }
//        }
        return resultList;
	}

}
