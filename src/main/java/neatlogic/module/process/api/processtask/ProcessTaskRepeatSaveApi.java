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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.dto.ProcessTaskRepeatVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskNotFoundException;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.SnowflakeUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author linbq
 * @since 2021/9/13 14:46
 **/
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class ProcessTaskRepeatSaveApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Override
    public String getToken() {
        return "processtask/repeat/save";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskrepeatsaveapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "term.itsm.processtaskid"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "common.source"),
            @Param(name = "repeatProcessTaskIdList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "term.itsm.repeatprocesstaskidlist")
    })
    @Output({
            @Param(explode = ProcessTaskVo[].class, desc = "common.tbodylist")
    })
    @Description(desc = "nmpap.processtaskrepeatsaveapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long processTaskId = paramObj.getLong("processTaskId");
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_MARKREPEAT)
                .build()
                .checkAndNoPermissionThrowException();
        JSONArray repeatProcessTaskIdArray = paramObj.getJSONArray("repeatProcessTaskIdList");
        if (CollectionUtils.isEmpty(repeatProcessTaskIdArray)) {
            throw new ParamNotExistsException("repeatProcessTaskIdList");
        }
        List<Long> repeatProcessTaskIdList = repeatProcessTaskIdArray.toJavaList(Long.class);
        Set<Long> repeatProcessTaskIdSet = new HashSet<>(repeatProcessTaskIdList);
        repeatProcessTaskIdList = new ArrayList<>(repeatProcessTaskIdSet);
        List<Long> processTaskIdList = processTaskMapper.checkProcessTaskIdListIsExists(repeatProcessTaskIdList);
        if (processTaskIdList.size() < repeatProcessTaskIdList.size()) {
            repeatProcessTaskIdList.removeAll(processTaskIdList);
            throw new ProcessTaskNotFoundException(repeatProcessTaskIdList);
        }
        List<Long> markedprocessTaskIdList = new ArrayList<>();
        Long repeatGroupId = processTaskMapper.getRepeatGroupIdByProcessTaskId(processTaskId);
        if (repeatGroupId != null) {
            markedprocessTaskIdList = processTaskMapper.getProcessTaskIdListByRepeatGroupId(repeatGroupId);
            repeatProcessTaskIdList.removeAll(markedprocessTaskIdList);
        } else {
            repeatGroupId = SnowflakeUtil.uniqueLong();
        }

        if (CollectionUtils.isEmpty(repeatProcessTaskIdList)) {
            return null;
        }
        Set<Long> allRepeatProcessTaskIdSet = new HashSet<>();
        for (Long repeatProcessTaskId : repeatProcessTaskIdList) {
            getRepeatProcessTaskList(repeatProcessTaskId, allRepeatProcessTaskIdSet);
        }
        List<Long> allRepeatProcessTaskIdList = new ArrayList<>(allRepeatProcessTaskIdSet);
        allRepeatProcessTaskIdList.removeAll(markedprocessTaskIdList);
        if (CollectionUtils.isEmpty(allRepeatProcessTaskIdList)) {
            return null;
        }
        List<Long> removeAll = ListUtils.removeAll(allRepeatProcessTaskIdList, repeatProcessTaskIdList);
        List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskListByIdList(allRepeatProcessTaskIdList);
        if (CollectionUtils.isNotEmpty(removeAll)) {
            return processTaskList;
        }
        List<ProcessTaskVo> runningProcessTaskList = new ArrayList<>();
        List<ProcessTaskRepeatVo> processTaskRepeatList = new ArrayList<>();
        String source = paramObj.getString("source");
        for (ProcessTaskVo processTaskVo : processTaskList) {
            //1. 如果工单不是取消状态，则取消工单
            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
                runningProcessTaskList.add(processTaskVo);
            }
            processTaskRepeatList.add(new ProcessTaskRepeatVo(processTaskVo.getId(), repeatGroupId));
            if (processTaskRepeatList.size() >= 1000) {
                processTaskMapper.replaceProcessTaskRepeatList(processTaskRepeatList);
                processTaskRepeatList.clear();
            }
            ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
            processTaskStepVo.setProcessTaskId(processTaskVo.getId());
            processTaskStepVo.getParamObj().put("source", source);
            processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.BINDREPEAT);
        }
        if (CollectionUtils.isNotEmpty(processTaskRepeatList)) {
            processTaskMapper.replaceProcessTaskRepeatList(processTaskRepeatList);
        }
        processTaskMapper.replaceProcessTaskRepeat(new ProcessTaskRepeatVo(processTaskId, repeatGroupId));
        ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
        processTaskStepVo.setProcessTaskId(processTaskId);
        processTaskStepVo.getParamObj().put("source", source);
        processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.BINDREPEAT);

        for (ProcessTaskVo processTaskVo : runningProcessTaskList) {
            //当前用户可能没有工单的取消权限，所以用系统用户操作
            UserContext.init(SystemUser.SYSTEM);
            ProcessStepHandlerFactory.getHandler().abortProcessTask(processTaskVo);
            ProcessTaskStepVo processTaskStep = new ProcessTaskStepVo();
            processTaskStep.setProcessTaskId(processTaskVo.getId());
            processTaskStep.getParamObj().put("source", source);
            processStepHandlerUtil.notify(processTaskStep, ProcessTaskNotifyTriggerType.MARKREPEATPROCESSTASK);
        }
        return null;
    }

    private void getRepeatProcessTaskList(Long processTaskId, Set<Long> resultSet) {
        resultSet.add(processTaskId);
        //2. 如果工单在另一个重复工单组A中，则把工单组A的所有工单加到新的重复工单组B
        Long repeatGroupId = processTaskMapper.getRepeatGroupIdByProcessTaskId(processTaskId);
        if (repeatGroupId != null) {
            List<Long> repeatProcessTaskIdList = processTaskMapper.getProcessTaskIdListByRepeatGroupId(repeatGroupId);
            for (Long repeatProcessTaskId : repeatProcessTaskIdList) {
                if (resultSet.contains(repeatProcessTaskId)) {
                    continue;
                }
                getRepeatProcessTaskList(repeatProcessTaskId, resultSet);
            }
        }
    }
}
