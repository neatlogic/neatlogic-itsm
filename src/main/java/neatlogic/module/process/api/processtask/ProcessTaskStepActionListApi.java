package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@AuthAction(action = PROCESS_BASE.class)
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
        return "获取工单步骤当前用户操作权限列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
        @Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "工单步骤id")})
    @Output({@Param(name = "Return", explode = ValueTextVo[].class, desc = "当前用户操作权限列表")})
    @Description(desc = "获取工单步骤当前用户操作权限列表")
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
