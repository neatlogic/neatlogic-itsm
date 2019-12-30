package codedriver.module.process.api.worktime;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.exception.WorktimeNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class WorktimeCalendarGetApi extends ApiComponentBase {

	@Autowired
	private WorktimeMapper worktimeMapper;
	
	@Override
	public String getToken() {
		return "worktime/calendar/get";
	}

	@Override
	public String getName() {
		return "工作日历信息获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "worktimeUuid", type = ApiParamType.STRING, isRequired = true, desc = "工作时间窗口uuid"),
		@Param(name = "workYear", type = ApiParamType.INTEGER, isRequired = true, desc = "年份")
	})
	@Output({
		@Param(name = "Return", type = ApiParamType.JSONARRAY, desc = "工作日历列表")
	})
	@Description(desc = "工作日历信息获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String worktimeUuid = jsonObj.getString("worktimeUuid");
		if(worktimeMapper.checkWorktimeIsExists(worktimeUuid) == 0) {
			throw new WorktimeNotFoundException(worktimeUuid);
		}
		Integer workYear = jsonObj.getInteger("workYear");
		List<String> worktimeDateList = worktimeMapper.getWorktimeDateList(worktimeUuid, workYear);
		return worktimeDateList;
	}

}
