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

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskStepOperationType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskCanReactivateStepListApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;
    @Override
    public String getName() {
        return "当前用户可以重新激活的步骤列表";
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "term.itsm.processtaskid", isRequired = true)
    })
    @Output({
            @Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "当前用户可以重新激活的步骤列表")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long processTaskId = paramObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskVo.getId());
        List<Long> processTaskStepIdList = processTaskStepList.stream().map(ProcessTaskStepVo::getId).collect(Collectors.toList());
        Map<Long, Set<IOperationType>> operateMap = new ProcessAuthManager.Builder().addProcessTaskStepId(processTaskStepIdList).addOperationType(ProcessTaskStepOperationType.STEP_REACTIVATE).build().getOperateMap();
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            Set<IOperationType> operationTypeSet = operateMap.get(processTaskStepVo.getId());
            if (CollectionUtils.isNotEmpty(operationTypeSet) && operationTypeSet.contains(ProcessTaskStepOperationType.STEP_REACTIVATE)) {
                resultList.add(processTaskStepVo);
            }
        }
        return resultList;
    }

    @Override
    public String getToken() {
        return "processtask/canreactivatestep/list";
    }
}
