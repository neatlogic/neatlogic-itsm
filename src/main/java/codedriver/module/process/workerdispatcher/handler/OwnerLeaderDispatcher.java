package codedriver.module.process.workerdispatcher.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.TeamLevel;
import codedriver.framework.common.constvalue.TeamUserTitle;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workerdispatcher.core.WorkerDispatcherBase;
@Service
public class OwnerLeaderDispatcher extends WorkerDispatcherBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private TeamMapper teamMapper;

	@Override
	public String getName() {
		return "上报人领导分派器";
	}

	@Override
	public JSONArray getConfig() {
		JSONArray resultArray = new JSONArray();
		
		JSONObject teamUserTitleFilterConfigObj = new JSONObject();
		teamUserTitleFilterConfigObj.put("plugin", "teamUserTitleFilter");
		teamUserTitleFilterConfigObj.put("pluginName", "头衔");
		JSONObject teamUserTitleFilterPluginConfigObj = new JSONObject();
		teamUserTitleFilterPluginConfigObj.put("isMultiple", false);
		List<ValueTextVo> teamUserTitleFilterDataList = new ArrayList<>();
		for(TeamUserTitle title : TeamUserTitle.values()) {
			teamUserTitleFilterDataList.add(new ValueTextVo(title.getValue(), title.getText()));
		}
		teamUserTitleFilterPluginConfigObj.put("dataList", teamUserTitleFilterDataList);
		teamUserTitleFilterConfigObj.put("config",teamUserTitleFilterPluginConfigObj);
		resultArray.add(teamUserTitleFilterConfigObj);
		
		return resultArray;
	}

	@Override
	public String getHelp() {
		return "在处理人范围中";
	}

	@Override
	protected List<String> myGetWorker(ProcessTaskStepVo processTaskStepVo, JSONObject configObj) {
		List<String> resultList = new ArrayList<>();
		String teamUserTitleFilter = configObj.getString("teamUserTitleFilter");
		if(StringUtils.isNotBlank(teamUserTitleFilter)) {
			TeamLevel teamLevel = null;
			if(TeamUserTitle.GROUPLEADER.getValue().equals(teamUserTitleFilter)) {
				teamLevel = TeamLevel.GROUP;
			}else if(TeamUserTitle.COMPANYLEADER.getValue().equals(teamUserTitleFilter)) {
				teamLevel = TeamLevel.COMPANY;
			}else if(TeamUserTitle.CENTERLEADER.getValue().equals(teamUserTitleFilter)) {
				teamLevel = TeamLevel.CENTER;
			}else if(TeamUserTitle.DEPARTMENTLEADER.getValue().equals(teamUserTitleFilter)) {
				teamLevel = TeamLevel.DEPARTMENT;
			}else if(TeamUserTitle.TEAMLEADER.getValue().equals(teamUserTitleFilter)) {
				teamLevel = TeamLevel.TEAM;
			}else if(TeamUserTitle.GENERALSTAFF.getValue().equals(teamUserTitleFilter)) {
				teamLevel = TeamLevel.TEAM;
			}else {
				return resultList;
			}
			ProcessTaskVo processTask = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
			List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(processTask.getOwner());
			if(CollectionUtils.isNotEmpty(teamUuidList)) {
				List<TeamVo> teamList = teamMapper.getTeamByUuidList(teamUuidList);
				if(CollectionUtils.isNotEmpty(teamList)) {
					for(TeamVo teamVo : teamList) {
						List<String> userUuidList = teamMapper.getTeamUserUuidListByLftRhtLevelTitle(teamVo.getLft(), teamVo.getRht(), teamLevel.getValue(), teamUserTitleFilter);
						if(CollectionUtils.isNotEmpty(userUuidList)) {
							resultList.addAll(userUuidList);
						}
					}
				}
			}
		}
	
		return resultList;
	}

}
