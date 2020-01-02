package codedriver.module.process.api.worktime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.exception.WorktimeNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.WorktimeRangeVo;
import codedriver.module.process.dto.WorktimeVo;
@Service
@Transactional
public class WorktimeCalendarSaveApi extends ApiComponentBase {

	@Autowired
	private WorktimeMapper worktimeMapper;
	
	@Override
	public String getToken() {
		return "process/worktime/calendar/save";
	}

	@Override
	public String getName() {
		return "工作日历信息保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "worktimeUuid", type = ApiParamType.STRING, isRequired = true, desc = "工作时间窗口uuid"),
		@Param(name = "year", type = ApiParamType.INTEGER, isRequired = true, desc = "年份"),
		@Param(name = "dateList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "工作日历列表")
	})
	@Description(desc = "工作日历信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {	
		WorktimeRangeVo worktimeRangeVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<WorktimeRangeVo>() {});
		String worktimeUuid = worktimeRangeVo.getWorktimeUuid();
		WorktimeVo worktimeVo = worktimeMapper.getWorktimeByUuid(worktimeUuid);
		if(worktimeVo == null) {
			throw new WorktimeNotFoundException(worktimeUuid);
		}

		JSONObject config = JSON.parseObject(worktimeVo.getConfig());
		worktimeMapper.deleteWorktimeRange(worktimeRangeVo);
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
		Calendar calendar = Calendar.getInstance();
		WorktimeRangeVo worktimeRange = null;
		JSONArray defineList = null;
		List<WorktimeRangeVo> worktimeRangeList = new ArrayList<>();
		List<String> dateList = worktimeRangeVo.getDateList();
		for(String workDate : dateList) {
			simpleDateFormat.applyPattern("yyyy-MM-dd");
			Date date = simpleDateFormat.parse(workDate);
			calendar.setTime(date);
			int weekday = calendar.get(Calendar.DAY_OF_WEEK);
			if(weekday == 1) {
				weekday = 7;
			}else {
				weekday -= 1;
			}
			defineList = config.getJSONArray(String.valueOf(weekday));
			if(defineList == null) {
				continue;
			}
			simpleDateFormat.applyPattern("yyyy-MM-dd HH:mm");
			for(int i = 0; i < defineList.size(); i++) {
				JSONObject define = defineList.getJSONObject(i);
				worktimeRange = new WorktimeRangeVo();
				worktimeRange.setWorktimeUuid(worktimeUuid);
				worktimeRange.setYear(worktimeRangeVo.getYear());
				worktimeRange.setDate(workDate);			
				worktimeRange.setStartTime(simpleDateFormat.parse(workDate + " " + define.getString("startTime")).getTime());
				worktimeRange.setEndTime(simpleDateFormat.parse(workDate + " " + define.getString("endTime")).getTime());
				worktimeRangeList.add(worktimeRange);
				if(worktimeRangeList.size() > 1000) {
					worktimeMapper.insertBatchWorktimeRange(worktimeRangeList);
					worktimeRangeList.clear();
				}
			}	
		}
		worktimeMapper.insertBatchWorktimeRange(worktimeRangeList);
		return null;
	}

}
