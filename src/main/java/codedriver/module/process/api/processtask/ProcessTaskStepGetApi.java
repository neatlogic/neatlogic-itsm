package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadpool.CachedThreadPool;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.score.ScoreTemplateMapper;
import codedriver.framework.process.dto.ProcessTaskScoreTemplateVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.common.config.ProcessConfig;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Phaser;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepGetApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private ScoreTemplateMapper scoreTemplateMapper;

    @Resource
    private ChannelMapper channelMapper;

    @Override
    public String getToken() {
        return "processtask/step/get";
    }

    @Override
    public String getName() {
        return "工单步骤基本信息获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "工单步骤id")})
    @Output({@Param(name = "processTask", explode = ProcessTaskVo.class, desc = "工单信息")})
    @Description(desc = "工单步骤基本信息获取接口，当前步骤名称、激活时间、状态、处理人、协助处理人、处理时效、表单属性显示控制等")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        ProcessAuthManager.Builder builder = new ProcessAuthManager.Builder()
                .addProcessTaskId(processTaskId)
                .addOperationType(ProcessTaskOperationType.PROCESSTASK_VIEW)
                .addOperationType(ProcessTaskOperationType.PROCESSTASK_FOCUSUSER_UPDATE)
                .addOperationType(ProcessTaskOperationType.PROCESSTASK_SCORE);
        if (processTaskStepId != null) {
            builder.addProcessTaskStepId(processTaskStepId)
                    .addOperationType(ProcessTaskOperationType.STEP_VIEW)
                    .addOperationType(ProcessTaskOperationType.STEP_SAVE)
                    .addOperationType(ProcessTaskOperationType.STEP_COMPLETE);
        }
        ProcessAuthManager processAuthManager = builder.build();
        Map<Long, Set<ProcessTaskOperationType>> operationTypeSetMap = processAuthManager.getOperateMap();

        Set<ProcessTaskOperationType> taskOperationTypeSet = operationTypeSetMap.get(processTaskId);
        if (!taskOperationTypeSet.contains(ProcessTaskOperationType.PROCESSTASK_VIEW)) {
            ProcessTaskPermissionDeniedException exception = processAuthManager.getProcessTaskPermissionDeniedException(processTaskId, ProcessTaskOperationType.PROCESSTASK_VIEW);
            if (exception != null) {
                throw new PermissionDeniedException(exception.getMessage());
            }
//            if (ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
//                throw new ProcessTaskViewDeniedException();
//            } else {
//                ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
//                if (channelVo == null) {
//                    throw new ChannelNotFoundException(processTaskVo.getChannelUuid());
//                }
//                throw new ProcessTaskViewDeniedException(channelVo.getName());
//            }
        }
        Phaser phaser = new Phaser();
        phaser.register();
        CodeDriverThread ProcessTaskDetailThread = new CodeDriverThread("PROCESSTASK_DETAIL_" + processTaskId, true) {
            @Override
            protected void execute() {
                try {
                    processTaskService.setProcessTaskDetail(processTaskVo);
                } finally {
                    phaser.arrive();
                }
            }
        };
        CachedThreadPool.execute(ProcessTaskDetailThread);

        phaser.register();
        CodeDriverThread startProcessTaskStepThread = new CodeDriverThread("START_PROCESSTASKSTEP_" + processTaskId, true) {
            @Override
            protected void execute() {
                try {
                    processTaskVo.setStartProcessTaskStep(processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskId));
                } finally {
                    phaser.arrive();
                }
            }
        };
        CachedThreadPool.execute(startProcessTaskStepThread);

        ProcessTaskStepVo currentProcessTaskStepVo = processTaskVo.getCurrentProcessTaskStep();
        if (currentProcessTaskStepVo != null) {
            Set<ProcessTaskOperationType> stepOperationTypeSet = operationTypeSetMap.get(processTaskStepId);
            if (stepOperationTypeSet.contains(ProcessTaskOperationType.STEP_VIEW)) {
                phaser.register();
                CodeDriverThread currentProcessTaskStepDetailThread = new CodeDriverThread("CURRENT_PROCESSTASKSTEP_DETAIL_" + processTaskStepId, true) {
                    @Override
                    protected void execute() {
                        try {
                            processTaskService.getCurrentProcessTaskStepDetail(currentProcessTaskStepVo, stepOperationTypeSet.contains(ProcessTaskOperationType.STEP_COMPLETE));
                        } finally {
                            phaser.arrive();
                        }
                    }
                };
                CachedThreadPool.execute(currentProcessTaskStepDetailThread);
            }
        }

        /* 查询当前用户是否有权限修改工单关注人 **/
        if (taskOperationTypeSet.contains(ProcessTaskOperationType.PROCESSTASK_FOCUSUSER_UPDATE)) {
            processTaskVo.setCanEditFocusUser(1);
        } else {
            processTaskVo.setCanEditFocusUser(0);
        }

        if (taskOperationTypeSet.contains(ProcessTaskOperationType.PROCESSTASK_SCORE)) {
            ProcessTaskScoreTemplateVo processTaskScoreTemplateVo = processTaskMapper.getProcessTaskScoreTemplateByProcessTaskId(processTaskId);
            if (processTaskScoreTemplateVo != null) {
                processTaskVo.setScoreTemplateVo(scoreTemplateMapper.getScoreTemplateById(processTaskScoreTemplateVo.getScoreTemplateId()));
                ProcessTaskStepVo endProcessTaskStepVo = processTaskMapper.getEndProcessTaskStepByProcessTaskId(processTaskId);
                List<ProcessTaskStepVo> processTaskStepVoList = processTaskService.getBackwardNextStepListByProcessTaskStepId(endProcessTaskStepVo.getId());
                processTaskVo.setRedoStepList(processTaskStepVoList);
            }
        }

        // TODO 兼容老工单表单（判断是否存在旧表单）
        Map<String, String> oldFormPropMap = processTaskMapper.getProcessTaskOldFormAndPropByTaskId(processTaskId);
        if (oldFormPropMap != null && oldFormPropMap.size() > 0) {
            processTaskVo.setIsHasOldFormProp(1);
        }
        // 移动端默认展开表单
        processTaskVo.setMobileFormUIType(Integer.valueOf(ProcessConfig.MOBILE_FORM_UI_TYPE()));
        phaser.awaitAdvance(0);
        if (currentProcessTaskStepVo != null) {
            Set<ProcessTaskOperationType> stepOperationTypeSet = operationTypeSetMap.get(processTaskStepId);
            if (stepOperationTypeSet.contains(ProcessTaskOperationType.STEP_SAVE)) {
                // 回复框内容和附件暂存回显
                processTaskService.setTemporaryData(processTaskVo, currentProcessTaskStepVo);
            }
        }
        JSONObject resultObj = new JSONObject();
        resultObj.put("processTask", processTaskVo);
        return resultObj;
    }

}
