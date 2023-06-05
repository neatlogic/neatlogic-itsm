/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.api.workcenter;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.NewWorkcenterService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchWorkcenterApi extends PrivateApiComponentBase {
    @Resource
    WorkcenterMapper workcenterMapper;

    @Resource
    NewWorkcenterService newWorkcenterService;

    @Override
    public String getToken() {
        return "workcenter/search";
    }

    @Override
    public String getName() {
        return "nmpaw.searchworkcenterapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }
    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "common.typeuuid", isRequired = true),
            @Param(name = "conditionConfig", type = ApiParamType.JSONOBJECT, desc = "nmpaw.searchworkcenterapi.input.param.desc.conditionconfig"),
            @Param(name = "sortList", type = ApiParamType.JSONARRAY, desc = "common.sort"),
            @Param(name = "headerList", type = ApiParamType.JSONARRAY, desc = "nmpaw.searchworkcenterapi.input.param.desc.headerlist"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize")
    })
    @Output({
            @Param(name = "theadList", type = ApiParamType.JSONARRAY, desc = "common.theadlist"),
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "common.tbodylist"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "nmpaw.searchworkcenterapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        WorkcenterVo workcenterVo = JSONObject.toJavaObject(jsonObj, WorkcenterVo.class);
        if (MapUtils.isEmpty(workcenterVo.getConditionConfig())) {
            WorkcenterVo workcenter = workcenterMapper.getWorkcenterByUuid(uuid);
            workcenterVo.setConditionConfig(workcenter.getConditionConfig());
        }
        return newWorkcenterService.doSearch(workcenterVo);
    }

}
