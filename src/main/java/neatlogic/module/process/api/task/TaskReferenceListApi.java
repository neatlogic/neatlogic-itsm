/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
 */

package neatlogic.module.process.api.task;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.task.TaskMapper;
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
        return "获取子任务被引用的流程列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", isRequired = true, type = ApiParamType.STRING, desc = "子任务策略id"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, isRequired = true, desc = "当前页码"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, isRequired = true, desc = "页大小"),
            @Param(name = "pageCount", type = ApiParamType.INTEGER, isRequired = true, desc = "总页数"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, isRequired = true, desc = "总行数"),
            @Param(name = "tbodyList", explode = ValueTextVo[].class, desc = "流程列表")
    })
    @Description(desc = "获取子任务被引用的流程列表接口")
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
