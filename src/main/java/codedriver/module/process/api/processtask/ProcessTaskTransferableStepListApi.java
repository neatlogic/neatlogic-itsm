package codedriver.module.process.api.processtask;

import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

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
public class ProcessTaskTransferableStepListApi extends PrivateApiComponentBase {
    
    @Autowired
    private ProcessTaskService processTaskService;
    @Autowired
    private UserMapper userMapper;
	
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
        String userUuid = userMapper.getUserUuidByAgentUuidAndFunc(UserContext.get().getUserUuid(true), "processtask");
        if(StringUtils.isNotBlank(userUuid)) {
            Set<ProcessTaskStepVo> retractableStepList = processTaskService.getTransferableStepListByProcessTask(processTaskVo, userUuid);
            for(ProcessTaskStepVo processTaskStepVo : retractableStepList) {
                if(!resultList.contains(processTaskStepVo)) {
                    resultList.add(processTaskStepVo);
                }
            }
        }
        return resultList;
	}

}
