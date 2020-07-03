package codedriver.module.process.workerdispatcher.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.TeamLevel;
import codedriver.framework.common.constvalue.TeamUserTitle;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.workerdispatcher.core.WorkerDispatcherBase;

@Service
public class LeaderDispatcher extends WorkerDispatcherBase {
	
	@Autowired
	private TeamMapper teamMapper;

	@Override
	public String getName() {
		return "分组领导分派器";
	}

	@Override
	protected List<String> myGetWorker(ProcessTaskStepVo processTaskStepVo, JSONObject configObj) {
		List<String> resultList = new ArrayList<>();
		if(MapUtils.isNotEmpty(configObj)) {
			String team = configObj.getString("team");
			String teamUserTitle = configObj.getString("teamUserTitle");
			if(StringUtils.isNotBlank(teamUserTitle) && StringUtils.isNotBlank(team) && team.startsWith(GroupSearch.TEAM.getValuePlugin())) {
				TeamLevel teamLevel = null;
				if(TeamUserTitle.GROUPLEADER.getValue().equals(teamUserTitle)) {
					teamLevel = TeamLevel.GROUP;
				}else if(TeamUserTitle.COMPANYLEADER.getValue().equals(teamUserTitle)) {
					teamLevel = TeamLevel.COMPANY;
				}else if(TeamUserTitle.CENTERLEADER.getValue().equals(teamUserTitle)) {
					teamLevel = TeamLevel.CENTER;
				}else if(TeamUserTitle.DEPARTMENTLEADER.getValue().equals(teamUserTitle)) {
					teamLevel = TeamLevel.DEPARTMENT;
				}else if(TeamUserTitle.TEAMLEADER.getValue().equals(teamUserTitle)) {
					teamLevel = TeamLevel.TEAM;
				}else {
					return resultList;
				}
				String[] split = team.split("#");
				TeamVo teamVo = teamMapper.getTeamByUuid(split[1]);
				if(teamVo != null) {					
					List<String> userUuidList = teamMapper.getTeamUserUuidListByLftRhtLevelTitle(teamVo.getLft(), teamVo.getRht(), teamLevel.getValue(), teamUserTitle);
					if(CollectionUtils.isNotEmpty(userUuidList)) {
						resultList.addAll(userUuidList);
					}
				}
			}
		}
		return resultList;
	}

	@Override
	public String getHelp() {
		return "在处理人范围中";
	}

	@Override
	public JSONArray getConfig() {
		JSONArray resultArray = new JSONArray();
		/** 选择处理组 **/
		{
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("type", "userselect");
			jsonObj.put("name", "team");
			jsonObj.put("label", "处理组");
			jsonObj.put("validateList", Arrays.asList("required"));
			jsonObj.put("multiple", false);
			jsonObj.put("value", "");
			jsonObj.put("defaultValue", "");
			jsonObj.put("groupList", Arrays.asList("team"));
			resultArray.add(jsonObj);
		}
		/** 选择头衔 **/
		{
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("type", "select");
			jsonObj.put("name", "teamUserTitle");
			jsonObj.put("search", false);
			jsonObj.put("label", "头衔");
			jsonObj.put("validateList", Arrays.asList("required"));
			jsonObj.put("multiple", false);
			jsonObj.put("value", "");
			jsonObj.put("defaultValue", "");
			List<ValueTextVo> dataList = new ArrayList<>();
			for(TeamUserTitle title : TeamUserTitle.values()) {
				dataList.add(new ValueTextVo(title.getValue(), title.getText()));
			}
			jsonObj.put("dataList", dataList);
			resultArray.add(jsonObj);
		}
		return resultArray;
	}

}
