package codedriver.module.process.api.form;

import java.util.List;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.restful.annotation.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.form.dto.AttributeDataVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.process.dto.ProcessFormVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.form.exception.FormActiveVersionNotFoundExcepiton;
import codedriver.framework.form.exception.FormAttributeHandlerNotFoundException;
import codedriver.framework.form.exception.FormAttributeNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.form.attribute.core.FormAttributeHandlerFactory;
import codedriver.framework.form.attribute.core.IFormAttributeHandler;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.restful.constvalue.OperationTypeEnum;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class FormAttributeCheckApi extends PrivateApiComponentBase {
	
	@Resource
	private ProcessTaskMapper processTaskMapper;

	@Resource
	private ChannelMapper channelMapper;

	@Resource
	private ProcessMapper processMapper;
	
	@Resource
	private FormMapper formMapper;
    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;
	
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
	@Output({
			@Param(name = "Return", type = ApiParamType.BOOLEAN, desc = "校验结果")
	})
	@Description(desc = "表单属性值校验接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject configObj = jsonObj.getJSONObject("config");
		Long processTaskId = configObj.getLong("processTaskId");
		String channelUuid = configObj.getString("channelUuid");
		FormVersionVo formVersionVo = null;
		String worktimeUuid = null;
		if(processTaskId != null) {
			ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
			if(processTaskVo == null) {
				throw new ProcessTaskNotFoundException(processTaskId.toString());
			}
			worktimeUuid = processTaskVo.getWorktimeUuid();
			ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
			if(processTaskFormVo == null || StringUtils.isBlank(processTaskFormVo.getFormContentHash())) {
				return false;
			}
			String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
            if(StringUtils.isBlank(formContent)) {
				return false;
            }
			formVersionVo = new FormVersionVo();
			formVersionVo.setFormUuid(processTaskFormVo.getFormUuid());
			formVersionVo.setFormName(processTaskFormVo.getFormName());
			formVersionVo.setFormConfig(formContent);
			
		}else if(StringUtils.isNotBlank(channelUuid)){
			ChannelVo channelVo = channelMapper.getChannelByUuid(channelUuid);
			if(channelVo == null) {
				throw new ChannelNotFoundException(channelUuid);
			}
			worktimeUuid = channelVo.getWorktimeUuid();
			ProcessFormVo processFormVo = processMapper.getProcessFormByProcessUuid(channelVo.getProcessUuid());
			if(processFormVo == null) {
				return false;
			}
			formVersionVo = formMapper.getActionFormVersionByFormUuid(processFormVo.getFormUuid());
			if(formVersionVo == null) {
				throw new FormActiveVersionNotFoundExcepiton(processFormVo.getFormUuid());
			}
		}else {
			throw new ParamIrregularException("config参数中必须包含'processTaskId'或'channelUuid'");
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
					configObj.put("worktimeUuid", worktimeUuid);
					return handler.valid(attributeDataVo, configObj);
				}else {
					throw new FormAttributeHandlerNotFoundException(formAttribute.getHandler());
				}
			}
		}
		throw new FormAttributeNotFoundException(attributeUuid);
	}

}
