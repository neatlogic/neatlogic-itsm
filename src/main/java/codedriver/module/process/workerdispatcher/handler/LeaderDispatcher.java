package codedriver.module.process.workerdispatcher.handler;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamUserTitleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.workerdispatcher.core.WorkerDispatcherBase;
import codedriver.framework.process.workerdispatcher.core.WorkerDispatcherForm;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaderDispatcher extends WorkerDispatcherBase {

    @Resource
    private TeamMapper teamMapper;

    @Override
    public String getName() {
        return "分组领导分派器";
    }

    @Override
    protected List<String> myGetWorker(ProcessTaskStepVo processTaskStepVo, JSONObject configObj) {
        List<String> resultList = new ArrayList<>();
        if (MapUtils.isNotEmpty(configObj)) {
            String team = configObj.getString("team");
            String teamUserTitle = configObj.getString("teamUserTitle");
            if (StringUtils.isNotBlank(teamUserTitle) && StringUtils.isNotBlank(team) && team.startsWith(GroupSearch.TEAM.getValuePlugin())) {
//				TeamLevel teamLevel = null;
//				if(TeamUserTitle.GROUPLEADER.getValue().equals(teamUserTitle)) {
//					teamLevel = TeamLevel.GROUP;
//				}else if(TeamUserTitle.COMPANYLEADER.getValue().equals(teamUserTitle)) {
//					teamLevel = TeamLevel.COMPANY;
//				}else if(TeamUserTitle.CENTERLEADER.getValue().equals(teamUserTitle)) {
//					teamLevel = TeamLevel.CENTER;
//				}else if(TeamUserTitle.DEPARTMENTLEADER.getValue().equals(teamUserTitle)) {
//					teamLevel = TeamLevel.DEPARTMENT;
//				}else if(TeamUserTitle.TEAMLEADER.getValue().equals(teamUserTitle)) {
//					teamLevel = TeamLevel.TEAM;
//				}else {
//					return resultList;
//				}
                String[] split = team.split("#");
                TeamVo teamVo = teamMapper.getTeamByUuid(split[1]);
                if (teamVo != null) {
//					List<String> userUuidList = teamMapper.getTeamUserUuidListByLftRhtLevelTitle(teamVo.getLft(), teamVo.getRht(), teamLevel.getValue(), teamUserTitle);
					/*List<String> userUuidList = teamMapper.getTeamUserUuidListByLftRhtTitle(teamVo.getLft(), teamVo.getRht(), teamUserTitle);
					if(CollectionUtils.isNotEmpty(userUuidList)) {
						resultList.addAll(userUuidList);
					}*/
                    List<TeamUserTitleVo> teamUserTileList = teamMapper.getTeamUserTitleListByTeamUuid(teamVo.getUuid());
                    //List<String> userUuidList = teamMapper.getTeamUserUuidListByLftRhtTitle(teamVo.getLft(), teamVo.getRht(), teamUserTitle);
                    if (CollectionUtils.isNotEmpty(teamUserTileList)) {
                        teamUserTileList = teamUserTileList.stream().filter(o -> Objects.equals(o.getTitle(), teamUserTitle)).collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(teamUserTileList)) {
                            resultList.addAll(teamUserTileList.get(0).getUserList());
                        }
                    }
                }
            }
        }
        return resultList;
    }

    @Override
    public String getHelp() {
        return "在选择的组及父级组中，找出与选择头衔相同的用户作为当前步骤的处理人";
    }

    @Override
    public JSONArray getConfig() {
        JSONArray resultArray = new JSONArray();
        /** 选择处理组 **/
        {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("type", WorkerDispatcherForm.USER_SELECT.getValue());
            jsonObj.put("name", "team");
            jsonObj.put("label", "处理组");
            jsonObj.put("validateList", Collections.singletonList("required"));
            jsonObj.put("multiple", false);
            jsonObj.put("value", "");
            jsonObj.put("defaultValue", "");
            jsonObj.put("groupList", Collections.singletonList("team"));
            resultArray.add(jsonObj);
        }
        /** 选择头衔 **/
        {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("type", WorkerDispatcherForm.SELECT.getValue());
            jsonObj.put("name", "teamUserTitle");
            jsonObj.put("search", true);
            jsonObj.put("dynamicUrl", "api/rest/user/title/search");
            jsonObj.put("label", "头衔");
            jsonObj.put("validateList", Collections.singletonList("required"));
            jsonObj.put("multiple", false);
            jsonObj.put("value", "");
            jsonObj.put("textName", "name");
            jsonObj.put("valueName", "name");
            jsonObj.put("rootName", "tbodyList");
            jsonObj.put("defaultValue", "");
            resultArray.add(jsonObj);
        }
        return resultArray;
    }

}
