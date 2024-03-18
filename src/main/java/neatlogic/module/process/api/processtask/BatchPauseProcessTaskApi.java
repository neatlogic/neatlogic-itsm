/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
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
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class BatchPauseProcessTaskApi extends PrivateApiComponentBase {
    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "nmpap.batchpauseprocesstaskapi.getname";
    }
    @Input({
            @Param(name = "processTaskIdList", type = ApiParamType.JSONARRAY, isRequired = true, minSize = 1, desc = "term.itsm.processtaskidlist"),
            @Param(name = "content", type = ApiParamType.STRING, isRequired = true, desc = "common.content"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "common.source")
    })
    @Output({})
    @Description(desc = "nmpap.batchpauseprocesstaskapi.getname")
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
