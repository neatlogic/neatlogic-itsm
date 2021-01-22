package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@Transactional
public class ProcessTaskStepActionListApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/step/action/list";
    }

    @Override
    public String getName() {
        return "工单步骤当前用户操作权限列表获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
        @Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "工单步骤id")})
    @Output({@Param(name = "Return", explode = ValueTextVo[].class, desc = "当前用户操作权限列表")})
    @Description(desc = "工单步骤当前用户操作权限列表获取接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo =
            processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        ProcessTaskStepVo processTaskStepVo = processTaskVo.getCurrentProcessTaskStep();
        Map<String, String> customButtonMap = new HashMap<>();
        if (processTaskStepVo != null) {
            customButtonMap = ProcessStepInternalHandlerFactory.getHandler().getCustomButtonMapByConfigHashAndHandler(processTaskStepVo.getConfigHash(), processTaskStepVo.getHandler());
        }
        List<ValueTextVo> resultList = new ArrayList<>();
        Map<Long, Set<ProcessTaskOperationType>> operationTypeSetMap = new ProcessAuthManager.Builder()
            .addProcessTaskId(processTaskId).addProcessTaskStepId(processTaskStepId).build().getOperateMap();
        for (Map.Entry<Long, Set<ProcessTaskOperationType>> entry : operationTypeSetMap.entrySet()) {
            for (ProcessTaskOperationType operationType : entry.getValue()) {
                String text = customButtonMap.get(operationType.getValue());
                if (StringUtils.isBlank(text)) {
                    text = operationType.getText();
                }
                if (StringUtils.isNotBlank(text)) {
                    ValueTextVo valueText = new ValueTextVo();
                    valueText.setValue(operationType.getValue());
                    valueText.setText(text);
                    resultList.add(valueText);
                }
            }
        }

        return resultList;
    }

}
