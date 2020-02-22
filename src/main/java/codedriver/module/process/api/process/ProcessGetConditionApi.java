package codedriver.module.process.api.process;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.attribute.dto.AttributeVo;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessService;

@Service
@Transactional
public class ProcessGetConditionApi extends ApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;
	@Autowired
	private FormMapper formMapper;
	@Autowired
	private ProcessService processService;
	
	@Override
	public String getToken() {
		return "process/get/condition";
	}

	@Override
	public String getName() {
		return "流程编辑获取条件接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "formUuid", type = ApiParamType.STRING, isRequired = true, desc = "流程绑定表单的uuid")
	})
	@Output({
		@Param(name = "Return", type = ApiParamType.STRING, desc = "")
	})
	@Description(desc = "流程编辑获取条件接口，目前用于流程编辑，初始化条件使用")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String formUuid = jsonObj.getString("uuid");
		List<AttributeVo> attrList = formMapper.getAttributeByFormUuid(formUuid);
		
		
		return null;
	}

}
