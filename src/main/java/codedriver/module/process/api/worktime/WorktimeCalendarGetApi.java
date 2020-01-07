package codedriver.module.process.api.worktime;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.exception.WorktimeNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.WorktimeRangeVo;

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
		@Param(name = "year", type = ApiParamType.INTEGER, isRequired = true, desc = "年份")
	})
	@Output({
		@Param(name = "Return", type = ApiParamType.JSONARRAY, desc = "工作日历列表")
	})
	@Description(desc = "工作日历信息获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		WorktimeRangeVo worktimeDetailVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<WorktimeRangeVo>() {});
		if(worktimeMapper.checkWorktimeIsExists(worktimeDetailVo.getWorktimeUuid()) == 0) {
			throw new WorktimeNotFoundException(worktimeDetailVo.getWorktimeUuid());
		}
		List<String> worktimeDateList = worktimeMapper.getWorktimeDateList(worktimeDetailVo);
		return worktimeDateList;
	}

}
