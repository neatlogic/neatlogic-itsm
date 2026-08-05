package neatlogic.module.process.api.workcenter;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.module.process.dao.mapper.workcenter.WorkcenterMapper;
import neatlogic.framework.process.workcenter.dto.WorkcenterCatalogVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author longrf
 * @date 2022/1/10 2:34 下午
 */
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkcenterCatalogListApi extends PrivateApiComponentBase {

    @Resource
    WorkcenterMapper workcenterMapper;

    @Override
    public String getName() {
        return "nmpaw.workcentercataloglistapi.getname";
    }

    @Override
    public String getToken() {
        return "workcenter/catalog/list";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "nmpaw.workcentercataloglistapi.input.param.desc.keyword")
    })
    @Output({
            @Param(explode = WorkcenterCatalogVo.class,desc = "nmpaw.workcentercataloglistapi.output.param.desc.workcentercatalogvo")
    })
    @Description(desc = "nmpaw.workcentercataloglistapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        return workcenterMapper.getWorkcenterCatalogListByName(paramObj.getString("keyword"));
    }

}
