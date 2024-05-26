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

package neatlogic.module.process.api.processtask.test;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessStepMode;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
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
            ProcessTaskStepThread thread = new ProcessTaskStepThread(ProcessTaskOperationType.STEP_ACTIVE, processTaskStepVo, ProcessStepMode.MT) {
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
