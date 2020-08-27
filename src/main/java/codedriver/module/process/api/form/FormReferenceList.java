package codedriver.module.process.api.form;

import java.util.List;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dto.ProcessFormVo;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.process.exception.form.FormNotFoundException;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class FormReferenceList extends PrivateApiComponentBase {

	@Autowired
	private FormMapper formMapper;
	
	@Override
	public String getToken() {
		return "process/form/reference/list";
	}

	@Override
	public String getName() {
		return "表单引用列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "formUuid", type = ApiParamType.STRING, isRequired = true, desc = "表单uuid"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
	})
	@Output({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
		@Param(name = "processList", explode = ProcessVo[].class, desc = "表单引用列表")
	})
	@Description(desc = "表单引用列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessFormVo processFormVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessFormVo>() {});
		//判断表单是否存在
		if(formMapper.checkFormIsExists(processFormVo.getFormUuid()) == 0) {
			throw new FormNotFoundException(processFormVo.getFormUuid());
		}
		JSONObject resultObj = new JSONObject();
		if(processFormVo.getNeedPage()) {
			int rowNum = formMapper.getFormReferenceCount(processFormVo.getFormUuid());
			int pageCount = PageUtil.getPageCount(rowNum,processFormVo.getPageSize());
			processFormVo.setPageCount(pageCount);
			processFormVo.setRowNum(rowNum);
			resultObj.put("currentPage",processFormVo.getCurrentPage());
			resultObj.put("pageSize",processFormVo.getPageSize());
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
		}	
		List<ProcessVo> processList = formMapper.getFormReferenceList(processFormVo);
		resultObj.put("processList", processList);
		return resultObj;
	}

}
