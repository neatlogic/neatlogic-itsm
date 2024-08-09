package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessStepType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import neatlogic.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import neatlogic.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepDataMapper;
import neatlogic.module.process.service.ProcessTaskService;
import neatlogic.module.process.service.ProcessTaskStepTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepListApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Resource
    private ProcessTaskStepTaskService processTaskStepTaskService;

    @Override
    public String getToken() {
        return "processtask/step/list";
    }

    @Override
    public String getName() {
        return "工单步骤列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")})
    @Output({@Param(name = "Return", explode = ProcessTaskStepVo[].class, desc = "步骤信息列表")})
    @Description(desc = "工单步骤列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        try {
            new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_VIEW)
                    .build()
                    .checkAndNoPermissionThrowException();
        } catch (ProcessTaskPermissionDeniedException e) {
            throw new PermissionDeniedException(e.getMessage());
        }

        ProcessTaskStepVo startProcessTaskStepVo = getStartProcessTaskStepByProcessTaskId(processTaskId);
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        resultList.add(startProcessTaskStepVo);
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.PROCESS.getValue());
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            if (Objects.equals(processTaskStepVo.getId(), startProcessTaskStepVo.getId())) {
                continue;
            }
            if (processTaskStepVo.getActiveTime() != null) {
                resultList.add(processTaskStepVo);
            }
        }

        // 其他处理步骤
        if (CollectionUtils.isNotEmpty(resultList)) {
            List<Long> processTaskStepIdList = resultList.stream().map(ProcessTaskStepVo::getId).collect(Collectors.toList());
            Map<Long, Set<ProcessTaskOperationType>> operateMap = new ProcessAuthManager.Builder()
                    .addProcessTaskStepId(processTaskStepIdList)
                    .addOperationType(ProcessTaskOperationType.STEP_VIEW)
                    .build()
                    .getOperateMap();
            for (ProcessTaskStepVo processTaskStepVo : resultList) {
                // 判断当前用户是否有权限查看该节点信息
                IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
                if (handler == null) {
                    throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
                }
                Set<ProcessTaskOperationType> processTaskStepOperateSet = operateMap.get(processTaskStepVo.getId());
                if (CollectionUtils.isNotEmpty(processTaskStepOperateSet) && processTaskStepOperateSet.contains(ProcessTaskOperationType.STEP_VIEW)) {
                    processTaskStepVo.setIsView(1);
                    if (!Objects.equals(processTaskStepVo.getId(), startProcessTaskStepVo.getId())) {
                        getProcessTaskStepDetail(processTaskStepVo);
                    }
                } else {
                    processTaskStepVo.setIsView(0);
                }
            }
        }
        for (ProcessTaskStepVo processTaskStepVo : resultList) {
            processTaskStepVo.setIsInTheCurrentStepTab(0);
            if (Objects.equals(processTaskStepVo.getIsActive(), 1)) {
                if (Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStepStatus.PENDING.getValue())
                        || Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStepStatus.RUNNING.getValue())
                        || Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStepStatus.HANG.getValue())) {
                    processTaskStepVo.setIsInTheCurrentStepTab(1);
                }
            }
        }
        resultList.sort(Comparator.comparing(ProcessTaskStepVo::getActiveTime));
        return resultList;
    }

    private ProcessTaskStepVo getStartProcessTaskStepByProcessTaskId(Long processTaskId) {
        ProcessTaskStepVo startProcessTaskStepVo = processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskId);
        processTaskService.setProcessTaskStepUser(startProcessTaskStepVo);

        // 时效列表
        startProcessTaskStepVo.setSlaTimeList(processTaskService.getSlaTimeListByProcessTaskStepId(startProcessTaskStepVo.getId()));
        // 步骤评论列表
        List<String> typeList = new ArrayList<>();
        typeList.add(ProcessTaskOperationType.STEP_COMMENT.getValue());
        typeList.add(ProcessTaskOperationType.STEP_COMPLETE.getValue());
        typeList.add(ProcessTaskOperationType.STEP_BACK.getValue());
        typeList.add(ProcessTaskOperationType.PROCESSTASK_RETREAT.getValue());
        typeList.add(ProcessTaskOperationType.PROCESSTASK_TRANSFER.getValue());
        typeList.add(ProcessTaskOperationType.STEP_REAPPROVAL.getValue());
        typeList.add(ProcessTaskOperationType.PROCESSTASK_START.getValue());
        typeList.add(ProcessTaskOperationType.STEP_TRANSFER.getValue());
        startProcessTaskStepVo.setCommentList(
                processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(startProcessTaskStepVo.getId(), typeList));
        //任务列表
        processTaskStepTaskService.getProcessTaskStepTask(startProcessTaskStepVo);
        startProcessTaskStepVo.setIsView(1);
        return startProcessTaskStepVo;
    }

    private void getProcessTaskStepDetail(ProcessTaskStepVo processTaskStepVo) {
        // 处理人列表
        processTaskService.setProcessTaskStepUser(processTaskStepVo);

        /** 当前步骤特有步骤信息 **/
        IProcessStepInternalHandler processStepUtilHandler =
                ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (processStepUtilHandler == null) {
            throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        processTaskStepVo.setHandlerStepInfo(processStepUtilHandler.getNonStartStepInfo(processTaskStepVo));
        // 步骤评论列表
        List<String> typeList = new ArrayList<>();
        typeList.add(ProcessTaskOperationType.STEP_COMMENT.getValue());
        typeList.add(ProcessTaskOperationType.STEP_COMPLETE.getValue());
        typeList.add(ProcessTaskOperationType.STEP_BACK.getValue());
        typeList.add(ProcessTaskOperationType.PROCESSTASK_RETREAT.getValue());
        typeList.add(ProcessTaskOperationType.PROCESSTASK_TRANSFER.getValue());
        typeList.add(ProcessTaskOperationType.STEP_REAPPROVAL.getValue());
        typeList.add(ProcessTaskOperationType.PROCESSTASK_START.getValue());
        typeList.add(ProcessTaskOperationType.STEP_TRANSFER.getValue());
        processTaskStepVo.setCommentList(
                processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(processTaskStepVo.getId(), typeList));

        //任务列表
        processTaskStepTaskService.getProcessTaskStepTask(processTaskStepVo);
        List<TaskConfigVo> taskConfigList = processTaskStepTaskService.getTaskConfigList(processTaskStepVo);
        processTaskStepVo.setTaskConfigList(taskConfigList);
        // 时效列表
        processTaskStepVo.setSlaTimeList(processTaskService.getSlaTimeListByProcessTaskStepId(processTaskStepVo.getId()));
        // automatic processtaskStepData
        ProcessTaskStepDataVo stepDataVo = processTaskStepDataMapper
                .getProcessTaskStepData(new ProcessTaskStepDataVo(processTaskStepVo.getProcessTaskId(),
                        processTaskStepVo.getId(), processTaskStepVo.getHandler(), SystemUser.SYSTEM.getUserUuid()));
        if (stepDataVo != null) {
            JSONObject stepDataJson = stepDataVo.getData();
            stepDataJson.put("isStepUser",
                    processTaskMapper
                            .checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(),
                                    processTaskStepVo.getId(), UserContext.get().getUserUuid())) > 0 ? 1 : 0);
            processTaskStepVo.setProcessTaskStepData(stepDataJson);
        }
        processTaskStepVo.setReplaceableTextList(processTaskService.getReplaceableTextList(processTaskStepVo));
        processTaskStepVo.setCustomStatusList(processTaskService.getCustomStatusList(processTaskStepVo));
        processTaskStepVo.setCustomButtonList(processTaskService.getCustomButtonList(processTaskStepVo));
    }
}
