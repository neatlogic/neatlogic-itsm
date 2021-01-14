package codedriver.module.process.api.processtask;


import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@SuppressWarnings("deprecation")
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskOldFormPropGetApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getToken() {
		return "processtask/old/formprop/get";
	}

	@Override
	public String getName() {
		return "查询旧工单表单信息";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单Id", isRequired = true)
	})
	@Output({
	    @Param(name = "form", type = ApiParamType.JSONOBJECT, desc = "表单信息"),
	    @Param(name = "prop", type = ApiParamType.JSONOBJECT, desc = "自定义属性信息")
	})
	@Description(desc = "查询旧工单表单信息")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		Map<String,String> oldFormProp= processTaskMapper.getProcessTaskOldFormAndPropByTaskId(processTaskId);
		JSONObject resutl = new JSONObject();
		resutl.put("form", StringEscapeUtils.unescapeHtml4(oldFormProp.get("form")));
		resutl.put("prop", JSONArray.parse(StringEscapeUtils.unescapeHtml4(oldFormProp.get("prop"))));
		return resutl;
	}

}
