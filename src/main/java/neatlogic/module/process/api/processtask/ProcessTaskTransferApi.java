package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskStepDataType;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.dto.ProcessTaskStepDataVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import neatlogic.framework.process.stephandler.core.IProcessStepHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepDataMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskTransferApi extends PrivateApiComponentBase {
    
    @Autowired
    private ProcessTaskService processTaskService;

	@Autowired
	private ProcessTaskStepDataMapper processTaskStepDataMapper;
	
	@Override
	public String getToken() {
		return "processtask/transfer";
	}

	@Override
	public String getName() {
		return "nmpap.processtasktransferapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
			@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
			@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskstepid"),
			@Param(name = "workerList", type = ApiParamType.NOAUTH, isRequired = true, desc = "nmpap.processtasktransferapi.input.param.desc.workerlist", help = "[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]"),
			@Param(name = "isSaveData", type = ApiParamType.ENUM, rule = "0,1", desc = "nmpap.processtasktransferapi.input.param.desc.issavedata"),
			@Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "common.source"),
			@Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "common.content")
	})
	@Output({})
	@Description(desc = "nmpap.processtasktransferapi.getname")
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
		Integer isSaveData = jsonObj.getInteger("isSaveData");
		if (Objects.equals(isSaveData, 1)) {
			JSONObject data = processTaskService.getProcessTaskStepStagingData(processTaskId, processTaskStepId);
			processTaskStepVo.getParamObj().putAll(data);
		}
		processTaskStepVo.getParamObj().putAll(jsonObj);
		handler.transfer(processTaskStepVo,processTaskStepWorkerList);
		if (Objects.equals(isSaveData, 1)) {
			ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
			processTaskStepDataVo.setProcessTaskId(processTaskId);
			processTaskStepDataVo.setProcessTaskStepId(processTaskStepId);
			processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
			processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
			processTaskStepDataMapper.deleteProcessTaskStepData(processTaskStepDataVo);
		}
		return null;
	}

}
