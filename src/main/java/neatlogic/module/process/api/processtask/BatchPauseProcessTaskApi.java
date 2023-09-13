/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import neatlogic.framework.process.stephandler.core.IProcessStepHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
//@Transactional
@OperationType(type = OperationTypeEnum.OPERATE)
public class BatchPauseProcessTaskApi extends PrivateApiComponentBase {
    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "批量暂停工单";
    }
    @Input({
            @Param(name = "processTaskIdList", type = ApiParamType.JSONARRAY, isRequired = true, minSize = 1, desc = "工单Id列表"),
            @Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "描述"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源")
    })
    @Output({})
    @Description(desc = "批量暂停工单")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String source = paramObj.getString("source");
        String content = paramObj.getString("content");
        List<Long> processTaskIdList = paramObj.getJSONArray("processTaskIdList").toJavaList(Long.class);
        for (Long processTaskId : processTaskIdList) {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
            if (processTaskVo == null) {
                return null;
            }
            List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
            for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
                if (Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStepStatus.RUNNING.getValue())) {
                    IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
                    if(handler == null) {
                        throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
                    }
                    processTaskStepVo.getParamObj().put("source", source);
                    processTaskStepVo.getParamObj().put("content", content);
                    try {
                        handler.pause(processTaskStepVo);
                    } catch (Exception e) {
                        // 忽略
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getToken() {
        return "processtask/batch/pause";
    }
}
