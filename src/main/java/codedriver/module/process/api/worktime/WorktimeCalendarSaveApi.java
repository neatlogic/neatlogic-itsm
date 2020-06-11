package codedriver.module.process.api.worktime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.WorktimeRangeVo;
import codedriver.framework.process.dto.WorktimeVo;
import codedriver.framework.process.exception.worktime.WorktimeNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
public class WorktimeCalendarSaveApi extends ApiComponentBase {

	@Autowired
	private WorktimeMapper worktimeMapper;
	
	@Override
	public String getToken() {
		return "worktime/calendar/save";
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
		@Param(name = "calendarList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "工作日历列表")
	})
	@Description(desc = "工作日历信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String worktimeUuid = jsonObj.getString("worktimeUuid");
		WorktimeVo worktimeVo = worktimeMapper.getWorktimeByUuid(worktimeUuid);
		if(worktimeVo == null) {
			throw new WorktimeNotFoundException(worktimeUuid);
		}
		JSONObject config = JSON.parseObject(worktimeVo.getConfig());
		
		WorktimeRangeVo worktimeRangeVo = new WorktimeRangeVo();
		worktimeRangeVo.setWorktimeUuid(worktimeUuid);
		Integer year = jsonObj.getInteger("year");
		worktimeRangeVo.setYear(year);
		worktimeMapper.deleteWorktimeRange(worktimeRangeVo);
		
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		WorktimeRangeVo worktimeRange = null;
		JSONArray defineList = null;
		List<WorktimeRangeVo> worktimeRangeList = new ArrayList<>();
		JSONArray calendarList = jsonObj.getJSONArray("calendarList");
		List<String> dateList = generateDateList(calendarList);
		for(String workDate : dateList) {
			LocalDate localDate= LocalDate.from(dateFormatter.parse(workDate));
			defineList = config.getJSONArray(localDate.getDayOfWeek().name().toLowerCase());
			if(defineList == null) {
				continue;
			}

			for(int i = 0; i < defineList.size(); i++) {
				JSONObject define = defineList.getJSONObject(i);
				worktimeRange = new WorktimeRangeVo();
				worktimeRange.setWorktimeUuid(worktimeUuid);
				worktimeRange.setYear(worktimeRangeVo.getYear());
				worktimeRange.setDate(workDate);
				LocalDateTime startLocalDateTime = LocalDateTime.from(dateTimeFormatter.parse(workDate + " " + define.getString("startTime")));
				LocalDateTime endLocalDateTime = LocalDateTime.from(dateTimeFormatter.parse(workDate + " " + define.getString("endTime")));
				long startTime = startLocalDateTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
				long endTime = endLocalDateTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
				worktimeRange.setStartTime(startTime);
				worktimeRange.setEndTime(endTime);
				worktimeRangeList.add(worktimeRange);
				if(worktimeRangeList.size() > 1000) {
					worktimeMapper.insertBatchWorktimeRange(worktimeRangeList);
					worktimeRangeList.clear();
				}
			}	
		}
		if(worktimeRangeList.size() > 0) {
			worktimeMapper.insertBatchWorktimeRange(worktimeRangeList);
		}
		return null;
	}

	private List<String> generateDateList(JSONArray calendarList) {
		List<String> resultList = new ArrayList<>();
	
		if(calendarList == null || calendarList.isEmpty()) {
			return resultList;
		}
		for(int i = 0; i < calendarList.size(); i++) {
			JSONObject trObj = calendarList.getJSONObject(i);
			if(trObj == null || trObj.isEmpty()) {
				continue;
			}
			JSONArray monthList = trObj.getJSONArray("monthList");
			if(monthList == null || monthList.isEmpty()) {
				continue;
			}
			for(int j = 0; j < monthList.size(); j++) {
				JSONObject monthObj = monthList.getJSONObject(j);
				if(monthObj == null || monthObj.isEmpty()) {
					continue;
				}
				JSONArray dateList = monthObj.getJSONArray("dateList");
				if(dateList == null || dateList.isEmpty()) {
					continue;
				}
				for(int k = 0; k < dateList.size(); k++) {
					JSONObject dateObj = dateList.getJSONObject(k);
					if(dateObj == null || dateObj.isEmpty()) {
						continue;
					}

					int classType = dateObj.getIntValue("classType");
					if(classType > 3) {//被选中的日期
						resultList.add(dateObj.getString("name"));
					}
				}
			}
		}
		return resultList;
	}
}
