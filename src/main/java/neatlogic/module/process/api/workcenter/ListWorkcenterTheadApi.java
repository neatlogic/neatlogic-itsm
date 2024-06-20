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

package neatlogic.module.process.api.workcenter;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnFactory;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.exception.workcenter.WorkcenterNotFoundException;
import neatlogic.framework.process.workcenter.dto.WorkcenterTheadVo;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.workcenter.WorkcenterMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListWorkcenterTheadApi extends PrivateApiComponentBase {

    @Resource
    WorkcenterMapper workcenterMapper;

    @Override
    public String getToken() {
        return "workcenter/thead/list";
    }

    @Override
    public String getName() {
        return "nmpaw.listworkcentertheadapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "common.typeuuid")
    })
    @Output({
            @Param(explode = WorkcenterTheadVo.class, desc = "nmpaw.listworkcentertheadapi.output.param.desc")
    })
    @Description(desc = "nmpaw.listworkcentertheadapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<WorkcenterTheadVo> theadList = new ArrayList<>();
        String uuid = jsonObj.getString("uuid");
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        if(StringUtils.isNotBlank(uuid)){
            WorkcenterVo workcenter = workcenterMapper.getWorkcenterByUuid(uuid);
            if (workcenter == null) {
                throw new WorkcenterNotFoundException(uuid);
            }
            if (StringUtils.isNotBlank(workcenter.getTheadConfigHash())) {
                String theadConfigStr = workcenterMapper.getWorkcenterTheadConfigByHash(workcenter.getTheadConfigHash());
                workcenter.setTheadConfigStr(theadConfigStr);
                theadList = workcenter.getTheadList();
            }
            //多删
            theadList.removeIf(thead -> !columnComponentMap.containsKey(thead.getName()));
        }
        // 少补
        for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
            IProcessTaskColumn column = entry.getValue();
            if (Objects.equals(column.getType(), ProcessFieldType.COMMON.getValue()) && CollectionUtils.isEmpty(theadList.stream()
                    .filter(data -> column.getName().endsWith(data.getName())).collect(Collectors.toList()))) {
                if (Boolean.TRUE.equals(column.getDisabled())) {
                    continue;
                }
                theadList.add(new WorkcenterTheadVo(column));
            }
        }
        return theadList.stream().sorted(Comparator.comparing(WorkcenterTheadVo::getSort)).collect(Collectors.toList());
    }
}
