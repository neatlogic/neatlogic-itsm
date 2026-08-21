package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.CatalogChannelAuthorityAction;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.CatalogService;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStartProcessApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private CatalogService catalogService;

    @Override
    public String getToken() {
        return "processtask/startprocess";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskstartprocessapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "nmpap.processtaskstartprocessapi.input.param.desc.processtaskid"),
            @Param(name = "nextStepId", type = ApiParamType.LONG, desc = "nmpap.processtaskstartprocessapi.input.param.desc.nextstepid"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "nmpap.processtaskstartprocessapi.input.param.desc.source"),
            @Param(name = "assignWorkerList", type = ApiParamType.JSONARRAY, desc = "nmpap.processtaskstartprocessapi.input.param.desc.assignworkerlist")
    })
    @Description(desc = "nmpap.processtaskstartprocessapi.getname")
    @Override
    @ResubmitInterval(3)
    public Object myDoService(JSONObject jsonObj) throws Exception {
        // 提交时再次校验代报授权，防止调用者绕过前端直接提交他人的工单。
        Long processTaskId = jsonObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
        String currentUserUuid = UserContext.get().getUserUuid(true);
        if (processTaskVo != null && StringUtils.isNotBlank(processTaskVo.getOwner())
                && !currentUserUuid.equals(processTaskVo.getOwner())
                && !catalogService.channelIsAuthority(processTaskVo.getChannelUuid(), currentUserUuid, CatalogChannelAuthorityAction.DELEGATE)) {
            throw new PermissionDeniedException();
        }
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
