package codedriver.module.process.api.form;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormVo;
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
@Deprecated
public class FormSearchForSelectApi extends PrivateApiComponentBase {

	@Autowired
	private FormMapper formMapper;
	
	@Override
	public String getToken() {
		return "process/form/search/forselect";
	}

	@Override
	public String getName() {
		return "查询表单列表_下拉框";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字，匹配名称", xss = true),
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
		@Param(name="list",explode=ValueTextVo[].class,desc="表单列表")
	})
	@Description(desc = "查询表单列表_下拉框")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		FormVo formVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<FormVo>() {});
		JSONObject resultObj = new JSONObject();
		if(formVo.getNeedPage()) {
			int rowNum = formMapper.searchFormCount(formVo);
			int pageCount = PageUtil.getPageCount(rowNum, formVo.getPageSize());
			formVo.setPageCount(pageCount);
			resultObj.put("currentPage", formVo.getCurrentPage());
			resultObj.put("pageSize", formVo.getPageSize());
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
		}
		List<ValueTextVo> formList = formMapper.searchFormListForSelect(formVo);
		resultObj.put("list", formList);
		return resultObj;
	}

}
