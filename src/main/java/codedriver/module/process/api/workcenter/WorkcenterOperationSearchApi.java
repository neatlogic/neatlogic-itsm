package codedriver.module.process.api.workcenter;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.NewWorkcenterService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkcenterOperationSearchApi extends PrivateApiComponentBase {
    @Resource
    NewWorkcenterService newWorkcenterService;

    @Override
    public String getToken() {
        return "workcenter/operation/list";
    }

    @Override
    public String getName() {
        return "搜索工单中心操作";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskIdList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "工单id列表"),
    })
    @Output({
    })
    @Description(desc = "工单中心操作搜索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<Long> processTaskIdList = jsonObj.getJSONArray("processTaskIdList").toJavaList(Long.class);
        return newWorkcenterService.doSearch(processTaskIdList);
    }
}
