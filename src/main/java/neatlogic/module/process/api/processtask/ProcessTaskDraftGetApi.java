/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.api.processtask;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.dao.mapper.*;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.channel.ChannelNotFoundEditTargetException;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.channeltype.ChannelTypeNotFoundException;
import neatlogic.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;
import neatlogic.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import neatlogic.framework.process.exception.processtask.ProcessTaskNotFoundEditTargetException;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.ProcessMapper;
import neatlogic.module.process.service.CatalogService;
import neatlogic.module.process.service.ProcessTaskService;
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
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskDraftGetApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private CatalogMapper catalogMapper;

    @Resource
    private ChannelTypeMapper channelTypeMapper;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private CatalogService catalogService;

    @Override
    public String getToken() {
        return "processtask/draft/get";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskdraftgetapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "term.itsm.processtaskid", help = "从工单中心进入上报页时，传processTaskId"),
            @Param(name = "copyProcessTaskId", type = ApiParamType.LONG, desc = "term.itsm.copyprocesstaskid", help = "从复制上报进入上报页时，传copyProcessTaskId"),
            @Param(name = "channelUuid", type = ApiParamType.STRING, desc = "term.itsm.channeluuid", help = "从服务目录进入上报页时，传channelUuid"),
            @Param(name = "fromProcessTaskId", type = ApiParamType.LONG, desc = "term.itsm.fromprocesstaskid", help = "从转报进入上报页时，传fromProcessTaskId"),
            @Param(name = "channelTypeRelationId", type = ApiParamType.LONG, desc = "term.itsm.channeltyperelationid", help = "从转报进入上报页时，传channelTypeRelationId")
    })
    @Output({
            @Param(explode = ProcessTaskVo.class, desc = "term.itsm.processtaskinfo")
    })
    @Description(desc = "nmpap.processtaskdraftgetapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long copyProcessTaskId = jsonObj.getLong("copyProcessTaskId");
        String channelUuid = jsonObj.getString("channelUuid");
        ProcessTaskVo processTaskVo = null;
        if (processTaskId != null) {
            //已经暂存，从工单中心进入上报页
            if (processTaskMapper.getProcessTaskStepBaseInfoById(processTaskId) == null) {
                throw new ProcessTaskNotFoundEditTargetException(processTaskId);
            }
            try {
                new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_START)
                        .build()
                        .checkAndNoPermissionThrowException();
            } catch (ProcessTaskPermissionDeniedException e) {
                throw new PermissionDeniedException(e.getMessage());
            }
            processTaskVo=  getProcessTaskVoByProcessTaskId(processTaskId);
        } else if (copyProcessTaskId != null) {
            //复制上报
            if (processTaskMapper.getProcessTaskStepBaseInfoById(copyProcessTaskId) == null) {
                 throw new ProcessTaskNotFoundEditTargetException(copyProcessTaskId);
            }
            try {
                new ProcessAuthManager.TaskOperationChecker(copyProcessTaskId, ProcessTaskOperationType.PROCESSTASK_COPYPROCESSTASK)
                        .build()
                        .checkAndNoPermissionThrowException();
            } catch (ProcessTaskPermissionDeniedException e) {
                throw new PermissionDeniedException(e.getMessage());
            }
            processTaskVo =  getProcessTaskVoByCopyProcessTaskId(copyProcessTaskId);
        } else if (channelUuid != null) {
            if (channelMapper.checkChannelIsExists(channelUuid) == 0) {
                throw new ChannelNotFoundEditTargetException(channelUuid);
            }
            Long channelTypeRelationId = jsonObj.getLong("channelTypeRelationId");
            Long fromProcessTaskId = jsonObj.getLong("fromProcessTaskId");
            if (fromProcessTaskId != null) {
//                ProcessTaskVo fromProcessTaskVo = processTaskService.checkProcessTaskParamsIsLegal(fromProcessTaskId);
                if (channelTypeRelationId == null) {
                    throw new ParamNotExistsException("channelTypeRelationId");
                }
                //转报
                try {
                    new ProcessAuthManager.TaskOperationChecker(fromProcessTaskId, ProcessTaskOperationType.PROCESSTASK_TRANSFERREPORT)
                            .addExtraParam("channelTypeRelationId", channelTypeRelationId)
                            .build()
                            .checkAndNoPermissionThrowException();
                } catch (ProcessTaskPermissionDeniedException e) {
                    throw new PermissionDeniedException(e.getMessage());
                }
//                boolean flag = processTaskService.checkTransferReportAuthorization(fromProcessTaskVo, UserContext.get().getUserUuid(true), channelTypeRelationId);
//                if (!flag) {
//                    new ProcessTaskOperationUnauthorizedException(ProcessTaskOperationType.PROCESSTASK_TRANSFERREPORT);
//                }
            }
            /** 判断当前用户是否拥有channelUuid服务的上报权限 **/
            if (!catalogService.channelIsAuthority(channelUuid, UserContext.get().getUserUuid(true))) {
                throw new PermissionDeniedException();
            }
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
                fromFormVersion.setFormConfig(JSONObject.parseObject(formContent));
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
                toFormVersion.setFormConfig(toProcessTaskFormConfig);
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
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        processTaskService.setProcessTaskDetail(processTaskVo);
        String owner = processTaskVo.getOwner();
        if (StringUtils.isNotBlank(owner)) {
            owner = GroupSearch.USER.getValuePlugin() + owner;
            processTaskVo.setOwner(owner);
        }

        ProcessTaskStepVo startProcessTaskStepVo = processTaskService.getStartProcessTaskStepByProcessTaskId(processTaskId);
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

        ProcessTaskStepVo startProcessTaskStepVo = processTaskVo.getStartProcessTaskStep();
        ProcessTaskStepVo oldStartProcessTaskStepVo = processTaskService.getStartProcessTaskStepByProcessTaskId(copyProcessTaskId);
        ProcessTaskStepReplyVo oldComment = oldStartProcessTaskStepVo.getComment();
        if (oldComment != null) {
            String processUuid = processTaskVo.getProcessUuid();
            ProcessStepVo processStepVo = processMapper.getStartProcessStepByProcessUuid(processUuid);
            String processStepConfigStr = processStepVo.getConfig();
            if (StringUtils.isNotBlank(processStepConfigStr)) {
                Integer isNeedContent = (Integer) JSONPath.read(processStepConfigStr, "isNeedContent");
                Integer isNeedUploadFile = (Integer) JSONPath.read(processStepConfigStr, "isNeedUploadFile");
                if (Objects.equals(isNeedContent, 0) && Objects.equals(isNeedUploadFile, 0)) {
                    oldComment = null;
                } else if (Objects.equals(isNeedContent, 0)) {
                    oldComment.setContent(null);
                } else if (Objects.equals(isNeedUploadFile, 0)) {
                    oldComment.setFileIdList(null);
                    oldComment.setFileList(null);
                }
                startProcessTaskStepVo.setComment(oldComment);
            }
        }
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
        processTaskVo.setChannelVo(channel);
        processTaskVo.setProcessUuid(processUuid);
        CatalogVo catalogVo = catalogMapper.getCatalogByUuid(channel.getParentUuid());
        if (catalogVo != null) {
            List<CatalogVo> catalogList = catalogMapper.getAncestorsAndSelfByLftRht(catalogVo.getLft(), catalogVo.getRht());
            List<String> nameList = catalogList.stream().map(CatalogVo::getName).collect(Collectors.toList());
            nameList.add(channel.getName());
            processTaskVo.setChannelPath(String.join("/", nameList));
        }

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

        startProcessTaskStepVo.setIsRequired((Integer) JSONPath.read(startProcessStepVo.getConfig(), "isRequired"));
        startProcessTaskStepVo.setIsNeedContent((Integer) JSONPath.read(startProcessStepVo.getConfig(), "isNeedContent"));
        startProcessTaskStepVo.setIsNeedUploadFile((Integer) JSONPath.read(startProcessStepVo.getConfig(), "isNeedUploadFile"));
        startProcessTaskStepVo.setFormSceneUuid((String) JSONPath.read(startProcessStepVo.getConfig(), "formSceneUuid"));
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
