package codedriver.module.process.api.processtask;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.DELETE)
public class ProcessTaskDeleteApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "processtask/delete";
    }

    @Override
    public String getName() {
        return "批量删除工单";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
        @Param(name = "processTaskIdList", type = ApiParamType.JSONARRAY, desc = "工单id列表", isRequired = true)
    })
    @Description(desc = "批量删除工单")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
