/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.process.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.change.constvalue.ChangeProcessStepHandlerType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.common.constvalue.UserType;
import neatlogic.framework.config.ConfigManager;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dao.mapper.region.RegionMapper;
import neatlogic.framework.dto.*;
import neatlogic.framework.dto.region.RegionVo;
import neatlogic.framework.event.constvalue.EventProcessStepHandlerType;
import neatlogic.framework.exception.file.FileNotFoundException;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.exception.user.UserNotFoundException;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.AttributeDataVo;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.exception.FormActiveVersionNotFoundExcepiton;
import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import neatlogic.framework.fulltextindex.core.IFullTextIndexHandler;
import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.notify.dto.NotifyReceiverVo;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnFactory;
import neatlogic.framework.process.constvalue.*;
import neatlogic.framework.process.crossover.IProcessTaskCrossoverService;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.file.ProcessTaskFileDownloadException;
import neatlogic.framework.process.exception.operationauth.*;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;
import neatlogic.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import neatlogic.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import neatlogic.framework.process.exception.processtask.*;
import neatlogic.framework.process.exception.processtask.task.ProcessTaskStepTaskNotCompleteException;
import neatlogic.framework.process.fulltextindex.ProcessFullTextIndexType;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyTriggerType;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyTriggerType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.process.stephandler.core.IProcessStepHandler;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.process.stepremind.core.ProcessTaskStepRemindTypeFactory;
import neatlogic.framework.process.task.TaskConfigManager;
import neatlogic.framework.process.workerpolicy.core.IWorkerPolicyHandler;
import neatlogic.framework.process.workerpolicy.core.WorkerPolicyHandlerFactory;
import neatlogic.framework.service.AuthenticationInfoService;
import neatlogic.framework.util.$;
import neatlogic.framework.util.FormUtil;
import neatlogic.framework.util.TimeUtil;
import neatlogic.framework.worktime.dao.mapper.WorktimeMapper;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.catalog.CatalogMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.module.process.dao.mapper.catalog.PriorityMapper;
import neatlogic.module.process.dao.mapper.process.ProcessCommentTemplateMapper;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.process.ProcessStepHandlerMapper;
import neatlogic.module.process.dao.mapper.process.ProcessTagMapper;
import neatlogic.module.process.dao.mapper.processtask.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

@Service
public class ProcessTaskServiceImpl implements ProcessTaskService, IProcessTaskCrossoverService {

    // private static final Logger logger = LoggerFactory.getLogger(ProcessTaskServiceImpl.class);

    private final Pattern pattern_html = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskSlaMapper processTaskSlaMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private ProcessTaskStepTaskMapper processTaskStepTaskMapper;

    @Resource
    private WorktimeMapper worktimeMapper;

    @Resource
    ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private FormMapper formMapper;

    @Resource
    private ProcessStepHandlerMapper processStepHandlerMapper;

    @Resource
    private PriorityMapper priorityMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private ChannelTypeMapper channelTypeMapper;
    @Resource
    private CatalogMapper catalogMapper;
    @Resource
    private ProcessCommentTemplateMapper commentTemplateMapper;
    @Resource
    private ProcessTaskStepTaskService processTaskStepTaskService;
    @Resource
    private ProcessTaskAgentService processTaskAgentService;
    @Resource
    private ProcessTagMapper processTagMapper;

    @Resource
    private CatalogService catalogService;

    @Resource
    private TaskConfigManager taskConfigManager;
    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Resource
    private RegionMapper regionMapper;

    @Resource
    private ProcessTaskActionMapper processTaskActionMapper;
    @Resource
    private IntegrationMapper integrationMapper;
//    @Override
//    public void setProcessTaskFormAttributeAction(ProcessTaskVo processTaskVo,
//                                                  Map<String, String> formAttributeActionMap, int mode) {
//        Map<String, Object> formAttributeDataMap = processTaskVo.getFormAttributeDataMap();
//        if (formAttributeDataMap == null) {
//            formAttributeDataMap = new HashMap<>();
//        }
//        JSONObject formConfigObj = processTaskVo.getFormConfig();
//        if (MapUtils.isNotEmpty(formConfigObj)) {
//            JSONArray controllerList = formConfigObj.getJSONArray("controllerList");
//            if (CollectionUtils.isNotEmpty(controllerList)) {
//                List<String> currentUserProcessUserTypeList = new ArrayList<>();
//                AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(UserContext.get().getUserUuid(true));
//                List<String> currentUserTeamList = authenticationInfoVo.getTeamUuidList();
//                List<String> roleUuidList = authenticationInfoVo.getRoleUuidList();
//                if (mode == 0) {
//                    currentUserProcessUserTypeList.add(UserType.ALL.getValue());
//                    if (UserContext.get().getUserUuid(true).equals(processTaskVo.getOwner())) {
//                        currentUserProcessUserTypeList.add(ProcessUserType.OWNER.getValue());
//                    }
//                    if (UserContext.get().getUserUuid(true).equals(processTaskVo.getReporter())) {
//                        currentUserProcessUserTypeList.add(ProcessUserType.REPORTER.getValue());
//                    }
//                } else if (mode == 1) {
//                    if (formAttributeActionMap == null) {
//                        formAttributeActionMap = new HashMap<>();
//                    }
//                }
//
//                for (int i = 0; i < controllerList.size(); i++) {
//                    JSONObject attributeObj = controllerList.getJSONObject(i);
//                    String action = FormAttributeAction.HIDE.getValue();
//                    JSONObject config = attributeObj.getJSONObject("config");
//                    if (mode == 0) {
//                        if (MapUtils.isNotEmpty(config)) {
//                            List<String> authorityList =
//                                    JSON.parseArray(config.getString("authorityConfig"), String.class);
//                            if (CollectionUtils.isNotEmpty(authorityList)) {
//                                for (String authority : authorityList) {
//                                    String[] split = authority.split("#");
//                                    if (GroupSearch.COMMON.getValue().equals(split[0])) {
//                                        if (currentUserProcessUserTypeList.contains(split[1])) {
//                                            action = FormAttributeAction.READ.getValue();
//                                            break;
//                                        }
//                                    } else if (ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue()
//                                            .equals(split[0])) {
//                                        if (currentUserProcessUserTypeList.contains(split[1])) {
//                                            action = FormAttributeAction.READ.getValue();
//                                            break;
//                                        }
//                                    } else if (GroupSearch.USER.getValue().equals(split[0])) {
//                                        if (UserContext.get().getUserUuid(true).equals(split[1])) {
//                                            action = FormAttributeAction.READ.getValue();
//                                            break;
//                                        }
//                                    } else if (GroupSearch.TEAM.getValue().equals(split[0])) {
//                                        if (currentUserTeamList.contains(split[1])) {
//                                            action = FormAttributeAction.READ.getValue();
//                                            break;
//                                        }
//                                    } else if (GroupSearch.ROLE.getValue().equals(split[0])) {
//                                        if (roleUuidList.contains(split[1])) {
//                                            action = FormAttributeAction.READ.getValue();
//                                            break;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    } else if (mode == 1) {
//                        action = formAttributeActionMap.get(attributeObj.getString("uuid"));
//                        if (StringUtils.isBlank(action)) {
//                            action = formAttributeActionMap.get("all");
//                        }
//                    }
//                    if (FormAttributeAction.READ.getValue().equals(action)) {
//                        attributeObj.put("isReadonly", true);
//                    } else if (FormAttributeAction.HIDE.getValue().equals(action)) {
//                        attributeObj.put("isHide", true);
//                        formAttributeDataMap.remove(attributeObj.getString("uuid"));// 对于隐藏属性，不返回值
//                        if (config != null) {
//                            config.remove("value");
//                            config.remove("defaultValueList");// 对于隐藏属性，不返回默认值
//                        }
//                    }
//                }
//                processTaskVo.setFormConfig(formConfigObj);
//            }
//        }
//
//    }

