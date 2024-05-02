package neatlogic.module.process.workerdispatcher.handler;

import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.TeamUserTitleVo;
import neatlogic.framework.dto.TeamVo;
import neatlogic.framework.dto.UserTitleVo;
import neatlogic.framework.exception.team.TeamUserTitleNotFoundException;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workerdispatcher.core.WorkerDispatcherBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class OwnerLeaderDispatcher extends WorkerDispatcherBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getName() {
        return "nmpwh.ownerleaderdispatcher.getname";
    }

    @Override
    public JSONArray getConfig() {
        JSONArray resultArray = new JSONArray();
        /** 选择头衔 **/
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("type", "select");
        jsonObj.put("name", "teamUserTitle");
        jsonObj.put("search", true);
        jsonObj.put("dynamicUrl", "api/rest/user/title/search");
        jsonObj.put("label", "职务");
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
        return "在上报人所在的组及父组中，找出与选择头衔相同的用户作为当前步骤的处理人";
    }

    @Override
    protected List<ProcessTaskStepWorkerVo> myGetWorker(ProcessTaskStepVo processTaskStepVo, JSONObject configObj) {
        List<ProcessTaskStepWorkerVo> resultList = new ArrayList<>();
        String teamUserTitle = configObj.getString("teamUserTitle");
        if (StringUtils.isNotBlank(teamUserTitle)) {
            UserTitleVo userTitleVo = userMapper.getUserTitleByName(teamUserTitle);
            if (userTitleVo == null) {
                throw new TeamUserTitleNotFoundException(teamUserTitle);
            }
            ProcessTaskVo processTask = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
            List<TeamVo> teamList = teamMapper.getTeamListByUserUuid(processTask.getOwner());
            if (CollectionUtils.isNotEmpty(teamList)) {
                //循环穿透当前用户所在分组列表，找到第一个符合的头衔的用户s
                for (TeamVo teamVo : teamList) {
                    List<TeamUserTitleVo> teamUserTitleVoList = teamMapper.getTeamUserTitleListByTeamlrAndTitleId(teamVo.getLft(), teamVo.getRht(), userTitleVo.getId());
                    if (CollectionUtils.isNotEmpty(teamUserTitleVoList)) {
                        List<String> userUuidList = teamUserTitleVoList.get(0).getUserList();
                        for (String userUuid : userUuidList) {
                            ProcessTaskStepWorkerVo worker = new ProcessTaskStepWorkerVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), GroupSearch.USER.getValue(), userUuid, ProcessUserType.MAJOR.getValue());
                            resultList.add(worker);
                        }
                        break;
                    }
                }
            }
        }
        return resultList;
    }

}
