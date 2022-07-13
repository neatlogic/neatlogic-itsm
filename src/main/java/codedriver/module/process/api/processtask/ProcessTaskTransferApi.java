package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.crossover.IProcessTaskTransferApiCrossoverService;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskTransferApi extends PrivateApiComponentBase implements IProcessTaskTransferApiCrossoverService {
    
    @Autowired
    private ProcessTaskService processTaskService;
	
	@Override
	public String getToken() {
		return "processtask/transfer";
	}

	@Override
	public String getName() {
		return "工单转交接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
			@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id"),
			@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "工单步骤Id"),
			@Param(name = "workerList", type = ApiParamType.NOAUTH, isRequired = true, desc = "新可处理对象列表，[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]"),
			@Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源"),
			@Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "原因")
	})
	@Output({})
	@Description(desc = "工单转交接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");       
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
		ProcessTaskStepVo processTaskStepVo = processTaskVo.getCurrentProcessTaskStep();		
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
		if(handler == null) {
            throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());			
		}     
        List<ProcessTaskStepWorkerVo> processTaskStepWorkerList =  new ArrayList<ProcessTaskStepWorkerVo>();
        List<String> workerList = new ArrayList<>();
        Object workerListObj = jsonObj.get("workerList");
        if(workerListObj instanceof JSONArray) {
            workerList = JSON.parseArray(JSON.toJSONString(workerListObj), String.class);
        }else if(workerListObj instanceof String) {
            workerList.add((String)workerListObj);
        }        
        for(String worker : workerList) {   
            String[] split = worker.split("#");
            if(GroupSearch.getValue(split[0]) != null) {
                processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(processTaskId, processTaskStepId, split[0], split[1], ProcessUserType.MAJOR.getValue()));
            }
        }
        processTaskStepVo.getParamObj().putAll(jsonObj);
		handler.transfer(processTaskStepVo,processTaskStepWorkerList);
		return null;
	}

}
