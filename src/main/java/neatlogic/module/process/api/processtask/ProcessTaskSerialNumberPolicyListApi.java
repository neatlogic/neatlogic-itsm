package neatlogic.module.process.api.processtask;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.ProcessTaskSerialNumberPolicyHandlerFactory;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskSerialNumberPolicyListApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "processtask/serialnumber/policy/list";
    }

    @Override
    public String getName() {
        return "查询工单号生成规则列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(explode = ValueTextVo[].class, desc = "工单号生成规则列表")})
    @Description(desc = "查询工单号生成规则列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return ProcessTaskSerialNumberPolicyHandlerFactory.getPolicyHandlerList();
    }

}
