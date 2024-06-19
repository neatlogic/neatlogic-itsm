package neatlogic.module.process.api.workcenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.exception.workcenter.WorkcenterNotFoundException;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.workcenter.WorkcenterMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Transactional
@Service
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class SaveWorkcenterTheadApi extends PrivateApiComponentBase {

    @Resource
    WorkcenterMapper workcenterMapper;

    @Override
    public String getToken() {
        return "workcenter/thead/save";
    }

    @Override
    public String getName() {
        return "nmpaw.saveworkcentertheadapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "common.typeuuid", isRequired = true),
            @Param(name = "theadList", type = ApiParamType.JSONARRAY, desc = "common.typeuuid", isRequired = true),
            @Param(name = "theadList[0].name", type = ApiParamType.STRING, desc = "nmpaw.saveworkcentertheadapi.input.param.desc.name"),
            @Param(name = "theadList[0].width", type = ApiParamType.INTEGER, desc = "nmpaw.saveworkcentertheadapi.input.param.desc.width"),
            @Param(name = "theadList[0].isShow", type = ApiParamType.INTEGER, desc = "nmpaw.saveworkcentertheadapi.input.param.desc.isshow"),
            @Param(name = "theadList[0].type", type = ApiParamType.STRING, desc = "nmpaw.saveworkcentertheadapi.input.param.desc.type"),
            @Param(name = "theadList[0].sort", type = ApiParamType.INTEGER, desc = "nmpaw.saveworkcentertheadapi.input.param.desc.sort")
    })
    @Output({

    })
    @Description(desc = "nmpaw.saveworkcentertheadapi.description.desc")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        WorkcenterVo workcenterVo = JSON.toJavaObject(jsonObj, WorkcenterVo.class);
        if (workcenterMapper.getWorkcenterByUuid(workcenterVo.getUuid()) == null) {
            throw new WorkcenterNotFoundException(workcenterVo.getUuid());
        }
        if (StringUtils.isNotBlank(workcenterVo.getTheadConfigStr())) {
            workcenterMapper.insertWorkcenterThead(workcenterVo, UserContext.get().getUserUuid(true));
            workcenterMapper.insertWorkcenterTheadConfig(workcenterVo.getTheadConfigHash(), workcenterVo.getTheadConfigStr());
        }
        return null;
    }

}
