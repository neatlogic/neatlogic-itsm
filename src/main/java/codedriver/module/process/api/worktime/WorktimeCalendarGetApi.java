package codedriver.module.process.api.worktime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.WorktimeRangeVo;
import codedriver.framework.process.exception.worktime.WorktimeNotFoundException;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
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
		JSONArray jsonArray = generateCalendar(jsonObj.getIntValue("year"), worktimeDateList);
		return jsonArray;
	}

	/**
	 * 
	* @Time:2020年8月17日
	* @Description: 生产日历数据
	* @param year 年份
	* @param worktimeDateList 已选中的日期数据
	* @return JSONArray
	 */
	private JSONArray generateCalendar(int year, List<String> worktimeDateList) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		JSONObject monthObj = new JSONObject();
		JSONObject trObj = new JSONObject();
		JSONArray dateList = new JSONArray();

		JSONArray monthList = new JSONArray();
		JSONArray trList = new JSONArray();
		int trIndex = 0;
		int monthIndex = 0;
		String dateName = null;
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, 0, 1);
		for(int i = 1; i < calendar.get(Calendar.DAY_OF_WEEK); i++) {
			JSONObject nonDateObj = new JSONObject();
			nonDateObj.put("name", "");
			nonDateObj.put("dayOfMonth", "");
			nonDateObj.put("dayOfWeek", 0);
			nonDateObj.put("selected", 0);
			dateList.add(nonDateObj);
		}
		while(calendar.get(Calendar.YEAR) == year) {
			if(calendar.get(Calendar.MONTH) != monthIndex) {
				monthObj.put("name", getMonthName(monthIndex));
				monthObj.put("dateList", dateList);
				monthList.add(monthObj);
				monthIndex = calendar.get(Calendar.MONTH);
				monthObj = new JSONObject();
				dateList = new JSONArray();
				
				for(int i = 1; i < calendar.get(Calendar.DAY_OF_WEEK); i++) {
					JSONObject nonDateObj = new JSONObject();
					nonDateObj.put("name", "");
					nonDateObj.put("dayOfMonth", "");
					nonDateObj.put("dayOfWeek", 0);
					nonDateObj.put("selected", 0);
					dateList.add(nonDateObj);
				}
			}
			
			int index = calendar.get(Calendar.MONTH) / 4;
			if(index != trIndex) {
				trObj.put("index", trIndex);
				trObj.put("monthList", monthList);
				trList.add(trObj);
				trIndex = index;
				trObj = new JSONObject();
				monthList = new JSONArray();
			}
			JSONObject dateObj = new JSONObject();
			dateName = sdf.format(calendar.getTime());
			dateObj.put("name", dateName);
			dateObj.put("dayOfMonth", calendar.get(Calendar.DAY_OF_MONTH));
			dateObj.put("dayOfWeek", calendar.get(Calendar.DAY_OF_WEEK));
			dateObj.put("selected", worktimeDateList.contains(dateName) ? 1 : 0);
			dateList.add(dateObj);
			calendar.add(Calendar.DATE, 1);
		}
		
		monthObj.put("name", getMonthName(monthIndex));
		monthObj.put("dateList", dateList);
		monthList.add(monthObj);
		trObj.put("index", trIndex);
		trObj.put("monthList", monthList);
		trList.add(trObj);
		return trList;
	}
	/**
	 * 
	* @Time:2020年8月17日
	* @Description: 获取月份名称 
	* @param monthIndex
	* @return String
	 */
	private String getMonthName(int monthIndex) {
		monthIndex += 1;
		if(monthIndex < 10) {
			return "0" + monthIndex;
		}
		return "" + monthIndex;
	}

//	private Object getClassType(int dayOfWeek, boolean selected) {
//		int classType = 0;
//		if(dayOfWeek >=2 && dayOfWeek <= 6) {//工作日
//			classType = 1;
//		}else if(dayOfWeek == 1 || dayOfWeek == 7){//非工作日
//			classType = 2;
//		}else {//空格
//			return classType;	
//		}
//		
//		if(selected) {//已选中
//			classType *= 4;
//		}
//		return classType;
//	}
}
