package neatlogic.module.process.api.processtask;


import java.util.Map;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

@SuppressWarnings("deprecation")
@Service
@AuthAction(action = PROCESS_BASE.class)
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
