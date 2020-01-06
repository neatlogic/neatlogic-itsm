package codedriver.module.process.api.process;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ChannelVo;
import codedriver.module.process.dto.ProcessVo;
@Service
public class ProcessSearchApi extends ApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;
	
	@Override
	public String getToken() {
		return "process/search";
	}

	@Override
	public String getName() {
		return "流程列表搜索接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字，匹配名称"),
		@Param(name = "isActive", type = ApiParamType.ENUM, desc = "是否激活", rule = "0,1"),
		@Param(name = "type", type = ApiParamType.INTEGER, desc = "流程类型id"),
		@Param(name = "belong", type = ApiParamType.ENUM, desc = "流程归属", rule = "bug,itsm,request,task"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
		})
	@Output({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
		@Param(name="processList",explode=ChannelVo[].class,desc="流程列表")
	})
	@Description(desc = "流程列表搜索接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessVo processVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessVo>() {});
		
		JSONObject resultObj = new JSONObject();
		if(processVo.getNeedPage()) {
			int rowNum = processMapper.searchProcessCount(processVo);
			int pageCount = PageUtil.getPageCount(rowNum, processVo.getPageSize());
			processVo.setPageCount(pageCount);
			processVo.setRowNum(rowNum);
			resultObj.put("currentPage", processVo.getCurrentPage());
			resultObj.put("pageSize", processVo.getPageSize());
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
		}
		List<ProcessVo> processList = processMapper.searchProcessList(processVo);
		resultObj.put("processList", processList);
		return resultObj;
	}

}
