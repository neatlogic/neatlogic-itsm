package neatlogic.module.process.api.workcenter;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.module.process.dao.mapper.workcenter.WorkcenterMapper;
import neatlogic.framework.process.exception.workcenter.WorkcenterCatalogIdNotFoundException;
import neatlogic.framework.process.exception.workcenter.WorkcenterCatalogIsUsedException;
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
public class WorkcenterCatalogDeleteApi extends PrivateApiComponentBase {

    @Resource
    WorkcenterMapper workcenterMapper;

    @Override
    public String getName() {
        return "删除工单中心菜单类型";
    }

    @Override
    public String getToken() {
        return "workcenter/catalog/delete";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "类型id")
    })
    @Output({
    })
    @Description(desc = "删除工单中心菜单类型接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        if (workcenterMapper.checkWorkcenterCatalogIsExists(id) == 0) {
            throw new WorkcenterCatalogIdNotFoundException(id);
        }
        if (workcenterMapper.checkWorkcenterCatalogIsUsed(id) > 0) {
            throw new WorkcenterCatalogIsUsedException(id);
        }
        workcenterMapper.deleteWorkcenterCatalogById(id);
        return null;
    }

}
