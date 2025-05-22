/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.module.process.service.ProcessTaskService;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author linbq
 * @since 2021/9/14 11:09
 **/
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class ProcessTaskRepeatDeleteApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Override
    public String getToken() {
        return "processtask/repeat/delete";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskrepeatdeleteapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "repeatProcessTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.repeatprocesstaskid"),
            @Param(name = "source", type = ApiParamType.STRING, desc = "common.source"),// ok
    })
    @Description(desc = "nmpap.processtaskrepeatdeleteapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String source = paramObj.getString("source");
        Long processTaskId = paramObj.getLong("processTaskId");
        Long repeatProcessTaskId = paramObj.getLong("repeatProcessTaskId");
        ProcessTaskVo processTask = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        ProcessTaskVo repeatProcessTask = processTaskService.checkProcessTaskParamsIsLegal(repeatProcessTaskId);
        Long repeatGroupId = processTaskMapper.getRepeatGroupIdByProcessTaskId(repeatProcessTaskId);
        if (repeatGroupId != null) {
            processTaskMapper.deleteProcessTaskRepeatByProcessTaskId(repeatProcessTaskId);
            {
                ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
                processTaskStepVo.setProcessTaskId(repeatProcessTaskId);
                processTaskStepVo.getParamObj().put("source", source);
                processTaskStepVo.getParamObj().put("repeatProcessTaskSerialNumber", processTask.getSerialNumber());
                processTaskStepVo.getParamObj().put("repeatProcessTaskTitle", processTask.getTitle());
                processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.UNBOUNDREPEAT);
            }
            List<Long> repeatProcessTaskIdList = processTaskMapper.getProcessTaskIdListByRepeatGroupId(repeatGroupId);
            if (repeatProcessTaskIdList.size() == 1) {
                processTaskMapper.deleteProcessTaskRepeatByProcessTaskId(repeatProcessTaskIdList.get(0));
            }
            {
                ProcessTaskStepVo processTaskStep = new ProcessTaskStepVo();
                processTaskStep.setProcessTaskId(processTaskId);
                processTaskStep.getParamObj().put("source", source);
                processTaskStep.getParamObj().put("repeatProcessTaskSerialNumber", repeatProcessTask.getSerialNumber());
                processTaskStep.getParamObj().put("repeatProcessTaskTitle", repeatProcessTask.getTitle());
                processStepHandlerUtil.audit(processTaskStep, ProcessTaskAuditType.UNBINDREPEAT);
            }
        }
        return null;
    }
}
