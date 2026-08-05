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

package neatlogic.module.process.api.task;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.module.process.dao.mapper.task.TaskMapper;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service

@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TaskReferenceListApi extends PrivateApiComponentBase {
    @Resource
    TaskMapper taskMapper;

    @Override
    public String getToken() {
        return "task/reference/list";
    }

    @Override
    public String getName() {
        return "nmpat.taskreferencelistapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", isRequired = true, type = ApiParamType.STRING, desc = "nmpat.taskreferencelistapi.input.param.desc.id"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "nmpat.taskreferencelistapi.input.param.desc.needpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "nmpat.taskreferencelistapi.input.param.desc.pagesize"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "nmpat.taskreferencelistapi.input.param.desc.currentpage")
    })
    @Output({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpat.taskreferencelistapi.output.param.desc.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpat.taskreferencelistapi.output.param.desc.pagesize"),
            @Param(name = "pageCount", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpat.taskreferencelistapi.output.param.desc.pagecount"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpat.taskreferencelistapi.output.param.desc.rownum"),
            @Param(name = "tbodyList", explode = ValueTextVo[].class, desc = "nmpat.taskreferencelistapi.output.param.desc.tbodylist")
    })
    @Description(desc = "nmpat.taskreferencelistapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        jsonObj.remove("id");
        BasePageVo basePageVo = JSONObject.toJavaObject(jsonObj, BasePageVo.class);
        List<ValueTextVo> tbodyList = new ArrayList<>();
        int rowNum = taskMapper.getTaskConfigReferenceProcessCount(id);
        if (rowNum > 0) {
            basePageVo.setRowNum(rowNum);
            tbodyList = taskMapper.getTaskConfigReferenceProcessList(id, basePageVo);
        }
        return TableResultUtil.getResult(tbodyList, null, basePageVo);
    }

}