    @Override
    public void setProcessTaskFormInfo(ProcessTaskVo processTaskVo) {
        Long processTaskId = processTaskVo.getId();
        if (processTaskId != null) {
            ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
//            String formContent = selectContentByHashMapper.getProcessTaskFromContentByProcessTaskId(processTaskId);
            if (processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContent())) {
                processTaskVo.setFormConfig(JSON.parseObject(processTaskFormVo.getFormContent()));
                List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = getProcessTaskFormAttributeDataListByProcessTaskId(processTaskVo.getId());
                for (ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                    processTaskVo.getFormAttributeDataMap().put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
                }
                processTaskVo.setProcessTaskFormAttributeDataList(processTaskFormAttributeDataList);
                // 获取工单流程图信息
                String taskConfig = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
                JSONArray formConfigAuthorityList = (JSONArray) JSONPath.read(taskConfig, "process.formConfig.authorityList");
                processTaskVo.setFormConfigAuthorityList(formConfigAuthorityList);
                List<String> formAttributeHideList = getFormConfigAuthorityConfig(processTaskVo);
                processTaskVo.setFormAttributeHideList(formAttributeHideList);
            }
        } else {
            String processUuid = processTaskVo.getProcessUuid();
            if (StringUtils.isNotBlank(processUuid)) {
                ProcessVo processVo = processMapper.getProcessByUuid(processUuid);
                if (processVo != null) {
                    String formUuid = processVo.getFormUuid();
                    if (StringUtils.isNotBlank(formUuid)) {
                        FormVersionVo formVersion = formMapper.getActionFormVersionByFormUuid(formUuid);
                        if (formVersion == null) {
                            throw new FormActiveVersionNotFoundExcepiton(formUuid);
                        }
                        processTaskVo.setFormConfig(formVersion.getFormConfig());
                        JSONObject processConfig = processVo.getConfig();
                        if (MapUtils.isNotEmpty(processConfig)) {
                            JSONObject process = processConfig.getJSONObject("process");
                            if (MapUtils.isNotEmpty(process)) {
                                JSONObject formConfig = process.getJSONObject("formConfig");
                                if (MapUtils.isNotEmpty(formConfig)) {
                                    JSONArray authorityList = formConfig.getJSONArray("authorityList");
                                    processTaskVo.setFormConfigAuthorityList(authorityList);
                                }
                            }
                        }
                        List<String> formAttributeHideList = getFormConfigAuthorityConfig(processTaskVo);
                        processTaskVo.setFormAttributeHideList(formAttributeHideList);
                    }
                }
            }
        }
    }

    @Override
    public List<String> getFormConfigAuthorityConfig(ProcessTaskVo processTaskVo) {
        List<String> resultList = new ArrayList<>();
        JSONObject formConfigObj = processTaskVo.getFormConfig();
        if (MapUtils.isNotEmpty(formConfigObj)) {
            JSONArray controllerList = formConfigObj.getJSONArray("controllerList");
            if (CollectionUtils.isNotEmpty(controllerList)) {
                Set<String> typeSet = new HashSet<>();
                for (int i = 0; i < controllerList.size(); i++) {
                    JSONObject attributeObj = controllerList.getJSONObject(i);
                    JSONObject config = attributeObj.getJSONObject("config");
                    if (MapUtils.isNotEmpty(config)) {
                        JSONArray authorityArray = config.getJSONArray("authorityConfig");
                        if (CollectionUtils.isNotEmpty(authorityArray)) {
                            List<String> authorityList = authorityArray.toJavaList(String.class);
                            for (String authority : authorityList) {
                                String[] split = authority.split("#");
                                if (ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue().equals(split[0])) {
                                    typeSet.add(split[1]);
                                } else if (GroupSearch.USER.getValue().equals(split[0])) {
                                    typeSet.add(split[0]);
                                } else if (GroupSearch.TEAM.getValue().equals(split[0])) {
                                    typeSet.add(split[0]);
                                } else if (GroupSearch.ROLE.getValue().equals(split[0])) {
                                    typeSet.add(split[0]);
                                }
                            }
                        }
                    }
                }
                List<String> userUuidList = new ArrayList<>();
                String userUuid = UserContext.get().getUserUuid(true);
                userUuidList.add(userUuid);
                if (CollectionUtils.isNotEmpty(typeSet)) {
                    List<String> fromUserUuidList = processTaskAgentService.getFromUserUuidListByToUserUuidAndChannelUuid(userUuid, processTaskVo.getChannelUuid());
                    userUuidList.addAll(fromUserUuidList);
                }

                List<String> teamUuidList = new ArrayList<>();
                List<String> roleUuidList = new ArrayList<>();
                AuthenticationInfoVo authenticationInfoVo = null;
                if (typeSet.contains(GroupSearch.TEAM.getValue()) || typeSet.contains(GroupSearch.ROLE.getValue()) || typeSet.contains(ProcessUserType.WORKER.getValue())) {
                    authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userUuidList);
                    teamUuidList = authenticationInfoVo.getTeamUuidList();
                    roleUuidList = authenticationInfoVo.getRoleUuidList();
                }

                List<String> currentUserProcessUserTypeList = new ArrayList<>();
                currentUserProcessUserTypeList.add(UserType.ALL.getValue());
                if (processTaskVo.getId() != null) {
                    if (userUuidList.contains(processTaskVo.getOwner())) {
                        currentUserProcessUserTypeList.add(ProcessUserType.OWNER.getValue());
                    }
                    if (userUuidList.contains(processTaskVo.getReporter())) {
                        currentUserProcessUserTypeList.add(ProcessUserType.REPORTER.getValue());
                    }
                    ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
                    processTaskStepUserVo.setProcessTaskId(processTaskVo.getId());
                    processTaskStepUserVo.setUserUuidList(userUuidList);
                    if (typeSet.contains(ProcessUserType.MAJOR.getValue())) {
                        processTaskStepUserVo.setUserType(ProcessUserType.MAJOR.getValue());
                        if (processTaskMapper.checkIsProcessTaskStepUser(processTaskStepUserVo) > 0) {
                            currentUserProcessUserTypeList.add(ProcessUserType.MAJOR.getValue());
                        }
                    }

                    if (typeSet.contains(ProcessUserType.MINOR.getValue())) {
                        processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
                        if (processTaskMapper.checkIsProcessTaskStepUser(processTaskStepUserVo) > 0) {
                            currentUserProcessUserTypeList.add(ProcessUserType.MINOR.getValue());
                        }
                    }
                    if (typeSet.contains(ProcessUserType.WORKER.getValue())) {
                        if (processTaskMapper.checkIsWorker(processTaskVo.getId(), null, null, authenticationInfoVo) > 0) {
                            currentUserProcessUserTypeList.add(ProcessUserType.WORKER.getValue());
                        }
                    }
                } else {
                    // 没有工单id说明是在上报页，当用户即是上报人、代报人、处理人、协助处理人、待处理人
                    currentUserProcessUserTypeList.add(ProcessUserType.OWNER.getValue());
                    currentUserProcessUserTypeList.add(ProcessUserType.REPORTER.getValue());
                    currentUserProcessUserTypeList.add(ProcessUserType.MAJOR.getValue());
                    currentUserProcessUserTypeList.add(ProcessUserType.MINOR.getValue());
                    currentUserProcessUserTypeList.add(ProcessUserType.WORKER.getValue());
                }

                for (int i = 0; i < controllerList.size(); i++) {
                    JSONObject attributeObj = controllerList.getJSONObject(i);
                    String uuid = attributeObj.getString("uuid");
                    JSONObject config = attributeObj.getJSONObject("config");
                    if (MapUtils.isNotEmpty(config)) {
                        boolean isHide = true;
                        JSONArray authorityArray = config.getJSONArray("authorityConfig");
                        if (CollectionUtils.isNotEmpty(authorityArray)) {
                            List<String> authorityList = authorityArray.toJavaList(String.class);
                            for (String authority : authorityList) {
                                String[] split = authority.split("#");
                                if (GroupSearch.COMMON.getValue().equals(split[0])) {
                                    if (currentUserProcessUserTypeList.contains(split[1])) {
                                        isHide = false;
                                        break;
                                    }
                                } else if (ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue().equals(split[0])) {
                                    if (currentUserProcessUserTypeList.contains(split[1])) {
                                        isHide = false;
                                        break;
                                    }
                                } else if (GroupSearch.USER.getValue().equals(split[0])) {
                                    if (UserContext.get().getUserUuid(true).equals(split[1])) {
                                        isHide = false;
                                        break;
                                    }
                                } else if (GroupSearch.TEAM.getValue().equals(split[0])) {
                                    if (teamUuidList.contains(split[1])) {
                                        isHide = false;
                                        break;
                                    }
                                } else if (GroupSearch.ROLE.getValue().equals(split[0])) {
                                    if (roleUuidList.contains(split[1])) {
                                        isHide = false;
                                        break;
                                    }
                                }
                            }
                        }
                        if (isHide) {
                            resultList.add(uuid);
                        }
                    }
                }
            }
        }
        return resultList;
    }

    @Override
    public void parseProcessTaskStepReply(ProcessTaskStepReplyVo processTaskStepReplyVo) {
        if (StringUtils.isBlank(processTaskStepReplyVo.getContent())
                && StringUtils.isNotBlank(processTaskStepReplyVo.getContentHash())) {
            processTaskStepReplyVo.setContent(
                    selectContentByHashMapper.getProcessTaskContentStringByHash(processTaskStepReplyVo.getContentHash()));
        }
        List<Long> fileIdList = processTaskMapper.getFileIdListByContentId(processTaskStepReplyVo.getId());
        if (CollectionUtils.isNotEmpty(fileIdList)) {
            List<FileVo> fileList = fileMapper.getFileListByIdList(fileIdList);
            if (CollectionUtils.isNotEmpty(fileList)) {
                processTaskStepReplyVo.setFileIdList(fileList.stream().map(FileVo::getId).collect(Collectors.toList()));
                processTaskStepReplyVo.setFileList(fileList);
            }
        }
        List<WorkAssignmentUnitVo> targetList = processTaskMapper.getTargetListByContentId(processTaskStepReplyVo.getId());
        processTaskStepReplyVo.setTargetList(targetList);
    }

    @Override
    public ProcessTaskStepVo getProcessTaskStepDetailInfoById(Long processTaskStepId) {
        // 获取步骤信息
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);

        // 处理人列表
        List<ProcessTaskStepUserVo> majorUserList =
                processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MAJOR.getValue());
        if (CollectionUtils.isNotEmpty(majorUserList)) {
            processTaskStepVo.setMajorUser(majorUserList.get(0));
        }
        List<ProcessTaskStepUserVo> minorUserList =
                processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, ProcessUserType.MINOR.getValue());
        processTaskStepVo.setMinorUserList(minorUserList);

        List<ProcessTaskStepWorkerVo> workerList =
                processTaskMapper.getProcessTaskStepWorkerByProcessTaskIdAndProcessTaskStepId(
                        processTaskStepVo.getProcessTaskId(), processTaskStepId);
        for (ProcessTaskStepWorkerVo workerVo : workerList) {
            if (workerVo.getType().equals(GroupSearch.USER.getValue())) {
                UserVo userVo = userMapper.getUserBaseInfoByUuid(workerVo.getUuid());
                if (userVo != null) {
                    workerVo.setWorker(new WorkAssignmentUnitVo(userVo));
                    workerVo.setName(userVo.getUserName());
                }
            } else if (workerVo.getType().equals(GroupSearch.TEAM.getValue())) {
                TeamVo search = new TeamVo();
                search.setUuid(workerVo.getUuid());
                TeamVo teamVo = teamMapper.getTeamSimpleInfoByUuid(search);
                if (teamVo != null) {
                    workerVo.setWorker(new WorkAssignmentUnitVo(teamVo));
                    workerVo.setName(teamVo.getName());
                }
            } else if (workerVo.getType().equals(GroupSearch.ROLE.getValue())) {
                RoleVo roleVo = roleMapper.getRoleSimpleInfoByUuid(workerVo.getUuid());
                if (roleVo != null) {
                    workerVo.setWorker(new WorkAssignmentUnitVo(roleVo));
                    workerVo.setName(roleVo.getName());
                }
            }
        }
        processTaskStepVo.setWorkerList(workerList);

        return processTaskStepVo;
    }

    @Override
    public List<String> getProcessUserTypeList(Long processTaskId, AuthenticationInfoVo authenticationInfoVo) {
        List<String> processUserTypeList = new ArrayList<>();
        if (processTaskId != null) {
            String userUuid = authenticationInfoVo.getUserUuid();
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
            if (userUuid.equals(processTaskVo.getOwner())) {
                processUserTypeList.add(ProcessUserType.OWNER.getValue());
            }
            if (userUuid.equals(processTaskVo.getReporter())) {
                processUserTypeList.add(ProcessUserType.REPORTER.getValue());
            }
            List<ProcessTaskStepUserVo> processTaskStepUserList = processTaskMapper.getProcessTaskStepUserList(new ProcessTaskStepUserVo(processTaskId, null, userUuid));
            for (ProcessTaskStepUserVo processTaskStepUserVo : processTaskStepUserList) {
                if (processTaskStepUserVo.getUserType().equals(ProcessUserType.MAJOR.getValue())) {
                    processUserTypeList.add(ProcessUserType.MAJOR.getValue());
                } else {
                    processUserTypeList.add(ProcessUserType.MINOR.getValue());
                }
            }
            if (processUserTypeList.contains(ProcessUserType.MAJOR.getValue())) {
                processUserTypeList.add(ProcessUserType.WORKER.getValue());
            } else {
                if (processTaskMapper.checkIsWorker(processTaskVo.getId(), null, ProcessUserType.MAJOR.getValue(), authenticationInfoVo) > 0) {
                    processUserTypeList.add(ProcessUserType.WORKER.getValue());
                }
            }
        } else {
            processUserTypeList.add(ProcessUserType.OWNER.getValue());
            processUserTypeList.add(ProcessUserType.REPORTER.getValue());
            processUserTypeList.add(ProcessUserType.MAJOR.getValue());
        }
        return processUserTypeList;
    }

    @Override
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId, Long processTaskStepId, Long nextStepId)
            throws Exception {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
        if (processTaskVo == null) {
            throw new ProcessTaskNotFoundException(processTaskId);
        }
//        if (processTaskVo.getIsShow() != 1 && !AuthActionChecker.check(PROCESSTASK_MODIFY.class.getSimpleName())) {
//            throw new PermissionDeniedException(PROCESSTASK_MODIFY.class);
//        }
        if (processTaskStepId != null) {
            ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
            if (processTaskStepVo == null) {
                throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
            }
            IProcessStepInternalHandler processStepUtilHandler =
                    ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
            if (processStepUtilHandler == null) {
                throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
            }
            if (!processTaskId.equals(processTaskStepVo.getProcessTaskId())) {
                throw new ProcessTaskNotFoundSpecifiedStepException(processTaskVo.getTitle(), processTaskStepVo.getName());
            }
            processTaskVo.setCurrentProcessTaskStep(processTaskStepVo);
        }
        if (nextStepId != null) {
            ProcessTaskStepVo nextProcessTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(nextStepId);
            if (nextProcessTaskStepVo == null) {
                throw new ProcessTaskStepNotFoundException(nextStepId.toString());
            }
            IProcessStepInternalHandler processStepUtilHandler =
                    ProcessStepInternalHandlerFactory.getHandler(nextProcessTaskStepVo.getHandler());
            if (processStepUtilHandler == null) {
                throw new ProcessStepUtilHandlerNotFoundException(nextProcessTaskStepVo.getHandler());
            }
            if (!processTaskId.equals(nextProcessTaskStepVo.getProcessTaskId())) {
                throw new ProcessTaskNotFoundSpecifiedStepException(processTaskVo.getTitle(), nextProcessTaskStepVo.getName());
            }
        }
        return processTaskVo;
    }

    @Override
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId, Long processTaskStepId) throws Exception {
        return checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId, null);
    }

    @Override
    public ProcessTaskVo checkProcessTaskParamsIsLegal(Long processTaskId) throws Exception {
        return checkProcessTaskParamsIsLegal(processTaskId, null, null);
    }

    @Override
    public List<ProcessTaskStepReplyVo> getProcessTaskStepReplyListByProcessTaskStepId(Long processTaskStepId,
                                                                                       List<String> typeList) {
        List<ProcessTaskStepReplyVo> processTaskStepReplyList = new ArrayList<>();
        List<ProcessUserType> processUserTypeList = new ArrayList<>();
        processUserTypeList.add(ProcessUserType.OWNER);
        processUserTypeList.add(ProcessUserType.REPORTER);
        processUserTypeList.add(ProcessUserType.MAJOR);
        processUserTypeList.add(ProcessUserType.MINOR);
        processUserTypeList.add(ProcessUserType.FOCUS_USER);
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
        Map<ProcessUserType, List<String>> processUserTypeListMap = getProcessTaskStepProcessUserTypeData(processTaskStepVo, processUserTypeList);
        List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(processTaskStepId);
        for (ProcessTaskStepContentVo processTaskStepContentVo : processTaskStepContentList) {
            if (typeList.contains(processTaskStepContentVo.getType())) {
                ProcessTaskStepReplyVo processTaskStepReplyVo = new ProcessTaskStepReplyVo(processTaskStepContentVo);
                parseProcessTaskStepReply(processTaskStepReplyVo);
                if (Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStepStatus.RUNNING.getValue())
                        && Objects.equals(UserContext.get().getUserUuid(), processTaskStepReplyVo.getFcu())) {
                    processTaskStepReplyVo.setIsEditable(1);
                    processTaskStepReplyVo.setIsDeletable(1);
                } else {
                    processTaskStepReplyVo.setIsEditable(0);
                    processTaskStepReplyVo.setIsDeletable(0);
                }
                List<ProcessUserType> operatorProcessUserTypeList = new ArrayList<>();
                for (Map.Entry<ProcessUserType, List<String>> entry : processUserTypeListMap.entrySet()) {
                    List<String> uuidList = entry.getValue();
                    if (CollectionUtils.isEmpty(uuidList)) {
                        continue;
                    }
                    for (String uuid : uuidList) {
                        if (uuid.contains(processTaskStepReplyVo.getLcu())) {
                            operatorProcessUserTypeList.add(entry.getKey());
                            break;
                        }
                    }
                }
                List<String> operatorProcessUserTypeTextList = new ArrayList<>(operatorProcessUserTypeList.size());
                for (ProcessUserType processUserType : operatorProcessUserTypeList) {
                    operatorProcessUserTypeTextList.add(processUserType.getText());
                }
                processTaskStepReplyVo.setOperatorRole(String.join("、", operatorProcessUserTypeTextList));
                processTaskStepReplyList.add(processTaskStepReplyVo);
            }
        }
        return processTaskStepReplyList;
    }

    @Override
    public Map<Long, List<AssignableWorkerStepVo>> getAssignableWorkerStepMap(ProcessTaskStepVo currentProcessTaskStepVo) {
        Map<Long, List<AssignableWorkerStepVo>> assignableWorkerStepMap = new HashMap<>();
        ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
        processTaskStepWorkerPolicyVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
        List<ProcessTaskStepWorkerPolicyVo> processTaskStepWorkerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicy(processTaskStepWorkerPolicyVo);
        if (CollectionUtils.isEmpty(processTaskStepWorkerPolicyList)) {
            return assignableWorkerStepMap;
        }
        int isOnlyOnceExecute = 0;
        IWorkerPolicyHandler workerPolicyHandler = WorkerPolicyHandlerFactory.getHandler(WorkerPolicy.PRESTEPASSIGN.getValue());
        if (workerPolicyHandler == null) {
            isOnlyOnceExecute = workerPolicyHandler.isOnlyOnceExecute();
        }
        for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : processTaskStepWorkerPolicyList) {
            if (!WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
                continue;
            }
            JSONObject configObj = workerPolicyVo.getConfigObj();
            if (MapUtils.isEmpty(configObj)) {
                continue;
            }
            JSONArray processStepList = configObj.getJSONArray("processStepList");
            if (CollectionUtils.isNotEmpty(processStepList)) {
                for (int i = 0; i < processStepList.size(); i++) {
                    JSONObject processStepObj = processStepList.getJSONObject(i);
                    String processStepUuid = processStepObj.getString("uuid");
                    if (!Objects.equals(currentProcessTaskStepVo.getProcessStepUuid(), processStepUuid)) {
                        continue;
                    }
                    List<ProcessTaskStepUserVo> majorList = processTaskMapper.getProcessTaskStepUserByStepId(workerPolicyVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
                    if (CollectionUtils.isNotEmpty(majorList) && isOnlyOnceExecute == 1) {
                        break;
                    }
                    List<String> nextStepUuidList = new ArrayList<>();
                    JSONArray nextStepUuidArray = processStepObj.getJSONArray("condition");
                    if (CollectionUtils.isNotEmpty(nextStepUuidArray)) {
                        nextStepUuidList = nextStepUuidArray.toJavaList(String.class);
                    }
                    List<Long> nextStepIdList = processStepHandlerUtil.getNextStepIdList(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), nextStepUuidList);
                    if (CollectionUtils.isEmpty(nextStepIdList)) {
                        break;
                    }
                    ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(workerPolicyVo.getProcessTaskStepId());
                    AssignableWorkerStepVo assignableWorkerStepVo = new AssignableWorkerStepVo();
                    assignableWorkerStepVo.setId(processTaskStepVo.getId());
                    assignableWorkerStepVo.setProcessStepUuid(processTaskStepVo.getProcessStepUuid());
                    assignableWorkerStepVo.setName(processTaskStepVo.getName());
                    assignableWorkerStepVo.setIsRequired(configObj.getInteger("isRequired"));
                    assignableWorkerStepVo.setGroupList(configObj.getJSONArray("groupList"));
                    assignableWorkerStepVo.setRangeList(configObj.getJSONArray("rangeList"));
                    for (Long nextStepId : nextStepIdList) {
                        assignableWorkerStepMap.computeIfAbsent(nextStepId, key -> new ArrayList<>()).add(assignableWorkerStepVo);
                    }
                }
            } else {
                JSONArray processStepUuidList = configObj.getJSONArray("processStepUuidList");
                if (CollectionUtils.isEmpty(processStepUuidList)) {
                    continue;
                }
                for (String stepUuid : processStepUuidList.toJavaList(String.class)) {
                    if (!currentProcessTaskStepVo.getProcessStepUuid().equals(stepUuid)) {
                        continue;
                    }
                    List<ProcessTaskStepUserVo> majorList = processTaskMapper.getProcessTaskStepUserByStepId(workerPolicyVo.getProcessTaskStepId(), ProcessUserType.MAJOR.getValue());
                    if (CollectionUtils.isNotEmpty(majorList) && isOnlyOnceExecute == 1) {
                        break;
                    }
                    List<Long> nextStepIdList = processStepHandlerUtil.getNextStepIdList(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), workerPolicyVo.getProcessTaskStepId());
                    if (CollectionUtils.isEmpty(nextStepIdList)) {
                        break;
                    }
                    ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(workerPolicyVo.getProcessTaskStepId());
                    AssignableWorkerStepVo assignableWorkerStepVo = new AssignableWorkerStepVo();
                    assignableWorkerStepVo.setId(processTaskStepVo.getId());
                    assignableWorkerStepVo.setProcessStepUuid(processTaskStepVo.getProcessStepUuid());
                    assignableWorkerStepVo.setName(processTaskStepVo.getName());
                    assignableWorkerStepVo.setIsRequired(configObj.getInteger("isRequired"));
                    assignableWorkerStepVo.setGroupList(configObj.getJSONArray("groupList"));
                    assignableWorkerStepVo.setRangeList(configObj.getJSONArray("rangeList"));
                    for (Long nextStepId : nextStepIdList) {
                        assignableWorkerStepMap.computeIfAbsent(nextStepId, key -> new ArrayList<>()).add(assignableWorkerStepVo);
                    }
                }
            }
        }
        return assignableWorkerStepMap;
    }

    @Override
    public List<ProcessTaskSlaTimeVo> getSlaTimeListByProcessTaskStepId(Long processTaskStepId) {
        List<Long> slaIdList = processTaskSlaMapper.getSlaIdListByProcessTaskStepId(processTaskStepId);
        if (CollectionUtils.isNotEmpty(slaIdList)) {
            return getSlaTimeListBySlaIdList(slaIdList);
        }
        return new ArrayList<>();
    }

    @Override
    public List<ProcessTaskSlaTimeVo> getSlaTimeListBySlaIdList(List<Long> slaIdList) {
        List<ProcessTaskSlaTimeVo> processTaskSlaTimeList = processTaskSlaMapper.getProcessTaskSlaTimeListBySlaIdList(slaIdList);
        if (CollectionUtils.isEmpty(processTaskSlaTimeList)) {
            return processTaskSlaTimeList;
        }
        List<ProcessTaskStepSlaDelayVo> processTaskStepSlaDelayList = processTaskSlaMapper.getProcessTaskStepSlaDelayListBySlaIdList(slaIdList);
        Set<Long> processTaskIdSet = processTaskSlaTimeList.stream().map(ProcessTaskSlaTimeVo::getProcessTaskId).collect(Collectors.toSet());
        List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskListByIdList(new ArrayList<>(processTaskIdSet));
        Map<Long, String> worktimeUuidMap = processTaskList.stream().collect(Collectors.toMap(ProcessTaskVo::getId, ProcessTaskVo::getWorktimeUuid));
        long currentTimeMillis = System.currentTimeMillis();
//        String displayModeAfterTimeout = ConfigManager.getConfig(ItsmTenantConfig.DISPLAY_MODE_AFTER_TIMEOUT);
        String slaTimeDisplayMode = ConfigManager.getConfig(ItsmTenantConfig.SLA_TIME_DISPLAY_MODE);
        for (ProcessTaskSlaTimeVo processTaskSlaTimeVo : processTaskSlaTimeList) {
//            processTaskSlaTimeVo.setDisplayModeAfterTimeout(displayModeAfterTimeout);
            processTaskSlaTimeVo.setSlaTimeDisplayMode(slaTimeDisplayMode);
            if (!Objects.equals(SlaStatus.DOING.name().toLowerCase(), processTaskSlaTimeVo.getStatus())) {
                continue;
            }
            Long calculationTimeLong = processTaskSlaTimeVo.getCalculationTimeLong();
            if (calculationTimeLong == null) {
                continue;
            }
            long realCostTime = currentTimeMillis - calculationTimeLong;
            processTaskSlaTimeVo.setRealTimeLeft(processTaskSlaTimeVo.getRealTimeLeft() - realCostTime);
            String worktimeUuid = worktimeUuidMap.get(processTaskSlaTimeVo.getProcessTaskId());
            if (StringUtils.isBlank(worktimeUuid)) {
                processTaskSlaTimeVo.setTimeLeft(processTaskSlaTimeVo.getTimeLeft() - realCostTime);
            } else {
                long costTime = worktimeMapper.calculateCostTime(worktimeUuid, calculationTimeLong, currentTimeMillis);
                processTaskSlaTimeVo.setTimeLeft(processTaskSlaTimeVo.getTimeLeft() - costTime);
            }
            if (CollectionUtils.isNotEmpty(processTaskStepSlaDelayList)) {
                List<ProcessTaskStepSlaDelayVo> delayList = new ArrayList<>();
                for (ProcessTaskStepSlaDelayVo processTaskStepSlaDelayVo : processTaskStepSlaDelayList) {
                    if (Objects.equals(processTaskSlaTimeVo.getSlaId(), processTaskStepSlaDelayVo.getSlaId())) {
                        delayList.add(processTaskStepSlaDelayVo);
                    }
                }
                processTaskSlaTimeVo.setDelayList(delayList);
            }
        }
        return processTaskSlaTimeList;
    }

    @Override
    public void setNextStepList(ProcessTaskStepVo processTaskStepVo) {
        Map<Long, List<AssignableWorkerStepVo>> assignableWorkerStepMap = getAssignableWorkerStepMap(processTaskStepVo);
        List<ProcessTaskStepVo> forwardNextStepList = new ArrayList<>();
        List<ProcessTaskStepVo> backwardNextStepList = new ArrayList<>();
        IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        List<ProcessTaskStepVo> nextStepList = handler.getNextStepList(processTaskStepVo, null);
        for (ProcessTaskStepVo processTaskStep : nextStepList) {
            List<AssignableWorkerStepVo> assignableWorkerStepList = assignableWorkerStepMap.get(processTaskStep.getId());
            if (CollectionUtils.isNotEmpty(assignableWorkerStepList)) {
                processTaskStep.setAssignableWorkerStepList(assignableWorkerStepList);
            }
            if (ProcessFlowDirection.FORWARD.getValue().equals(processTaskStep.getFlowDirection())) {
                processTaskStep.setFlowDirection(ProcessFlowDirection.FORWARD.getText());
                forwardNextStepList.add(processTaskStep);
            } else if (ProcessFlowDirection.BACKWARD.getValue().equals(processTaskStep.getFlowDirection())) {
                if (!Objects.equals(processTaskStep.getIsActive(), 0)) {
                    processTaskStep.setFlowDirection(ProcessFlowDirection.BACKWARD.getText());
                    backwardNextStepList.add(processTaskStep);
                }
            }
        }
        // 回退步骤有多个时，如果回退步骤列表中包含“上一步骤”时，将“上一步骤”排在第一位，点击回退按钮时默认选择“上一步骤”
        if (backwardNextStepList.size() > 1) {
            // 获取“上一步骤”列表
            List<Long> fromStepIdList = new ArrayList<>();
            List<ProcessTaskStepRelVo> fromProcessTaskStepRelList = processTaskMapper.getProcessTaskStepRelByToId(processTaskStepVo.getId());
            for (ProcessTaskStepRelVo relVo : fromProcessTaskStepRelList) {
                if (Objects.equals(relVo.getIsHit(), 1) && Objects.equals(relVo.getType(), ProcessFlowDirection.FORWARD.getValue())) {
                    fromStepIdList.add(relVo.getFromProcessTaskStepId());
                }
            }
            // 遍历回退步骤列表，区分“上一步骤”和非“上一步骤”
            List<ProcessTaskStepVo> previousStepList = new ArrayList<>();
            List<ProcessTaskStepVo> nonPreviousStepList = new ArrayList<>();
            for (ProcessTaskStepVo processTaskStep : backwardNextStepList) {
                if (fromStepIdList.contains(processTaskStep.getId())) {
                    if (StringUtils.isNotBlank(processTaskStep.getAliasName())) {
                        processTaskStep.setAliasName(processTaskStep.getAliasName() + "(" + $.t("term.itsm.previousstep") + ")");
                    } else {
                        processTaskStep.setName(processTaskStep.getName() + "(" + $.t("term.itsm.previousstep") + ")");
                    }
                    previousStepList.add(processTaskStep);
                } else {
                    nonPreviousStepList.add(processTaskStep);
                }
            }
            backwardNextStepList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(previousStepList)) {
                // 存在多个时，按步骤结束时间排序，结束时间大的排在前面
                if (previousStepList.size() > 1) {
                    previousStepList.sort((o1, o2) -> {
                        if (o1.getEndTime() == null && o2.getEndTime() == null) {
                            return 0;
                        } else if (o2.getEndTime() == null) {
                            return -1;
                        } else if (o1.getEndTime() == null) {
                            return 1;
                        } else {
                            return o2.getEndTime().compareTo(o1.getEndTime());
                        }
                    });
                }
                backwardNextStepList.addAll(previousStepList);
            }
            if (CollectionUtils.isNotEmpty(nonPreviousStepList)) {
                // 存在多个时，按步骤结束时间排序，结束时间大的排在前面
                if (nonPreviousStepList.size() > 1) {
                    nonPreviousStepList.sort((o1, o2) -> {
                        if (o1.getEndTime() == null && o2.getEndTime() == null) {
                            return 0;
                        } else if (o2.getEndTime() == null) {
                            return -1;
                        } else if (o1.getEndTime() == null) {
                            return 1;
                        } else {
                            return o2.getEndTime().compareTo(o1.getEndTime());
                        }
                    });
                }
                backwardNextStepList.addAll(nonPreviousStepList);
            }
        }
        processTaskStepVo.setForwardNextStepList(forwardNextStepList);
        processTaskStepVo.setBackwardNextStepList(backwardNextStepList);
    }

    @Override
    public List<ProcessTaskStepVo> getForwardNextStepListByProcessTaskStepId(ProcessTaskStepVo processTaskStepVo) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        List<ProcessTaskStepVo> nextStepList = handler.getNextStepList(processTaskStepVo, ProcessFlowDirection.FORWARD);
        for (ProcessTaskStepVo processTaskStep : nextStepList) {
            if (StringUtils.isNotBlank(processTaskStep.getAliasName())) {
                processTaskStep.setName(processTaskStep.getAliasName());
                processTaskStep.setFlowDirection("");
            } else {
                processTaskStep.setFlowDirection(ProcessFlowDirection.FORWARD.getText());
            }
            resultList.add(processTaskStep);
        }
        return resultList;
    }

    @Override
    public List<ProcessTaskStepVo> getBackwardNextStepListByProcessTaskStepId(ProcessTaskStepVo processTaskStepVo) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepUtilHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        List<ProcessTaskStepVo> nextStepList = handler.getNextStepList(processTaskStepVo, ProcessFlowDirection.BACKWARD);
        for (ProcessTaskStepVo processTaskStep : nextStepList) {
            if (!Objects.equals(processTaskStep.getIsActive(), 0)) {
                if (StringUtils.isNotBlank(processTaskStep.getAliasName())) {
                    processTaskStep.setName(processTaskStep.getAliasName());
                    processTaskStep.setFlowDirection("");
                } else {
                    processTaskStep.setFlowDirection(ProcessFlowDirection.BACKWARD.getText());
                }
                resultList.add(processTaskStep);
            }
        }
        return resultList;
    }

    @Override
    public void setProcessTaskStepUser(ProcessTaskStepVo processTaskStepVo) {
        List<ProcessTaskStepUserVo> majorUserList = new ArrayList<>();
        List<ProcessTaskStepUserVo> minorUserList = new ArrayList<>();
        List<ProcessTaskStepUserVo> stepUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), null);
        for (ProcessTaskStepUserVo stepUserVo : stepUserList) {
            UserVo userVo = userMapper.getUserBaseInfoByUuid(stepUserVo.getUserUuid());
            if (userVo != null) {
                stepUserVo.setUserName(userVo.getUserName());
            }
            if (stepUserVo.getUserType().equals(ProcessUserType.MAJOR.getValue())) {
                majorUserList.add(stepUserVo);
            } else if (stepUserVo.getUserType().equals(ProcessUserType.MINOR.getValue())) {
                minorUserList.add(stepUserVo);
            }
        }
