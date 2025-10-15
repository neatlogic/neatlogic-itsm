package neatlogic.module.process.workerpolicy.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.RoleMapper;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.RoleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.form.attribute.core.FormAttributeDataConversionHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeDataConversionHandler;
import neatlogic.framework.form.constvalue.FormHandler;
import neatlogic.framework.form.dto.AttributeDataVo;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.constvalue.WorkerPolicy;
import neatlogic.framework.process.dto.ProcessTaskFormAttributeDataVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.workerpolicy.core.IWorkerPolicyHandler;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FormWorkerPolicyHandler implements IWorkerPolicyHandler {

    @Override
    public String getType() {
        return WorkerPolicy.FORM.getValue();
    }

    @Override
    public String getName() {
        return WorkerPolicy.FORM.getText();
    }

    @Override
    public int isOnlyOnceExecute() {
        return 0;
    }

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private RoleMapper roleMapper;
    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public List<ProcessTaskStepWorkerVo> execute(ProcessTaskStepWorkerPolicyVo workerPolicyVo,
                                                 ProcessTaskStepVo currentProcessTaskStepVo) {
        List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = new ArrayList<>();
        if (MapUtils.isNotEmpty(workerPolicyVo.getConfigObj())) {
            /* 选择的表单属性uuid */
            JSONArray attributeUuidArray = workerPolicyVo.getConfigObj().getJSONArray("attributeUuidList");
            if (CollectionUtils.isNotEmpty(attributeUuidArray)) {
                List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskService.getProcessTaskFormAttributeDataListByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
                Map<String, ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataMap = processTaskFormAttributeDataList.stream().collect(Collectors.toMap(AttributeDataVo::getAttributeUuid, e -> e));
                List<FormAttributeVo> formAttributeList = processTaskService.getFormAttributeListByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
                Map<String, FormAttributeVo> formAttributeMap = formAttributeList.stream().collect(Collectors.toMap(FormAttributeVo::getUuid, e -> e));
                List<String> attributeUuidList = attributeUuidArray.toJavaList(String.class);
                for (String attributeUuid : attributeUuidList) {
                    FormAttributeVo formAttributeVo = formAttributeMap.get(attributeUuid);
                    if (formAttributeVo == null) {
                        continue;
                    }
                    ProcessTaskFormAttributeDataVo processTaskFormAttributeData = processTaskFormAttributeDataMap.get(attributeUuid);
                    if (processTaskFormAttributeData == null) {
                        continue;
                    }
                    Object dataObj = processTaskFormAttributeData.getDataObj();
                    if (dataObj == null) {
                        continue;
                    }
                    /* 只有表单属性类型为用户选择器才生效 */
                    if (FormHandler.FORMUSERSELECT.getHandler().equals(processTaskFormAttributeData.getHandler())) {
                        IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(FormHandler.FORMUSERSELECT.getHandler());
                        if (handler != null) {
                            JSONObject detailedData = handler.getDetailedData(processTaskFormAttributeData, formAttributeVo.getConfig());
                            JSONArray valueList = detailedData.getJSONArray("valueList");
                            if (CollectionUtils.isNotEmpty(valueList)) {
                                List<String> dataList = valueList.toJavaList(String.class);
                                for (String value : dataList) {
                                    /* 校验属性值是否合法，只有是当前存在的用户、组、角色才合法 */
                                    if (value.contains("#")) {
                                        String[] split = value.split("#");
                                        List<ProcessTaskStepWorkerVo> list = generateProcessTaskStepWorkerVo(split[1], split[0], currentProcessTaskStepVo);
                                        processTaskStepWorkerList.addAll(list);
                                    }
                                }
                            }
                        }
                    } else if (FormHandler.FORMSELECT.getHandler().equals(processTaskFormAttributeData.getHandler())) {
                        IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(FormHandler.FORMSELECT.getHandler());
                        if (handler != null) {
                            JSONObject detailedData = handler.getDetailedData(processTaskFormAttributeData, formAttributeVo.getConfig());
                            JSONArray valueList = detailedData.getJSONArray("valueList");
                            if (CollectionUtils.isNotEmpty(valueList)) {
                                List<String> dataList = valueList.toJavaList(String.class);
                                for (String value : dataList) {
                                    if (StringUtils.isNotBlank(value)) {
                                        List<ProcessTaskStepWorkerVo> list = generateProcessTaskStepWorkerVo(value, null, currentProcessTaskStepVo);
                                        processTaskStepWorkerList.addAll(list);
                                    }
                                }
                            }
                        }
                    } else if (Objects.equals(formAttributeVo.getHandler(), FormHandler.FORMTEXT.getHandler())) {
                        List<ProcessTaskStepWorkerVo> list = generateProcessTaskStepWorkerVo(dataObj.toString(), null, currentProcessTaskStepVo);
                        processTaskStepWorkerList.addAll(list);
                    }
                }
            }
        }
        return processTaskStepWorkerList;
    }

    private List<ProcessTaskStepWorkerVo> generateProcessTaskStepWorkerVo(String value, String type, ProcessTaskStepVo currentProcessTaskStepVo) {
        List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = new ArrayList<>();
        if (StringUtils.isNotBlank(type)) {
            if (Objects.equals(type, GroupSearch.USER.getValue())) {
                List<UserVo> userList = searchUserList(value);
                for (UserVo user : userList) {
                    processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(
                            currentProcessTaskStepVo.getProcessTaskId(),
                            currentProcessTaskStepVo.getId(),
                            GroupSearch.USER.getValue(),
                            user.getUuid(),
                            ProcessUserType.MAJOR.getValue())
                    );
                }
            } else if (Objects.equals(type, GroupSearch.TEAM.getValue())) {
                List<TeamVo> teamList = searchTeamList(value);
                for (TeamVo teamVo : teamList) {
                    processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(),
                            currentProcessTaskStepVo.getId(), GroupSearch.TEAM.getValue(),
                            teamVo.getUuid(), ProcessUserType.MAJOR.getValue()));
                }
            } else if (Objects.equals(type, GroupSearch.ROLE.getValue())) {
                RoleVo roleVo = searchRole(value);
                if (roleVo != null) {
                    processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(
                            currentProcessTaskStepVo.getProcessTaskId(),
                            currentProcessTaskStepVo.getId(),
                            GroupSearch.ROLE.getValue(),
                            roleVo.getUuid(),
                            ProcessUserType.MAJOR.getValue()
                    ));
                }
            }
        } else {
            List<UserVo> userList = searchUserList(value);
            if (CollectionUtils.isNotEmpty(userList)) {
                for (UserVo user : userList) {
                    processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(
                            currentProcessTaskStepVo.getProcessTaskId(),
                            currentProcessTaskStepVo.getId(),
                            GroupSearch.USER.getValue(),
                            user.getUuid(),
                            ProcessUserType.MAJOR.getValue())
                    );
                }
            } else {
                List<TeamVo> teamList = searchTeamList(value);
                if (CollectionUtils.isNotEmpty(teamList)) {
                    for (TeamVo teamVo : teamList) {
                        processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(),
                                currentProcessTaskStepVo.getId(), GroupSearch.TEAM.getValue(),
                                teamVo.getUuid(), ProcessUserType.MAJOR.getValue()));
                    }
                } else {
                    RoleVo roleVo = searchRole(value);
                    if (roleVo != null) {
                        processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(
                                currentProcessTaskStepVo.getProcessTaskId(),
                                currentProcessTaskStepVo.getId(),
                                GroupSearch.ROLE.getValue(),
                                roleVo.getUuid(),
                                ProcessUserType.MAJOR.getValue()
                        ));
                    }
                }
            }
        }
        return processTaskStepWorkerList;
    }

    private List<UserVo> searchUserList(String value) {
        List<UserVo> userList = new ArrayList<>();
        UserVo userVo = userMapper.getUserByUser(value);
        if (userVo != null) {
            if (Objects.equals(userVo.getIsActive(), 1)) {
                userList.add(userVo);
            }
        } else {
            List<String> userUuidList = userMapper.getUserUuidListByUserName(value);
            if (CollectionUtils.isNotEmpty(userUuidList)) {
                List<UserVo> list = userMapper.getUserByUserUuidList(userUuidList);
                if (CollectionUtils.isNotEmpty(list)) {
                    for (UserVo user : list) {
                        if (user != null && Objects.equals(user.getIsActive(), 1)) {
                            userList.add(user);

                        }
                    }
                }
            }
        }
        return userList;
    }

    private List<TeamVo> searchTeamList(String value) {
        List<TeamVo> teamList = new ArrayList<>();
        TeamVo teamVo = teamMapper.getTeamByUuid(value);
        if (teamVo != null) {
            teamList.add(teamVo);
        } else {
            List<String> teamUuidList = teamMapper.getTeamUuidByName(value);
            if (CollectionUtils.isNotEmpty(teamUuidList)) {
                List<TeamVo> list = teamMapper.getTeamByUuidList(teamUuidList);
                teamList.addAll(list);
            }
        }
        return teamList;
    }

    private RoleVo searchRole(String value) {
        RoleVo roleVo = roleMapper.getRoleSimpleInfoByUuid(value);
        if (roleVo != null && Objects.equals(roleVo.getIsDelete(), 0)) {
            return roleVo;
        } else {
            RoleVo role = roleMapper.getRoleByName(value);
            if (role != null && Objects.equals(role.getIsDelete(), 0)) {
                return role;
            }
        }
        return null;
    }
}
