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

package neatlogic.module.process.api.processtask.test;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessStepMode;
import neatlogic.framework.process.constvalue.ProcessTaskStepOperationType;
import neatlogic.framework.process.dto.ProcessTaskStepRelVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.stephandler.core.ProcessTaskStepThread;
import neatlogic.framework.process.stephandler.core.ProcessTaskStepThreadComparator;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStepExecuteSortComparatorTestApi extends PrivateApiComponentBase {

    @Autowired
    ProcessTaskMapper processtaskMapper;

    @Override
    public String getName() {
        return "测试工单步骤执行排序比较器";
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid")
    })
    @Output({
            @Param(name = "tbodyList", explode = ProcessTaskStepVo.class, desc = "common.tbodylist")
    })
    @Description(desc = "测试工单步骤执行排序比较器")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long processTaskId = paramObj.getLong("processTaskId");
        List<ProcessTaskStepVo> processTaskStepList = processtaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
        ProcessTaskStepVo endProcessTaskStepVo = null;
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            if (Objects.equals(processTaskStepVo.getHandler(), "end")) {
                endProcessTaskStepVo = processTaskStepVo;
            }
        }
        List<ProcessTaskStepRelVo> processTaskStepRelList = processtaskMapper.getProcessTaskStepRelByProcessTaskId(processTaskId);
        ProcessTaskStepThreadComparator comparator = new ProcessTaskStepThreadComparator(processTaskStepRelList, endProcessTaskStepVo.getId());

        List<ProcessTaskStepThread> processTaskStepThreadList = new ArrayList<>();
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            ProcessTaskStepThread thread = new ProcessTaskStepThread(ProcessTaskStepOperationType.STEP_ACTIVE, processTaskStepVo, ProcessStepMode.MT) {
                @Override
                protected void myExecute(ProcessTaskStepVo processTaskStepVo) {

                }
            };
            processTaskStepThreadList.add(thread);
        }
        processTaskStepThreadList.sort(comparator);
        List<ProcessTaskStepVo> tbodyList = processTaskStepThreadList.stream().map(ProcessTaskStepThread::getProcessTaskStepVo).collect(Collectors.toList());
        return TableResultUtil.getResult(tbodyList);
    }

    @Override
    public String getToken() {
        return "processtask/step/executesortcomparator/test";
    }
}
