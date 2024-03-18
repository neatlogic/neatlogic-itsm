/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ProcessTaskSlaTimeVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListProcessTaskSlaTimeApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/slatime/list";
    }

    @Override
    public String getName() {
        return "工单时效列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "slaIdList", type = ApiParamType.JSONARRAY, isRequired = true, minSize = 1, desc = "时效ID列表")
    })
    @Output({
            @Param(name = "tbodyList", explode = ProcessTaskSlaTimeVo[].class, desc = "时效列表")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONArray slaIdArray = paramObj.getJSONArray("slaIdList");
        List<Long> slaIdList = slaIdArray.toJavaList(Long.class);
        List<ProcessTaskSlaTimeVo> processTaskSlaTimeList = processTaskService.getSlaTimeListBySlaIdList(slaIdList);
        return TableResultUtil.getResult(processTaskSlaTimeList);
    }
}
