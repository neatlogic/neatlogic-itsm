package codedriver.module.process.api.channel;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.form.FormIllegalParameterException;
import codedriver.framework.process.exception.form.FormNotFoundException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessFormHandler;
import codedriver.module.process.dto.ChannelVo;
import codedriver.module.process.dto.FormAttributeVo;
import codedriver.module.process.dto.FormVo;
import codedriver.module.process.dto.ProcessVo;
@Service
public class ChannelFormGetApi extends ApiComponentBase {
	
	@Autowired
	private FormMapper formMapper;
	
	@Autowired
	private ProcessMapper processMapper;
	
	@Autowired
	private ChannelMapper channelMapper;
	
	@Override
	public String getToken() {
		return "process/channel/form/get";
	}

	@Override
	public String getName() {
		return "服务绑定的表单属性信息获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "channelUuid", type = ApiParamType.LONG, isRequired = true, desc = "服务uuid")
	})
	@Output({
		@Param(explode = FormAttributeVo[].class, desc = "表单属性列表")
	})
	@Description(desc = "服务绑定的表单属性信息获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String channelUuid = jsonObj.getString("channelUuid");
		ChannelVo channel = channelMapper.getChannelByUuid(channelUuid);
		if(channel == null) {
			throw new ChannelNotFoundException(channelUuid);
		}
		String processUuid = channel.getProcessUuid();
		if(processUuid == null) {
			throw new FormIllegalParameterException("服务:'" + channelUuid + "'没有绑定流程图");
		}
		ProcessVo process = processMapper.getProcessByUuid(processUuid);
		if(process == null) {
			throw new ProcessNotFoundException(processUuid);
		}
		String formUuid = process.getFormUuid();
		if(formUuid == null) {
			throw new FormIllegalParameterException("流程图:'" + processUuid + "'没有绑定表单");
		}
		FormVo formVo = formMapper.getFormByUuid(formUuid);
		//判断表单是否存在
		if (formVo == null) {
			throw new FormNotFoundException(formUuid);
		}
		List<FormAttributeVo> formAttributeList = formMapper.getFormAttributeList(new FormAttributeVo(formUuid));
		for(FormAttributeVo formAttributeVo : formAttributeList) {
			formAttributeVo.setExpressionList(ProcessFormHandler.getExpressionList(formAttributeVo.getHandler()));
		}
		return formAttributeList;
	}

}
