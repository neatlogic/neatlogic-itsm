package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.exception.processtask.ProcessTaskViewDeniedException;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskStepTaskService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

//    @Autowired
//    private ProcessTaskStepSubtaskService processTaskStepSubtaskService;

    @Resource
    private ProcessTaskStepTaskService processTaskStepTaskService;

    @Resource
    private ChannelMapper channelMapper;

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
//        if (!new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_VIEW).build().check()) {
//            if (ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
//                throw new ProcessTaskViewDeniedException();
//            } else {
//                ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
//                if (channelVo == null) {
//                    throw new ChannelNotFoundException(processTaskVo.getChannelUuid());
//                }
//                throw new ProcessTaskViewDeniedException(channelVo.getName());
//            }
//        }
        ProcessTaskStepVo startProcessTaskStepVo = getStartProcessTaskStepByProcessTaskId(processTaskId);
        startProcessTaskStepVo.setReplaceableTextList(processTaskService.getReplaceableTextList(startProcessTaskStepVo));
        startProcessTaskStepVo.setCustomStatusList(processTaskService.getCustomStatusList(startProcessTaskStepVo));
        startProcessTaskStepVo.setCustomButtonList(processTaskService.getCustomButtonList(startProcessTaskStepVo));
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
//        Map<Long, ProcessTaskStepVo> processTaskStepMap = new HashMap<>();
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.PROCESS.getValue());
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            if (Objects.equals(processTaskStepVo.getId(), startProcessTaskStepVo.getId())) {
                continue;
            }
            if (processTaskStepVo.getActiveTime() != null) {
                resultList.add(processTaskStepVo);
//                processTaskStepMap.put(processTaskStepVo.getId(), processTaskStepVo);
            }
        }

//        Map<Long, List<Long>> fromStepIdMap = new HashMap<>();
//        List<ProcessTaskStepRelVo> prcessTaskStepRelList = processTaskMapper.getProcessTaskStepRelByProcessTaskId(processTaskId);
//        for (ProcessTaskStepRelVo processTaskStepRelVo : prcessTaskStepRelList) {
//            if (ProcessFlowDirection.FORWARD.getValue().equals(processTaskStepRelVo.getType())) {
//                Long fromStepId = processTaskStepRelVo.getFromProcessTaskStepId();
//                Long toStepId = processTaskStepRelVo.getToProcessTaskStepId();
//                List<Long> toStepIdList = fromStepIdMap.get(fromStepId);
//                if (toStepIdList == null) {
//                    toStepIdList = new ArrayList<>();
//                    fromStepIdMap.put(fromStepId, toStepIdList);
//                }
//                toStepIdList.add(toStepId);
//            }
//
//        }
//        List<ProcessTaskStepVo> resultList = new ArrayList<>();
//        Set<Long> fromStepIdList = new HashSet<>();
//        fromStepIdList.add(startProcessTaskStepVo.getId());
//        while (!processTaskStepMap.isEmpty()) {
//            Set<Long> newFromStepIdList = new HashSet<>();
//            for (Long fromStepId : fromStepIdList) {
//                List<Long> toStepIdList = fromStepIdMap.get(fromStepId);
//                List<ProcessTaskStepVo> toStepList = new ArrayList<>(toStepIdList.size());
//                for (Long toStepId : toStepIdList) {
//                    ProcessTaskStepVo toStep = processTaskStepMap.remove(toStepId);
//                    if (toStep != null) {
//                        toStepList.add(toStep);
//                    }
//                    if (fromStepIdMap.containsKey(toStepId)) {
//                        newFromStepIdList.add(toStepId);
//                    }
//                }
//                if (toStepList.size() > 1) {
//                    // 按开始时间正序排序
//                    toStepList.sort(Comparator.comparing(ProcessTaskStepVo::getActiveTime));
//                }
//                resultList.addAll(toStepList);
//            }
//            fromStepIdList.clear();
//            fromStepIdList.addAll(newFromStepIdList);
//        }

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
                    getProcessTaskStepDetail(processTaskStepVo);
                } else {
                    processTaskStepVo.setIsView(0);
                }
            }
        }
        resultList.add(0, startProcessTaskStepVo);
        resultList.sort(Comparator.comparing(ProcessTaskStepVo::getActiveTime));
        return resultList;
    }

    private ProcessTaskStepVo getStartProcessTaskStepByProcessTaskId(Long processTaskId) {
        ProcessTaskStepVo startProcessTaskStepVo = processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskId);
        processTaskService.setProcessTaskStepUser(startProcessTaskStepVo);

        // 时效列表
        startProcessTaskStepVo.setSlaTimeList(processTaskService.getSlaTimeListByProcessTaskStepId(startProcessTaskStepVo.getId()));
        // 步骤评论列表
        startProcessTaskStepVo.setCommentList(processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(
                startProcessTaskStepVo.getId(), Arrays.asList(ProcessTaskOperationType.STEP_COMMENT.getValue())));
        // 子任务列表
//        List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskStepSubtaskService
//            .getProcessTaskStepSubtaskListByProcessTaskStepId(startProcessTaskStepVo.getId());
//        for (ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
//            processTaskStepSubtask.setIsAbortable(0);
//            processTaskStepSubtask.setIsCompletable(0);
//            processTaskStepSubtask.setIsEditable(0);
//            processTaskStepSubtask.setIsRedoable(0);
//        }
//        startProcessTaskStepVo.setProcessTaskStepSubtaskList(processTaskStepSubtaskList);
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
        processTaskStepVo.setHandlerStepInfo(processStepUtilHandler.getHandlerStepInitInfo(processTaskStepVo));
        // 步骤评论列表
        List<String> typeList = new ArrayList<>();
        typeList.add(ProcessTaskOperationType.STEP_COMMENT.getValue());
        typeList.add(ProcessTaskOperationType.STEP_COMPLETE.getValue());
        typeList.add(ProcessTaskOperationType.STEP_BACK.getValue());
        typeList.add(ProcessTaskOperationType.PROCESSTASK_RETREAT.getValue());
        typeList.add(ProcessTaskOperationType.PROCESSTASK_TRANSFER.getValue());
        typeList.add(ProcessTaskOperationType.STEP_REAPPROVAL.getValue());
        processTaskStepVo.setCommentList(
                processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(processTaskStepVo.getId(), typeList));
        // 子任务列表
//        List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList =
//            processTaskStepSubtaskService.getProcessTaskStepSubtaskListByProcessTaskStepId(processTaskStepVo.getId());
//        for (ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
//            processTaskStepSubtask.setIsAbortable(0);
//            processTaskStepSubtask.setIsCompletable(0);
//            processTaskStepSubtask.setIsEditable(0);
//            processTaskStepSubtask.setIsRedoable(0);
//        }
//        processTaskStepVo.setProcessTaskStepSubtaskList(processTaskStepSubtaskList);

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
