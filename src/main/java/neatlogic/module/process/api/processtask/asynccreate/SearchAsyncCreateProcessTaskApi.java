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

package neatlogic.module.process.api.processtask.asynccreate;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.process.auth.PROCESSTASK_MODIFY;
import neatlogic.framework.process.dto.ProcessTaskAsyncCreateVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskAsyncCreateMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = PROCESSTASK_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchAsyncCreateProcessTaskApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskAsyncCreateMapper processTaskAsyncCreateMapper;

    @Override
    public String getName() {
        return "nmpapa.searchasynccreateprocesstaskapi.getname";
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "nmpapa.searchasynccreateprocesstaskapi.input.param.desc.id"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
    })
    @Output({
            @Param(name = "tbodyList", explode = ProcessTaskAsyncCreateVo[].class, desc = "term.itsm.processtaskidlist"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "nmpapa.searchasynccreateprocesstaskapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<ProcessTaskAsyncCreateVo> tbodyList = new ArrayList<>();
        Long id = paramObj.getLong("id");
        if (id != null) {
            ProcessTaskAsyncCreateVo processTaskAsyncCreateVo = processTaskAsyncCreateMapper.getProcessTaskAsyncCreateById(id);
            if (processTaskAsyncCreateVo != null) {
                tbodyList.add(processTaskAsyncCreateVo);
            }
            return TableResultUtil.getResult(tbodyList);
        }
        ProcessTaskAsyncCreateVo searchVo = paramObj.toJavaObject(ProcessTaskAsyncCreateVo.class);
        int rowNum = processTaskAsyncCreateMapper.getProcessTaskAsyncCreateFailedCount(searchVo);
        if (rowNum > 0) {
            searchVo.setRowNum(rowNum);
            tbodyList = processTaskAsyncCreateMapper.getProcessTaskAsyncCreateFailedList(searchVo);
            return TableResultUtil.getResult(tbodyList, searchVo);
        }
        return TableResultUtil.getResult(tbodyList, searchVo);
    }

    @Override
    public String getToken() {
        return "processtask/asynccreate/search";
    }
}
