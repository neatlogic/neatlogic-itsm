package codedriver.framework.process.workerdispatcher.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.workerdispatcher.WorkerDispatcherBase;

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
	public String getConfigPage() {
		return "process.workerdispatcher.handler.teamleader";
	}

}
