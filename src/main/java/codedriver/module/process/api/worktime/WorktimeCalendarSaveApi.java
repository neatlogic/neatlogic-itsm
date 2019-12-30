package codedriver.module.process.api.worktime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.exception.WorktimeNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.WorktimeDefineVo;
import codedriver.module.process.dto.WorktimeDetailVo;
import codedriver.module.process.dto.WorktimeVo;
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
		@Param(name = "workYear", type = ApiParamType.INTEGER, desc = "年份"),
		@Param(name = "workDateList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "工作日历列表")
	})
	@Description(desc = "工作日历信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {	
		WorktimeDetailVo worktimeDetailVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<WorktimeDetailVo>() {});
		String worktimeUuid = worktimeDetailVo.getWorktimeUuid();
		WorktimeVo worktimeVo = worktimeMapper.getWorktimeByUuid(worktimeUuid);
		if(worktimeVo == null) {
			throw new WorktimeNotFoundException(worktimeUuid);
		}
		Map<Integer, List<WorktimeDefineVo>> weekdayDefineMap = new HashMap<>();
		List<WorktimeDefineVo> defineList = null;	
		List<WorktimeDefineVo> worktimeDefineList = worktimeVo.getWorktimeDefineList();
		for(WorktimeDefineVo worktimeDefine : worktimeDefineList) {
			Integer weekday = worktimeDefine.getWeekday();			
			defineList = weekdayDefineMap.get(weekday);
			if(defineList == null) {
				defineList = new ArrayList<>();
			}
			defineList.add(worktimeDefine);
		}
		
		Integer workYear = worktimeDetailVo.getWorkYear();
		worktimeMapper.deleteWorktimeDetail(worktimeUuid, workYear);
		
		SimpleDateFormat sdf = new SimpleDateFormat();
		Calendar calendar = Calendar.getInstance();
		WorktimeDetailVo worktimeDetail = null;
		List<WorktimeDetailVo> worktimeDetailList = new ArrayList<>();
		List<String> workDateList = worktimeDetailVo.getWorkDateList();
		for(String workDate : workDateList) {
			sdf.applyPattern("yyyy-MM-dd");
			Date date = sdf.parse(workDate);
			calendar.setTime(date);
			int weekday = calendar.get(Calendar.DAY_OF_WEEK);
			if(weekday == 1) {
				weekday = 7;
			}else {
				weekday -= 1;
			}
			defineList = weekdayDefineMap.get(weekday);
			sdf.applyPattern("yyyy-MM-dd H:mm");
			for(WorktimeDefineVo worktimeDefine : defineList) {
				worktimeDetail = new WorktimeDetailVo();
				worktimeDetail.setWorktimeUuid(worktimeUuid);
				worktimeDetail.setWorkYear(worktimeDetailVo.getWorkYear());
				worktimeDetail.setWorkDate(workDate);			
				worktimeDetail.setWorkStart(sdf.parse(workDate + " " + worktimeDefine.getStartTime()).getTime());
				worktimeDetail.setWorkEnd(sdf.parse(workDate + " " + worktimeDefine.getEndTime()).getTime());
				worktimeDetailList.add(worktimeDetail);
				if(worktimeDetailList.size() > 1000) {
					worktimeMapper.insertBatchWorktimeDetail(worktimeDetailList);
					worktimeDetailList.clear();
				}
			}
		}
		return null;
	}

	public static void main(String[] args) {
//		SimpleDateFormat sdf = new SimpleDateFormat();
//		sdf.applyPattern("yyyy-MM-dd");
//		System.out.println(sdf.format(new Date()));
//		sdf.applyPattern("yyyy-MM-dd H:mm");
//		System.out.println(sdf.format(new Date()));
//		Calendar calendar = Calendar.getInstance();
//		calendar.setFirstDayOfWeek(Calendar.MONDAY);
//		System.out.println(calendar.get(Calendar.DAY_OF_WEEK));
	}
}
