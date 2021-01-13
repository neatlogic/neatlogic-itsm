package codedriver.module.process.api.process;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessSearchForSelectApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;
	
	@Override
	public String getToken() {
		return "process/search/forselect";
	}

	@Override
	public String getName() {
		return "查询流程列表_下拉框";
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
		@Param(name="list",explode=ValueTextVo[].class,desc="流程列表")
	})
	@Description(desc = "查询流程列表_下拉框")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessVo processVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessVo>() {});
		int isICreated = jsonObj.getIntValue("isICreated");
		if(isICreated == 1) {
			processVo.setFcu(UserContext.get().getUserUuid(true));
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
		List<ValueTextVo> processList = processMapper.searchProcessListForSelect(processVo);
		resultObj.put("list", processList);
		return resultObj;
	}

}
