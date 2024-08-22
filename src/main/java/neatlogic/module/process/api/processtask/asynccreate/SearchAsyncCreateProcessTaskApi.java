/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
            @Param(name = "id", type = ApiParamType.LONG, desc = "id"),
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
