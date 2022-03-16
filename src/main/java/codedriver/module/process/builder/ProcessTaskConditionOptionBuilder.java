/*
 * Copyright(c) 2022 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.builder;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.TeamLevel;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.AuthenticationInfoVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ConditionProcessTaskOptions;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.*;
import codedriver.framework.process.dto.*;
import codedriver.framework.service.AuthenticationInfoService;
import codedriver.module.process.service.ProcessTaskStepTaskService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author linbq
 * @since 2022/3/15 18:51
 **/
@Component
public class ProcessTaskConditionOptionBuilder {
    private static ProcessTaskMapper processTaskMapper;
    private static ChannelMapper channelMapper;
    private static TeamMapper teamMapper;
    private static UserMapper userMapper;
    private static SelectContentByHashMapper selectContentByHashMapper;
    private static AuthenticationInfoService authenticationInfoService;
    private static ProcessTaskStepTaskService processTaskStepTaskService;

    @Resource
    public void setProcessTaskMapper(ProcessTaskMapper _processTaskMapper) {
        processTaskMapper = _processTaskMapper;
    }
    @Resource
    public void setChannelMapper(ChannelMapper _channelMapper) {
        channelMapper = _channelMapper;
    }
    @Resource
    public void setTeamMapper(TeamMapper _teamMapper) {
        teamMapper = _teamMapper;
    }
    @Resource
    public void setUserMapper(UserMapper _userMapper) {
        userMapper = _userMapper;
    }
    @Resource
    public void setSelectContentByHashMapper(SelectContentByHashMapper _selectContentByHashMapper) {
        selectContentByHashMapper = _selectContentByHashMapper;
    }
    @Resource
    public void setAuthenticationInfoService(AuthenticationInfoService _authenticationInfoService) {
        authenticationInfoService = _authenticationInfoService;
    }
    @Resource
    public void setProcessTaskStepTaskService(ProcessTaskStepTaskService _processTaskStepTaskService) {
        processTaskStepTaskService = _processTaskStepTaskService;
    }

    private Long processTaskId;
    private Long processTaskStepId;
    List<ConditionProcessTaskOptions> optionList = new ArrayList<>();

    public ProcessTaskConditionOptionBuilder() {
    }

    public ProcessTaskConditionOptionBuilder(Long processTaskId) {
        this.processTaskId = processTaskId;
    }

    public ProcessTaskConditionOptionBuilder addProcessTaskStepId(Long processTaskStepId) {
        this.processTaskStepId = processTaskStepId;
        return this;
    }
    public ProcessTaskConditionOptionBuilder addConditionOption(ConditionProcessTaskOptions processField) {
        optionList.add(processField);
        return this;
    }
    public ProcessTaskConditionOptionBuilder addConditionOptions(ConditionProcessTaskOptions ... processFields) {
        for (ConditionProcessTaskOptions processField : processFields) {
            optionList.add(processField);
        }
        return this;
    }

