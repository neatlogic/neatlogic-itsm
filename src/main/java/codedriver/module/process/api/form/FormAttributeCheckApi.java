package codedriver.module.process.api.form;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.AttributeDataVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.FormAttributeVo;
import codedriver.framework.process.dto.FormVersionVo;
import codedriver.framework.process.dto.ProcessFormVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.form.FormActiveVersionNotFoundExcepiton;
import codedriver.framework.process.exception.form.FormAttributeHandlerNotFoundException;
import codedriver.framework.process.exception.form.FormAttributeNotFoundException;
import codedriver.framework.process.exception.form.FormIllegalParameterException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.formattribute.core.FormAttributeHandlerFactory;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class FormAttributeCheckApi extends ApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Autowired
	private ChannelMapper channelMapper;

	@Autowired
	private ProcessMapper processMapper;
	
	@Autowired
	private FormMapper formMapper;
	
	@Override
	public String getToken() {
		return "process/form/attribute/check";
	}

	@Override
	public String getName() {
		return "表单属性值校验接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "attributeUuid", type = ApiParamType.STRING, isRequired= true, desc = "表单属性uuid"),
		@Param(name = "data", type = ApiParamType.STRING, isRequired= true, desc = "属性值"),
		@Param(name = "config", type = ApiParamType.JSONOBJECT, isRequired= true, desc = "校验用到的相关数据")
	})
	@Description(desc = "表单属性值校验接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject configObj = jsonObj.getJSONObject("config");
		Long processTaskId = configObj.getLong("processTaskId");
		String channelUuid = configObj.getString("channelUuid");
		FormVersionVo formVersionVo = null;
		if(processTaskId != null) {
			ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
			if(processTaskVo == null) {
				throw new ProcessTaskNotFoundException(processTaskId.toString());
			}
			ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
			if(processTaskFormVo == null) {
				throw new FormIllegalParameterException("工单：'" + processTaskId + "'没有绑定表单");
			}
			formVersionVo = new FormVersionVo();
			formVersionVo.setFormUuid(processTaskFormVo.getFormUuid());
			formVersionVo.setFormName(processTaskFormVo.getFormName());
			formVersionVo.setFormConfig(processTaskFormVo.getFormContent());
			
		}else if(StringUtils.isNotBlank(channelUuid)){
			ChannelVo channelVo = channelMapper.getChannelByUuid(channelUuid);
			if(channelVo == null) {
				throw new ChannelNotFoundException(channelUuid);
			}
			ProcessFormVo processFormVo = processMapper.getProcessFormByProcessUuid(channelVo.getProcessUuid());
			if(processFormVo == null) {
				throw new FormIllegalParameterException("流程：'" + channelVo.getProcessUuid() + "'没有绑定表单");
			}
			formVersionVo = formMapper.getActionFormVersionByFormUuid(processFormVo.getFormUuid());
			if(formVersionVo == null) {
				throw new FormActiveVersionNotFoundExcepiton(processFormVo.getFormUuid());
			}
		}else {
			throw new FormIllegalParameterException("config参数中必须包含'processTaskId'或'channelUuid'");
		}
		String attributeUuid = jsonObj.getString("attributeUuid");
		List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
		for(FormAttributeVo formAttribute : formAttributeList) {
			if(attributeUuid.equals(formAttribute.getUuid())) {
				IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(formAttribute.getHandler());
				if(handler != null) {
					AttributeDataVo attributeDataVo = new AttributeDataVo();
					attributeDataVo.setAttributeUuid(attributeUuid);
					attributeDataVo.setData(jsonObj.getString("data"));
					configObj.put("attributeConfig", formAttribute.getConfig());
					return handler.valid(attributeDataVo, configObj);
				}else {
					throw new FormAttributeHandlerNotFoundException(formAttribute.getHandler());
				}
			}
		}
		throw new FormAttributeNotFoundException(attributeUuid);
	}

}
