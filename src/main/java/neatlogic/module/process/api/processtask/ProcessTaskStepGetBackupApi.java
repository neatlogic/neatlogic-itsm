package neatlogic.module.process.api.processtask;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.score.ScoreTemplateMapper;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.dto.ProcessTaskScoreTemplateVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.processtask.ProcessTaskViewDeniedException;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.common.config.ProcessConfig;
import neatlogic.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepGetBackupApi extends PrivateApiComponentBase {

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
        return "processtask/step/get/backup";
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
        Map<Long, Set<ProcessTaskOperationType>> operationTypeSetMap = builder.build().getOperateMap();

        Set<ProcessTaskOperationType> taskOperationTypeSet = operationTypeSetMap.get(processTaskId);
//        if (!new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_VIEW).build().check()) {
        if (!taskOperationTypeSet.contains(ProcessTaskOperationType.PROCESSTASK_VIEW)) {
            if (ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
                throw new ProcessTaskViewDeniedException();
            } else {
                ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
                if (channelVo == null) {
                    throw new ChannelNotFoundException(processTaskVo.getChannelUuid());
                }
                throw new ProcessTaskViewDeniedException(channelVo.getName());
            }
        }
        processTaskService.setProcessTaskDetail(processTaskVo);


        /* 查询当前用户是否有权限修改工单关注人 **/
//        int canEditFocusUser = new ProcessAuthManager
//                .TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_FOCUSUSER_UPDATE).build()
//                .check() ? 1 : 0;
//        processTaskVo.setCanEditFocusUser(canEditFocusUser);
        if (taskOperationTypeSet.contains(ProcessTaskOperationType.PROCESSTASK_FOCUSUSER_UPDATE)) {
            processTaskVo.setCanEditFocusUser(1);
        } else {
            processTaskVo.setCanEditFocusUser(0);
        }

//        if (new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_SCORE).build().check()) {
        if (taskOperationTypeSet.contains(ProcessTaskOperationType.PROCESSTASK_SCORE)) {
            ProcessTaskScoreTemplateVo processTaskScoreTemplateVo = processTaskMapper.getProcessTaskScoreTemplateByProcessTaskId(processTaskId);
            if (processTaskScoreTemplateVo != null) {
                processTaskVo.setScoreTemplateVo(scoreTemplateMapper.getScoreTemplateById(processTaskScoreTemplateVo.getScoreTemplateId()));
                ProcessTaskStepVo endProcessTaskStepVo = processTaskMapper.getEndProcessTaskStepByProcessTaskId(processTaskId);
                List<ProcessTaskStepVo> processTaskStepVoList = processTaskService.getBackwardNextStepListByProcessTaskStepId(endProcessTaskStepVo);
                processTaskVo.setRedoStepList(processTaskStepVoList);
            }
        }
        processTaskVo.setStartProcessTaskStep(processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskId));
        ProcessTaskStepVo currentProcessTaskStepVo = processTaskVo.getCurrentProcessTaskStep();
        if (currentProcessTaskStepVo != null) {
            Set<ProcessTaskOperationType> stepOperationTypeSet = operationTypeSetMap.get(processTaskStepId);
            if (stepOperationTypeSet.contains(ProcessTaskOperationType.STEP_VIEW)) {
                processTaskService.getCurrentProcessTaskStepDetail(currentProcessTaskStepVo, stepOperationTypeSet.contains(ProcessTaskOperationType.STEP_COMPLETE));
//                if (new ProcessAuthManager.StepOperationChecker(processTaskStepId, ProcessTaskOperationType.STEP_SAVE).build().check()) {
                if (stepOperationTypeSet.contains(ProcessTaskOperationType.STEP_SAVE)) {
                    // 回复框内容和附件暂存回显
                    processTaskService.setTemporaryData(processTaskVo, currentProcessTaskStepVo);
                }
            }
        }

        // TODO 兼容老工单表单（判断是否存在旧表单）
        Map<String, String> oldFormPropMap = processTaskMapper.getProcessTaskOldFormAndPropByTaskId(processTaskId);
        if (oldFormPropMap != null && oldFormPropMap.size() > 0) {
            processTaskVo.setIsHasOldFormProp(1);
        }

        // 移动端默认展开表单
        processTaskVo.setMobileFormUIType(Integer.valueOf(ProcessConfig.MOBILE_FORM_UI_TYPE()));
        JSONObject resultObj = new JSONObject();
        resultObj.put("processTask", processTaskVo);
        return resultObj;
    }

}
