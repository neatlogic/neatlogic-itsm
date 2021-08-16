package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.type.ParamNotExistsException;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepUserStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskFlowChartApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/flowchart";
    }

    @Override
    public String getName() {
        return "工单流程图";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id"),
            @Param(name = "channelUuid", type = ApiParamType.STRING, desc = "工单id")
    })
    @Output({
            @Param(name = "config", explode = ProcessTaskStepVo[].class, desc = "流程图信息"),
            @Param(name = "processTaskStepList", explode = ProcessTaskStepVo[].class, desc = "步骤状态列表"),
            @Param(name = "processTaskStepRelList", explode = ProcessTaskStepVo[].class, desc = "连线状态列表")
    })
    @Description(desc = "工单流程图")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        String channelUuid = jsonObj.getString("channelUuid");
        if (processTaskId != null) {
            ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
            String config = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
            List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
            if (CollectionUtils.isNotEmpty(processTaskStepList)) {
                for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
                    processTaskService.setProcessTaskStepUser(processTaskStepVo);
                    processTaskStepVo.setReplaceableTextList(processTaskService.getReplaceableTextList(processTaskStepVo));
                    processTaskStepVo.setAssignableWorkerStepList(null);
                    processTaskStepVo.setBackwardNextStepList(null);
                    processTaskStepVo.setCommentList(null);
                    processTaskStepVo.setFormAttributeList(null);
                    processTaskStepVo.setFormAttributeVoList(null);
                    processTaskStepVo.setForwardNextStepList(null);
                    processTaskStepVo.setIsCurrentUserDone(null);
                    processTaskStepVo.setProcessTaskStepRemindList(null);
                    processTaskStepVo.setProcessTaskStepSubtaskList(null);
                    processTaskStepVo.setSlaTimeList(null);
                    processTaskStepVo.setUserList(null);
                    processTaskStepVo.setWorkerPolicyList(null);
                }
            }
            List<ProcessTaskStepRelVo> processTaskStepRelVoList = processTaskMapper.getProcessTaskStepRelByProcessTaskId(processTaskId);
            JSONObject resultObj = new JSONObject();
            resultObj.put("config", JSONObject.parseObject(config));
            resultObj.put("processTaskStepList", processTaskStepList);
            resultObj.put("processTaskStepRelList", processTaskStepRelVoList);
            return resultObj;
        } else if (channelUuid != null) {
            ChannelVo channelVo = channelMapper.getChannelByUuid(channelUuid);
            if(channelVo == null){
                throw new ChannelNotFoundException(channelUuid);
            }
            String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
            ProcessVo processVo = processMapper.getProcessByUuid(processUuid);
            if(processVo == null){
                throw new ProcessNotFoundException(processUuid);
            }
            Date startTime = new Date();
            ProcessStepVo processStepVo = processMapper.getStartProcessStepByProcessUuid(processUuid);
            ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo(processStepVo);
            processTaskStepVo.setIsAutoGenerateId(false);
            processTaskStepVo.setStartTime(startTime);
            processTaskStepVo.setIsActive(1);
            processTaskStepVo.setStatus(ProcessTaskStatus.DRAFT.getValue());
            processTaskStepVo.setStatusVo(new ProcessTaskStatusVo(ProcessTaskStatus.DRAFT.getValue()));
            processTaskStepVo.setAssignableWorkerStepList(null);
            processTaskStepVo.setBackwardNextStepList(null);
            processTaskStepVo.setCommentList(null);
            processTaskStepVo.setFormAttributeList(null);
            processTaskStepVo.setFormAttributeVoList(null);
            processTaskStepVo.setForwardNextStepList(null);
            processTaskStepVo.setIsCurrentUserDone(null);
            processTaskStepVo.setProcessTaskStepRemindList(null);
            processTaskStepVo.setProcessTaskStepSubtaskList(null);
            processTaskStepVo.setSlaTimeList(null);
            processTaskStepVo.setUserList(null);
            processTaskStepVo.setWorkerPolicyList(null);
            ProcessTaskStepUserVo majorUser = new ProcessTaskStepUserVo();
            UserVo userVo = userMapper.getUserBaseInfoByUuid(UserContext.get().getUserUuid(true));
            majorUser.setUserVo(userVo);
            majorUser.setUserType(ProcessUserType.MAJOR.getValue());
            majorUser.setStatus(ProcessTaskStepUserStatus.DOING.getValue());
            majorUser.setStartTime(startTime);
            processTaskStepVo.setMajorUser(majorUser);
            List<ProcessTaskStepVo> processTaskStepList = new ArrayList<>();
            processTaskStepList.add(processTaskStepVo);
            JSONObject resultObj = new JSONObject();
            resultObj.put("config", processVo.getConfig());
            resultObj.put("processTaskStepList", processTaskStepList);
            resultObj.put("processTaskStepRelList", new ArrayList<>());
            return resultObj;
        } else {
            throw new ParamNotExistsException("processTaskId", "channelUuid");
        }
    }

}
