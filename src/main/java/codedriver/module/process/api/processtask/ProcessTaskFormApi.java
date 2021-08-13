package codedriver.module.process.api.processtask;

import java.util.HashMap;
import java.util.Map;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.auth.PROCESS_BASE;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
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
@Deprecated//这个接口前端没有使用
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskFormApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskService processTaskService;

    @Autowired
    private ProcessTaskStepDataMapper processTaskStepDataMapper;

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
        ProcessTaskVo processTaskVo =
            processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.TASK_VIEW).build()
            .checkAndNoPermissionThrowException();
        /** 检查工单是否存在表单 **/
        processTaskService.setProcessTaskFormInfo(processTaskVo);
        if (MapUtils.isNotEmpty(processTaskVo.getFormConfig())) {
            if (processTaskStepId != null) {
                if (new ProcessAuthManager.StepOperationChecker(processTaskStepId, ProcessTaskOperationType.STEP_VIEW)
                    .build().check()) {
                    /** 查出暂存数据中的表单数据 **/
                    ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
                    processTaskStepDataVo.setProcessTaskId(processTaskId);
                    processTaskStepDataVo.setProcessTaskStepId(processTaskStepId);
                    processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
                    processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
                    ProcessTaskStepDataVo stepDraftSaveData =
                        processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
                    if (stepDraftSaveData != null) {
                        JSONObject dataObj = stepDraftSaveData.getData();
                        if (MapUtils.isNotEmpty(dataObj)) {
                            JSONArray formAttributeDataList = dataObj.getJSONArray("formAttributeDataList");
                            if (CollectionUtils.isNotEmpty(formAttributeDataList)) {
                                Map<String, Object> formAttributeDataMap = new HashMap<>();
                                for (int i = 0; i < formAttributeDataList.size(); i++) {
                                    JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
                                    formAttributeDataMap.put(formAttributeDataObj.getString("attributeUuid"),
                                        formAttributeDataObj.get("dataList"));
                                }
                                processTaskVo.setFormAttributeDataMap(formAttributeDataMap);
                            }
                        }
                    }
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
