package codedriver.module.process.api.process;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
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
		@Param(name = "isICreated", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "是否只查询我创建的"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
		})
	@Output({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
		@Param(name="processList",explode=ProcessVo[].class,desc="流程列表")
	})
	@Description(desc = "流程列表搜索接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessVo processVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessVo>() {});
		int isICreated = jsonObj.getIntValue("isICreated");
		if(isICreated == 1) {
			processVo.setFcu(UserContext.get().getUserId());
		}
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
		int count = 0;
		for(ProcessVo process : processList) {
			count = processMapper.getProcessReferenceCount(process.getUuid());
			process.setReferenceCount(count);
			process.setConfig(null);
		}	
		resultObj.put("processList", processList);
		return resultObj;
	}

}
