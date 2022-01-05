package codedriver.module.process.api.workcenter;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.exception.workcenter.WorkcenterNotFoundException;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkcenterGetApi extends PrivateApiComponentBase {

    @Resource
    WorkcenterMapper workcenterMapper;
    @Resource
    UserMapper userMapper;

    @Override
    public String getToken() {
        return "workcenter/get";
    }

    @Override
    public String getName() {
        return "获取工单中心分类接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "分类uuid", isRequired = true)
    })
    @Output({
            @Param(name = "workcenter", explode = WorkcenterVo.class, desc = "分类信息")
    })
    @Description(desc = "获取工单中心分类接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        WorkcenterVo workcenter = workcenterMapper.getWorkcenterByUuid(uuid);
        if (workcenter == null) {
            throw new WorkcenterNotFoundException(uuid);
        }
        return workcenter;
    }
}
