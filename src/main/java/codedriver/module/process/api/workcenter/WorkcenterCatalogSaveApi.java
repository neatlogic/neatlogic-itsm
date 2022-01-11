package codedriver.module.process.api.workcenter;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.exception.workcenter.WorkcenterCatalogIdNotFoundException;
import codedriver.framework.process.exception.workcenter.WorkcenterCatalogNameRepeatsException;
import codedriver.framework.process.workcenter.dto.WorkcenterCatalogVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author longrf
 * @date 2022/1/10 2:34 下午
 */
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class WorkcenterCatalogSaveApi extends PrivateApiComponentBase {


    @Resource
    WorkcenterMapper workcenterMapper;

    @Override
    public String getName() {
        return "保存工单中心菜单类型";
    }

    @Override
    public String getToken() {
        return "workcenter/catalog/save";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "类型id"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "类型名")
    })
    @Output({
            @Param(explode = WorkcenterCatalogVo.class,desc = "工单中心菜单类型")
    })
    @Description(desc = "保存工单中心菜单类型接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        WorkcenterCatalogVo catalogVo = JSON.toJavaObject(paramObj, WorkcenterCatalogVo.class);
        if (workcenterMapper.checkWorkcenterCatalogNameIsRepeats(catalogVo) > 0) {
            throw new WorkcenterCatalogNameRepeatsException(catalogVo.getName());
        }
        if (id != null) {
            if (workcenterMapper.checkWorkcenterCatalogIsExists(id) == 0) {
                throw new WorkcenterCatalogIdNotFoundException(id);
            }
        }
        workcenterMapper.insertWorkcenterCatalog(catalogVo);
        return catalogVo;
    }
}
