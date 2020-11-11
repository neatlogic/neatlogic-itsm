package codedriver.module.process.api.processtask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskTagUpdateApi extends PrivateApiComponentBase{

    @Autowired
    ProcessTaskService processTaskService;
    
    @Override
    public String getToken() {
        return "processtask/tag/update";
    }

    @Override
    public String getName() {
        return "更新工单标签";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
        @Param(name="processTaskId", type = ApiParamType.LONG, desc="工单id",isRequired=true), 
        @Param(name="processTaskStepId", type = ApiParamType.LONG, desc="工单步骤id",isRequired=true), 
        @Param(name="tagList", type=ApiParamType.JSONARRAY, desc = "标签列表",isRequired=true)
    })
    @Output({
       
    })
    @Description(desc = "更新工单标签")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        processTaskService.updateTag(processTaskId, processTaskStepId,jsonObj);
        return null;
    }

}
