/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */


package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.change.constvalue.ChangeProcessStepHandlerType;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.AuthenticationInfoVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.processtask.*;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.publicapi.PublicApiComponentBase;
import codedriver.framework.service.AuthenticationInfoService;
import codedriver.module.process.service.ProcessTaskCompleteService;
import codedriver.module.process.service.ProcessTaskStartService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.OPERATE)
public class ProcessTaskStepBatchCompleteApi extends PublicApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ProcessTaskStartService processTaskStartService;

    @Resource
    private ProcessTaskCompleteService processTaskCompleteService;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Override
    public String getToken() {
        return "processtask/step/batch/complete";
    }

    @Override
    public String getName() {
        return "批量完成工单步骤";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskIdList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "工单Id列表"),
            @Param(name = "userId", type = ApiParamType.STRING, isRequired = true, desc = "处理人userId"),
    })
    @Description(desc = "批量完成工单步骤")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject result = new JSONObject();
        List<Long> notFoundProcessTaskIdList = new ArrayList<>(); // 工单不存在的id列表
        List<Long> currentStepOverOneProcessTaskIdList = new ArrayList<>();// 当前步骤超过一个的工单id列表
        List<Long> noAuthProcessTaskIdList = new ArrayList<>(); // 无权限处理的工单id列表
        Map<Long, String> exceptionMap = new HashMap<>(); // 处理发生异常的工单
        List<Long> idList = jsonObj.getJSONArray("processTaskIdList").toJavaList(Long.class);
        String userId = jsonObj.getString("userId");
        UserVo user = userMapper.getUserByUserId(userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        // 检查工单是否存在
        List<Long> processTaskIdList = processTaskMapper.checkProcessTaskIdListIsExists(idList);
        if (processTaskIdList.size() < idList.size()) {
            idList.removeAll(processTaskIdList);
            notFoundProcessTaskIdList.addAll(idList);
        }
        // 检查哪些工单的当前步骤超过1个
        List<Long> processTaskIdListOfCurrentStepIsOne = processTaskMapper.checkCurrentProcessTaskStepCountIsOverOneByProcessTaskIdList(processTaskIdList);
        if (processTaskIdListOfCurrentStepIsOne.size() > 0) {
            processTaskIdList.removeAll(processTaskIdListOfCurrentStepIsOne);
            currentStepOverOneProcessTaskIdList.addAll(processTaskIdListOfCurrentStepIsOne);
        }
        if (processTaskIdList.size() > 0) {
            // 查询工单的当前步骤
            List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getCurrentProcessTaskStepIdByProcessTaskIdList(processTaskIdList);
            AuthenticationInfoVo authenticationInfo = authenticationInfoService.getAuthenticationInfo(user.getUuid());
            for (ProcessTaskStepVo currentStep : processTaskStepList) {
                try {
                    if (!"process".equals(currentStep.getType())) {
                        throw new ProcessTaskStepIsNotManualException(currentStep.getProcessTaskId(), currentStep.getName());
                    }
                    // todo 变更和事件必须在页面上处理
                    if ("event".equals(currentStep.getHandler()) || ChangeProcessStepHandlerType.CHANGECREATE.getHandler().equals(currentStep.getHandler())
                            || ChangeProcessStepHandlerType.CHANGEHANDLE.getHandler().equals(currentStep.getHandler())) {
                        throw new ProcessTaskStepMustBeManualException(currentStep.getProcessTaskId(), currentStep.getName());
                    }
                    Map<Long, Set<ProcessTaskOperationType>> auth = checkAuth(user, authenticationInfo, currentStep);
                    if (MapUtils.isEmpty(auth) || auth.values().stream().findFirst().get().size() == 0) {
                        noAuthProcessTaskIdList.add(currentStep.getProcessTaskId());
                        continue;
                    }
                    ProcessTaskOperationType operationType = auth.values().stream().findFirst().get().stream().findFirst().get();
                    if (ProcessTaskOperationType.STEP_ACCEPT.getValue().equals(operationType.getValue())
                            || ProcessTaskOperationType.STEP_START.getValue().equals(operationType.getValue())) {
                        JSONObject param = new JSONObject();
                        param.put("processTaskId", currentStep.getProcessTaskId());
                        param.put("processTaskStepId", currentStep.getId());
                        if (ProcessTaskOperationType.STEP_ACCEPT.getValue().equals(operationType.getValue())) {
                            param.put("action", "accept");
                        } else {
                            param.put("action", "start");
                        }
                        UserContext.init(user, authenticationInfo, SystemUser.SYSTEM.getTimezone());
                        processTaskStartService.start(param);
                    }
                    UserContext.init(user, authenticationInfo, SystemUser.SYSTEM.getTimezone());
                    completeProcessTaskStep(currentStep);
                } catch (Exception ex) {
                    exceptionMap.put(currentStep.getProcessTaskId(), ex.getMessage());
                }
            }
        }

        if (notFoundProcessTaskIdList.size() > 0) {
            result.put("工单不存在的ID", notFoundProcessTaskIdList);
        }
        if (currentStepOverOneProcessTaskIdList.size() > 0) {
            result.put("当前步骤超过一个的工单", currentStepOverOneProcessTaskIdList);
        }
        if (noAuthProcessTaskIdList.size() > 0) {
            result.put("无权限处理的工单", noAuthProcessTaskIdList.stream().map(Objects::toString).collect(Collectors.joining()));
        }
        if (!exceptionMap.isEmpty()) {
            result.put("处理发生异常的工单", exceptionMap);
        }
        return result;
    }

    /**
     * 检查用户是否有工单完成权限
     *
     * @param user               处理人
     * @param authenticationInfo 处理人权限
     * @param processTaskStepVo  工单步骤
     * @return
     */
    private Map<Long, Set<ProcessTaskOperationType>> checkAuth(UserVo user, AuthenticationInfoVo authenticationInfo, ProcessTaskStepVo processTaskStepVo) {
        UserContext.init(user, authenticationInfo, SystemUser.SYSTEM.getTimezone());
        ProcessAuthManager.Builder builder = new ProcessAuthManager.Builder();
        builder.addProcessTaskId(processTaskStepVo.getProcessTaskId());
        builder.addProcessTaskStepId(processTaskStepVo.getId());
        return builder.addOperationType(ProcessTaskOperationType.STEP_START)
                .addOperationType(ProcessTaskOperationType.STEP_ACCEPT)
                .addOperationType(ProcessTaskOperationType.STEP_COMPLETE)
                .build().getOperateMap();
    }

    /**
     * 完成工单步骤
     *
     * @param processTaskStepVo 工单步骤
     * @throws Exception
     */
    private void completeProcessTaskStep(ProcessTaskStepVo processTaskStepVo) throws Exception {
        // 查询后续节点，不包括回退节点
        List<Long> nextStepIdList =
                processTaskMapper.getToProcessTaskStepIdListByFromIdAndType(processTaskStepVo.getId(), ProcessFlowDirection.FORWARD.getValue());
        if (CollectionUtils.isEmpty(nextStepIdList)) {
            throw new ProcessTaskNextStepIllegalException(processTaskStepVo.getProcessTaskId());
        }
        if (nextStepIdList.size() > 1) {
            throw new ProcessTaskNextStepOverOneException(processTaskStepVo.getProcessTaskId());
        }
        JSONObject param = new JSONObject();
        param.put("processTaskId", processTaskStepVo.getProcessTaskId());
        param.put("processTaskStepId", processTaskStepVo.getId());
        param.put("nextStepId", nextStepIdList.get(0));
        param.put("action", "complete");
        processTaskCompleteService.complete(param);
    }


}
