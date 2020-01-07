package codedriver.module.process.api.worktime;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.WorktimeVo;

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
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
		@Param(name="worktimeList",explode=WorktimeVo[].class,desc="工作时间窗口列表")
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
		resultObj.put("worktimeList", worktimeList);
		return resultObj;
	}

}