    public JSONObject build() {
        JSONObject resultObj = new JSONObject();
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
        if (processTaskVo == null) {
            return resultObj;
        }
        if (optionList.contains(ConditionProcessTaskOptions.TASKID)) {
            resultObj.put(ConditionProcessTaskOptions.TASKID.getValue(), processTaskId);
        }
        if (optionList.contains(ConditionProcessTaskOptions.CONTENT)) {
            String content = processTaskMapper.getProcessTaskStartContentByProcessTaskId(processTaskId);
            if (StringUtils.isBlank(content)) {
                content = "";
            }
            resultObj.put(ConditionProcessTaskOptions.CONTENT.getValue(), content);
        }
        if (optionList.contains(ConditionProcessTaskOptions.TITLE)) {
            resultObj.put(ConditionProcessTaskOptions.TITLE.getValue(), processTaskVo.getTitle());
        }
        if (optionList.contains(ConditionProcessTaskOptions.CHANNELTYPE)) {
            ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
            if (channelVo != null) {
                resultObj.put(ConditionProcessTaskOptions.CHANNELTYPE.getValue(), channelVo.getChannelTypeUuid());
            }
        }
        if (optionList.contains(ConditionProcessTaskOptions.STARTTIME)) {
            resultObj.put(ConditionProcessTaskOptions.STARTTIME.getValue(), processTaskVo.getStartTime());
        }
        if (optionList.contains(ConditionProcessTaskOptions.PRIORITY)) {
            resultObj.put(ConditionProcessTaskOptions.PRIORITY.getValue(), processTaskVo.getPriorityUuid());
        }
        String owner = processTaskVo.getOwner();
        if (optionList.contains(ConditionProcessTaskOptions.OWNER)) {
            resultObj.put(ConditionProcessTaskOptions.OWNER.getValue(), owner);
        }
        if (optionList.contains(ConditionProcessTaskOptions.OWNERLEVEL)) {
            UserVo ownerVo = userMapper.getUserBaseInfoByUuid(owner);
            if (ownerVo != null) {
                resultObj.put(ConditionProcessTaskOptions.OWNERLEVEL.getValue(), ownerVo.getVipLevel());
            }
        }
        if (optionList.contains(ConditionProcessTaskOptions.OWNERROLE)) {
            AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(owner);
            List<String> list = authenticationInfoVo.getRoleUuidList().stream().map(o -> GroupSearch.ROLE.getValuePlugin() + o).collect(Collectors.toList());
            resultObj.put(ConditionProcessTaskOptions.OWNERROLE.getValue(), list);
        }
        /** 上报人公司、部门列表 **/
        if (optionList.contains(ConditionProcessTaskOptions.OWNERDEPARTMENT) || optionList.contains(ConditionProcessTaskOptions.OWNERCOMPANY)) {
            List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(owner);
            if (CollectionUtils.isNotEmpty(teamUuidList)) {
                Set<String> upwardUuidSet = new HashSet<>();
                List<TeamVo> teamList = teamMapper.getTeamByUuidList(teamUuidList);
                for (TeamVo teamVo : teamList) {
                    String upwardUuidPath = teamVo.getUpwardUuidPath();
                    if (StringUtils.isNotBlank(upwardUuidPath)) {
                        String[] upwardUuidArray = upwardUuidPath.split(",");
                        for (String upwardUuid : upwardUuidArray) {
                            upwardUuidSet.add(upwardUuid);
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(upwardUuidSet)) {
                    List<TeamVo> upwardTeamList = teamMapper.getTeamByUuidList(new ArrayList<>(upwardUuidSet));
                    if (optionList.contains(ConditionProcessTaskOptions.OWNERDEPARTMENT)) {
                        List<String> departmentUuidList = new ArrayList<>();
                        for (TeamVo teamVo : upwardTeamList) {
                            if (TeamLevel.DEPARTMENT.getValue().equals(teamVo.getLevel())) {
                                departmentUuidList.add(teamVo.getUuid());
                            }
                        }
                        resultObj.put(ConditionProcessTaskOptions.OWNERDEPARTMENT.getValue(), departmentUuidList);
                    }
                    if (optionList.contains(ConditionProcessTaskOptions.OWNERCOMPANY)) {
                        List<String> companyUuidList = new ArrayList<>();
                        for (TeamVo teamVo : upwardTeamList) {
                            if (TeamLevel.COMPANY.getValue().equals(teamVo.getLevel())) {
                                companyUuidList.add(teamVo.getUuid());
                            }
                        }
                        resultObj.put(ConditionProcessTaskOptions.OWNERCOMPANY.getValue(), companyUuidList);
                    }
                }
            }
        }
        if (processTaskStepId != null) {
            if (optionList.contains(ConditionProcessTaskOptions.STEPID)) {
                resultObj.put(ConditionProcessTaskOptions.STEPID.getValue(), processTaskStepId);
            }
            if (optionList.contains(ConditionProcessTaskOptions.STEPTASK) || optionList.contains(ConditionProcessTaskOptions.STEPTASKID)) {
                ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
                if (processTaskStepVo != null && processTaskStepVo.getIsActive() == 1 && ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus())) {
                    processTaskStepTaskService.getProcessTaskStepTask(processTaskStepVo);
                    ProcessTaskStepTaskVo stepTaskVo = processTaskStepVo.getProcessTaskStepTaskVo();
                    if (stepTaskVo != null) {
                        if (optionList.contains(ConditionProcessTaskOptions.STEPTASK)) {
                            List<Long> dataList = Collections.singletonList(stepTaskVo.getTaskConfigId());
                            resultObj.put(ConditionProcessTaskOptions.STEPTASK.getValue(), dataList);
                        }
                        if (optionList.contains(ConditionProcessTaskOptions.STEPTASKID)) {
                            resultObj.put(ConditionProcessTaskOptions.STEPTASKID.getValue(), stepTaskVo.getId());
                        }
                    }
                }
            }
        }

        if (optionList.contains(ConditionProcessTaskOptions.ACTIONTRIGGERUSER)) {
            if (UserContext.get() != null) {
                resultObj.put(ConditionProcessTaskOptions.ACTIONTRIGGERUSER.getValue(), UserContext.get().getUserUuid());
            }
        }
        /** 表单信息数据 **/
        ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
        if (processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContentHash())) {
            String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
            if (StringUtils.isNotBlank(formContent)) {
                resultObj.put("formConfig", formContent);
                List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskId);
                for (ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                    resultObj.put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
                }
            }
        }
        return resultObj;
    }
}
