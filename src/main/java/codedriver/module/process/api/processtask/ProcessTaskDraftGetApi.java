package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.dao.mapper.*;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.channeltype.ChannelTypeNotFoundException;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.form.FormActiveVersionNotFoundExcepiton;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.CatalogService;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskDraftGetApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private ProcessMapper processMapper;

    @Autowired
    private FormMapper formMapper;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private ProcessTaskService processTaskService;

    @Autowired
    private SelectContentByHashMapper selectContentByHashMapper;

    @Override
    public String getToken() {
        return "processtask/draft/get";
    }

    @Override
    public String getName() {
        return "工单草稿数据获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id，从工单中心进入上报页时，传processTaskId"),
            @Param(name = "copyProcessTaskId", type = ApiParamType.LONG, desc = "复制工单id，从复制上报进入上报页时，传copyProcessTaskId"),
            @Param(name = "channelUuid", type = ApiParamType.STRING, desc = "服务uuid，从服务目录进入上报页时，传channelUuid"),
            @Param(name = "fromProcessTaskId", type = ApiParamType.LONG, desc = "来源工单id，从转报进入上报页时，传fromProcessTaskId"),
            @Param(name = "channelTypeRelationId", type = ApiParamType.LONG, desc = "关系类型id，从转报进入上报页时，传channelTypeRelationId")
    })
    @Output({
            @Param(explode = ProcessTaskVo.class, desc = "工单信息")
    })
    @Description(desc = "工单详情数据获取接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long copyProcessTaskId = jsonObj.getLong("copyProcessTaskId");
        String channelUuid = jsonObj.getString("channelUuid");
        if (processTaskId != null) {
            return getProcessTaskVoByProcessTaskId(processTaskId);
        } else if (copyProcessTaskId != null) {
            return getProcessTaskVoByCopyProcessTaskId(copyProcessTaskId);
        } else if (channelUuid != null) {
            Long fromProcessTaskId = jsonObj.getLong("fromProcessTaskId");
            return getProcessTaskVoByChannelUuid(channelUuid, fromProcessTaskId);
        } else {
            throw new ProcessTaskRuntimeException("参数'processTaskId'、'copyProcessTaskId'和'channelUuid'，至少要传一个");
        }
    }

    /**
     * @Description: 获取来源工单中与当前工单表单属性标签名相同的属性值
     * @Author: linbq
     * @Date: 2021/1/27 15:26
     * @Params:[fromProcessTaskId, toProcessTaskFormConfig]
     * @Returns:java.util.Map<java.lang.String,java.lang.Object>
     **/
    private Map<String, Object> getFromFormAttributeDataMap(Long fromProcessTaskId, String toProcessTaskFormConfig){
        Map<String, Object> resultObj = new HashMap<>();
        // 获取旧工单表单信息
        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(fromProcessTaskId);
        if (processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContentHash())) {
            String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
            if (StringUtils.isNotBlank(formContent)) {
                Map<String, String> labelUuidMap = new HashMap<>();
                FormVersionVo fromFormVersion = new FormVersionVo();
                fromFormVersion.setFormConfig(formContent);
                List<FormAttributeVo> fromFormAttributeList = fromFormVersion.getFormAttributeList();
                for (FormAttributeVo formAttributeVo : fromFormAttributeList) {
                    labelUuidMap.put(formAttributeVo.getLabel(), formAttributeVo.getUuid());
                }
                Map<String, Object> formAttributeDataMap = new HashMap<>();
                List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(fromProcessTaskId);
                for (ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                    formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
                }

                FormVersionVo toFormVersion = new FormVersionVo();
                toFormVersion.setFormConfig(toProcessTaskFormConfig);
                for (FormAttributeVo formAttributeVo : toFormVersion.getFormAttributeList()) {
                    String fromFormAttributeUuid = labelUuidMap.get(formAttributeVo.getLabel());
                    if (StringUtils.isNotBlank(fromFormAttributeUuid)) {
                        Object data = formAttributeDataMap.get(fromFormAttributeUuid);
                        if (data != null) {
                            resultObj.put(formAttributeVo.getUuid(), data);
                        }
                    }
                }
            }
        }
        return resultObj;
    }

    private ProcessTaskVo getProcessTaskVoByProcessTaskId(Long processTaskId) throws Exception {
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(processTaskId);
        /** 判断当前用户是否拥有channelUuid服务的上报权限 **/
        if (!catalogService.channelIsAuthority(processTaskVo.getChannelUuid(), UserContext.get().getUserUuid(true))) {
            // throw new ProcessTaskNoPermissionException("上报");
            throw new PermissionDeniedException();
        }

        String owner = processTaskVo.getOwner();
        if (StringUtils.isNotBlank(owner)) {
            owner = GroupSearch.USER.getValuePlugin() + owner;
            processTaskVo.setOwner(owner);
        }

        ProcessTaskStepVo startProcessTaskStepVo = processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskId);
        // 获取须指派的步骤列表
        startProcessTaskStepVo.setAssignableWorkerStepList(
                processTaskService.getAssignableWorkerStepList(startProcessTaskStepVo.getProcessTaskId(), startProcessTaskStepVo.getProcessStepUuid())
        );
        processTaskVo.setStartProcessTaskStep(startProcessTaskStepVo);

        if (StringUtils.isNotBlank(processTaskVo.getFormConfig())) {
            List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList = processTaskMapper.getProcessTaskStepFormAttributeByProcessTaskStepId(startProcessTaskStepVo.getId());
            if (CollectionUtils.isNotEmpty(processTaskStepFormAttributeList)) {
                Map<String, String> formAttributeActionMap = new HashMap<>();
                for (ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo : processTaskStepFormAttributeList) {
                    formAttributeActionMap.put(processTaskStepFormAttributeVo.getAttributeUuid(), processTaskStepFormAttributeVo.getAction());
                }
                processTaskService.setProcessTaskFormAttributeAction(processTaskVo, formAttributeActionMap, 1);
                startProcessTaskStepVo.setStepFormConfig(processTaskStepFormAttributeList);
            }
        }
        processTaskService.setTemporaryData(processTaskVo, startProcessTaskStepVo);
        return processTaskVo;
    }

    private ProcessTaskVo getProcessTaskVoByCopyProcessTaskId(Long copyProcessTaskId) throws Exception {
        ProcessTaskVo oldProcessTaskVo = processTaskService.checkProcessTaskParamsIsLegal(copyProcessTaskId);
        ProcessTaskVo processTaskVo = getProcessTaskVoByChannelUuid(oldProcessTaskVo.getChannelUuid(), null);

        processTaskVo.setTitle(oldProcessTaskVo.getTitle());
        List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(oldProcessTaskVo.getChannelUuid());
        for (ChannelPriorityVo channelPriority : channelPriorityList) {
            if (oldProcessTaskVo.getPriorityUuid().equals(channelPriority.getPriorityUuid())) {
                processTaskVo.setPriorityUuid(channelPriority.getPriorityUuid());
                break;
            }
        }

        ProcessTaskStepVo oldStartProcessTaskStepVo = processTaskService.getStartProcessTaskStepByProcessTaskId(copyProcessTaskId);
        ProcessTaskStepVo startProcessTaskStepVo = processTaskVo.getStartProcessTaskStep();
        startProcessTaskStepVo.setComment(oldStartProcessTaskStepVo.getComment());
        startProcessTaskStepVo.setHandlerStepInfo(oldStartProcessTaskStepVo.getHandlerStepInfo());

        processTaskVo.setFormAttributeDataMap(getFromFormAttributeDataMap(copyProcessTaskId, processTaskVo.getFormConfig()));
        // 标签列表
        processTaskVo.setTagVoList(processTaskMapper.getProcessTaskTagListByProcessTaskId(copyProcessTaskId));
        return processTaskVo;
    }

    private ProcessTaskVo getProcessTaskVoByChannelUuid(String channelUuid, Long fromProcessTaskId) throws PermissionDeniedException {
        ChannelVo channel = channelMapper.getChannelByUuid(channelUuid);
        if (channel == null) {
            throw new ChannelNotFoundException(channelUuid);
        }
        /** 判断当前用户是否拥有channelUuid服务的上报权限 **/
        if (!catalogService.channelIsAuthority(channelUuid, UserContext.get().getUserUuid(true))) {
            throw new PermissionDeniedException();
        }
        ChannelTypeVo channelTypeVo = channelMapper.getChannelTypeByUuid(channel.getChannelTypeUuid());
        if (channelTypeVo == null) {
            throw new ChannelTypeNotFoundException(channel.getChannelTypeUuid());
        }

        ProcessVo processVo = processMapper.getProcessByUuid(channel.getProcessUuid());
        if (processVo == null) {
            throw new ProcessNotFoundException(channel.getProcessUuid());
        }
        ProcessTaskVo processTaskVo = new ProcessTaskVo();
        processTaskVo.setIsAutoGenerateId(false);
        try {
            processTaskVo.setChannelType(channelTypeVo.clone());
        } catch (CloneNotSupportedException e) {
        }
        processTaskVo.setChannelUuid(channelUuid);
        processTaskVo.setProcessUuid(channel.getProcessUuid());
        processTaskVo.setConfig(processVo.getConfig());
        processTaskVo.setWorktimeUuid(channel.getWorktimeUuid());
        List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(channelUuid);
        for (ChannelPriorityVo channelPriority : channelPriorityList) {
            if (channelPriority.getIsDefault().intValue() == 1) {
                processTaskVo.setPriorityUuid(channelPriority.getPriorityUuid());
            }
        }

        ProcessStepVo startProcessStepVo = processMapper.getStartProcessStepByProcessUuid(channel.getProcessUuid());
        ProcessTaskStepVo startProcessTaskStepVo = new ProcessTaskStepVo(startProcessStepVo);
        startProcessTaskStepVo.setIsAutoGenerateId(false);

        // 获取须指派的步骤列表
        startProcessTaskStepVo.setAssignableWorkerStepList(processTaskService.getAssignableWorkerStepList(channel.getProcessUuid(), startProcessTaskStepVo.getProcessStepUuid()));
        startProcessTaskStepVo.setIsRequired((Integer) JSONPath.read(startProcessTaskStepVo.getConfig(), "workerPolicyConfig.isRequired"));
        startProcessTaskStepVo.setIsNeedContent((Integer) JSONPath.read(startProcessTaskStepVo.getConfig(), "workerPolicyConfig.isNeedContent"));
        processTaskVo.setStartProcessTaskStep(startProcessTaskStepVo);

        if (fromProcessTaskId != null) {
            processTaskVo.getTranferReportProcessTaskList().add(processTaskService.getFromProcessTasById(fromProcessTaskId));
        }
        if (StringUtils.isNotBlank(processVo.getFormUuid())) {
            FormVersionVo formVersion = formMapper.getActionFormVersionByFormUuid(processVo.getFormUuid());
            if (formVersion == null) {
                throw new FormActiveVersionNotFoundExcepiton(processVo.getFormUuid());
            }
            processTaskVo.setFormConfig(formVersion.getFormConfig());
            if (fromProcessTaskId != null) {
                processTaskVo.setFormAttributeDataMap(getFromFormAttributeDataMap(fromProcessTaskId, processTaskVo.getFormConfig()));
            }
//            if (CollectionUtils.isNotEmpty(processTaskVo.getTranferReportProcessTaskList())
//                    && StringUtils.isNotBlank(processTaskVo.getTranferReportProcessTaskList().get(0).getFormConfig())) {
//                transferFormAttributeValue(processTaskVo.getTranferReportProcessTaskList().get(0), processTaskVo);
//            }
            List<ProcessStepFormAttributeVo> processStepFormAttributeList = processMapper.getProcessStepFormAttributeByStepUuid(startProcessTaskStepVo.getProcessStepUuid());
            if (CollectionUtils.isNotEmpty(processStepFormAttributeList)) {
                Map<String, String> formAttributeActionMap = new HashMap<>();
                List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList = new ArrayList<>();
                for (ProcessStepFormAttributeVo processStepFormAttribute : processStepFormAttributeList) {
                    formAttributeActionMap.put(processStepFormAttribute.getAttributeUuid(), processStepFormAttribute.getAction());
                    processTaskStepFormAttributeList.add(new ProcessTaskStepFormAttributeVo(processStepFormAttribute));
                }
                processTaskService.setProcessTaskFormAttributeAction(processTaskVo, formAttributeActionMap, 1);
                startProcessTaskStepVo.setStepFormConfig(processTaskStepFormAttributeList);
            }
        }
        return processTaskVo;
    }
}
