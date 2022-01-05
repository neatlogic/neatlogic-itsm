package codedriver.module.process.workerdispatcher.handler;

import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.TeamUserTitleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserTitleVo;
import codedriver.framework.exception.team.TeamUserTitleNotFoundException;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepUserStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.workerdispatcher.core.WorkerDispatcherBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class HandlerLeaderDispatcher extends WorkerDispatcherBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getName() {
        return "处理人领导分派器";
    }

    @Override
    public JSONArray getConfig() {
        JSONArray resultArray = new JSONArray();
        /** 前置步骤 **/
        JSONObject preStepJsonObj = new JSONObject();
        preStepJsonObj.put("type", "select");
        preStepJsonObj.put("name", "preStepList");
        preStepJsonObj.put("label", "前置步骤");
        preStepJsonObj.put("validateList", Collections.singletonList("required"));
        preStepJsonObj.put("multiple", true);
        preStepJsonObj.put("policy", "preStepList");
        resultArray.add(preStepJsonObj);
        /** 选择头衔 **/
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("type", "select");
        jsonObj.put("name", "teamUserTitle");
        jsonObj.put("search", true);
        jsonObj.put("dynamicUrl", "api/rest/user/title/search");
        jsonObj.put("label", "头衔");
        jsonObj.put("validateList", Collections.singletonList("required"));
        jsonObj.put("multiple", false);
        jsonObj.put("textName", "name");
        jsonObj.put("valueName", "name");
        jsonObj.put("rootName", "tbodyList");
        jsonObj.put("value", "");
        jsonObj.put("defaultValue", "");
        resultArray.add(jsonObj);
        return resultArray;
    }

    @Override
    public String getHelp() {
        return "在前置步骤处理人所在的组及父组中，找出第一个与选择头衔相同的用户作为当前步骤的处理人";
    }

    @Override
    protected List<String> myGetWorker(ProcessTaskStepVo processTaskStepVo, JSONObject configObj) {
        List<String> resultList = new ArrayList<>();
        if (MapUtils.isNotEmpty(configObj)) {
            JSONArray preStepArray = configObj.getJSONArray("preStepList");
            String teamUserTitle = configObj.getString("teamUserTitle");
            if (StringUtils.isNotBlank(teamUserTitle) && CollectionUtils.isNotEmpty(preStepArray)) {
                UserTitleVo userTitleVo = userMapper.getUserTitleByName(teamUserTitle);
                if (userTitleVo == null) {
                    throw new TeamUserTitleNotFoundException(teamUserTitle);
                }
                /* 找出该工单所有前置步骤已完成的处理人的所在组s */
                List<ProcessTaskStepVo> processTaskStepVoList = processTaskMapper.getProcessTaskStepListByProcessTaskIdAndProcessStepUuidList(processTaskStepVo.getProcessTaskId(), preStepArray.stream().map(Object::toString).collect(Collectors.toList()));
                processTaskStepVoList = processTaskStepVoList.stream().filter(o->Objects.equals(o.getStatus(),ProcessTaskStatus.SUCCEED.getValue())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(processTaskStepVoList)) {
                    List<ProcessTaskStepUserVo> userVos = processTaskMapper.getProcessTaskStepUserByStepIdList(processTaskStepVoList.stream().map(ProcessTaskStepVo::getId).collect(Collectors.toList()), ProcessUserType.MAJOR.getValue());
                    userVos = userVos.stream().filter(o -> Objects.equals(ProcessTaskStepUserStatus.DONE.getValue(), o.getStatus())).collect(Collectors.toList());
                    List<TeamVo> teamList = teamMapper.getTeamListByUserUuidList(userVos.stream().map(ProcessTaskStepUserVo::getUserUuid).collect(Collectors.toList()));
                    if (CollectionUtils.isNotEmpty(teamList)) {
                        /* 循环穿透当前用户所在分组列表，找到第一个符合的头衔的用户s */
                        for (TeamVo teamVo : teamList) {
                            List<TeamUserTitleVo> teamUserTitleVoList = teamMapper.getTeamUserTitleListByTeamlrAndTitleId(teamVo.getLft(), teamVo.getRht(), userTitleVo.getId());
                            if (CollectionUtils.isNotEmpty(teamUserTitleVoList)) {
                                resultList.addAll(teamUserTitleVoList.get(0).getUserList());
                                break;
                            }
                        }
                    }
                }
            }
        }
        return resultList;
    }

}
