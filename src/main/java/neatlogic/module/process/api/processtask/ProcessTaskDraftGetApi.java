/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
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
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.channel.ChannelNotFoundEditTargetException;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.channeltype.ChannelTypeNotFoundException;
import neatlogic.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;
import neatlogic.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import neatlogic.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import neatlogic.framework.process.exception.processtask.ProcessTaskNotFoundEditTargetException;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.catalog.CatalogMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.CatalogService;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
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
            @Param(name = "channelTypeRelationId", type = ApiParamType.LONG, desc = "term.itsm.channeltyperelationid", help = "从转报进入上报页时，传channelTypeRelationId"),
            @Param(name = "parentProcessTaskStepId", type = ApiParamType.LONG, desc = "nmpap.processtaskdraftgetapi.input.param.desc.parentprocesstaskstepid", help = "创建子流程时，传parentProcessTaskStepId")
    })
    @Output({
            @Param(explode = ProcessTaskVo.class, desc = "term.itsm.processtaskinfo")
    })
    @Description(desc = "nmpap.processtaskdraftgetapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long copyProcessTaskId = jsonObj.getLong("copyProcessTaskId");
        Long parentProcessTaskStepId = jsonObj.getLong("parentProcessTaskStepId");
        String channelUuid = jsonObj.getString("channelUuid");
        ProcessTaskVo processTaskVo = null;
        if (processTaskId != null) {
            //已经暂存，从工单中心进入上报页
            if (processTaskMapper.getProcessTaskBaseInfoById(processTaskId) == null) {
                throw new ProcessTaskNotFoundEditTargetException(processTaskId);
            }
            try {
                new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_START)
                        .build()
                        .checkAndNoPermissionThrowException();
            } catch (ProcessTaskPermissionDeniedException e) {
                throw new PermissionDeniedException(e.getMessage());
            }
            processTaskVo = getProcessTaskVoByProcessTaskId(processTaskId);
        } else if (copyProcessTaskId != null) {
            //复制上报
            if (processTaskMapper.getProcessTaskBaseInfoById(copyProcessTaskId) == null) {
                throw new ProcessTaskNotFoundEditTargetException(copyProcessTaskId);
            }
            try {
                new ProcessAuthManager.TaskOperationChecker(copyProcessTaskId, ProcessTaskOperationType.PROCESSTASK_COPYPROCESSTASK)
                        .build()
                        .checkAndNoPermissionThrowException();
            } catch (ProcessTaskPermissionDeniedException e) {
                throw new PermissionDeniedException(e.getMessage());
            }
            processTaskVo = getProcessTaskVoByCopyProcessTaskId(copyProcessTaskId);
        } else if (channelUuid != null) {
            if (channelMapper.checkChannelIsExists(channelUuid) == 0) {
                throw new ChannelNotFoundEditTargetException(channelUuid);
            }
            /* 判断当前用户是否拥有channelUuid服务的上报权限 */
            if (!catalogService.channelIsAuthority(channelUuid, UserContext.get().getUserUuid(true))) {
                throw new PermissionDeniedException();
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
            } else if (parentProcessTaskStepId != null) {
                return getParentProcessTask(parentProcessTaskStepId, channelUuid);
            }
            processTaskVo = getProcessTaskVoByChannelUuid(channelUuid, fromProcessTaskId, channelTypeRelationId);
        } else {
            throw new ParamNotExistsException("processTaskId", "copyProcessTaskId", "channelUuid", "parentProcessTaskStepId");
        }
        return processTaskVo;
    }

    /**
     * 获取来源工单中与当前工单表单属性标签名相同的属性值
     *
     * @param fromProcessTaskId       来源工单id
     * @param toProcessTaskFormConfig 目标工单表单配置
     **/
    private Map<String, Object> getFromFormAttributeDataMap(Long fromProcessTaskId, JSONObject toProcessTaskFormConfig) {
        Map<String, Object> resultObj = new HashMap<>();
        if (MapUtils.isEmpty(toProcessTaskFormConfig)) {
            return resultObj;
        }
        JSONObject fromProcessTaskFormAttrDataMap = getFromFormAttributeDataMap(fromProcessTaskId);
        JSONObject labelUuidMap = fromProcessTaskFormAttrDataMap.getJSONObject("labelUuidMap");
        JSONObject labelHandlerMap = fromProcessTaskFormAttrDataMap.getJSONObject("labelHandlerMap");
        JSONObject keyUuidMap = fromProcessTaskFormAttrDataMap.getJSONObject("keyUuidMap");
        JSONObject keyHandlerMap = fromProcessTaskFormAttrDataMap.getJSONObject("keyHandlerMap");
        JSONObject formAttributeDataMap = fromProcessTaskFormAttrDataMap.getJSONObject("formAttributeDataMap");
        //获取目标表单值
        FormVersionVo toFormVersion = new FormVersionVo();
        toFormVersion.setFormConfig(toProcessTaskFormConfig);
        String mainSceneUuid = toProcessTaskFormConfig.getString("uuid");
        toFormVersion.setSceneUuid(mainSceneUuid);
        for (FormAttributeVo formAttributeVo : toFormVersion.getFormAttributeList()) {
            String fromFormAttributeHandler = keyHandlerMap.getString(formAttributeVo.getKey());
            if (Objects.equals(fromFormAttributeHandler, formAttributeVo.getHandler())) {
                String fromFormAttributeUuid = keyUuidMap.getString(formAttributeVo.getKey());
                if (StringUtils.isNotBlank(fromFormAttributeUuid)) {
                    Object data = formAttributeDataMap.get(fromFormAttributeUuid);
                    if (data != null) {
                        resultObj.put(formAttributeVo.getUuid(), data);
                    }
                }
            } else {
                fromFormAttributeHandler = labelHandlerMap.getString(formAttributeVo.getLabel());
                if (Objects.equals(fromFormAttributeHandler, formAttributeVo.getHandler())) {
                    String fromFormAttributeUuid = labelUuidMap.getString(formAttributeVo.getLabel());
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

    /**
     * 获取来源工单的表单值映射
     *
     * @param fromProcessTaskId 来源工单id
     */
    private JSONObject getFromFormAttributeDataMap(Long fromProcessTaskId) {
        JSONObject resultObj = new JSONObject();
        Map<String, String> labelUuidMap = new HashMap<>();
        Map<String, String> labelHandlerMap = new HashMap<>();
        Map<String, String> keyUuidMap = new HashMap<>();
        Map<String, String> keyHandlerMap = new HashMap<>();
        Map<String, Object> formAttributeDataMap = new HashMap<>();
        // 获取旧工单表单信息
        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(fromProcessTaskId);
        if (processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContentHash())) {
            String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
            if (StringUtils.isNotBlank(formContent)) {
                JSONObject formConfig = JSON.parseObject(formContent);
                FormVersionVo fromFormVersion = new FormVersionVo();
                fromFormVersion.setFormConfig(formConfig);
                String mainSceneUuid = formConfig.getString("uuid");
                fromFormVersion.setSceneUuid(mainSceneUuid);
                List<FormAttributeVo> fromFormAttributeList = fromFormVersion.getFormAttributeList();
                for (FormAttributeVo formAttributeVo : fromFormAttributeList) {
                    labelUuidMap.put(formAttributeVo.getLabel(), formAttributeVo.getUuid());
                    labelHandlerMap.put(formAttributeVo.getLabel(), formAttributeVo.getHandler());
                    if (StringUtils.isNotBlank(formAttributeVo.getKey())) {
                        keyUuidMap.put(formAttributeVo.getKey(), formAttributeVo.getUuid());
                        keyHandlerMap.put(formAttributeVo.getKey(), formAttributeVo.getHandler());
                    }
                }
                List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskService.getProcessTaskFormAttributeDataListByProcessTaskId(fromProcessTaskId);
                for (ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                    formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
                }
            }
        }
        resultObj.put("labelUuidMap", labelUuidMap);
        resultObj.put("labelHandlerMap", labelHandlerMap);
        resultObj.put("keyUuidMap", keyUuidMap);
        resultObj.put("keyHandlerMap", keyHandlerMap);
        resultObj.put("formAttributeDataMap", formAttributeDataMap);
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

    /**
     * 获取子流程工单信息（包含父流程的表单值）
     *
     * @param parentProcessTaskStepId 父工单步骤id
     * @param channelUuid             子流程服务uuid
     */
    private ProcessTaskVo getParentProcessTask(Long parentProcessTaskStepId, String channelUuid) throws Exception {
        ProcessTaskStepVo parentProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(parentProcessTaskStepId);
        processTaskService.checkProcessTaskParamsIsLegal(parentProcessTaskStepVo.getProcessTaskId(), parentProcessTaskStepId);
        ProcessTaskVo processTaskVo = getProcessTaskVoByChannelUuid(channelUuid);
        JSONObject fromProcessTaskFormAttrDataMap = getFromFormAttributeDataMap(parentProcessTaskStepVo.getProcessTaskId());
        JSONObject labelUuidMap = fromProcessTaskFormAttrDataMap.getJSONObject("labelUuidMap");
        JSONObject labelHandlerMap = fromProcessTaskFormAttrDataMap.getJSONObject("labelHandlerMap");
        JSONObject formAttributeDataMap = fromProcessTaskFormAttrDataMap.getJSONObject("formAttributeDataMap");
        if (MapUtils.isNotEmpty(labelUuidMap) && MapUtils.isNotEmpty(labelHandlerMap) && MapUtils.isNotEmpty(formAttributeDataMap)) {
            //获取父流程步骤配置信息
            IProcessStepInternalHandler processStepUtilHandler = ProcessStepInternalHandlerFactory.getHandler(parentProcessTaskStepVo.getHandler());
            if (processStepUtilHandler == null) {
                throw new ProcessStepUtilHandlerNotFoundException(parentProcessTaskStepVo.getHandler());
            }
            Object parenStepInfoObj = processStepUtilHandler.getNonStartStepInfo(parentProcessTaskStepVo);
            if (parenStepInfoObj != null) {
                JSONObject parenStepInfo = (JSONObject) parenStepInfoObj;
                JSONObject parentStepChannelFormMapping = parenStepInfo.getJSONObject("formMapping");
                if (MapUtils.isNotEmpty(parentStepChannelFormMapping)) {
                    JSONObject parentSubProcessTaskStepConfigFormMapping = parentStepChannelFormMapping.getJSONObject(channelUuid);
                    if (MapUtils.isNotEmpty(parentSubProcessTaskStepConfigFormMapping)) {
                        //获取目标表单值
                        Map<String, Object> resultObj = new HashMap<>();
                        FormVersionVo toFormVersion = new FormVersionVo();
                        toFormVersion.setFormConfig(processTaskVo.getFormConfig());
                        String mainSceneUuid = processTaskVo.getFormConfig().getString("uuid");
                        toFormVersion.setSceneUuid(mainSceneUuid);
                        for (FormAttributeVo formAttributeVo : toFormVersion.getFormAttributeList()) {
                            String parentFormLabel = parentSubProcessTaskStepConfigFormMapping.getString(formAttributeVo.getLabel());
                            if (StringUtils.isNotBlank(parentFormLabel)) {
                                String fromFormAttributeHandler = labelHandlerMap.getString(parentFormLabel);
                                if (Objects.equals(fromFormAttributeHandler, formAttributeVo.getHandler())) {
                                    String fromFormAttributeUuid = labelUuidMap.getString(parentFormLabel);
                                    if (StringUtils.isNotBlank(fromFormAttributeUuid)) {
                                        Object data = formAttributeDataMap.get(fromFormAttributeUuid);
                                        if (data != null) {
                                            resultObj.put(formAttributeVo.getUuid(), data);
                                        }
                                    }
                                }
                            }
                        }
                        processTaskVo.setFormAttributeDataMap(resultObj);
                    }
                }
            }
        }
        return processTaskVo;
    }

    private ProcessTaskVo getProcessTaskVoByCopyProcessTaskId(Long copyProcessTaskId) throws Exception {
        ProcessTaskVo oldProcessTaskVo = processTaskService.checkProcessTaskParamsIsLegal(copyProcessTaskId);
        ProcessTaskVo processTaskVo = getProcessTaskVoByChannelUuid(oldProcessTaskVo.getChannelUuid(), null, null);

        processTaskVo.setTitle(oldProcessTaskVo.getTitle());
        List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(oldProcessTaskVo.getChannelUuid());
        if (CollectionUtils.isNotEmpty(channelPriorityList)) {
            processTaskVo.setIsNeedPriority(1);
            for (ChannelPriorityVo channelPriority : channelPriorityList) {
                if (oldProcessTaskVo.getPriorityUuid().equals(channelPriority.getPriorityUuid())) {
                    processTaskVo.setPriorityUuid(channelPriority.getPriorityUuid());
                    break;
                }
            }
        } else {
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
        startProcessTaskStepVo.setHandlerStepInfo(startProcessStepUtilHandler.getNonStartStepInfo(oldStartProcessTaskStepVo));
        processTaskVo.setFormAttributeDataMap(getFromFormAttributeDataMap(copyProcessTaskId, processTaskVo.getFormConfig()));
        // 标签列表
        processTaskVo.setTagVoList(processTaskMapper.getProcessTaskTagListByProcessTaskId(copyProcessTaskId));
        return processTaskVo;
    }

    private ProcessTaskVo getProcessTaskVoByChannelUuid(String channelUuid) {
        return getProcessTaskVoByChannelUuid(channelUuid, null, null);
    }

    private ProcessTaskVo getProcessTaskVoByChannelUuid(String channelUuid, Long fromProcessTaskId, Long channelTypeRelationId) {
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
        processTaskVo.setProcessDispatcherList(getProcessDispatcherList(processVo.getConfig()));
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
        if (CollectionUtils.isNotEmpty(channelPriorityList)) {
            processTaskVo.setIsNeedPriority(1);
            for (ChannelPriorityVo channelPriority : channelPriorityList) {
                if (channelPriority.getIsDefault().intValue() == 1) {
                    processTaskVo.setPriorityUuid(channelPriority.getPriorityUuid());
                }
            }
        } else {
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

    /**
     * 根据流程配置返回分派器列表
     *
     * @param processConfig 流程配置
     */
    private List<String> getProcessDispatcherList(JSONObject processConfig) {
        List<String> processDispathcerList = new ArrayList<>();
        if (processConfig.getJSONObject("process") != null) {
            JSONArray stepListArray = processConfig.getJSONObject("process").getJSONArray("stepList");
            if (CollectionUtils.isNotEmpty(stepListArray)) {
                for (int i = 0; i < stepListArray.size(); i++) {
                    JSONObject step = stepListArray.getJSONObject(i);
                    if (Objects.equals(step.getString("type"), "process")) {
                        if (step.getJSONObject("stepConfig") != null && step.getJSONObject("stepConfig").getJSONObject("workerPolicyConfig") != null) {
                            JSONArray policyArray = step.getJSONObject("stepConfig").getJSONObject("workerPolicyConfig").getJSONArray("policyList");
                            if (CollectionUtils.isNotEmpty(policyArray)) {
                                for (int j = 0; j < policyArray.size(); j++) {
                                    JSONObject policy = policyArray.getJSONObject(j);
                                    if (Objects.equals(policy.getString("type"), "automatic")) {
                                        if (policy.getJSONObject("config") != null && StringUtils.isNotBlank(policy.getJSONObject("config").getString("handler"))) {
                                            processDispathcerList.add(policy.getJSONObject("config").getString("handler"));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
        return processDispathcerList;
    }
}
