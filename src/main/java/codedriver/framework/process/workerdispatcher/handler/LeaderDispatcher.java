package codedriver.framework.process.workerdispatcher.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.workerdispatcher.core.WorkerDispatcherBase;
import codedriver.module.process.dto.ProcessTaskStepVo;

@Service
public class LeaderDispatcher extends WorkerDispatcherBase {
	@Autowired
	private UserMapper userMapper;

	@Autowired
	private TeamMapper teamMapper;

	@Override
	public String getHandler() {
		return this.getClass().getName();
	}

	@Override
	public String getName() {
		return "分组领导分派器";
	}

	@Override
	protected List<String> myGetWorker(ProcessTaskStepVo processTaskStepVo, JSONObject configObj) {
		if (configObj != null && configObj.containsKey("value")) {
			String teamUuid = configObj.getString("value");
			if (StringUtils.isNotBlank(teamUuid)) {
				List<String> teamIdList = new ArrayList<>();
				teamIdList.add(teamUuid);
				return userMapper.getLeaderUserIdByTeamIds(teamIdList);
			}
		}
		return null;
	}

	@Override
	public String getHelp() {
		return "在处理人范围中";
	}

	@Override
	public JSONArray getConfig() {
		JSONArray resultArray = new JSONArray();
		JSONObject configObj = new JSONObject();
		configObj.put("plugin", "teamFilter");
		configObj.put("pluginName", "处理组");
		JSONObject pluginConfigObj = new JSONObject();
		pluginConfigObj.put("isMultiple", false);
		configObj.put("config",pluginConfigObj);
		resultArray.add(configObj);
		return resultArray;
	}

}
