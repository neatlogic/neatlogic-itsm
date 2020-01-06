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
		
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		WorktimeRangeVo worktimeRange = null;
		JSONArray defineList = null;
		List<WorktimeRangeVo> worktimeRangeList = new ArrayList<>();
		List<String> dateList = worktimeRangeVo.getDateList();
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
		worktimeMapper.insertBatchWorktimeRange(worktimeRangeList);
		return null;
	}
}
