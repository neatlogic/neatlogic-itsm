package codedriver.module.process.api.processtask;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.CREATE)
@Transactional
public class ProcessTaskTransferKnowledgeAuditApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "processtask/transfer/knowledge/audit";
    }

    @Override
    public String getName() {
        return "记录工单转知识活动";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
        @Param(name = "processtaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")
    })
    @Description(desc = "记录工单转知识活动")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
