package codedriver.module.process.api.worktime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.WorktimeVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class WorktimeSearchApi extends ApiComponentBase {

	@Autowired
	private WorktimeMapper worktimeMapper;
	
	@Override
	public String getToken() {
		return "worktime/search";
	}

	@Override
	public String getName() {
		return "工作时间窗口列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "模糊搜索", xss = true),
		@Param(name = "isActive", type = ApiParamType.ENUM, desc = "是否激活", rule = "0,1"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
	})
	@Output({
		@Param(explode = BasePageVo.class),
		@Param(name = "tbodyList", explode = WorktimeVo[].class, desc = "工作时间窗口列表")
	})
	@Description(desc = "工作时间窗口列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		WorktimeVo worktimeVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<WorktimeVo>() {});
		JSONObject resultObj = new JSONObject();
		if(worktimeVo.getNeedPage()) {
			int rowNum = worktimeMapper.searchWorktimeCount(worktimeVo);
			int pageCount = PageUtil.getPageCount(rowNum, worktimeVo.getPageSize());
			worktimeVo.setPageCount(pageCount);
			worktimeVo.setRowNum(rowNum);
			resultObj.put("currentPage", worktimeVo.getCurrentPage());
			resultObj.put("pageSize", worktimeVo.getPageSize());
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
		}
		
		List<WorktimeVo> worktimeList = worktimeMapper.searchWorktimeList(worktimeVo);
		List<String> worktimeUuidList = worktimeList.stream().map(WorktimeVo::getUuid).collect(Collectors.toList());
		List<WorktimeVo> worktimeUuidYearListList = worktimeMapper.getYearListByWorktimeUuidList(worktimeUuidList);
		Map<String, List<Integer>> worktimeUuidYearListMap = new HashMap<>();
		for(WorktimeVo worktime : worktimeUuidYearListList) {
			worktimeUuidYearListMap.put(worktime.getUuid(), worktime.getYearList());
		}
		for(WorktimeVo worktime : worktimeList) {
			worktime.setYearList(worktimeUuidYearListMap.get(worktime.getUuid()));
			if(StringUtils.isNotBlank(worktime.getConfig())) {
				JSONObject config = JSON.parseObject(worktime.getConfig());
				Set<String> workingHoursSet = new HashSet<>();
				for(Entry<String, Object> entry : config.entrySet()) {
					Object value = entry.getValue();
					if(value instanceof JSONArray) {
						JSONArray jsonArray = (JSONArray) value;
						for(int i = 0; i < jsonArray.size(); i++) {
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							String startTime = "undefine";
							String endTime = "undefine";
							if(jsonObject.containsKey("startTime")) {
								startTime = jsonObject.getString("startTime");
							}
							if(jsonObject.containsKey("endTime")) {
								endTime = jsonObject.getString("endTime");
							}						
							workingHoursSet.add(startTime + " ~ " + endTime);
						}
					}
				}
				worktime.setWorkingHoursSet(workingHoursSet);
				worktime.setConfig(null);
			}
		}
		resultObj.put("tbodyList", worktimeList);
		return resultObj;
	}

}
