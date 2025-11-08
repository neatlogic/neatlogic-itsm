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
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class BatchHideProcessTaskApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "nmpap.batchhideprocesstaskapi.getname";
    }
    @Input({
            @Param(name = "processTaskIdList", type = ApiParamType.JSONARRAY, isRequired = true, minSize = 1, desc = "term.itsm.processtaskidlist"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "common.source")
    })
    @Output({})
    @Description(desc = "nmpap.batchhideprocesstaskapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<Long> processTaskIdList = paramObj.getJSONArray("processTaskIdList").toJavaList(Long.class);
        for (Long processTaskId : processTaskIdList) {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
            if (processTaskVo == null) {
                return null;
            }
            boolean flag = new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_HIDE)
                    .build()
                    .check();
            if (flag) {
                processTaskVo.setIsShow(0);
                processTaskMapper.updateProcessTaskIsShow(processTaskVo);
            }
        }
        return null;
    }

    @Override
    public String getToken() {
        return "processtask/batch/hide";
    }
}
