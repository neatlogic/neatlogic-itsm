/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.api.workcenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.workcenter.WorkcenterMapper;
import neatlogic.module.process.service.NewWorkcenterService;
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
        WorkcenterVo workcenterVo = JSON.toJavaObject(jsonObj, WorkcenterVo.class);
        WorkcenterVo workcenter = workcenterMapper.getWorkcenterByUuid(uuid);
        workcenterVo.setTheadConfigHash(workcenter.getTheadConfigHash());
        if (MapUtils.isEmpty(workcenterVo.getConditionConfig())) {
            workcenterVo.setConditionConfig(workcenter.getConditionConfig());
        }
        return newWorkcenterService.doSearch(workcenterVo);
    }

}
