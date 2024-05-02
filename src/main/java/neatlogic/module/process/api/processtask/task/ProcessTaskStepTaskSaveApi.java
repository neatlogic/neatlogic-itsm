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

package neatlogic.module.process.api.processtask.task;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.*;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepTaskMapper;
import neatlogic.module.process.dao.mapper.task.TaskMapper;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import neatlogic.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import neatlogic.framework.process.exception.processtask.task.*;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyTriggerType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.service.UserService;
import neatlogic.module.process.service.ProcessTaskStepTaskService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lvzk
 * @since 2021/8/31 11:03
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskStepTaskSaveApi extends PrivateApiComponentBase {
    @Resource
    ProcessTaskMapper processTaskMapper;
    @Resource
    ProcessTaskStepTaskMapper processTaskStepTaskMapper;
    @Resource
    ProcessTaskStepTaskService processTaskStepTaskService;
    @Resource
    TaskMapper taskMapper;
    @Resource
    UserService userService;
    @Resource
    UserMapper userMapper;
    @Resource
    IProcessStepHandlerUtil processStepHandlerUtil;

    @Override
    public String getToken() {
        return "processtask/step/task/save";
    }

    @Override
    public String getName() {
        return "保存任务";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "任务id，如果不为空则是编辑，为空则新增"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "步骤id"),
            @Param(name = "stepTaskUserVoList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "任务处理人列表"),
            @Param(name = "taskConfigId", type = ApiParamType.LONG, isRequired = true, desc = "任务策略id"),
            @Param(name = "content", type = ApiParamType.STRING, isRequired = true, minLength = 1, desc = "描述"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.LONG, desc = "任务id")
    })
    @Description(desc = "保存任务接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessTaskStepTaskVo processTaskStepTaskVo = jsonObj.toJavaObject(ProcessTaskStepTaskVo.class);
        // 第一步 判断入参是否合法
        Long processTaskStepId = processTaskStepTaskVo.getProcessTaskStepId();
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
        if (processTaskStepVo == null) {
            throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
        }

        //获取流程步骤配置中的 任务策略和人员范围
        JSONObject taskConfig = processTaskStepTaskService.getTaskConfig(processTaskStepVo.getConfigHash());
        if (MapUtils.isEmpty(taskConfig)) {
            throw new TaskConfigException(processTaskStepVo.getName());
        }

        Long taskConfigId = processTaskStepTaskVo.getTaskConfigId();
        JSONArray taskConfigIdArray = taskConfig.getJSONArray("idList");
        if (CollectionUtils.isEmpty(taskConfigIdArray)) {
            throw new ProcessTaskStepTaskConfigIllegalException(taskConfigId.toString());
        }
        List<Long> taskConfigIdList = taskConfigIdArray.toJavaList(Long.class);
        if (!taskConfigIdList.contains(taskConfigId)) {
            throw new ProcessTaskStepTaskConfigIllegalException(taskConfigId.toString());
        }
        TaskConfigVo taskConfigVo = taskMapper.getTaskConfigById(taskConfigId);
        if (taskConfigVo == null) {
            throw new ProcessTaskStepTaskConfigIllegalException(taskConfigId.toString());
        }

        JSONArray stepTaskUserVoList = jsonObj.getJSONArray("stepTaskUserVoList");
        if (CollectionUtils.isEmpty(stepTaskUserVoList)) {
            throw new ParamIrregularException("stepTaskUserVoList");
        }
        int num = taskConfigVo.getNum();
        if (num != -1 && num != stepTaskUserVoList.size()) {
            throw new ProcessTaskStepTaskUserCountIllegalException(taskConfigVo.getName(), taskConfigVo.getNum());
        }
        List<String> allUserUuidList = new ArrayList<>();
        Set<String> newUserUuidSet = new HashSet<>();
        Map<Long, String> stepTaskUserMap = new HashMap<>();
        for (int i = 0; i < stepTaskUserVoList.size(); i++) {
            JSONObject stepTaskUserObj = stepTaskUserVoList.getJSONObject(i);
            String userUuid = stepTaskUserObj.getString("userUuid");
            if (StringUtils.isBlank(userUuid)) {
                throw new ParamIrregularException("stepTaskUserVoList[" + i + "].userUuid");
            }
            allUserUuidList.add(userUuid);
            Long stepTaskUserId = stepTaskUserObj.getLong("id");
            if (stepTaskUserId == null) {
                newUserUuidSet.add(userUuid);
                continue;
            }
            stepTaskUserMap.put(stepTaskUserId, userUuid);
        }

        JSONArray rangeList = taskConfig.getJSONArray("rangeList");
        if (CollectionUtils.isNotEmpty(rangeList)) {
            //校验用户是否在配置范围内
            checkUserIsLegal(allUserUuidList.stream().map(Object::toString).collect(Collectors.toList()), rangeList.stream().map(Object::toString).collect(Collectors.toList()));
        }

        //第二步 校验执行权限
        new ProcessAuthManager.StepOperationChecker(processTaskStepId, ProcessTaskOperationType.TASK_CREATE)
                .build()
                .checkAndNoPermissionThrowException();

        //第三步 保存数据
        // 锁定当前流程
        Long processTaskId = processTaskStepVo.getProcessTaskId();
        processTaskMapper.getProcessTaskLockById(processTaskId);
        boolean isChange = false;
        ProcessTaskAuditType auditType = ProcessTaskAuditType.CREATETASK;
        ProcessTaskStepTaskNotifyTriggerType triggerType = ProcessTaskStepTaskNotifyTriggerType.CREATETASK;
        Long id = jsonObj.getLong("id");
        if (id != null) {
            //更新
            auditType = ProcessTaskAuditType.EDITTASK;
            triggerType = ProcessTaskStepTaskNotifyTriggerType.EDITTASK;
            ProcessTaskStepTaskVo stepTaskVo = processTaskStepTaskMapper.getStepTaskById(id);
            if (stepTaskVo == null) {
                throw new ProcessTaskStepTaskNotFoundException(id.toString());
            }
            boolean isChangeContent = false;
            ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(processTaskStepTaskVo.getContent());
            processTaskStepTaskVo.setContentHash(processTaskContentVo.getHash());
            if (!Objects.equals(processTaskContentVo.getHash(), stepTaskVo.getContentHash())) {
                processTaskMapper.insertIgnoreProcessTaskContent(processTaskContentVo);
                isChangeContent = true;
            }
            boolean isChangeOwner = false;
            if (!Objects.equals(UserContext.get().getUserUuid(true), stepTaskVo.getOwner())) {
                processTaskStepTaskVo.setOwner(UserContext.get().getUserUuid(true));
                isChangeOwner = true;
            }
            if (isChangeContent || isChangeOwner) {
                processTaskStepTaskMapper.updateTask(processTaskStepTaskVo);
                isChange = true;
            }
            //找出需要删除的stepUserId
            List<Long> needDeleteStepTaskUserIdList = new ArrayList<>();
            List<ProcessTaskStepTaskUserVo> processTaskStepTaskUserList = processTaskStepTaskMapper.getStepTaskUserListByStepTaskId(id);
            for (ProcessTaskStepTaskUserVo processTaskStepTaskUserVo : processTaskStepTaskUserList) {
                if (Objects.equals(processTaskStepTaskUserVo.getIsDelete(), 1)) {
                    continue;
                }
                String userUuid = stepTaskUserMap.get(processTaskStepTaskUserVo.getId());
                if (StringUtils.isNotBlank(userUuid)) {
                    if (!Objects.equals(userUuid, processTaskStepTaskUserVo.getUserUuid())) {
                        //删除
                        needDeleteStepTaskUserIdList.add(processTaskStepTaskUserVo.getId());
                        newUserUuidSet.add(userUuid);
                    }
                } else {
                    //删除
                    needDeleteStepTaskUserIdList.add(processTaskStepTaskUserVo.getId());
                }
            }
            if (CollectionUtils.isNotEmpty(needDeleteStepTaskUserIdList)) {
                processTaskStepTaskMapper.updateTaskUserIsDeleteByIdList(needDeleteStepTaskUserIdList, 1);
//                processTaskStepTaskMapper.deleteProcessTaskStepTaskUserAgentByStepTaskUserIdList(needDeleteStepTaskUserIdList);
                isChange = true;
            }
            //找出需要恢复的stepUserId
            List<String> needRecoverUserUuidList = new ArrayList<>();
            List<Long> needRecoverStepTaskUserIdList = new ArrayList<>();
            for (ProcessTaskStepTaskUserVo processTaskStepTaskUserVo : processTaskStepTaskUserList) {
                if (Objects.equals(processTaskStepTaskUserVo.getIsDelete(), 0)) {
                    continue;
                }
                if (newUserUuidSet.contains(processTaskStepTaskUserVo.getUserUuid())) {
                    needRecoverStepTaskUserIdList.add(processTaskStepTaskUserVo.getId());
                    needRecoverUserUuidList.add(processTaskStepTaskUserVo.getUserUuid());
                }
            }
            if (CollectionUtils.isNotEmpty(needRecoverStepTaskUserIdList)) {
                newUserUuidSet.removeAll(needRecoverUserUuidList);
                processTaskStepTaskMapper.updateTaskUserIsDeleteByIdList(needRecoverStepTaskUserIdList, 0);
                isChange = true;
            }
        } else {
            //新增
            ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(processTaskStepTaskVo.getContent());
            processTaskMapper.insertIgnoreProcessTaskContent(processTaskContentVo);
            processTaskStepTaskVo.setContentHash(processTaskContentVo.getHash());
            processTaskStepTaskVo.setProcessTaskId(processTaskId);
            processTaskStepTaskVo.setOwner(UserContext.get().getUserUuid(true));
            processTaskStepTaskMapper.insertTask(processTaskStepTaskVo);
        }
        //插上新taskUser
        if (CollectionUtils.isNotEmpty(newUserUuidSet)) {
            ProcessTaskStepTaskUserVo processTaskStepTaskUserVo = new ProcessTaskStepTaskUserVo();
            processTaskStepTaskUserVo.setProcessTaskStepTaskId(processTaskStepTaskVo.getId());
            processTaskStepTaskUserVo.setStatus(ProcessTaskStepTaskUserStatus.PENDING.getValue());
            for (String userUuid : newUserUuidSet) {
                processTaskStepTaskUserVo.setId(null);
                processTaskStepTaskUserVo.setUserUuid(userUuid);
                processTaskStepTaskMapper.insertTaskUser(processTaskStepTaskUserVo);
            }
            isChange = true;
        }
        //第四步 更新`processtask_step_worker`表和`processtask_step_user`表、记录活动、触发通知、动作
        if (isChange) {
            IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
            if (handler == null) {
                throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
            }
            handler.updateProcessTaskStepUserAndWorker(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId());

            //活动参数
            List<String> workerList = new ArrayList<>();
            List<ProcessTaskStepTaskUserVo> processTaskStepTaskUserList = processTaskStepTaskVo.getStepTaskUserVoList();
            for (ProcessTaskStepTaskUserVo processTaskStepTaskUserVo : processTaskStepTaskUserList) {
                workerList.add(GroupSearch.USER.getValuePlugin() + processTaskStepTaskUserVo.getUserUuid());
            }
            JSONObject paramObj = new JSONObject();
            paramObj.put("replaceable_task", taskConfigVo.getName());
            paramObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), processTaskStepTaskVo.getContent());
            paramObj.put(ProcessTaskAuditDetailType.WORKERLIST.getParamName(), JSONObject.toJSONString(workerList));
            paramObj.put("source", jsonObj.getString("source"));
            processTaskStepVo.getParamObj().putAll(paramObj);
            processStepHandlerUtil.audit(processTaskStepVo, auditType);
            processTaskStepTaskVo.setTaskConfigName(taskConfigVo.getName());
            processTaskStepVo.setProcessTaskStepTaskVo(processTaskStepTaskVo);
            processStepHandlerUtil.notify(processTaskStepVo, triggerType);
            processStepHandlerUtil.action(processTaskStepVo, triggerType);
        }
        return processTaskStepTaskVo.getId();
    }

    /**
     * 检查用户是否合法
     *
     * @param userUuidList 用户uuidList
     * @param rangeList    用户范围
     */
    private void checkUserIsLegal(List<String> userUuidList, List<String> rangeList) {
        UserVo userVo = new UserVo();
        userVo.setCurrentPage(1);
        userVo.setIsDelete(0);
        userVo.setIsActive(1);
        userService.getUserByRangeList(userVo, rangeList);
        List<String> legalUserUuidList = userMapper.checkUserInRangeList(userUuidList, userVo);
        if (legalUserUuidList.size() != userUuidList.size()) {
            userUuidList.removeAll(legalUserUuidList);
            if (CollectionUtils.isNotEmpty(userUuidList)) {
                throw new TaskUserIllegalException(String.join(",", userUuidList));
            }
        }
    }
}