//        List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
        if (CollectionUtils.isNotEmpty(majorUserList)) {
            processTaskStepVo.setMajorUser(majorUserList.get(0));
        } else {
            List<ProcessTaskStepWorkerVo> workerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskIdAndProcessTaskStepId(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId());
            for (ProcessTaskStepWorkerVo workerVo : workerList) {
                if (workerVo.getType().equals(GroupSearch.USER.getValue())) {
                    UserVo userVo = userMapper.getUserBaseInfoByUuid(workerVo.getUuid());
                    if (userVo != null) {
                        workerVo.setWorker(new WorkAssignmentUnitVo(userVo));
                        workerVo.setName(userVo.getUserName());
                    }
                } else if (workerVo.getType().equals(GroupSearch.TEAM.getValue())) {
                    TeamVo search = new TeamVo();
                    search.setUuid(workerVo.getUuid());
                    TeamVo teamVo = teamMapper.getTeamSimpleInfoByUuid(search);
                    if (teamVo != null) {
                        workerVo.setWorker(new WorkAssignmentUnitVo(teamVo));
                        workerVo.setName(teamVo.getName());
                    }
                } else if (workerVo.getType().equals(GroupSearch.ROLE.getValue())) {
                    RoleVo roleVo = roleMapper.getRoleSimpleInfoByUuid(workerVo.getUuid());
                    if (roleVo != null) {
                        workerVo.setWorker(new WorkAssignmentUnitVo(roleVo));
                        workerVo.setName(roleVo.getName());
                    }
                }
            }
            processTaskStepVo.setWorkerList(workerList);
        }
//        processTaskStepVo.setMinorUserList(processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(),
//                ProcessUserType.MINOR.getValue()));
        processTaskStepVo.setMinorUserList(minorUserList);
    }

    @Override
    public boolean saveProcessTaskStepReply(JSONObject jsonObj, ProcessTaskStepReplyVo oldReplyVo) {
        if (oldReplyVo == null) {
            return false;
        }
        String content = jsonObj.getString("content");
        List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("fileIdList")), Long.class);
        if (content == null && fileIdList == null) {
            return false;
        }
        Long processTaskId = oldReplyVo.getProcessTaskId();
        Long processTaskStepId = oldReplyVo.getProcessTaskStepId();
        boolean isUpdate = false;
        // 获取上传附件id列表
        List<Long> oldFileIdList = new ArrayList<>();
        // 获取上报描述内容
        String oldContent = null;
        Long oldContentId = null;
        if (oldReplyVo.getId() != null) {
            parseProcessTaskStepReply(oldReplyVo);
            oldContentId = oldReplyVo.getId();
            oldContent = oldReplyVo.getContent();
            oldFileIdList = oldReplyVo.getFileIdList();
        }
        String source = jsonObj.getString("source");
        if (StringUtils.isNotBlank(content) && StringUtils.isNotBlank(oldContent)) {
            if (content.equals(oldContent)) {
                jsonObj.remove("content");
            } else {
                jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldContent);
                isUpdate = true;
                ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
                processTaskMapper.insertIgnoreProcessTaskContent(contentVo);
                if (oldContentId == null) {
                    ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(processTaskId, processTaskStepId, contentVo.getHash(), ProcessTaskOperationType.PROCESSTASK_START.getValue());
                    if (StringUtils.isNotBlank(source)) {
                        processTaskStepContentVo.setSource(source);
                    }
                    processTaskMapper.insertProcessTaskStepContent(processTaskStepContentVo);
                    oldContentId = processTaskStepContentVo.getId();
                } else {
                    ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(oldContentId, contentVo.getHash());
                    if (StringUtils.isNotBlank(source)) {
                        processTaskStepContentVo.setSource(source);
                    }
                    processTaskMapper.updateProcessTaskStepContentById(processTaskStepContentVo);
                }
            }
        } else if (StringUtils.isNotBlank(content)) {
            isUpdate = true;
            ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
            processTaskMapper.insertIgnoreProcessTaskContent(contentVo);
            if (oldContentId == null) {
                ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(processTaskId, processTaskStepId, contentVo.getHash(), ProcessTaskOperationType.PROCESSTASK_START.getValue());
                if (StringUtils.isNotBlank(source)) {
                    processTaskStepContentVo.setSource(source);
                }
                processTaskMapper.insertProcessTaskStepContent(processTaskStepContentVo);
                oldContentId = processTaskStepContentVo.getId();
            } else {
                ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(oldContentId, contentVo.getHash());
                if (StringUtils.isNotBlank(source)) {
                    processTaskStepContentVo.setSource(source);
                }
                processTaskMapper.updateProcessTaskStepContentById(processTaskStepContentVo);
            }
        } else if (StringUtils.isNotBlank(oldContent)) {
            isUpdate = true;
            jsonObj.remove("content");
            jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldContent);
            ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(oldContentId, null);
            if (StringUtils.isNotBlank(source)) {
                processTaskStepContentVo.setSource(source);
            }
            processTaskMapper.updateProcessTaskStepContentById(processTaskStepContentVo);
        } else {
            jsonObj.remove("content");
        }

        /* 保存新附件uuid **/
        if (CollectionUtils.isNotEmpty(fileIdList) && CollectionUtils.isNotEmpty(oldFileIdList)) {
            if (Objects.equals(oldFileIdList, fileIdList)) {
                jsonObj.remove("fileIdList");
            } else {
                processTaskMapper.deleteProcessTaskStepFileByContentId(oldContentId);
                jsonObj.put(ProcessTaskAuditDetailType.FILE.getOldDataParamName(), JSON.toJSONString(oldFileIdList));
                isUpdate = true;
                if (oldContentId == null) {
                    ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(processTaskId, processTaskStepId, null, ProcessTaskOperationType.PROCESSTASK_START.getValue());
                    if (StringUtils.isNotBlank(source)) {
                        processTaskStepContentVo.setSource(source);
                    }
                    processTaskMapper.insertProcessTaskStepContent(processTaskStepContentVo);
                    oldContentId = processTaskStepContentVo.getId();
                }
                ProcessTaskStepFileVo processTaskStepFileVo = new ProcessTaskStepFileVo();
                processTaskStepFileVo.setProcessTaskId(processTaskId);
                processTaskStepFileVo.setProcessTaskStepId(processTaskStepId);
                processTaskStepFileVo.setContentId(oldContentId);
                for (Long fileId : fileIdList) {
                    if (fileMapper.getFileById(fileId) == null) {
                        throw new FileNotFoundException(fileId);
                    }
                    processTaskStepFileVo.setFileId(fileId);
                    processTaskMapper.insertProcessTaskStepFile(processTaskStepFileVo);
                }
            }
        } else if (CollectionUtils.isNotEmpty(fileIdList)) {
            isUpdate = true;
            if (oldContentId == null) {
                ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(processTaskId, processTaskStepId, null, ProcessTaskOperationType.PROCESSTASK_START.getValue());
                if (StringUtils.isNotBlank(source)) {
                    processTaskStepContentVo.setSource(source);
                }
                processTaskMapper.insertProcessTaskStepContent(processTaskStepContentVo);
                oldContentId = processTaskStepContentVo.getId();
            }
            ProcessTaskStepFileVo processTaskStepFileVo = new ProcessTaskStepFileVo();
            processTaskStepFileVo.setProcessTaskId(processTaskId);
            processTaskStepFileVo.setProcessTaskStepId(processTaskStepId);
            processTaskStepFileVo.setContentId(oldContentId);
            for (Long fileId : fileIdList) {
                if (fileMapper.getFileById(fileId) == null) {
                    throw new FileNotFoundException(fileId);
                }
                processTaskStepFileVo.setFileId(fileId);
                processTaskMapper.insertProcessTaskStepFile(processTaskStepFileVo);
            }
        } else if (CollectionUtils.isNotEmpty(oldFileIdList)) {
            processTaskMapper.deleteProcessTaskStepFileByContentId(oldContentId);
            jsonObj.put(ProcessTaskAuditDetailType.FILE.getOldDataParamName(), JSON.toJSONString(oldFileIdList));
            isUpdate = true;
            jsonObj.remove("fileIdList");
        } else {
            jsonObj.remove("fileIdList");
        }

        if (oldContentId != null && StringUtils.isBlank(content) && CollectionUtils.isEmpty(fileIdList)) {
            processTaskMapper.deleteProcessTaskStepContentById(oldContentId);
        }
        return isUpdate;
    }

    /**
     * 检查当前用户是否配置该权限
     *
     * @param processTaskStepVo 步骤信息
     * @param owner             所有人
     * @param reporter          上报人
     * @param operationType     操作类型
     * @param userUuid          用户uuid
     * @return 是否拥有权限
     */
    @Override
    public boolean checkOperationAuthIsConfigured(ProcessTaskStepVo processTaskStepVo, String owner, String reporter,
                                                  ProcessTaskOperationType operationType, String userUuid) {
        JSONArray authorityList = null;
        String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
        Integer enableAuthority = (Integer) JSONPath.read(stepConfig, "enableAuthority");
        if (Objects.equals(enableAuthority, 1)) {
            authorityList = (JSONArray) JSONPath.read(stepConfig, "authorityList");
        } else {
            String handler = processTaskStepVo.getHandler();
            IProcessStepInternalHandler processStepUtilHandler = ProcessStepInternalHandlerFactory.getHandler(handler);
            if (processStepUtilHandler == null) {
                throw new ProcessStepUtilHandlerNotFoundException(handler);
            }
            String processStepHandlerConfig = processStepHandlerMapper.getProcessStepHandlerConfigByHandler(handler);
            JSONObject globalConfig = null;
            if (StringUtils.isNotBlank(processStepHandlerConfig)) {
                globalConfig = JSON.parseObject(processStepHandlerConfig);
            }
            globalConfig = processStepUtilHandler.makeupConfig(globalConfig);
            authorityList = globalConfig.getJSONArray("authorityList");
        }

        if (CollectionUtils.isNotEmpty(authorityList)) {
            return checkOperationAuthIsConfigured(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(),
                    owner, reporter, operationType, authorityList, userUuid);
        }
        return false;
    }

    /**
     * 检查当前用户是否配置该权限
     *
     * @param processTaskVo 作业信息
     * @param operationType 操作类型
     * @param userUuid      用户uuid
     * @return 是否拥有权限
     */
    @Override
    public boolean checkOperationAuthIsConfigured(ProcessTaskVo processTaskVo, ProcessTaskOperationType operationType,
                                                  String userUuid) {
        String config = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
        JSONArray authorityList = (JSONArray) JSONPath.read(config, "process.processConfig.authorityList");
        // 如果步骤自定义权限设置为空，则用组件的全局权限设置
        if (CollectionUtils.isNotEmpty(authorityList)) {
            return checkOperationAuthIsConfigured(processTaskVo.getId(), null, processTaskVo.getOwner(),
                    processTaskVo.getReporter(), operationType, authorityList, userUuid);
        }
        return false;
    }

    private boolean checkOperationAuthIsConfigured(Long processTaskId, Long processTaskStepId, String owner,
                                                   String reporter, ProcessTaskOperationType operationType, JSONArray authorityList, String userUuid) {
        for (int i = 0; i < authorityList.size(); i++) {
            JSONObject authorityObj = authorityList.getJSONObject(i);
            String action = authorityObj.getString("action");
            if (operationType.getValue().equals(action)) {
                JSONArray acceptList = authorityObj.getJSONArray("acceptList");
                if (acceptList == null) {
                    acceptList = authorityObj.getJSONArray("defaultValue");
                }
                if (CollectionUtils.isNotEmpty(acceptList)) {
                    AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userUuid);
                    List<String> teamUuidList = authenticationInfoVo.getTeamUuidList();
                    List<String> roleUuidList = authenticationInfoVo.getRoleUuidList();
                    ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
                    processTaskStepUserVo.setProcessTaskId(processTaskId);
                    processTaskStepUserVo.setProcessTaskStepId(processTaskStepId);
//                    processTaskStepUserVo.setUserVo(new UserVo(userUuid));
                    processTaskStepUserVo.setUserUuid(userUuid);
                    for (int j = 0; j < acceptList.size(); j++) {
                        String accept = acceptList.getString(j);
                        String[] split = accept.split("#");
                        if (GroupSearch.COMMON.getValue().equals(split[0])) {
                            if (UserType.ALL.getValue().equals(split[1])) {
                                return true;
                            }
                        } else if (ProcessTaskGroupSearch.PROCESSUSERTYPE.getValue().equals(split[0])) {
                            if (ProcessUserType.OWNER.getValue().equals(split[1])) {
                                if (userUuid.equals(owner)) {
                                    return true;
                                }
                            } else if (ProcessUserType.REPORTER.getValue().equals(split[1])) {
                                if (userUuid.equals(reporter)) {
                                    return true;
                                }
                            } else if (ProcessUserType.MAJOR.getValue().equals(split[1])) {
                                processTaskStepUserVo.setUserType(ProcessUserType.MAJOR.getValue());
                                if (processTaskMapper.checkIsProcessTaskStepUser(processTaskStepUserVo) > 0) {
                                    return true;
                                }
                            } else if (ProcessUserType.MINOR.getValue().equals(split[1])) {
                                processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
                                if (processTaskMapper.checkIsProcessTaskStepUser(processTaskStepUserVo) > 0) {
                                    return true;
                                }
                            }
                        } else if (GroupSearch.USER.getValue().equals(split[0])) {
                            if (userUuid.equals(split[1])) {
                                return true;
                            }
                        } else if (GroupSearch.TEAM.getValue().equals(split[0])) {
                            if (teamUuidList.contains(split[1])) {
                                return true;
                            }
                        } else if (GroupSearch.ROLE.getValue().equals(split[0])) {
                            if (roleUuidList.contains(split[1])) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取工单中当前用户能撤回的步骤列表
     *
     * @param processTaskVo 作业信息
     * @param userUuid      用户uuid
     * @return 步骤信息
     */
    @Override
    public Set<ProcessTaskStepVo> getRetractableStepListByProcessTask(ProcessTaskVo processTaskVo, String userUuid) {
        Set<ProcessTaskStepVo> resultSet = new HashSet<>();
        List<ProcessTaskStepVo> stepVoList =
                processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskVo.getId());
        for (ProcessTaskStepVo stepVo : stepVoList) {
            /* 找到所有已激活步骤 **/
            if (stepVo.getIsActive().equals(1)) {
                resultSet.addAll(getRetractableStepListByProcessTaskStepId(processTaskVo, stepVo.getId(), userUuid));
            }
        }
        return resultSet;
    }

    /**
     * 获取当前步骤的前置步骤列表中处理人是当前用户的步骤列表
     *
     * @param processTaskVo     作业信息
     * @param processTaskStepId 步骤id
     * @param userUuid          用户uuid
     * @return 步骤列表
     */
    @Override
    public List<ProcessTaskStepVo> getRetractableStepListByProcessTaskStepId(ProcessTaskVo processTaskVo,
                                                                             Long processTaskStepId, String userUuid) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        /* 所有前置步骤 **/
//        List<Long> fromStepIdList = processTaskMapper.getFromProcessTaskStepIdListByToId(processTaskStepId);
        List<Long> fromStepIdList = new ArrayList<>();
        List<ProcessTaskStepRelVo> fromProcessTaskStepRelList = processTaskMapper.getProcessTaskStepRelByToId(processTaskStepId);
        for (ProcessTaskStepRelVo relVo : fromProcessTaskStepRelList) {
            if (Objects.equals(relVo.getIsHit(), 1) && Objects.equals(relVo.getType(), ProcessFlowDirection.FORWARD.getValue())) {
                fromStepIdList.add(relVo.getFromProcessTaskStepId());
            }
        }
        if (CollectionUtils.isNotEmpty(fromStepIdList)) {
            List<ProcessTaskStepVo> fromStepList = processTaskMapper.getProcessTaskStepListByIdList(fromStepIdList);
            /* 找到所有已完成步骤 **/
            for (ProcessTaskStepVo fromStep : fromStepList) {
                IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(fromStep.getHandler());
                if (handler != null) {
                    if (ProcessStepMode.MT == handler.getMode()) {// 手动处理节点
                        if (checkOperationAuthIsConfigured(fromStep, processTaskVo.getOwner(), processTaskVo.getReporter(),
                                ProcessTaskOperationType.STEP_RETREAT, userUuid)) {
                            resultList.add(fromStep);
                        }
                    } else {// 自动处理节点，继续找前置节点
                        resultList.addAll(getRetractableStepListByProcessTaskStepId(processTaskVo, fromStep.getId(), userUuid));
                    }
                } else {
                    throw new ProcessStepHandlerNotFoundException(fromStep.getHandler());
                }
            }
        }
        return resultList;
    }

    /**
     * 获取工单中当前用户能催办的步骤列表
     *
     * @param processTaskVo 作业信息
     * @param userUuid      用户uuid
     * @return 步骤列表
     */
    @Override
    public List<ProcessTaskStepVo> getUrgeableStepList(ProcessTaskVo processTaskVo, String userUuid) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        if (checkOperationAuthIsConfigured(processTaskVo, ProcessTaskOperationType.PROCESSTASK_URGE, userUuid)) {
            List<ProcessTaskStepVo> processTaskStepList =
                    processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskVo.getId());
            for (ProcessTaskStepVo processTaskStep : processTaskStepList) {
                if (processTaskStep.getIsActive() == 1) {
                    resultList.add(processTaskStep);
                }
            }
        }
        return resultList;
    }

    @Override
    public List<ProcessTaskStepRemindVo> getProcessTaskStepRemindListByProcessTaskStepId(Long processTaskStepId) {
        List<ProcessTaskStepRemindVo> processTaskStepRemindList =
                processTaskMapper.getProcessTaskStepRemindListByProcessTaskStepId(processTaskStepId);
        for (ProcessTaskStepRemindVo processTaskStepRemindVo : processTaskStepRemindList) {
            processTaskStepRemindVo
                    .setActionName(ProcessTaskStepRemindTypeFactory.getText(processTaskStepRemindVo.getAction()));
            String contentHash = processTaskStepRemindVo.getContentHash();
            if (StringUtils.isNotBlank(contentHash)) {
                String content = selectContentByHashMapper.getProcessTaskContentStringByHash(contentHash);
                if (StringUtils.isNotBlank(content)) {
                    /* 有图片标签才显式点击详情 **/
                    if (content.contains("<figure class=\"image\">") && content.contains("</figure>")) {
                        processTaskStepRemindVo.setDetail(content);
                    }
                    processTaskStepRemindVo.setContent(pattern_html.matcher(content).replaceAll(""));
                }
            }
        }
        return processTaskStepRemindList;
    }

    @Override
    public Set<ProcessTaskStepVo> getTransferableStepListByProcessTask(ProcessTaskVo processTaskVo, String userUuid) {
        Set<ProcessTaskStepVo> resultSet = new HashSet<>();
        List<ProcessTaskStepVo> stepVoList =
                processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskVo.getId());
        for (ProcessTaskStepVo stepVo : stepVoList) {
            /* 找到所有已激活步骤 **/
            if (stepVo.getIsActive().equals(1)) {
                if (checkOperationAuthIsConfigured(stepVo, processTaskVo.getOwner(), processTaskVo.getReporter(),
                        ProcessTaskOperationType.STEP_TRANSFER, userUuid)) {
                    resultSet.add(stepVo);
                }
            }
        }
        return resultSet;
    }

    @Override
    public void setProcessTaskDetail(ProcessTaskVo processTaskVo) {
        // 上报服务路径
        ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
        if (channelVo != null) {
            CatalogVo catalogVo = catalogMapper.getCatalogByUuid(channelVo.getParentUuid());
            if (catalogVo != null) {
                List<CatalogVo> catalogList = catalogMapper.getAncestorsAndSelfByLftRht(catalogVo.getLft(), catalogVo.getRht());
                List<String> nameList = catalogList.stream().map(CatalogVo::getName).collect(Collectors.toList());
                nameList.add(channelVo.getName());
                processTaskVo.setChannelPath(String.join("/", nameList));
            }
            ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid());
            if (channelTypeVo == null) {
                channelTypeVo = new ChannelTypeVo();
                channelTypeVo.setUuid(channelVo.getChannelTypeUuid());
            }
            try {
                processTaskVo.setChannelType(channelTypeVo.clone());
            } catch (CloneNotSupportedException ignored) {
            }
            processTaskVo.setIsActivePriority(channelVo.getIsActivePriority());
            processTaskVo.setIsDisplayPriority(channelVo.getIsDisplayPriority());
            if (Objects.equals(channelVo.getIsActivePriority(), 1)) {
                List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(processTaskVo.getChannelUuid());
                if (CollectionUtils.isNotEmpty(channelPriorityList)) {
                    PriorityVo priorityVo = priorityMapper.getPriorityByUuid(processTaskVo.getPriorityUuid());
                    if (priorityVo == null) {
                        priorityVo = new PriorityVo();
                        priorityVo.setUuid(processTaskVo.getPriorityUuid());
                    }
                    processTaskVo.setPriority(priorityVo);
                    for (ChannelPriorityVo channelPriority : channelPriorityList) {
                        if (Objects.equals(channelPriority.getIsDefault(), 1)) {
                            processTaskVo.setDefaultPriorityUuid(channelPriority.getPriorityUuid());
                            break;
                        }
                    }
                }
            }
        }
        // 耗时
        if (processTaskVo.getEndTime() != null) {
            long timeCost = worktimeMapper.calculateCostTime(processTaskVo.getWorktimeUuid(),
                    processTaskVo.getStartTime().getTime(), processTaskVo.getEndTime().getTime());
            processTaskVo.setTimeCost(timeCost);
            processTaskVo.setTimeCostStr(TimeUtil.millisecondsTransferMaxTimeUnit(timeCost));
        }
        // 获取工单表单信息
        setProcessTaskFormInfo(processTaskVo);
        Long processTaskId = processTaskVo.getId();
        /* 获取评分信息 */
        if (ProcessTaskStatus.SCORED.getValue().equals(processTaskVo.getStatus())) {
            String scoreInfo = processTaskMapper.getProcessTaskScoreInfoById(processTaskId);
            processTaskVo.setScoreInfo(scoreInfo);
        }
        /* 转报数据 **/
        ProcessTaskTransferReportVo processTaskTransferReportVo = processTaskMapper.getProcessTaskTransferReportByToProcessTaskId(processTaskId);
        if (processTaskTransferReportVo != null) {
            ProcessTaskVo fromProcessTaskVo = getFromProcessTaskById(processTaskTransferReportVo.getFromProcessTaskId());
            ChannelTypeRelationVo channelTypeRelationVo = channelTypeMapper.getChannelTypeRelationById(processTaskTransferReportVo.getChannelTypeRelationId());
            if (channelTypeRelationVo != null) {
                fromProcessTaskVo.setChannelTypeRelationName(channelTypeRelationVo.getName());
            }
            processTaskVo.getTranferReportProcessTaskList().add(fromProcessTaskVo);
        }
        // 页面不需要显示目标工单
