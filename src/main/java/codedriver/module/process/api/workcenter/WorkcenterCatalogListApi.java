package codedriver.module.process.api.workcenter;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.workcenter.dto.WorkcenterCatalogVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
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
        return "查询工单中心菜单类型列表";
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
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "类型名")
    })
    @Output({
            @Param(explode = WorkcenterCatalogVo.class,desc = "工单中心菜单类型列表")
    })
    @Description(desc = "查询工单中心菜单类型列表接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        return workcenterMapper.getWorkcenterCatalogListByName(paramObj.getString("name"));
    }

}
