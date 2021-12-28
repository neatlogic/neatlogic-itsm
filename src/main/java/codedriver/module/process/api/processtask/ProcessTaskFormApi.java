package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskFormApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/step/form";
    }

    @Override
    public String getName() {
        return "查询工单步骤表单数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
        @Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "工单步骤id")})
    @Output({@Param(name = "formAttributeDataMap", type = ApiParamType.JSONOBJECT, desc = "工单信息"),
        @Param(name = "formConfig", type = ApiParamType.JSONOBJECT, desc = "工单信息")})
    @Description(desc = "查询工单步骤表单数据")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_VIEW).build()
            .checkAndNoPermissionThrowException();
        /** 检查工单是否存在表单 **/
        processTaskService.setProcessTaskFormInfo(processTaskVo);
        if (MapUtils.isNotEmpty(processTaskVo.getFormConfig())) {
            if (processTaskStepId != null) {
                if (new ProcessAuthManager.StepOperationChecker(processTaskStepId, ProcessTaskOperationType.STEP_VIEW).build().check()) {
                    /** 查出暂存数据中的表单数据 **/
                    ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
                    processTaskStepVo.setId(processTaskStepId);
                    processTaskStepVo.setProcessTaskId(processTaskId);
                    processTaskService.setTemporaryData(processTaskVo, processTaskStepVo);
                }
            }

            resultObj.put("formAttributeDataMap", processTaskVo.getFormAttributeDataMap());
            resultObj.put("formConfig", processTaskVo.getFormConfig());
            resultObj.put("formConfigAuthorityList", processTaskVo.getFormConfigAuthorityList());
            resultObj.put("formAttributeHideList", processTaskVo.getFormAttributeHideList());
        }

        return resultObj;
    }

}