//        List<Long> toProcessTaskIdList = processTaskMapper.getToProcessTaskIdListByFromProcessTaskId(processTaskId);
//        for (Long toProcessTaskId : toProcessTaskIdList) {
//            ProcessTaskVo toProcessTaskVo = processTaskMapper.getProcessTaskBaseInfoById(toProcessTaskId);
//            if (toProcessTaskVo != null) {
//                toProcessTaskVo.setTranferReportDirection("to");
//                ChannelVo channel = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
//                if (channel != null) {
//                    ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(channel.getChannelTypeUuid());
//                    if (channelTypeVo == null) {
//                        channelTypeVo = new ChannelTypeVo();
//                        channelTypeVo.setUuid(channel.getChannelTypeUuid());
//                    }
//                    try {
//                        processTaskVo.setChannelType(channelTypeVo.clone());
//                    } catch (CloneNotSupportedException ignored) {
//                    }
//                }
//                processTaskVo.getTranferReportProcessTaskList().add(toProcessTaskVo);
//            }
//        }

        // 标签列表
        processTaskVo.setTagVoList(processTaskMapper.getProcessTaskTagListByProcessTaskId(processTaskId));
        /* 工单关注人列表 **/
        List<String> focusUserUuidList = processTaskMapper.getFocusUserListByTaskId(processTaskId);
        if (CollectionUtils.isNotEmpty(focusUserUuidList)) {
            processTaskVo.setFocusUserUuidList(focusUserUuidList);
            for (String focusUserUuid : focusUserUuidList) {
                if (focusUserUuid.contains(UserContext.get().getUserUuid())) {
                    processTaskVo.setIsFocus(1);
                    break;
                }
            }
        }

        String owner = processTaskVo.getOwner();
        UserVo ownerVo = userMapper.getUserBaseInfoByUuid(owner);
        if (ownerVo != null) {
            List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(owner);
            if (CollectionUtils.isNotEmpty(teamUuidList)) {
                List<TeamVo> teamList = teamMapper.getTeamByUuidList(teamUuidList);
                ownerVo.setTeamList(teamList);
            }
            processTaskVo.setOwnerVo(ownerVo);
        }
        //补充地域
        if (processTaskVo.getRegionId() != null) {
            RegionVo regionVo = regionMapper.getRegionById(processTaskVo.getRegionId());
            if (regionVo != null) {
                processTaskVo.setRegionVo(regionVo);
            }
        }
    }

    @Override
    public ProcessTaskStepVo getStartProcessTaskStepByProcessTaskId(Long processTaskId) {
        // 获取开始步骤id
        ProcessTaskStepVo startProcessTaskStepVo = processTaskMapper.getStartProcessTaskStepByProcessTaskId(processTaskId);
        // 获取上报描述内容
//        List<Long> fileIdList = new ArrayList<>();
        List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(startProcessTaskStepVo.getId());
        for (ProcessTaskStepContentVo processTaskStepContent : processTaskStepContentList) {
            if (ProcessTaskOperationType.PROCESSTASK_START.getValue().equals(processTaskStepContent.getType())) {
                ProcessTaskStepReplyVo comment = new ProcessTaskStepReplyVo();
                List<Long> fileIdList = processTaskMapper.getFileIdListByContentId(processTaskStepContent.getId());
                // 附件
                if (CollectionUtils.isNotEmpty(fileIdList)) {
                    comment.setFileIdList(fileIdList);
                    comment.setFileList(fileMapper.getFileListByIdList(fileIdList));
                }
                comment.setContent(selectContentByHashMapper.getProcessTaskContentStringByHash(processTaskStepContent.getContentHash()));
                startProcessTaskStepVo.setComment(comment);
                break;
            }
        }
        /* 当前步骤特有步骤信息 **/
        IProcessStepInternalHandler processStepUtilHandler = ProcessStepInternalHandlerFactory.getHandler(startProcessTaskStepVo.getHandler());
        if (processStepUtilHandler == null) {
            throw new ProcessStepHandlerNotFoundException(startProcessTaskStepVo.getHandler());
        }
        startProcessTaskStepVo.setHandlerStepInfo(processStepUtilHandler.getStartStepInfo(startProcessTaskStepVo));
        startProcessTaskStepVo.setReplaceableTextList(getReplaceableTextList(startProcessTaskStepVo));
        startProcessTaskStepVo.setCustomStatusList(getCustomStatusList(startProcessTaskStepVo));
        startProcessTaskStepVo.setCustomButtonList(getCustomButtonList(startProcessTaskStepVo));
        return startProcessTaskStepVo;
    }

    @Override
    public ProcessTaskVo getFromProcessTaskById(Long processTaskId) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
        if (processTaskVo != null) {
            ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
            if (channelVo != null) {
                ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid());
                if (channelTypeVo == null) {
                    channelTypeVo = new ChannelTypeVo();
                    channelTypeVo.setUuid(channelVo.getChannelTypeUuid());
                }
                try {
                    processTaskVo.setChannelType(channelTypeVo.clone());
                } catch (CloneNotSupportedException ignored) {
                }
            }
            // 获取工单表单信息
            setProcessTaskFormInfo(processTaskVo);
            processTaskVo.setStartProcessTaskStep(getStartProcessTaskStepByProcessTaskId(processTaskId));
            processTaskVo.setTranferReportDirection("from");
        }
        return processTaskVo;
    }

    /**
     * 获取所有工单干系人信息，用于通知接收人
     *
     * @param currentProcessTaskStepVo 当前步骤
     * @param receiverMap              通知人
     */
    @Override
    public void getReceiverMap(ProcessTaskStepVo currentProcessTaskStepVo,
                               Map<String, List<NotifyReceiverVo>> receiverMap, INotifyTriggerType notifyTriggerType) {
        ProcessTaskVo processTaskVo =
                processTaskMapper.getProcessTaskBaseInfoByIdIncludeIsDeleted(currentProcessTaskStepVo.getProcessTaskId());
        if (processTaskVo != null) {
            /* 上报人 **/
            if (StringUtils.isNotBlank(processTaskVo.getOwner())) {
                receiverMap.computeIfAbsent(ProcessUserType.OWNER.getValue(), k -> new ArrayList<>())
                        .add(new NotifyReceiverVo(GroupSearch.USER.getValue(), processTaskVo.getOwner()));
            }
            /* 代报人 **/
            if (StringUtils.isNotBlank(processTaskVo.getReporter())) {
                receiverMap.computeIfAbsent(ProcessUserType.REPORTER.getValue(), k -> new ArrayList<>())
                        .add(new NotifyReceiverVo(GroupSearch.USER.getValue(), processTaskVo.getReporter()));
            } else if (StringUtils.isNotBlank(processTaskVo.getOwner())) {
                receiverMap.computeIfAbsent(ProcessUserType.REPORTER.getValue(), k -> new ArrayList<>())
                        .add(new NotifyReceiverVo(GroupSearch.USER.getValue(), processTaskVo.getOwner()));
            }
        }
        ProcessTaskStepUserVo processTaskStepUser = new ProcessTaskStepUserVo();
        processTaskStepUser.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
        processTaskStepUser.setProcessTaskStepId(currentProcessTaskStepVo.getId());
        /* 主处理人 **/
        processTaskStepUser.setUserType(ProcessUserType.MAJOR.getValue());
        List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserList(processTaskStepUser);
        for (ProcessTaskStepUserVo processTaskStepUserVo : majorUserList) {
            receiverMap.computeIfAbsent(ProcessUserType.MAJOR.getValue(), k -> new ArrayList<>())
                    .add(new NotifyReceiverVo(GroupSearch.USER.getValue(), processTaskStepUserVo.getUserUuid()));
        }
        if (notifyTriggerType == ProcessTaskStepTaskNotifyTriggerType.COMPLETETASK
                || notifyTriggerType == ProcessTaskStepTaskNotifyTriggerType.CREATETASK
                || notifyTriggerType == ProcessTaskStepTaskNotifyTriggerType.DELETETASK
                || notifyTriggerType == ProcessTaskStepTaskNotifyTriggerType.EDITTASK
                || notifyTriggerType == ProcessTaskStepTaskNotifyTriggerType.COMPLETEALLTASK) {
            /* 当前任务处理人 */
            ProcessTaskStepTaskVo stepTaskVo = currentProcessTaskStepVo.getProcessTaskStepTaskVo();
            if (stepTaskVo != null) {
                List<ProcessTaskStepTaskUserVo> taskUserVoList = stepTaskVo.getStepTaskUserVoList();
                if (CollectionUtils.isNotEmpty(taskUserVoList)) {
                    for (ProcessTaskStepTaskUserVo taskUserVo : taskUserVoList) {
                        receiverMap.computeIfAbsent(ProcessUserType.MINOR.getValue(), k -> new ArrayList<>())
                                .add(new NotifyReceiverVo(GroupSearch.USER.getValue(), taskUserVo.getUserUuid()));
                    }
                }
            }
        } else {
            /* 所有任务处理人 **/
            processTaskStepUser.setUserType(ProcessUserType.MINOR.getValue());
            List<ProcessTaskStepUserVo> minorUserList = processTaskMapper.getProcessTaskStepUserList(processTaskStepUser);
            for (ProcessTaskStepUserVo processTaskStepUserVo : minorUserList) {
                receiverMap.computeIfAbsent(ProcessUserType.MINOR.getValue(), k -> new ArrayList<>())
                        .add(new NotifyReceiverVo(GroupSearch.USER.getValue(), processTaskStepUserVo.getUserUuid()));
            }
        }

        /* 待处理人 **/
        List<ProcessTaskStepWorkerVo> workerList =
                processTaskMapper.getProcessTaskStepWorkerByProcessTaskIdAndProcessTaskStepId(
                        currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId());
        for (ProcessTaskStepWorkerVo processTaskStepWorkerVo : workerList) {
            receiverMap.computeIfAbsent(ProcessUserType.WORKER.getValue(), k -> new ArrayList<>())
                    .add(new NotifyReceiverVo(processTaskStepWorkerVo.getType(), processTaskStepWorkerVo.getUuid()));
        }

        /* 工单关注人 */
        List<String> focusUserList =
                processTaskMapper.getFocusUserListByTaskId(currentProcessTaskStepVo.getProcessTaskId());
        for (String user : focusUserList) {
            String[] split = user.split("#");
            receiverMap.computeIfAbsent(ProcessUserType.FOCUS_USER.getValue(), k -> new ArrayList<>())
                    .add(new NotifyReceiverVo(split[0], split[1]));
        }

        /* 异常处理人 **/
        String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(currentProcessTaskStepVo.getConfigHash());
        if (StringUtils.isNotBlank(stepConfig)) {
            String defaultWorker = (String) JSONPath.read(stepConfig, "workerPolicyConfig.defaultWorker");
            if (StringUtils.isNotBlank(defaultWorker)) {
                String[] split = defaultWorker.split("#");
                receiverMap.computeIfAbsent(ProcessUserType.DEFAULT_WORKER.getValue(), k -> new ArrayList<>())
                        .add(new NotifyReceiverVo(split[0], split[1]));
            }
        }
    }

    /**
     * 设置步骤当前用户的暂存数据
     *
     * @param processTaskVo     任务信息
     * @param processTaskStepVo 步骤信息
     */
    @Override
    public void setTemporaryData(ProcessTaskVo processTaskVo, ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
        processTaskStepDataVo.setProcessTaskId(processTaskStepVo.getProcessTaskId());
        processTaskStepDataVo.setProcessTaskStepId(processTaskStepVo.getId());
        processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
        processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
        ProcessTaskStepDataVo stepDraftSaveData = processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
        if (stepDraftSaveData != null) {
            JSONObject dataObj = stepDraftSaveData.getData();
            if (MapUtils.isNotEmpty(dataObj)) {
                /* 表单属性 **/
                JSONArray formAttributeDataList = dataObj.getJSONArray("formAttributeDataList");
                if (CollectionUtils.isNotEmpty(formAttributeDataList)) {
                    Map<String, Object> formAttributeDataMap = new HashMap<>();
                    for (int i = 0; i < formAttributeDataList.size(); i++) {
                        JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
                        String attributeUuid = formAttributeDataObj.getString("attributeUuid");
                        if (StringUtils.isBlank(attributeUuid)) {
                            continue;
                        }
                        formAttributeDataMap.put(attributeUuid, formAttributeDataObj.get("dataList"));
                    }
//                    processTaskStepVo.setFormAttributeDataMap(formAttributeDataMap);
                    processTaskVo.setFormAttributeDataMap(formAttributeDataMap);
                }
                /* 描述及附件 **/
                ProcessTaskStepReplyVo commentVo = new ProcessTaskStepReplyVo();
                String content = dataObj.getString("content");
                commentVo.setContent(content);
                List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(dataObj.getJSONArray("fileIdList")), Long.class);
                if (CollectionUtils.isNotEmpty(fileIdList)) {
                    List<FileVo> fileList = fileMapper.getFileListByIdList(fileIdList);
                    if (CollectionUtils.isNotEmpty(fileList)) {
                        commentVo.setFileIdList(fileList.stream().map(FileVo::getId).collect(Collectors.toList()));
                        commentVo.setFileList(fileList);
                    }
                }
                processTaskStepVo.setComment(commentVo);
                /* 当前步骤特有步骤信息 **/
                JSONObject handlerStepInfo = dataObj.getJSONObject("handlerStepInfo");
                if (handlerStepInfo != null) {
                    processTaskStepVo.setHandlerStepInfo(handlerStepInfo);
                }
                /* 优先级 **/
                String priorityUuid = dataObj.getString("priorityUuid");
                if (StringUtils.isNotBlank(priorityUuid)) {
                    processTaskVo.setPriorityUuid(priorityUuid);
                    PriorityVo priorityVo = priorityMapper.getPriorityByUuid(priorityUuid);
                    if (priorityVo == null) {
                        priorityVo = new PriorityVo();
                        priorityVo.setUuid(priorityUuid);
                    }
                    processTaskVo.setPriority(priorityVo);
                }
                /* 标签列表 **/
                List<String> tagList = JSON.parseArray(JSON.toJSONString(dataObj.getJSONArray("tagList")), String.class);
                if (tagList != null) {
                    processTaskVo.setTagList(tagList);
                }
                /* 工单关注人列表 **/
                List<String> focusUserUuidList = JSON.parseArray(dataObj.getString("focusUserUuidList"), String.class);
                if (CollectionUtils.isNotEmpty(focusUserUuidList)) {
                    processTaskVo.setFocusUserUuidList(focusUserUuidList);
                }
            }
        }
    }


    /**
     * 查询待处理的工单，构造"用户uuid->List<工单字段中文名->值>"的map集合
     *
     * @param conditionMap 工单查询条件
     * @return "用户uuid->List<工单字段中文名->值>"的map集合
     */
    @Override
    public Map<String, List<Map<String, Object>>> getProcessingUserTaskMapByCondition(Map<String, Object> conditionMap) {

        Map<String, List<Map<String, Object>>> userTaskMap = new HashMap<>();
        List<UserVo> userList = (List<UserVo>) conditionMap.get("userList");
        /* 以处理组中的用户为单位，查询每个用户的待办工单 **/
        if (CollectionUtils.isNotEmpty(userList)) {
            for (UserVo user : userList) {
                getConditionMap(conditionMap, user);
                List<Map<String, Object>> taskList = new ArrayList<>();
                /* 查询工单 **/
                List<Long> taskIdList = processTaskMapper.getProcessingTaskIdListByCondition(conditionMap);
                if (CollectionUtils.isNotEmpty(taskIdList)) {
                    List<ProcessTaskVo> processTaskList = processTaskMapper.getTaskListByIdList(taskIdList);
                    for (ProcessTaskVo processTaskVo : processTaskList) {
                        Map<String, Object> map = new HashMap<>();
                        for (IProcessTaskColumn column : ProcessTaskColumnFactory.columnComponentMap.values()) {
                            if (!column.getDisabled() && column.getIsShow() && column.getIsExport()) {
                                map.put($.t(column.getDisplayName()), column.getSimpleValue(processTaskVo));
                            }
                        }
                        taskList.add(map);
                    }
                }
                if (CollectionUtils.isNotEmpty(taskList)) {
                    userTaskMap.put(user.getUuid(), taskList);
                }
            }
        }

        return userTaskMap;
    }

    /**
     * 查询每个用户待处理的工单数量，构造"用户uuid->工单数"的map集合
     *
     * @param conditionMap 工单查询条件
     * @return "用户uuid->工单数"的map集合
     */
    @Override
    public Map<String, Integer> getProcessingUserTaskCountByCondition(Map<String, Object> conditionMap) {
        Map<String, Integer> userTaskMap = new HashMap<>();
        List<UserVo> userList = (List<UserVo>) conditionMap.get("userList");
        /* 以处理组中的用户为单位，查询每个用户的待办工单数量 **/
        if (CollectionUtils.isNotEmpty(userList)) {
            for (UserVo user : userList) {
                getConditionMap(conditionMap, user);
                int taskCount = processTaskMapper.getProcessingTaskCountByCondition(conditionMap);
                if (taskCount > 0) {
                    userTaskMap.put(user.getUuid(), taskCount);
                }
            }
        }
        return userTaskMap;
    }

    /**
     * 把查询用户的待处理工单需要的userUuid、teamUuidList、roleUuidList条件put到conditionMap
     *
     * @param conditionMap 查询条件
     * @param user         用户
     */
    private void getConditionMap(Map<String, Object> conditionMap, UserVo user) {
        conditionMap.remove("teamUuidList");
        conditionMap.remove("roleUuidList");
        List<String> teamUuidList = user.getTeamUuidList();
        List<String> roleUuidList = user.getRoleUuidList();
        conditionMap.put("userUuid", user.getUuid());
        if (CollectionUtils.isNotEmpty(teamUuidList)) {
            conditionMap.put("teamUuidList", teamUuidList);
        }
        if (CollectionUtils.isNotEmpty(roleUuidList)) {
            conditionMap.put("roleUuidList", roleUuidList);
        }
    }

    @Override
    public JSONArray getReplaceableTextList(ProcessTaskStepVo processTaskStepVo) {
        String config = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
        boolean stepLevelTakesEffect = false;
        JSONArray replaceableTextList = (JSONArray) JSONPath.read(config, "replaceableTextList");
        if (CollectionUtils.isNotEmpty(replaceableTextList)) {
            for (int i = 0; i < replaceableTextList.size(); i++) {
                JSONObject replaceableText = replaceableTextList.getJSONObject(i);
                if (StringUtils.isNotBlank(replaceableText.getString("value"))) {
                    stepLevelTakesEffect = true;
                    break;
                }
            }
        }
        if (!stepLevelTakesEffect) {
            String handler = processTaskStepVo.getHandler();
            if (StringUtils.isNotBlank(handler)) {
                String processStepHandlerConfig = processStepHandlerMapper.getProcessStepHandlerConfigByHandler(handler);
                if (StringUtils.isNotBlank(processStepHandlerConfig)) {
                    JSONObject configObj = JSON.parseObject(processStepHandlerConfig);
                    if (MapUtils.isNotEmpty(configObj)) {
                        replaceableTextList = configObj.getJSONArray("replaceableTextList");
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(replaceableTextList)) {
            replaceableTextList = new JSONArray();
            for (ReplaceableText replaceableText : ReplaceableText.values()) {
                replaceableTextList.add(new JSONObject() {{
                    this.put("name", replaceableText.getValue());
                    this.put("text", replaceableText.getText());
                    this.put("value", "");
                }});
            }
        }
        return replaceableTextList;
    }

    @Override
    public JSONArray getCustomButtonList(ProcessTaskStepVo processTaskStepVo) {
        String config = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
        boolean stepLevelTakesEffect = false;
        JSONArray customButtonList = (JSONArray) JSONPath.read(config, "customButtonList");
        if (CollectionUtils.isNotEmpty(customButtonList)) {
            for (int i = 0; i < customButtonList.size(); i++) {
                JSONObject customButton = customButtonList.getJSONObject(i);
                if (StringUtils.isNotBlank(customButton.getString("value"))) {
                    stepLevelTakesEffect = true;
                    break;
                }
            }
        }
        if (!stepLevelTakesEffect) {
            String handler = processTaskStepVo.getHandler();
            if (StringUtils.isNotBlank(handler)) {
                String processStepHandlerConfig = processStepHandlerMapper.getProcessStepHandlerConfigByHandler(handler);
                if (StringUtils.isNotBlank(processStepHandlerConfig)) {
                    JSONObject configObj = JSON.parseObject(processStepHandlerConfig);
                    if (MapUtils.isNotEmpty(configObj)) {
                        customButtonList = configObj.getJSONArray("customButtonList");
                    }
                }
            }
        }
        return customButtonList;
    }

    @Override
    public JSONArray getCustomStatusList(ProcessTaskStepVo processTaskStepVo) {
        String config = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
        boolean stepLevelTakesEffect = false;
        JSONArray customStatusList = (JSONArray) JSONPath.read(config, "customStatusList");
        if (CollectionUtils.isNotEmpty(customStatusList)) {
            for (int i = 0; i < customStatusList.size(); i++) {
                JSONObject customStatus = customStatusList.getJSONObject(i);
                if (StringUtils.isNotBlank(customStatus.getString("value"))) {
                    stepLevelTakesEffect = true;
                    break;
                }
            }
        }
        if (!stepLevelTakesEffect) {
            String handler = processTaskStepVo.getHandler();
            if (StringUtils.isNotBlank(handler)) {
                String processStepHandlerConfig = processStepHandlerMapper.getProcessStepHandlerConfigByHandler(handler);
                if (StringUtils.isNotBlank(processStepHandlerConfig)) {
                    JSONObject configObj = JSON.parseObject(processStepHandlerConfig);
                    if (MapUtils.isNotEmpty(configObj)) {
                        customStatusList = configObj.getJSONArray("customStatusList");
                    }
                }
            }
        }
        return customStatusList;
    }

    /**
     * 刷新minor worker
     *
     * @param processTaskStepVo     步骤入参
     * @param processTaskStepTaskVo 步骤任务入参
     */
    @Override
    public void refreshStepMinorWorker(ProcessTaskStepVo processTaskStepVo, ProcessTaskStepTaskVo processTaskStepTaskVo) {
        //删除该step的所有minor工单步骤worker
        processTaskMapper.deleteProcessTaskStepWorkerMinorByProcessTaskStepId(processTaskStepVo.getId());
        List<ProcessTaskStepWorkerVo> workerVoList = new ArrayList<>();
        //重新更新每个模块的minor worker
        for (IProcessStepHandler handler : ProcessStepHandlerFactory.getHandlerList()) {
            workerVoList.addAll(handler.getMinorWorkerList(processTaskStepVo));

        }
        //重新插入pending任务用户到 工单步骤worker
        List<ProcessTaskStepTaskUserVo> taskUserVoList = processTaskStepTaskMapper.getStepTaskUserListByProcessTaskStepId(processTaskStepVo.getId());
        for (ProcessTaskStepTaskUserVo taskUserVo : taskUserVoList) {
            if (taskUserVo.getIsDelete() != 1 && Objects.equals(ProcessTaskStepTaskUserStatus.PENDING.getValue(), taskUserVo.getStatus())) {
                workerVoList.add(new ProcessTaskStepWorkerVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), GroupSearch.USER.getValue(), taskUserVo.getUserUuid(), ProcessUserType.MINOR.getValue()));
            }
        }

        for (ProcessTaskStepWorkerVo workerVo : workerVoList) {
            processTaskMapper.insertIgnoreProcessTaskStepWorker(workerVo);
        }
    }

    /**
     * 刷新minor user
     *
     * @param processTaskStepVo     步骤入参
     * @param processTaskStepTaskVo 步骤任务入参
     */
    @Override
    public void refreshStepMinorUser(ProcessTaskStepVo processTaskStepVo, ProcessTaskStepTaskVo processTaskStepTaskVo) {
        List<ProcessTaskStepUserVo> stepUserVoList = new ArrayList<>();
        //如果存在子任务已完成的用户,且该用户为succeed， 则更新到 processtask_step_user ，且status 为 done，type 为minor
        List<ProcessTaskStepTaskUserVo> taskUserVoList = processTaskStepTaskMapper.getStepTaskUserListByProcessTaskStepId(processTaskStepVo.getId());
        taskUserVoList = taskUserVoList.stream().filter(user -> Objects.equals(ProcessTaskStepTaskUserStatus.SUCCEED.getValue(), user.getStatus())).collect(collectingAndThen(toCollection(() -> new TreeSet<>(Comparator.comparing(ProcessTaskStepTaskUserVo::getUserUuid))), ArrayList::new));
        for (ProcessTaskStepTaskUserVo taskUserVo : taskUserVoList) {
            if (taskUserVo.getIsDelete() != 1) {
                String status = ProcessTaskStepUserStatus.DOING.getValue();
                if (Objects.equals(taskUserVo.getStatus(), ProcessTaskStepTaskUserStatus.SUCCEED.getValue())) {
                    status = ProcessTaskStepUserStatus.DONE.getValue();
                }
                UserVo userVo = userMapper.getUserBaseInfoByUuid(taskUserVo.getUserUuid());
                if (userVo != null) {
                    stepUserVoList.add(new ProcessTaskStepUserVo(status, processTaskStepTaskVo.getCreateTime(), taskUserVo.getEndTime(), processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), userVo.getUuid(), userVo.getUserName(), ProcessUserType.MINOR.getValue()));
                }
            }
        }
        //TODO 其它模块的taskStepUser
        //delete该step的所有minor工单步骤user
        processTaskMapper.deleteProcessTaskStepUserMinorByProcessTaskStepId(processTaskStepVo.getId());
        //insert该step的所有minor工单步骤user
        for (ProcessTaskStepUserVo stepUserVo : stepUserVoList) {
            processTaskMapper.insertIgnoreProcessTaskStepUser(stepUserVo);
        }
    }

    /**
     * @param processTaskStepVo 步骤信息
     * @return ProcessTaskStepVo
     * @Description: 获取当前步骤信息
     */
    @Override
    public ProcessTaskStepVo getCurrentProcessTaskStepDetail(ProcessTaskStepVo processTaskStepVo, boolean hasComplete) {
        Long processTaskStepId = processTaskStepVo.getId();
        // 处理人列表
        setProcessTaskStepUser(processTaskStepVo);

        /* 当前步骤特有步骤信息 **/
        IProcessStepInternalHandler processStepUtilHandler = ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
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
        processTaskStepVo.setCommentList(getProcessTaskStepReplyListByProcessTaskStepId(processTaskStepId, typeList));

        //任务列表
        if (processTaskStepVo.getIsActive() == 1 && ProcessTaskStepStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
            processTaskStepTaskService.getProcessTaskStepTask(processTaskStepVo);
            List<TaskConfigVo> taskConfigList = processTaskStepTaskService.getTaskConfigList(processTaskStepVo);
            processTaskStepVo.setTaskConfigList(taskConfigList);
        }

        // 获取可分配处理人的步骤列表
//        processTaskStepVo.setAssignableWorkerStepList(getAssignableWorkerStepList(processTaskStepVo));

        // 时效列表
        processTaskStepVo.setSlaTimeList(getSlaTimeListByProcessTaskStepId(processTaskStepId));

        if (ProcessStepHandlerType.AUTOMATIC.getHandler().equals(processTaskStepVo.getHandler())) {
            // 补充 automatic processtaskStepData
            ProcessTaskStepDataVo stepDataVo = processTaskStepDataMapper
                    .getProcessTaskStepData(new ProcessTaskStepDataVo(processTaskStepVo.getProcessTaskId(),
                            processTaskStepVo.getId(), processTaskStepVo.getHandler(), SystemUser.SYSTEM.getUserUuid()));

            if (stepDataVo != null) {
                JSONObject stepDataJson = stepDataVo.getData();
                processTaskStepVo.setProcessTaskStepData(stepDataJson);
                if (hasComplete) {// 有处理权限
                    stepDataJson.put("isStepUser", 1);
                    if (processTaskStepVo.getHandler().equals(ProcessStepHandlerType.AUTOMATIC.getHandler())) {
                        JSONObject requestAuditJson = stepDataJson.getJSONObject("requestAudit");
                        if (requestAuditJson.containsKey("status") && requestAuditJson.getJSONObject("status")
                                .getString("value").equals(ProcessTaskStatus.FAILED.getValue())) {
                            requestAuditJson.put("isRetry", 1);
                        } else {
                            requestAuditJson.put("isRetry", 0);
                        }
                        JSONObject callbackAuditJson = stepDataJson.getJSONObject("callbackAudit");
                        if (callbackAuditJson != null) {
                            if (callbackAuditJson.containsKey("status") && callbackAuditJson.getJSONObject("status")
                                    .getString("value").equals(ProcessTaskStatus.FAILED.getValue())) {
                                callbackAuditJson.put("isRetry", 1);
                            } else {
                                callbackAuditJson.put("isRetry", 0);
                            }
                        }
                    }
                }
            }
        }

        /* 下一步骤列表 **/
        setNextStepList(processTaskStepVo);
//        processTaskStepVo.setForwardNextStepList(getForwardNextStepListByProcessTaskStepId(processTaskStepVo.getId()));
//        processTaskStepVo.setBackwardNextStepList(getBackwardNextStepListByProcessTaskStepId(processTaskStepVo.getId()));

        /* 提醒列表 **/
        List<ProcessTaskStepRemindVo> processTaskStepRemindList = getProcessTaskStepRemindListByProcessTaskStepId(processTaskStepId);
        processTaskStepVo.setProcessTaskStepRemindList(processTaskStepRemindList);

        ProcessTaskStepAgentVo processTaskStepAgentVo = processTaskMapper.getProcessTaskStepAgentByProcessTaskStepId(processTaskStepId);
        if (processTaskStepAgentVo != null) {
            processTaskStepVo.setOriginalUser(processTaskStepAgentVo.getUserUuid());
            UserVo userVo = userMapper.getUserBaseInfoByUuid(processTaskStepAgentVo.getUserUuid());
            if (userVo != null) {
                UserVo vo = new UserVo();
                BeanUtils.copyProperties(userVo, vo);
                processTaskStepVo.setOriginalUserVo(vo);
//                    processTaskStepVo.setOriginalUserName(userVo.getUserName());
            }
        }
        /* 如果当前用户有处理权限，则获取其有权看到的配置的回复模版 */
        if (hasComplete) {
            processTaskStepVo.setCommentTemplate(getProcessStepCommentTemplate(processTaskStepVo.getProcessStepUuid(), UserContext.get().getAuthenticationInfoVo()));
        }
        processTaskStepVo.setReplaceableTextList(getReplaceableTextList(processTaskStepVo));
        processTaskStepVo.setCustomStatusList(getCustomStatusList(processTaskStepVo));
        processTaskStepVo.setCustomButtonList(getCustomButtonList(processTaskStepVo));

        List<Long> tagIdList = processTaskMapper.getTagIdListByProcessTaskStepId(processTaskStepId);
        if (CollectionUtils.isNotEmpty(tagIdList)) {
            List<ProcessTagVo> processTagList = processTagMapper.getProcessTagByIdList(tagIdList);
            if (CollectionUtils.isNotEmpty(processTagList)) {
                processTaskStepVo.setProcessTagList(processTagList);
            }
        }
        return processTaskStepVo;
    }

    @Override
    public ProcessCommentTemplateVo getProcessStepCommentTemplate(String processStepUuid, AuthenticationInfoVo authenticationInfoVo) {
        if (processStepUuid != null && authenticationInfoVo != null) {
            List<String> authList = new ArrayList<>();
            authList.addAll(authenticationInfoVo.getTeamUuidList());
            authList.addAll(authenticationInfoVo.getRoleUuidList());
            authList.add(UserType.ALL.getValue());
            authList.add(UserContext.get().getUserUuid());
            return commentTemplateMapper.getTemplateByStepUuidAndAuth(processStepUuid, authList);
        }
        return null;
    }

    @Override
    public Map<ProcessUserType, List<String>> getProcessTaskStepProcessUserTypeData(ProcessTaskStepVo processTaskStepVo, List<ProcessUserType> processUserTypeList) {
        Map<ProcessUserType, List<String>> resultMap = new HashMap<>();
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoByIdIncludeIsDeleted(processTaskStepVo.getProcessTaskId());
        if (processTaskVo != null) {
            /* 上报人 **/
            if (StringUtils.isNotBlank(processTaskVo.getOwner()) && processUserTypeList.contains(ProcessUserType.OWNER)) {
                resultMap.computeIfAbsent(ProcessUserType.OWNER, k -> new ArrayList<>())
                        .add(GroupSearch.USER.addPrefix(processTaskVo.getOwner()));
            }
            /* 代报人 **/
            if (StringUtils.isNotBlank(processTaskVo.getReporter()) && processUserTypeList.contains(ProcessUserType.REPORTER)) {
                resultMap.computeIfAbsent(ProcessUserType.REPORTER, k -> new ArrayList<>())
                        .add(GroupSearch.USER.addPrefix(processTaskVo.getReporter()));
            }
        }
        ProcessTaskStepUserVo processTaskStepUser = new ProcessTaskStepUserVo();
        processTaskStepUser.setProcessTaskId(processTaskStepVo.getProcessTaskId());
        processTaskStepUser.setProcessTaskStepId(processTaskStepVo.getId());
        if (processUserTypeList.contains(ProcessUserType.MAJOR)) {
            /* 主处理人 **/
            processTaskStepUser.setUserType(ProcessUserType.MAJOR.getValue());
            List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserList(processTaskStepUser);
            for (ProcessTaskStepUserVo processTaskStepUserVo : majorUserList) {
                resultMap.computeIfAbsent(ProcessUserType.MAJOR, k -> new ArrayList<>())
                        .add(GroupSearch.USER.addPrefix(processTaskStepUserVo.getUserUuid()));
            }
        }
        if (processUserTypeList.contains(ProcessUserType.MINOR)) {
            /* 所有任务处理人 **/
            processTaskStepUser.setUserType(ProcessUserType.MINOR.getValue());
            List<ProcessTaskStepUserVo> minorUserList = processTaskMapper.getProcessTaskStepUserList(processTaskStepUser);
            for (ProcessTaskStepUserVo processTaskStepUserVo : minorUserList) {
                resultMap.computeIfAbsent(ProcessUserType.MINOR, k -> new ArrayList<>())
                        .add(GroupSearch.USER.addPrefix(processTaskStepUserVo.getUserUuid()));
            }
        }
        if (processUserTypeList.contains(ProcessUserType.WORKER)) {
            /* 待处理人 **/
            List<ProcessTaskStepWorkerVo> workerList = processTaskMapper.getProcessTaskStepWorkerByProcessTaskIdAndProcessTaskStepId(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId());
            for (ProcessTaskStepWorkerVo processTaskStepWorkerVo : workerList) {
                resultMap.computeIfAbsent(ProcessUserType.WORKER, k -> new ArrayList<>()).add(processTaskStepWorkerVo.getType() + "#" + processTaskStepWorkerVo.getUuid());
            }
        }
        if (processUserTypeList.contains(ProcessUserType.FOCUS_USER)) {
            /* 工单关注人 */
            List<String> focusUserList = processTaskMapper.getFocusUserListByTaskId(processTaskStepVo.getProcessTaskId());
            for (String focusUser : focusUserList) {
                resultMap.computeIfAbsent(ProcessUserType.FOCUS_USER, k -> new ArrayList<>())
                        .add(focusUser);
            }
        }
        if (processUserTypeList.contains(ProcessUserType.DEFAULT_WORKER)) {
            /* 异常处理人 **/
            String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
            if (StringUtils.isNotBlank(stepConfig)) {
                String defaultWorker = (String) JSONPath.read(stepConfig, "workerPolicyConfig.defaultWorker");
                if (StringUtils.isNotBlank(defaultWorker)) {
                    resultMap.computeIfAbsent(ProcessUserType.DEFAULT_WORKER, k -> new ArrayList<>())
                            .add(defaultWorker);
                }
            }
        }
        return resultMap;
    }

    @Override
    public List<FormAttributeVo> getFormAttributeListByProcessTaskId(Long processTaskId) {
        return getFormAttributeListByProcessTaskIdAngTag(processTaskId, null);
    }

    @Override
    public List<FormAttributeVo> getFormAttributeListByProcessTaskIdAngTag(Long processTaskId, String tag) {
        List<FormAttributeVo> resultList = new ArrayList<>();
        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
        if (processTaskFormVo == null || StringUtils.isBlank(processTaskFormVo.getFormContent())) {
            // 工单没有表单直接返回
            return resultList;
        }
//        String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
        JSONObject config = JSON.parseObject(processTaskFormVo.getFormContent());
        // 默认场景的表单
        FormVersionVo formVersionVo = new FormVersionVo();
        formVersionVo.setFormUuid(processTaskFormVo.getFormUuid());
        formVersionVo.setFormName(processTaskFormVo.getFormName());
        formVersionVo.setFormConfig(config);
        String mainSceneUuid = config.getString("uuid");
        formVersionVo.setSceneUuid(mainSceneUuid);
        List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
        if (StringUtils.isBlank(tag)) {
            return formAttributeList;
        }
        List<String> parentUuidList = new ArrayList<>();
        List<FormAttributeVo> formExtendAttributeList = new ArrayList<>();
        List<FormAttributeVo> allFormExtendAttributeList = formVersionVo.getFormExtendAttributeList();
        if (CollectionUtils.isNotEmpty(allFormExtendAttributeList)) {
            for (FormAttributeVo formAttributeVo : allFormExtendAttributeList) {
                if (Objects.equals(formAttributeVo.getTag(), tag)) {
                    parentUuidList.add(formAttributeVo.getParentUuid());
                    formExtendAttributeList.add(formAttributeVo);
                }
            }
        }
        for (FormAttributeVo formAttributeVo : formAttributeList) {
            if (parentUuidList.contains(formAttributeVo.getUuid())) {
                continue;
            }
            resultList.add(formAttributeVo);
        }
        resultList.addAll(formExtendAttributeList);
        return resultList;
    }

    @Override
    public List<FormAttributeVo> getFormAttributeListByProcessTaskIdAngTagNew(Long processTaskId, String tag) {
        List<FormAttributeVo> resultList = new ArrayList<>();
        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
        if (processTaskFormVo == null || StringUtils.isBlank(processTaskFormVo.getFormContent())) {
            // 工单没有表单直接返回
            return resultList;
        }
//        String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
        JSONObject config = JSON.parseObject(processTaskFormVo.getFormContent());
        // 默认场景的表单
        FormVersionVo formVersionVo = new FormVersionVo();
        formVersionVo.setFormUuid(processTaskFormVo.getFormUuid());
        formVersionVo.setFormName(processTaskFormVo.getFormName());
        formVersionVo.setFormConfig(config);
        String mainSceneUuid = config.getString("uuid");
        formVersionVo.setSceneUuid(mainSceneUuid);
        if (StringUtils.isBlank(tag)) {
            tag = "common";
        }
        Set<String> tagSet = new HashSet<>();
        {
            List<FormAttributeVo> formCustomExtendAttributeList = formVersionVo.getFormCustomExtendAttributeList();
            if (CollectionUtils.isNotEmpty(formCustomExtendAttributeList)) {
                for (FormAttributeVo formAttributeVo : formCustomExtendAttributeList) {
                    tagSet.add(formAttributeVo.getTag());
                }
            }
        }
        if (tagSet.contains(tag)) {
            List<FormAttributeVo> allFormCustomExtendAttributeList = formVersionVo.getFormCustomExtendAttributeList();
            if (CollectionUtils.isNotEmpty(allFormCustomExtendAttributeList)) {
                for (FormAttributeVo formAttributeVo : allFormCustomExtendAttributeList) {
                    if (Objects.equals(formAttributeVo.getTag(), tag)) {
                        resultList.add(formAttributeVo);
                    }
                }
            }
            return resultList;
        } else {
            return getFormAttributeListByProcessTaskIdAngTag(processTaskId, tag);
        }
    }

    @Override
    public List<ProcessTaskFormAttributeDataVo> getProcessTaskFormAttributeDataListByProcessTaskId(Long processTaskId) {
        return getProcessTaskFormAttributeDataListByProcessTaskIdAndTag(processTaskId, null);
    }

    @Override
    public List<ProcessTaskFormAttributeDataVo> getProcessTaskFormAttributeDataListByProcessTaskIdAndTag(Long processTaskId, String tag) {
        List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = new ArrayList<>();
        List<AttributeDataVo> attributeDataList = processTaskMapper.getProcessTaskFormAttributeDataListByProcessTaskId(processTaskId);
        for (AttributeDataVo attributeDataVo : attributeDataList) {
            processTaskFormAttributeDataList.add(new ProcessTaskFormAttributeDataVo(processTaskId, attributeDataVo));
        }
        if (StringUtils.isNotBlank(tag)) {
            List<AttributeDataVo> extendAttributeDataList = processTaskMapper.getProcessTaskExtendFormAttributeDataListByProcessTaskId(processTaskId, tag);
            for (AttributeDataVo attributeDataVo : extendAttributeDataList) {
                processTaskFormAttributeDataList.add(new ProcessTaskFormAttributeDataVo(processTaskId, attributeDataVo));
            }
        }
        return processTaskFormAttributeDataList;
    }

    @Override
    public List<ProcessTaskFormAttributeDataVo> getProcessTaskFormAttributeDataListByProcessTaskIdAndTagNew(Long processTaskId, String tag) {
        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
        if (processTaskFormVo == null || StringUtils.isBlank(processTaskFormVo.getFormContent())) {
            // 工单没有表单直接返回
            return new ArrayList<>();
        }
//        String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
        JSONObject config = JSON.parseObject(processTaskFormVo.getFormContent());
        // 默认场景的表单
        FormVersionVo formVersionVo = new FormVersionVo();
        formVersionVo.setFormUuid(processTaskFormVo.getFormUuid());
        formVersionVo.setFormName(processTaskFormVo.getFormName());
        formVersionVo.setFormConfig(config);
        String mainSceneUuid = config.getString("uuid");
        formVersionVo.setSceneUuid(mainSceneUuid);
        if (StringUtils.isBlank(tag)) {
            tag = "common";
        }
        Set<String> tagSet = new HashSet<>();
        List<FormAttributeVo> formCustomExtendAttributeList = formVersionVo.getFormCustomExtendAttributeList();
        if (CollectionUtils.isNotEmpty(formCustomExtendAttributeList)) {
            for (FormAttributeVo formAttributeVo : formCustomExtendAttributeList) {
                tagSet.add(formAttributeVo.getTag());
            }
        }
        if (tagSet.contains(tag)) {
            List<String> attributeUuidList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(formCustomExtendAttributeList)) {
                for (FormAttributeVo formAttributeVo : formCustomExtendAttributeList) {
                    if (Objects.equals(formAttributeVo.getTag(), tag)) {
                        attributeUuidList.add(formAttributeVo.getUuid());
                    }
                }
            }
            List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = new ArrayList<>();
            List<AttributeDataVo> extendAttributeDataList = processTaskMapper.getProcessTaskExtendFormAttributeDataListByProcessTaskId(processTaskId, tag);
            for (AttributeDataVo attributeDataVo : extendAttributeDataList) {
                if (attributeUuidList.contains(attributeDataVo.getAttributeUuid())) {
                    processTaskFormAttributeDataList.add(new ProcessTaskFormAttributeDataVo(processTaskId, attributeDataVo));
                }
            }
            return processTaskFormAttributeDataList;
        } else {
            return getProcessTaskFormAttributeDataListByProcessTaskIdAndTag(processTaskId, tag);
        }
    }

    @Override
    public ProcessTaskFormAttributeDataVo getProcessTaskFormAttributeDataByProcessTaskIdAndAttributeUuid(Long processTaskId, String attributeUuid) {
        List<Long> formAttributeDataIdList = processTaskMapper.getProcessTaskFormAttributeDataIdListByProcessTaskId(processTaskId);
        if (CollectionUtils.isEmpty(formAttributeDataIdList)) {
            return null;
        }
        List<AttributeDataVo> attributeDataList = formMapper.getFormAttributeDataListByIdList(formAttributeDataIdList);
        if (CollectionUtils.isEmpty(attributeDataList)) {
            return null;
        }
        for (AttributeDataVo attributeDataVo : attributeDataList) {
            if (Objects.equals(attributeDataVo.getAttributeUuid(), attributeUuid)) {
                return new ProcessTaskFormAttributeDataVo(processTaskId, attributeDataVo);
            }
        }
        return null;
    }

    @Override
    public void deleteProcessTaskFormAttributeDataByProcessTaskId(Long processTaskId) {
        List<Long> formAttributeDataIdList = processTaskMapper.getProcessTaskFormAttributeDataIdListByProcessTaskId(processTaskId);
        if (CollectionUtils.isNotEmpty(formAttributeDataIdList)) {
            formMapper.deleteFormAttributeDataByIdList(formAttributeDataIdList);
            processTaskMapper.deleteProcessTaskFormAttributeByProcessTaskId(processTaskId);
        }
        List<Long> extendFormAttributeDataIdList = processTaskMapper.getProcessTaskExtendFormAttributeDataIdListByProcessTaskId(processTaskId);
        if (CollectionUtils.isNotEmpty(extendFormAttributeDataIdList)) {
            formMapper.deleteFormExtendAttributeDataByIdList(formAttributeDataIdList);
            processTaskMapper.deleteProcessTaskExtendFormAttributeByProcessTaskId(processTaskId);
        }
    }

    @Override
    public List<ProcessTaskActionVo> getProcessTaskActionListByProcessTaskStepId(Long processTaskStepId) {
        List<ProcessTaskActionVo> processTaskActionList = processTaskActionMapper.getProcessTaskActionListByProcessTaskStepId(processTaskStepId);
        if (CollectionUtils.isNotEmpty(processTaskActionList)) {
            List<String> integrationUuidList = processTaskActionList.stream().map(ProcessTaskActionVo::getIntegrationUuid).collect(Collectors.toList());
            List<IntegrationVo> integrationList = integrationMapper.getIntegrationListByUuidList(integrationUuidList);
            Map<String, String> uuid2NameMap = integrationList.stream().collect(Collectors.toMap(IntegrationVo::getUuid, IntegrationVo::getName));
            for (ProcessTaskActionVo processTaskActionVo : processTaskActionList) {
                String integrationName = uuid2NameMap.get(processTaskActionVo.getIntegrationUuid());
                if (StringUtils.isNotBlank(integrationName)) {
                    processTaskActionVo.setIntegrationName(integrationName);
                } else {
                    JSONObject config = processTaskActionVo.getConfig();
                    if (MapUtils.isNotEmpty(config)) {
                        integrationName = config.getString("integrationName");
                        if (StringUtils.isNotBlank(integrationName)) {
                            processTaskActionVo.setIntegrationName(integrationName);
                        }
                    }
                }
                if (Objects.equals(processTaskActionVo.getStatus(), "succeed")) {
                    processTaskActionVo.setStatusText("已成功");
                } else {
                    processTaskActionVo.setStatusText("已失败");
                }
                String triggerText = ProcessTaskStepNotifyTriggerType.getText(processTaskActionVo.getTrigger());
                if(StringUtils.isNotBlank(triggerText)) {
                    processTaskActionVo.setTriggerText(triggerText);
                }
            }
        }
        return processTaskActionList;
    }

    /**
     * 根据fileId 和 processTaskIdList 获取对应用户是否有该工单附件的下载权限
     *
     * @return true：有权限   false：没有权限
     */
    @Override
    public boolean getProcessFileHasDownloadAuthWithFileIdAndProcessTaskIdList(Long fileId, List<Long> processTaskIdList) {
        int hasDownloadAuth = 0;
        for (Long processTaskId : processTaskIdList) {
            if (new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_VIEW).build().check()) {
                hasDownloadAuth = 1;
                break;
            }
        }
        if (hasDownloadAuth == 0) {
            //所有工单都没有下载权限
            throw new ProcessTaskFileDownloadException(fileId);
        }
        return true;
    }

    @Override
    public List<ProcessTaskStepVo> getProcessableStepList(ProcessTaskVo processTaskVo, String action) {
        List<ProcessTaskStepVo> resultList = new ArrayList<>();
        Long processTaskId = processTaskVo.getId();
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskId);
        List<Long> processTaskStepIdList = processTaskStepList.stream().map(ProcessTaskStepVo::getId).collect(Collectors.toList());
        ProcessAuthManager.Builder builder = new ProcessAuthManager.Builder().addProcessTaskId(processTaskId).addProcessTaskStepId(processTaskStepIdList);
        ProcessTaskOperationType actionType = null;
        if (ProcessTaskOperationType.STEP_ACCEPT.getValue().equals(action)) {
            actionType = ProcessTaskOperationType.STEP_ACCEPT;
            builder.addOperationType(actionType);
        } else if (ProcessTaskOperationType.STEP_START.getValue().equals(action)) {
            actionType = ProcessTaskOperationType.STEP_START;
            builder.addOperationType(actionType);
        } else if (ProcessTaskOperationType.STEP_COMPLETE.getValue().equals(action)) {
            actionType = ProcessTaskOperationType.STEP_COMPLETE;
            builder.addOperationType(actionType);
        } else if (ProcessTaskOperationType.STEP_RECOVER.getValue().equals(action)) {
            actionType = ProcessTaskOperationType.STEP_RECOVER;
            builder.addOperationType(actionType);
        } else if (ProcessTaskOperationType.STEP_PAUSE.getValue().equals(action)) {
            actionType = ProcessTaskOperationType.STEP_PAUSE;
            builder.addOperationType(actionType);
        } else {
            builder.addOperationType(ProcessTaskOperationType.STEP_ACCEPT);
            builder.addOperationType(ProcessTaskOperationType.STEP_START);
            builder.addOperationType(ProcessTaskOperationType.STEP_COMPLETE);
            builder.addOperationType(ProcessTaskOperationType.STEP_RECOVER);
            builder.addOperationType(ProcessTaskOperationType.STEP_PAUSE);
        }
        String userUuid = UserContext.get().getUserUuid(true);
        AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
        Map<Long, Set<ProcessTaskOperationType>> operationTypeSetMap = builder.build().getOperateMap();
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            Set<ProcessTaskOperationType> set = operationTypeSetMap.get(processTaskStepVo.getId());
            if (CollectionUtils.isNotEmpty(set)) {
                if (actionType != null) {
                    if (set.contains(actionType)) {
                        resultList.add(processTaskStepVo);
                    }
                } else {
                    if (set.contains(ProcessTaskOperationType.STEP_ACCEPT)) {
                        resultList.add(processTaskStepVo);
                    } else if (set.contains(ProcessTaskOperationType.STEP_START)) {
                        resultList.add(processTaskStepVo);
                    } else if (set.contains(ProcessTaskOperationType.STEP_COMPLETE)) {
                        resultList.add(processTaskStepVo);
                    } else if (set.contains(ProcessTaskOperationType.STEP_RECOVER)) {
                        resultList.add(processTaskStepVo);
                    } else if (set.contains(ProcessTaskOperationType.STEP_PAUSE)) {
                        resultList.add(processTaskStepVo);
                    }
                }
            } else {
                // 对于状态为“处理中”的步骤，要检测子任务和变更步骤处理权限
                if (Objects.equals(processTaskStepVo.getStatus(), ProcessTaskStepStatus.RUNNING.getValue())) {
                    // 如果没有步骤处理权限，还需要判断是否有当前步骤的子任务或变更步骤处理权限
                    if (processTaskMapper.checkIsWorker(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), ProcessUserType.MINOR.getValue(), authenticationInfoVo) > 0) {
                        resultList.add(processTaskStepVo);
                        continue;
                    }
                    // 子任务用户A授权给B，B处理后，A也能处理
                    List<ProcessTaskStepTaskVo> processTaskStepTaskList = processTaskStepTaskMapper.getStepTaskListByProcessTaskStepId(processTaskStepVo.getId());
                    if (CollectionUtils.isNotEmpty(processTaskStepTaskList)) {
                        List<Long> stepTaskIdList = processTaskStepTaskList.stream().map(ProcessTaskStepTaskVo::getId).collect(Collectors.toList());
                        List<ProcessTaskStepTaskUserAgentVo> processTaskStepTaskUserAgentList = processTaskStepTaskMapper.getProcessTaskStepTaskUserAgentListByStepTaskIdList(stepTaskIdList);
                        for (ProcessTaskStepTaskUserAgentVo processTaskStepTaskUserAgentVo : processTaskStepTaskUserAgentList) {
                            if (Objects.equals(processTaskStepTaskUserAgentVo.getUserUuid(), userUuid)) {
                                resultList.add(processTaskStepVo);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return resultList;
    }
//    @Override
//    public List<ProcessTaskStepVo> getProcessableStepList(ProcessTaskVo processTaskVo, String action) {
//        Long processTaskId = processTaskVo.getId();
//        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskId);
//        List<ProcessTaskStepVo> processableStepList = getProcessableStepList(processTaskId, UserContext.get().getUserUuid(true), action, processTaskStepList);
//        // 用户A授权给B，B是当前登录人，查出用户A拥有的权限，叠加当前用户B权限里
//        List<String> fromUserUuidList = processTaskAgentService.getFromUserUuidListByToUserUuidAndChannelUuid(UserContext.get().getUserUuid(true), processTaskVo.getChannelUuid());
//        for (String userUuid : fromUserUuidList) {
//            for (ProcessTaskStepVo processTaskStepVo : getProcessableStepList(processTaskId, userUuid, action, processTaskStepList)) {
//                if (!processableStepList.contains(processTaskStepVo)) {
//                    processableStepList.add(processTaskStepVo);
//                }
//            }
//        }
//        return processableStepList;
//    }
//    /**
//     * @param processTaskId
//     * @return List<ProcessTaskStepVo>
//     * @Time:2020年4月3日
//     * @Description: 获取工单中当前用户能处理的步骤列表
//     */
//    private List<ProcessTaskStepVo> getProcessableStepList(Long processTaskId, String userUuid, String action, List<ProcessTaskStepVo> processTaskStepList) {
//        List<ProcessTaskStepVo> resultList = new ArrayList<>();
//        AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userUuid);
//        for (ProcessTaskStepVo stepVo : processTaskStepList) {
//            /** 找到所有已激活未处理的步骤 **/
//            if (stepVo.getIsActive().equals(1)) {
//                if (getProcessableStepList(authenticationInfoVo, userUuid, action, stepVo)) {
//                    resultList.add(stepVo);
//                } else {
//                    ProcessTaskStepAgentVo processTaskStepAgentVo = processTaskMapper.getProcessTaskStepAgentByProcessTaskStepId(stepVo.getId());
//                    if (processTaskStepAgentVo != null) {
//                        if (Objects.equals(processTaskStepAgentVo.getUserUuid(), userUuid)) {
//                            authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(processTaskStepAgentVo.getAgentUuid());
//                            if (getProcessableStepList(authenticationInfoVo, processTaskStepAgentVo.getAgentUuid(), action, stepVo)) {
//                                resultList.add(stepVo);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return resultList;
//    }
//
//    private boolean getProcessableStepList(AuthenticationInfoVo authenticationInfoVo, String userUuid, String action, ProcessTaskStepVo stepVo) {
//        if (ProcessTaskOperationType.STEP_ACCEPT.getValue().equals(action)) {
//            if (ProcessTaskStatus.PENDING.getValue().equals(stepVo.getStatus())) {
//                if (processTaskMapper.checkIsWorker(stepVo.getProcessTaskId(), stepVo.getId(), ProcessUserType.MAJOR.getValue(), authenticationInfoVo) > 0) {
//                    ProcessTaskStepUserVo stepUserVo = new ProcessTaskStepUserVo(stepVo.getProcessTaskId(), stepVo.getId(), userUuid, ProcessUserType.MAJOR.getValue());
//                    if (processTaskMapper.checkIsProcessTaskStepUser(stepUserVo) == 0) {
//                        return true;
//                    }
//                }
//            }
//        } else if (ProcessTaskOperationType.STEP_START.getValue().equals(action)) {
//            if (ProcessTaskStatus.PENDING.getValue().equals(stepVo.getStatus())) {
//                ProcessTaskStepUserVo stepUserVo = new ProcessTaskStepUserVo(stepVo.getProcessTaskId(), stepVo.getId(), userUuid, ProcessUserType.MAJOR.getValue());
//                if (processTaskMapper.checkIsProcessTaskStepUser(stepUserVo) > 0) {
//                    return true;
//                }
//            }
//        } else if (ProcessTaskOperationType.STEP_COMPLETE.getValue().equals(action)) {
//            if (ProcessTaskStatus.RUNNING.getValue().equals(stepVo.getStatus())) {
//                ProcessTaskStepUserVo stepUserVo = new ProcessTaskStepUserVo(stepVo.getProcessTaskId(), stepVo.getId(), userUuid, ProcessUserType.MAJOR.getValue());
//                if (processTaskMapper.checkIsProcessTaskStepUser(stepUserVo) > 0) {
//                    return true;
//                }
//            }
//        } else {
//            if (processTaskMapper.checkIsWorker(stepVo.getProcessTaskId(), stepVo.getId(), null, authenticationInfoVo) > 0) {
//                return true;
//            }
//            // 子任务处理人可以重复回复
//            ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
//            processTaskStepUserVo.setProcessTaskId(stepVo.getProcessTaskId());
//            processTaskStepUserVo.setProcessTaskStepId(stepVo.getId());
//            processTaskStepUserVo.setUserUuid(userUuid);
//            processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
//            if (processTaskMapper.checkIsProcessTaskStepUser(processTaskStepUserVo) > 0) {
//                return true;
//            }
//            // 子任务用户A授权给B，B处理后，A也能处理
//            List<ProcessTaskStepTaskVo> processTaskStepTaskList = processTaskStepTaskMapper.getStepTaskListByProcessTaskStepId(stepVo.getId());
//            if (CollectionUtils.isNotEmpty(processTaskStepTaskList)) {
//                List<Long> stepTaskIdList = processTaskStepTaskList.stream().map(ProcessTaskStepTaskVo::getId).collect(Collectors.toList());
//                List<ProcessTaskStepTaskUserAgentVo> processTaskStepTaskUserAgentList = processTaskStepTaskMapper.getProcessTaskStepTaskUserAgentListByStepTaskIdList(stepTaskIdList);
//                for (ProcessTaskStepTaskUserAgentVo processTaskStepTaskUserAgentVo : processTaskStepTaskUserAgentList) {
//                    if (Objects.equals(processTaskStepTaskUserAgentVo.getUserUuid(), userUuid)) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }

    /**
     * 暂存工单草稿
     */
    @Override
    public JSONObject saveProcessTaskDraft(JSONObject jsonObj, Long newProcessTaskId) throws Exception {
        String channelUuid = jsonObj.getString("channelUuid");
        ChannelVo channelVo = channelMapper.getChannelByUuid(channelUuid);
        if (channelVo == null) {
            throw new ChannelNotFoundException(channelUuid);
        }
        String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
        if (processMapper.checkProcessIsExists(processUuid) == 0) {
            throw new ProcessNotFoundException(processUuid);
        }
        /*
          由于批量上报是暂存与提交一并完成，
          如果不校验优先级，那么会出现批量上报记录显示上报失败，
          而实际上已经生成工单，只是状态是草稿
         */
        if (Objects.equals(channelVo.getIsActivePriority(), 1)) {
            String priorityUuid = jsonObj.getString("priorityUuid");
            if (StringUtils.isBlank(priorityUuid)) {
                throw new ProcessTaskPriorityIsEmptyException();
            }
            List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(channelUuid);
            if (channelPriorityList.stream().noneMatch(o -> o.getPriorityUuid().equals(priorityUuid))) {
                throw new ProcessTaskPriorityNotMatchException();
            }
        } else {
            jsonObj.remove("priorityUuid");
        }

        String owner = jsonObj.getString("owner");
        if (StringUtils.isNotBlank(owner)) {
            if (owner.contains("#")) {
                owner = owner.split("#")[1];
                jsonObj.put("owner", owner);
            }
            UserVo user = userMapper.getUserBaseInfoByUuid(owner);
            if (user == null) {
                throw new UserNotFoundException(owner);
            }
        } else {
            jsonObj.put("owner", null);
        }
        // 为了兼容第三方上报接口认证，当owner与当前登录用户不一致时，无须校验reporter必填
        String reporter = jsonObj.getString("reporter");
        if (StringUtils.isNotBlank(reporter)) {
            if (reporter.contains("#")) {
                reporter = reporter.split("#")[1];
                jsonObj.put("reporter", reporter);
            }
            UserVo user = userMapper.getUserBaseInfoByUuid(reporter);
            if (user == null) {
                throw new UserNotFoundException(reporter);
            }
        } else {
            jsonObj.put("reporter", null);
        }
        ProcessTaskStepVo startProcessTaskStepVo = null;

        FormVersionVo formVersionVo = null;
        Long processTaskId = jsonObj.getLong("processTaskId");
        if (processTaskId != null) {
            checkProcessTaskParamsIsLegal(processTaskId);
            startProcessTaskStepVo = processTaskMapper.getStartProcessTaskStepByProcessTaskId(processTaskId);
            ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
            if (processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContent())) {
//                String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
                formVersionVo = new FormVersionVo();
                formVersionVo.setFormUuid(processTaskFormVo.getFormUuid());
                formVersionVo.setFormName(processTaskFormVo.getFormName());
                formVersionVo.setFormConfig(JSON.parseObject(processTaskFormVo.getFormContent()));
            }
        } else {
            /* 判断当前用户是否拥有channelUuid服务的上报权限 **/
            if (!catalogService.channelIsAuthority(channelUuid, UserContext.get().getUserUuid(true), CatalogChannelAuthorityAction.REPORT)) {
                throw new PermissionDeniedException();
            }
            startProcessTaskStepVo = new ProcessTaskStepVo();
            startProcessTaskStepVo.setProcessUuid(processUuid);
            ProcessStepVo startProcessStepVo = processMapper.getStartProcessStepByProcessUuid(processUuid);
            startProcessTaskStepVo.setHandler(startProcessStepVo.getHandler());
            ProcessFormVo processFormVo = processMapper.getProcessFormByProcessUuid(processUuid);
            if (processFormVo != null) {
                formVersionVo = formMapper.getActionFormVersionByFormUuid(processFormVo.getFormUuid());
            }
        }

        JSONArray formAttributeDataList = jsonObj.getJSONArray("formAttributeDataList");
        FormUtil.formAttributeValueValid(formVersionVo, formAttributeDataList);

        IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(startProcessTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepHandlerNotFoundException(startProcessTaskStepVo.getHandler());
        }
        // 对表格输入组件中密码password类型的单元格数据进行加密
//        if (formVersionVo != null) {
//            List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
//            if (CollectionUtils.isNotEmpty(formAttributeList)) {
//                Map<String, FormAttributeVo> formAttributeMap = new HashMap<>();
//                for (FormAttributeVo formAttributeVo : formAttributeList) {
//                    formAttributeMap.put(formAttributeVo.getUuid(), formAttributeVo);
//                }
//                IFormCrossoverService formCrossoverService = CrossoverServiceFactory.getApi(IFormCrossoverService.class);
//                JSONArray formAttributeDataList = jsonObj.getJSONArray("formAttributeDataList");
//                for (int i = 0; i < formAttributeDataList.size(); i++) {
//                    JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
//                    String attributeUuid = formAttributeDataObj.getString("attributeUuid");
//                    FormAttributeVo formAttributeVo = formAttributeMap.get(attributeUuid);
//                    if (formAttributeVo != null) {
//                        if (Objects.equals(formAttributeVo.getHandler(), FormHandler.FORMTABLEINPUTER.getHandler())) {
//                            JSONArray dataList = formAttributeDataObj.getJSONArray("dataList");
//                            formCrossoverService.staticListPasswordEncrypt(dataList, formAttributeVo.getConfigObj());
//                        } else if (Objects.equals(formAttributeVo.getHandler(), FormHandler.FORMPASSWORD.getHandler())) {
//                            String dataList = formAttributeDataObj.getString("dataList");
//                            if (StringUtils.isNotBlank(dataList)) {
//                                formAttributeDataObj.put("dataList", RC4Util.encrypt(dataList));
//                            }
//                        }
//                    }
//                }
//            }
//        }

        ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
        processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
        processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));

        startProcessTaskStepVo.getParamObj().putAll(jsonObj);
        handler.saveDraft(startProcessTaskStepVo, newProcessTaskId);

        processTaskStepDataVo.setData(jsonObj.toJSONString());
        processTaskStepDataVo.setProcessTaskId(startProcessTaskStepVo.getProcessTaskId());
        processTaskStepDataVo.setProcessTaskStepId(startProcessTaskStepVo.getId());
        processTaskStepDataMapper.deleteProcessTaskStepData(processTaskStepDataVo);
        processTaskStepDataMapper.replaceProcessTaskStepData(processTaskStepDataVo);
        JSONObject resultObj = new JSONObject();
        resultObj.put("processTaskId", startProcessTaskStepVo.getProcessTaskId());
        resultObj.put("processTaskStepId", startProcessTaskStepVo.getId());

        //创建全文检索索引
        IFullTextIndexHandler indexHandler = FullTextIndexHandlerFactory.getHandler(ProcessFullTextIndexType.PROCESSTASK);
        if (indexHandler != null) {
            indexHandler.createIndex(startProcessTaskStepVo.getProcessTaskId());
        }
        return resultObj;
    }

    /**
     * void
     * 提交上报工单
     *
     * @return
     */
    @Override
    public void startProcessProcessTask(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long nextStepId = jsonObj.getLong("nextStepId");
        checkProcessTaskParamsIsLegal(processTaskId, null, nextStepId);

        ProcessTaskStepVo startProcessTaskStepVo = processTaskMapper.getStartProcessTaskStepByProcessTaskId(processTaskId);
        IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(startProcessTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepHandlerNotFoundException(startProcessTaskStepVo.getHandler());
        }

        ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
        processTaskStepDataVo.setProcessTaskId(startProcessTaskStepVo.getProcessTaskId());
        processTaskStepDataVo.setProcessTaskStepId(startProcessTaskStepVo.getId());
        processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
        processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
        processTaskStepDataVo = processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
        if (processTaskStepDataVo != null) {
            JSONObject dataObj = processTaskStepDataVo.getData();
            if (MapUtils.isNotEmpty(dataObj)) {
                Long fromProcessTaskId = dataObj.getLong("fromProcessTaskId");
                if (fromProcessTaskId != null) {
                    ProcessTaskVo fromProcessTaskVo = checkProcessTaskParamsIsLegal(fromProcessTaskId);
                    Long channelTypeRelationId = dataObj.getLong("channelTypeRelationId");
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
//                    boolean flag = checkTransferReportAuthorization(fromProcessTaskVo, UserContext.get().getUserUuid(true), channelTypeRelationId);
//                    if (!flag) {
//                        new ProcessTaskOperationUnauthorizedException(ProcessTaskOperationType.PROCESSTASK_TRANSFERREPORT);
//                    }
                }
                jsonObj.putAll(dataObj);
            }
        }
        startProcessTaskStepVo.getParamObj().putAll(jsonObj);
        handler.startProcess(startProcessTaskStepVo);
        processTaskStepDataMapper.deleteProcessTaskStepData(processTaskStepDataVo);
    }

    @Override
    public void completeProcessTaskStep(JSONObject paramObj) throws Exception {
        Long processTaskId = paramObj.getLong("processTaskId");
        Long processTaskStepId = paramObj.getLong("processTaskStepId");
        Long nextStepId = paramObj.getLong("nextStepId");
        ProcessTaskVo processTaskVo = checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId, nextStepId);
        ProcessTaskStepVo processTaskStepVo = processTaskVo.getCurrentProcessTaskStep();
        IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        //任务
        List<ProcessTaskStepTaskVo> stepTaskVoList = processTaskStepTaskMapper.getStepTaskWithUserByProcessTaskStepId(processTaskStepId);
        if (CollectionUtils.isNotEmpty(stepTaskVoList)) {
            for (ProcessTaskStepTaskVo stepTaskVo : stepTaskVoList) {
                TaskConfigManager.Action<ProcessTaskStepTaskVo> action = taskConfigManager.getConfigMap().get(stepTaskVo.getTaskConfigPolicy());
                if (action != null && !action.execute(stepTaskVo)) {
                    throw new ProcessTaskStepTaskNotCompleteException(stepTaskVo);
                }
            }
        }

        ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
        processTaskStepDataVo.setProcessTaskId(processTaskId);
        processTaskStepDataVo.setProcessTaskStepId(processTaskStepId);
        processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
        processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
        JSONObject data = getProcessTaskStepStagingData(processTaskId, processTaskStepId);
        processTaskStepVo.getParamObj().putAll(data);
        processTaskStepVo.getParamObj().putAll(paramObj);
        handler.complete(processTaskStepVo);
        processTaskStepDataMapper.deleteProcessTaskStepData(processTaskStepDataVo);

        //创建全文检索索引
        IFullTextIndexHandler indexFormHandler = FullTextIndexHandlerFactory.getHandler(ProcessFullTextIndexType.PROCESSTASK);
        if (indexFormHandler != null) {
            indexFormHandler.createIndex(processTaskStepVo.getProcessTaskId());
        }
    }

    @Override
    public void startProcessTaskStep(JSONObject paramObj) throws Exception {
        Long processTaskId = paramObj.getLong("processTaskId");
        Long processTaskStepId = paramObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        ProcessTaskStepVo processTaskStepVo = processTaskVo.getCurrentProcessTaskStep();
        IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        String action = paramObj.getString("action");
        processTaskStepVo.getParamObj().put("source", paramObj.getString("source"));
        if (ProcessTaskOperationType.STEP_ACCEPT.getValue().equals(action)) {
            handler.accept(processTaskStepVo);
        }
        handler.start(processTaskStepVo);
    }

    @Override
    public List<Map<String, Object>> getProcessTaskListWhichIsProcessingByUserAndTag(JSONObject jsonObj) {
        String userId = jsonObj.getString("userId");
        String tag = jsonObj.getString("tag");
        UserVo user = userMapper.getUserByUserId(userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        AuthenticationInfoVo authenticationInfo = authenticationInfoService.getAuthenticationInfo(user.getUuid());
        return processTaskMapper.getProcessTaskListWhichIsProcessingByUserAndTag(tag, user.getUuid(), authenticationInfo.getTeamUuidList(), authenticationInfo.getRoleUuidList());
    }

    @Override
    public JSONObject batchCompleteProcessTaskStep(JSONObject jsonObj) {
        JSONObject result = new JSONObject();
        List<Long> notFoundProcessTaskIdList = new ArrayList<>(); // 工单不存在的id列表
        List<Long> currentStepOverOneProcessTaskIdList = new ArrayList<>();// 当前步骤超过一个的工单id列表
        List<Long> noAuthProcessTaskIdList = new ArrayList<>(); // 无权限处理的工单id列表
        Map<Long, String> exceptionMap = new HashMap<>(); // 处理发生异常的工单
        List<Long> idList = jsonObj.getJSONArray("processTaskIdList").toJavaList(Long.class);
        String tag = jsonObj.getString("tag");
        String content = jsonObj.getString("content");
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
        if (!processTaskIdList.isEmpty()) {
            AuthenticationInfoVo authenticationInfo = authenticationInfoService.getAuthenticationInfo(user.getUuid());
            List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getCurrentProcessTaskStepListByProcessTaskIdListAndTag(processTaskIdList, tag);
            Map<Long, List<ProcessTaskStepVo>> map = processTaskStepList.stream().collect(Collectors.groupingBy(ProcessTaskStepVo::getProcessTaskId));
            for (Map.Entry<Long, List<ProcessTaskStepVo>> entry : map.entrySet()) {
                Long key = entry.getKey();
                List<ProcessTaskStepVo> value = entry.getValue();
                // 如果没有标签，则不处理当前步骤有多个的工单；如果有标签，尝试处理带标签的步骤
                if (StringUtils.isBlank(tag) && value.size() > 1) {
                    currentStepOverOneProcessTaskIdList.add(key);
                    continue;
                }
                for (ProcessTaskStepVo currentStep : value) {
                    try {
                        if (!ProcessStepType.PROCESS.getValue().equals(currentStep.getType())) {
                            throw new ProcessTaskStepIsNotManualException(currentStep.getProcessTaskId(), currentStep.getName());
                        }
                        // 变更和事件必须在页面上处理
                        if (EventProcessStepHandlerType.EVENT.getHandler().equals(currentStep.getHandler()) || ChangeProcessStepHandlerType.CHANGECREATE.getHandler().equals(currentStep.getHandler())
                                || ChangeProcessStepHandlerType.CHANGEHANDLE.getHandler().equals(currentStep.getHandler())) {
                            throw new ProcessTaskStepMustBeManualException(currentStep.getProcessTaskId(), currentStep.getName());
                        }
                        Map<Long, Set<ProcessTaskOperationType>> auth = checkProcessTaskStepCompleteAuth(user, authenticationInfo, currentStep);
                        if (MapUtils.isEmpty(auth) || auth.values().stream().findFirst().get().isEmpty()) {
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
                            startProcessTaskStep(param);
                        }
                        UserContext.init(user, authenticationInfo, SystemUser.SYSTEM.getTimezone());
                        completeProcessTaskStep(currentStep, content);
                    } catch (Exception ex) {
                        exceptionMap.put(currentStep.getProcessTaskId(), ex.getMessage());
                    }
                }
            }
        }

        if (!notFoundProcessTaskIdList.isEmpty()) {
            result.put("工单不存在的ID", notFoundProcessTaskIdList);
        }
        if (!currentStepOverOneProcessTaskIdList.isEmpty()) {
            result.put("当前步骤超过一个的工单", currentStepOverOneProcessTaskIdList);
        }
        if (!noAuthProcessTaskIdList.isEmpty()) {
            result.put("无权限处理的工单", noAuthProcessTaskIdList.stream().map(Objects::toString).collect(Collectors.joining(",")));
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
     */
    private Map<Long, Set<ProcessTaskOperationType>> checkProcessTaskStepCompleteAuth(UserVo user, AuthenticationInfoVo authenticationInfo, ProcessTaskStepVo processTaskStepVo) {
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
     * @param content           处理意见
     */
    private void completeProcessTaskStep(ProcessTaskStepVo processTaskStepVo, String content) throws Exception {
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
        if (StringUtils.isNotBlank(content)) {
            param.put("content", content);
        }
        param.put("action", "complete");
        completeProcessTaskStep(param);
    }

    /**
     * 检查工单状态，如果processTaskStatus属于status其中一员，则返回对应的异常对象，否则返回null
     *
     * @param processTaskStatus 工单状态
     * @param statuss           状态列表
     */
    @Override
    public ProcessTaskPermissionDeniedException checkProcessTaskStatus(String processTaskStatus, ProcessTaskStatus... statuss) {
        if (statuss != null) {
            for (ProcessTaskStatus status : statuss) {
                switch (status) {
                    case DRAFT:
                        if (ProcessTaskStatus.DRAFT.getValue().equals(processTaskStatus)) {
                            return new ProcessTaskUnsubmittedException();
                        }
                        break;
                    case SUCCEED:
                        if (ProcessTaskStatus.SUCCEED.getValue().equals(processTaskStatus)) {
                            return new ProcessTaskSucceededException();
                        }
                        break;
                    case ABORTED:
                        if (ProcessTaskStatus.ABORTED.getValue().equals(processTaskStatus)) {
                            return new ProcessTaskAbortedException();
                        }
                        break;
                    case FAILED:
                        if (ProcessTaskStatus.FAILED.getValue().equals(processTaskStatus)) {
                            return new ProcessTaskFailedException();
                        }
                        break;
                    case HANG:
                        if (ProcessTaskStatus.HANG.getValue().equals(processTaskStatus)) {
                            return new ProcessTaskHangException();
                        }
                        break;
                    case SCORED:
                        if (ProcessTaskStatus.SCORED.getValue().equals(processTaskStatus)) {
                            return new ProcessTaskScoredException();
                        }
                        break;
                    case RUNNING:
                        if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStatus)) {
                            return new ProcessTaskRunningException();
                        }
                        break;
                }
            }
        }
        return null;
    }

    /**
     * 检查步骤状态，如果stepStatus属于status其中一员，则返回对应的异常对象，否则返回null
     *
     * @param stepStatus 步骤状态
     * @param statuss    状态列表
     */
    @Override
    public ProcessTaskPermissionDeniedException checkProcessTaskStepStatus(String stepStatus, ProcessTaskStepStatus... statuss) {
        if (statuss != null) {
            for (ProcessTaskStepStatus status : statuss) {
                switch (status) {
                    case DRAFT:
                        if (ProcessTaskStepStatus.DRAFT.getValue().equals(stepStatus)) {
                            return new ProcessTaskStepUnsubmittedException();
                        }
                        break;
                    case PENDING:
                        if (ProcessTaskStepStatus.PENDING.getValue().equals(stepStatus)) {
                            return new ProcessTaskStepPendingException();
                        }
                        break;
                    case RUNNING:
                        if (ProcessTaskStepStatus.RUNNING.getValue().equals(stepStatus)) {
                            return new ProcessTaskStepRunningException();
                        }
                        break;
                    case SUCCEED:
                        if (ProcessTaskStepStatus.SUCCEED.getValue().equals(stepStatus)) {
                            return new ProcessTaskStepSucceededException();
                        }
                        break;
                    case FAILED:
                        if (ProcessTaskStepStatus.FAILED.getValue().equals(stepStatus)) {
                            return new ProcessTaskStepFailedException();
                        }
                        break;
                    case HANG:
                        if (ProcessTaskStepStatus.HANG.getValue().equals(stepStatus)) {
                            return new ProcessTaskStepHangException();
                        }
                        break;
                }
            }
        }
        return null;
    }

//    /**
//     * 判断当前用户是否拥有工单转报权限
//     * @param processTaskVo 工单信息
//     * @param userUuid 用户uuid
//     * @return
//     */
//    @Override
//    public boolean checkTransferReportAuthorization(ProcessTaskVo processTaskVo, String userUuid) {
//        return checkTransferReportAuthorization(processTaskVo, userUuid, null);
//    }

//    /**
//     * 判断当前用户是否拥有工单转报权限
//     * @param processTaskVo 工单信息
//     * @param userUuid 用户uuid
//     * @param relationId 转报关系id
//     * @return
//     */
//    @Override
//    public boolean checkTransferReportAuthorization(ProcessTaskVo processTaskVo, String userUuid, Long relationId) {
//        AuthenticationInfoVo authenticationInfoVo = null;
//        if (Objects.equals(UserContext.get().getUserUuid(), userUuid)) {
//            authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
//        } else {
//            authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userUuid);
//        }
//        List<String> processUserTypeList = new ArrayList<>();
//        if (userUuid.equals(processTaskVo.getOwner())) {
//            processUserTypeList.add(ProcessUserType.OWNER.getValue());
//        }
//        if (userUuid.equals(processTaskVo.getReporter())) {
//            processUserTypeList.add(ProcessUserType.REPORTER.getValue());
//        }
//
//        List<ProcessTaskStepVo> processTaskStepList = processTaskVo.getStepList();
//        if (CollectionUtils.isNotEmpty(processTaskStepList)) {
//            for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
//                for (ProcessTaskStepUserVo processTaskStepUserVo : processTaskStepVo.getUserList()) {
//                    if (userUuid.equals(processTaskStepUserVo.getUserUuid())) {
//                        if (ProcessUserType.MAJOR.getValue().equals(processTaskStepUserVo.getUserType())) {
//                            if (!processUserTypeList.contains(ProcessUserType.MAJOR.getValue())) {
//                                processUserTypeList.add(ProcessUserType.MAJOR.getValue());
//                            }
//                        }
//                        if (ProcessUserType.WORKER.getValue().equals(processTaskStepUserVo.getUserType())) {
//                            if (!processUserTypeList.contains(ProcessUserType.MAJOR.getValue())) {
//                                processUserTypeList.add(ProcessUserType.WORKER.getValue());
//                            }
//                        }
//                        if (ProcessUserType.MINOR.getValue().equals(processTaskStepUserVo.getUserType())) {
//                            if (!processUserTypeList.contains(ProcessUserType.MAJOR.getValue())) {
//                                processUserTypeList.add(ProcessUserType.MINOR.getValue());
//                            }
//                        }
//                    }
//                }
//            }
//        } else {
//            ProcessTaskStepUserVo processTaskStepUserVo = new ProcessTaskStepUserVo();
//            processTaskStepUserVo.setProcessTaskId(processTaskVo.getId());
//            processTaskStepUserVo.setUserUuid(userUuid);
//            processTaskStepUserVo.setUserType(ProcessUserType.MAJOR.getValue());
//            if (processTaskMapper.checkIsProcessTaskStepUser(processTaskStepUserVo) > 0) {
//                processUserTypeList.add(ProcessUserType.MAJOR.getValue());
//                processUserTypeList.add(ProcessUserType.WORKER.getValue());
//            } else if (processTaskMapper.checkIsWorker(processTaskVo.getId(), null, ProcessUserType.MAJOR.getValue(), authenticationInfoVo) > 0) {
//                processUserTypeList.add(ProcessUserType.WORKER.getValue());
//            }
//            processTaskStepUserVo.setUserType(ProcessUserType.MINOR.getValue());
//            if (processTaskMapper.checkIsProcessTaskStepUser(processTaskStepUserVo) > 0) {
//                processUserTypeList.add(ProcessUserType.MINOR.getValue());
//            }
//        }
//
//        List<Long> channelTypeRelationIdList = channelTypeMapper.getAuthorizedChannelTypeRelationIdListBySourceChannelUuid(
//                processTaskVo.getChannelUuid(), userUuid, authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), processUserTypeList);
//        if (CollectionUtils.isNotEmpty(channelTypeRelationIdList)) {
//            ChannelRelationVo channelRelationVo = new ChannelRelationVo();
//            channelRelationVo.setSource(processTaskVo.getChannelUuid());
//            for (Long channelTypeRelationId : channelTypeRelationIdList) {
//                if (relationId != null && !Objects.equals(channelTypeRelationId, relationId)) {
//                    continue;
//                }
//                channelRelationVo.setChannelTypeRelationId(channelTypeRelationId);
//                List<ChannelRelationVo> channelRelationTargetList = channelMapper.getChannelRelationTargetList(channelRelationVo);
//                if (CollectionUtils.isNotEmpty(channelRelationTargetList)) {
//                    List<String> channelTypeUuidList = channelTypeMapper.getChannelTypeRelationTargetListByChannelTypeRelationId(channelTypeRelationId);
//                    if (channelTypeUuidList.contains("all")) {
//                        channelTypeUuidList.clear();
//                    }
//                    for (ChannelRelationVo channelRelation : channelRelationTargetList) {
//                        if ("channel".equals(channelRelation.getType())) {
//                            return true;
//                        } else if ("catalog".equals(channelRelation.getType())) {
//                            if (channelTypeMapper.getActiveChannelCountByParentUuidAndChannelTypeUuidList(channelRelation.getTarget(), channelTypeUuidList) > 0) {
//                                return true;
//                            } else {
//                                CatalogVo catalogVo = catalogMapper.getCatalogByUuid(channelRelation.getTarget());
//                                if (catalogVo != null) {
//                                    List<String> uuidList = catalogMapper.getCatalogUuidListByLftRht(catalogVo.getLft(), catalogVo.getRht());
//                                    for (String uuid : uuidList) {
//                                        if (!channelRelation.getTarget().equals(uuid)) {
//                                            if (channelTypeMapper.getActiveChannelCountByParentUuidAndChannelTypeUuidList(uuid, channelTypeUuidList) > 0) {
//                                                return true;
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return false;
//    }

    @Override
    public JSONObject getProcessTaskStepStagingData(Long processTaskId, Long processTaskStepId) {
        JSONObject data = new JSONObject();
        ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
        processTaskStepDataVo.setProcessTaskId(processTaskId);
        processTaskStepDataVo.setProcessTaskStepId(processTaskStepId);
        processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
        processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
        ProcessTaskStepDataVo stepDraftSaveData = processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
        if (stepDraftSaveData != null) {
            JSONObject dataObj = stepDraftSaveData.getData();
            if (MapUtils.isNotEmpty(dataObj)) {
                JSONArray formAttributeDataList = dataObj.getJSONArray("formAttributeDataList");
                if (CollectionUtils.isNotEmpty(formAttributeDataList)) {
                    data.put("formAttributeDataList", formAttributeDataList);
                }
                JSONArray formExtendAttributeDataList = dataObj.getJSONArray("formExtendAttributeDataList");
                if (CollectionUtils.isNotEmpty(formExtendAttributeDataList)) {
                    data.put("formExtendAttributeDataList", formExtendAttributeDataList);
                }
                JSONArray hidecomponentList = dataObj.getJSONArray("hidecomponentList");
                if (CollectionUtils.isNotEmpty(hidecomponentList)) {
                    data.put("hidecomponentList", hidecomponentList);
                }
                JSONArray readcomponentList = dataObj.getJSONArray("readcomponentList");
                if (CollectionUtils.isNotEmpty(readcomponentList)) {
                    data.put("readcomponentList", readcomponentList);
                }
                JSONObject handlerStepInfo = dataObj.getJSONObject("handlerStepInfo");
                if (MapUtils.isNotEmpty(handlerStepInfo)) {
                    data.put("handlerStepInfo", handlerStepInfo);
                }
                String priorityUuid = dataObj.getString("priorityUuid");
                if (StringUtils.isNotBlank(priorityUuid)) {
                    data.put("priorityUuid", priorityUuid);
                }
                JSONArray fileIdList = dataObj.getJSONArray("fileIdList");
                if (CollectionUtils.isNotEmpty(fileIdList)) {
                    data.put("fileIdList", fileIdList);
                }
                String content = dataObj.getString("content");
                if (StringUtils.isNotBlank(content)) {
                    data.put("content", content);
                }
            }
        }
        return data;
    }
}
