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
        return "nmpap.processtaskcanreactivatesteplistapi.getname";
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "term.itsm.processtaskid", isRequired = true)
    })
    @Output({
            @Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmpap.processtaskcanreactivatesteplistapi.getname")
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
