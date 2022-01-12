/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.exception.type.ParamNotExistsException;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.*;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.channeltype.ChannelTypeNotFoundException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.CatalogService;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskDraftGetApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ChannelTypeMapper channelTypeMapper;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private CatalogService catalogService;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
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
        ProcessTaskVo processTaskVo = null;
        if (processTaskId != null) {
            processTaskVo=  getProcessTaskVoByProcessTaskId(processTaskId);
        } else if (copyProcessTaskId != null) {
            processTaskVo =  getProcessTaskVoByCopyProcessTaskId(copyProcessTaskId);
        } else if (channelUuid != null) {
            Long fromProcessTaskId = jsonObj.getLong("fromProcessTaskId");
            Long channelTypeRelationId = jsonObj.getLong("channelTypeRelationId");
            processTaskVo =  getProcessTaskVoByChannelUuid(channelUuid, fromProcessTaskId, channelTypeRelationId);
        } else {
            throw new ParamNotExistsException("processTaskId", "copyProcessTaskId", "channelUuid");
        }
        return processTaskVo;
    }

    /**
     * @Description: 获取来源工单中与当前工单表单属性标签名相同的属性值
     * @Author: linbq
     * @Date: 2021/1/27 15:26
     * @Params:[fromProcessTaskId, toProcessTaskFormConfig]
     * @Returns:java.util.Map<java.lang.String,java.lang.Object>
     **/
    private Map<String, Object> getFromFormAttributeDataMap(Long fromProcessTaskId, JSONObject toProcessTaskFormConfig){
        Map<String, Object> resultObj = new HashMap<>();
        if (MapUtils.isEmpty(toProcessTaskFormConfig)) {
            return resultObj;
        }
        // 获取旧工单表单信息
        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(fromProcessTaskId);
        if (processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContentHash())) {
            String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
            if (StringUtils.isNotBlank(formContent)) {
                Map<String, String> labelUuidMap = new HashMap<>();
                Map<String, String> labelHandlerMap = new HashMap<>();
                FormVersionVo fromFormVersion = new FormVersionVo();
                fromFormVersion.setFormConfig(formContent);
                List<FormAttributeVo> fromFormAttributeList = fromFormVersion.getFormAttributeList();
                for (FormAttributeVo formAttributeVo : fromFormAttributeList) {
                    labelUuidMap.put(formAttributeVo.getLabel(), formAttributeVo.getUuid());
                    labelHandlerMap.put(formAttributeVo.getLabel(), formAttributeVo.getHandler());
                }
                Map<String, Object> formAttributeDataMap = new HashMap<>();
                List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(fromProcessTaskId);
                for (ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                    formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
                }

                FormVersionVo toFormVersion = new FormVersionVo();
                toFormVersion.setFormConfig(JSONObject.toJSONString(toProcessTaskFormConfig));
                for (FormAttributeVo formAttributeVo : toFormVersion.getFormAttributeList()) {
                    String fromFormAttributeHandler = labelHandlerMap.get(formAttributeVo.getLabel());
                    if (Objects.equals(fromFormAttributeHandler, formAttributeVo.getHandler())) {
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
        }
        return resultObj;
    }

    private ProcessTaskVo getProcessTaskVoByProcessTaskId(Long processTaskId) throws Exception {
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        if(!new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_START).build().check()){
            throw new PermissionDeniedException();
        }
        ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(processTaskId);
        /* 判断当前用户是否拥有channelUuid服务的上报权限 **/
        if (!catalogService.channelIsAuthority(processTaskVo.getChannelUuid(), UserContext.get().getUserUuid(true))) {
            throw new PermissionDeniedException();
            /** 2021-10-11 开晚会时确认用户个人设置任务授权不包括服务上报权限 **/
//            String agentUuid = userMapper.getUserUuidByAgentUuidAndFunc(UserContext.get().getUserUuid(true), "processtask");
//            if(StringUtils.isNotBlank(agentUuid)){
//                if(!catalogService.channelIsAuthority(processTaskVo.getChannelUuid(), agentUuid)){
//                    throw new PermissionDeniedException();
//                }
//            }else{
//                throw new PermissionDeniedException();
//            }
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
        processTaskService.setTemporaryData(processTaskVo, startProcessTaskStepVo);
        return processTaskVo;
    }

    private ProcessTaskVo getProcessTaskVoByCopyProcessTaskId(Long copyProcessTaskId) throws Exception {
        ProcessTaskVo oldProcessTaskVo = processTaskService.checkProcessTaskParamsIsLegal(copyProcessTaskId);
        ProcessTaskVo processTaskVo = getProcessTaskVoByChannelUuid(oldProcessTaskVo.getChannelUuid(), null, null);

        processTaskVo.setTitle(oldProcessTaskVo.getTitle());
        List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(oldProcessTaskVo.getChannelUuid());
        if(CollectionUtils.isNotEmpty(channelPriorityList)) {
            processTaskVo.setIsNeedPriority(1);
            for (ChannelPriorityVo channelPriority : channelPriorityList) {
                if (oldProcessTaskVo.getPriorityUuid().equals(channelPriority.getPriorityUuid())) {
                    processTaskVo.setPriorityUuid(channelPriority.getPriorityUuid());
                    break;
                }
            }
        }else{
            processTaskVo.setIsNeedPriority(0);
        }

        ProcessTaskStepVo oldStartProcessTaskStepVo = processTaskService.getStartProcessTaskStepByProcessTaskId(copyProcessTaskId);
        ProcessTaskStepVo startProcessTaskStepVo = processTaskVo.getStartProcessTaskStep();
        startProcessTaskStepVo.setComment(oldStartProcessTaskStepVo.getComment());
//        startProcessTaskStepVo.setHandlerStepInfo(oldStartProcessTaskStepVo.getHandlerStepInfo());
        /** 当前步骤特有步骤信息 **/
        IProcessStepInternalHandler startProcessStepUtilHandler =
                ProcessStepInternalHandlerFactory.getHandler(oldStartProcessTaskStepVo.getHandler());
        if (startProcessStepUtilHandler == null) {
            throw new ProcessStepHandlerNotFoundException(oldStartProcessTaskStepVo.getHandler());
        }
        startProcessTaskStepVo.setHandlerStepInfo(startProcessStepUtilHandler.getHandlerStepInitInfo(oldStartProcessTaskStepVo));
        processTaskVo.setFormAttributeDataMap(getFromFormAttributeDataMap(copyProcessTaskId, processTaskVo.getFormConfig()));
        // 标签列表
        processTaskVo.setTagVoList(processTaskMapper.getProcessTaskTagListByProcessTaskId(copyProcessTaskId));
        return processTaskVo;
    }

    private ProcessTaskVo getProcessTaskVoByChannelUuid(String channelUuid, Long fromProcessTaskId, Long channelTypeRelationId) throws PermissionDeniedException {
        ChannelVo channel = channelMapper.getChannelByUuid(channelUuid);
        if (channel == null) {
            throw new ChannelNotFoundException(channelUuid);
        }
        /** 判断当前用户是否拥有channelUuid服务的上报权限 **/
        if (!catalogService.channelIsAuthority(channelUuid, UserContext.get().getUserUuid(true))) {
            throw new PermissionDeniedException();
            /** 2021-10-11 开晚会时确认用户个人设置任务授权不包括服务上报权限 **/
//            String agentUuid = userMapper.getUserUuidByAgentUuidAndFunc(UserContext.get().getUserUuid(true), "processtask");
//            if(StringUtils.isNotBlank(agentUuid)){
//                if(!catalogService.channelIsAuthority(channelUuid, agentUuid)){
//                    throw new PermissionDeniedException();
//                }
//            }else{
//                throw new PermissionDeniedException();
//            }
        }
        ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(channel.getChannelTypeUuid());
        if (channelTypeVo == null) {
            throw new ChannelTypeNotFoundException(channel.getChannelTypeUuid());
        }
        String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
        ProcessVo processVo = processMapper.getProcessByUuid(processUuid);
        if (processVo == null) {
            throw new ProcessNotFoundException(processUuid);
        }

        ProcessTaskVo processTaskVo = new ProcessTaskVo();
        processTaskVo.setIsAutoGenerateId(false);
        try {
            processTaskVo.setChannelType(channelTypeVo.clone());
        } catch (CloneNotSupportedException e) {
        }
        processTaskVo.setChannelUuid(channelUuid);
        processTaskVo.setProcessUuid(processUuid);

        String worktimeUuid = channelMapper.getWorktimeUuidByChannelUuid(channelUuid);
        processTaskVo.setWorktimeUuid(worktimeUuid);
        List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(channelUuid);
        if(CollectionUtils.isNotEmpty(channelPriorityList)) {
            processTaskVo.setIsNeedPriority(1);
            for (ChannelPriorityVo channelPriority : channelPriorityList) {
                if (channelPriority.getIsDefault().intValue() == 1) {
                    processTaskVo.setPriorityUuid(channelPriority.getPriorityUuid());
                }
            }
        }else{
            processTaskVo.setIsNeedPriority(0);
        }

        ProcessStepVo startProcessStepVo = processMapper.getStartProcessStepByProcessUuid(processUuid);
        ProcessTaskStepVo startProcessTaskStepVo = new ProcessTaskStepVo(startProcessStepVo);
        startProcessTaskStepVo.setIsAutoGenerateId(false);

        // 获取须指派的步骤列表
        startProcessTaskStepVo.setAssignableWorkerStepList(processTaskService.getAssignableWorkerStepList(processUuid, startProcessTaskStepVo.getProcessStepUuid()));
        startProcessTaskStepVo.setIsRequired((Integer) JSONPath.read(startProcessStepVo.getConfig(), "isRequired"));
        startProcessTaskStepVo.setIsNeedContent((Integer) JSONPath.read(startProcessStepVo.getConfig(), "isNeedContent"));
        processTaskVo.setStartProcessTaskStep(startProcessTaskStepVo);

        processTaskService.setProcessTaskFormInfo(processTaskVo);
        if (fromProcessTaskId != null) {
            ProcessTaskVo fromProcessTaskVo = processTaskService.getFromProcessTaskById(fromProcessTaskId);
            ChannelRelationVo channelRelationVo = new ChannelRelationVo();
            channelRelationVo.setSource(fromProcessTaskVo.getChannelUuid());
            channelRelationVo.setChannelTypeRelationId(channelTypeRelationId);
            Integer isUsePreOwner = channelMapper.getChannelRelationIsUsePreOwnerBySourceAndChannelTypeRelationId(channelRelationVo);
            if (Objects.equals(isUsePreOwner, 1)) {
                String owner = fromProcessTaskVo.getOwner();
                if (StringUtils.isNotBlank(owner)) {
                    owner = GroupSearch.USER.getValuePlugin() + owner;
                    processTaskVo.setOwner(owner);
                }
            }
            processTaskVo.getTranferReportProcessTaskList().add(fromProcessTaskVo);
            if (MapUtils.isNotEmpty(processTaskVo.getFormConfig())) {
                processTaskVo.setFormAttributeDataMap(getFromFormAttributeDataMap(fromProcessTaskId, processTaskVo.getFormConfig()));
            }
        }
        return processTaskVo;
    }
}
