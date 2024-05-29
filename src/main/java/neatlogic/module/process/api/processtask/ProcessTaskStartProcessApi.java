package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStartProcessApi extends PrivateApiComponentBase {
    
    @Autowired
    private ProcessTaskService processTaskService;

	@Resource
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getToken() {
		return "processtask/startprocess";
	}

	@Override
	public String getName() {
		return "工单上报提交接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id"),
		@Param(name = "nextStepId", type = ApiParamType.LONG, desc = "激活下一步骤Id（如果有且仅有一个下一节点，则可以不传这个参数）"),
		@Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源"),
		@Param(name = "assignWorkerList", type = ApiParamType.JSONARRAY, desc = "分配步骤处理人信息列表，格式[{\"processTaskStepId\":1, \"processStepUuid\":\"abc\", \"workerList\":[\"user#xxx\",\"team#xxx\",\"role#xxx\"]}]")
	})
	@Description(desc = "工单上报提交接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
//		Long processTaskId = jsonObj.getLong("processTaskId");
//        Long nextStepId = jsonObj.getLong("nextStepId");
//        processTaskService.checkProcessTaskParamsIsLegal(processTaskId, null, nextStepId);
//
//		ProcessTaskStepVo startProcessTaskStepVo = processTaskMapper.getStartProcessTaskStepByProcessTaskId(processTaskId);
//		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(startProcessTaskStepVo.getHandler());
//		if(handler == null) {
//            throw new ProcessStepHandlerNotFoundException(startProcessTaskStepVo.getHandler());
//		}
//
//		ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
//        processTaskStepDataVo.setProcessTaskId(startProcessTaskStepVo.getProcessTaskId());
//        processTaskStepDataVo.setProcessTaskStepId(startProcessTaskStepVo.getId());
//        processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
//        processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
//        processTaskStepDataVo = processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
//        if(processTaskStepDataVo != null) {
//            JSONObject dataObj = processTaskStepDataVo.getData();
//            if (MapUtils.isNotEmpty(dataObj)) {
//                jsonObj.putAll(dataObj);
//            }
//        }
//        startProcessTaskStepVo.getParamObj().putAll(jsonObj);
//        try {
//            handler.startProcess(startProcessTaskStepVo);
//			processTaskStepDataMapper.deleteProcessTaskStepData(processTaskStepDataVo);
//        }catch(ProcessTaskNoPermissionException e) {
//            throw new PermissionDeniedException();
//        }
		processTaskService.startProcessProcessTask(jsonObj);
		return null;
	}

}
