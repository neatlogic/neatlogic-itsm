/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.api.workcenter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.NewWorkcenterService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetWorkcenterProcessTaskApi extends PrivateApiComponentBase {
    @Resource
    NewWorkcenterService newWorkcenterService;

    @Override
    public String getToken() {
        return "workcenter/processtask/get";
    }

    @Override
    public String getName() {
        return "工单中心获取具工单接口";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id", isRequired = true),
            @Param(name = "workcenterUuid", type = ApiParamType.STRING, desc = "工单中心分类uuid", isRequired = true)

    })
    @Output({
    })
    @Description(desc = "工单中心获取具工单，目前用于移动端刷新单个工单的场景")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        String workcenterUuid = jsonObj.getString("workcenterUuid");
        JSONObject conditionConfig = new JSONObject();
        JSONArray keywordConditionList = new JSONArray();
        JSONObject idCondition = new JSONObject();
        idCondition.put("name","id");
        idCondition.put("valueList", Collections.singletonList(processTaskId));
        keywordConditionList.add(idCondition);
        conditionConfig.put("keywordConditionList",keywordConditionList);
        WorkcenterVo workcenterVo = new WorkcenterVo();
        workcenterVo.setUuid(workcenterUuid);
        workcenterVo.setConditionConfig(conditionConfig);
        JSONObject result = newWorkcenterService.doSearch(workcenterVo);
        JSONObject action = newWorkcenterService.doSearch(Collections.singletonList(processTaskId));
        result.put("action",action.getJSONObject(processTaskId.toString()));
        return result;
    }

}
