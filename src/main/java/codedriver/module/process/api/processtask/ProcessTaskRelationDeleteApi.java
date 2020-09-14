package codedriver.module.process.api.processtask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class ProcessTaskRelationDeleteApi extends PrivateApiComponentBase {
    
    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getToken() {
        return "processtask/relation/delete";
    }

    @Override
    public String getName() {
        return "删除工单关联";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
        @Param(name = "processTaskRelationId", type = ApiParamType.LONG, isRequired = true, desc = "工单关联id")
    })
    @Description(desc = "删除工单关联")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskRelationId = jsonObj.getLong("processTaskRelationId");
        processTaskMapper.deleteProcessTaskRelationById(processTaskRelationId);
        return null;
    }

}
