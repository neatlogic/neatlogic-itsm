package codedriver.module.process.api.workcenter;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.auth.WORKCENTER_MODIFY;
import codedriver.framework.process.constvalue.ProcessWorkcenterType;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.exception.workcenter.WorkcenterCanNotEditFactoryException;
import codedriver.framework.process.exception.workcenter.WorkcenterNoCustomAuthException;
import codedriver.framework.process.exception.workcenter.WorkcenterNoModifyAuthException;
import codedriver.framework.process.exception.workcenter.WorkcenterNotFoundException;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Transactional
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class WorkcenterConditionSaveApi extends PrivateApiComponentBase {

    @Resource
    WorkcenterMapper workcenterMapper;
    @Resource
    UserMapper userMapper;
    @Resource
    RoleMapper roleMapper;

    @Override
    public String getToken() {
        return "workcenter/condition/save";
    }

    @Override
    public String getName() {
        return "工单中心分类条件修改接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "分类uuid", isRequired = true),
            @Param(name = "conditionConfig", type = ApiParamType.JSONOBJECT, desc = "分类过滤配置，json格式", isRequired = true)
    })
    @Output({

    })
    @Description(desc = "工单中心分类条件修改接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        String userUuid = UserContext.get().getUserUuid(true);
        WorkcenterVo workcenterVo = workcenterMapper.getWorkcenterByUuid(uuid);
        if (workcenterVo == null) {
            throw new WorkcenterNotFoundException(uuid);
        }
        if (ProcessWorkcenterType.FACTORY.getValue().equals(workcenterVo.getType())) {
            throw new WorkcenterCanNotEditFactoryException();
        } else if (ProcessWorkcenterType.SYSTEM.getValue().equals(workcenterVo.getType()) && !AuthActionChecker.check(WORKCENTER_MODIFY.class.getSimpleName())) {
            throw new WorkcenterNoModifyAuthException();
        } else if (ProcessWorkcenterType.CUSTOM.getValue().equals(workcenterVo.getType()) && !workcenterVo.getOwner().equalsIgnoreCase(UserContext.get().getUserUuid(true))) {
            throw new WorkcenterNoCustomAuthException();
        }
        workcenterVo.setConditionConfig(jsonObj.toJSONString());
        workcenterMapper.updateWorkcenterCondition(workcenterVo);
        return null;
    }

}
