package codedriver.module.process.api.form;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.FormVersionVo;
import codedriver.framework.process.dto.FormVo;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.form.FormActiveVersionNotFoundExcepiton;
import codedriver.framework.process.exception.form.FormIllegalParameterException;
import codedriver.framework.process.exception.form.FormNotFoundException;
import codedriver.framework.process.exception.form.FormVersionNotFoundException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class FormGetApi extends ApiComponentBase {

	@Autowired
	private FormMapper formMapper;

	@Autowired
	private ProcessMapper processMapper;
	
	@Autowired
	private ChannelMapper channelMapper;
	
	@Override
	public String getToken() {
		return "process/form/get";
	}

	@Override
	public String getName() {
		return "单个表单查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Override
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid"), 
		@Param(name = "processUuid", type = ApiParamType.STRING, desc = "流程uuid"), 
		@Param(name = "channelUuid", type = ApiParamType.STRING, desc = "服务uuid"), 
		@Param(name = "currentVersionUuid", type = ApiParamType.STRING, desc = "选择表单版本uuid"), 
	})
	@Output({ @Param(explode = FormVo.class) })
	@Description(desc = "单个表单查询接口")
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		if(uuid == null) {
			String processUuid = jsonObj.getString("processUuid");
			if(processUuid == null) {
				String channelUuid = jsonObj.getString("channelUuid");
				if(channelUuid == null) {
					throw new FormIllegalParameterException("uuid，processUuid，channelUuid这个三个参数必须传一个才能获取表单信息");
				}else {
					ChannelVo channel = channelMapper.getChannelByUuid(channelUuid);
					if(channel == null) {
						throw new ChannelNotFoundException(channelUuid);
					}
					processUuid = channel.getProcessUuid();
					if(processUuid == null) {
						throw new FormIllegalParameterException("服务:'" + channelUuid + "'没有绑定流程图");
					}
				}
			}
			ProcessVo process = processMapper.getProcessByUuid(processUuid);
			if(process == null) {
				throw new ProcessNotFoundException(processUuid);
			}
			uuid = process.getFormUuid();
			if(uuid == null) {
				throw new FormIllegalParameterException("流程图:'" + processUuid + "'没有绑定表单");
			}
		}
		FormVo formVo = formMapper.getFormByUuid(uuid);
		//判断表单是否存在
		if (formVo == null) {
			throw new FormNotFoundException(uuid);
		}
		FormVersionVo formVersion = null;
		if(jsonObj.containsKey("currentVersionUuid")) {
			String currentVersionUuid = jsonObj.getString("currentVersionUuid");			
			formVersion = formMapper.getFormVersionByUuid(currentVersionUuid);
			//判断表单版本是否存在
			if(formVersion == null) {
				throw new FormVersionNotFoundException(uuid);
			}
			if(!uuid.equals(formVersion.getFormUuid())) {
				throw new FormIllegalParameterException("表单版本：'" + currentVersionUuid + "'不属于表单：'" + uuid + "'的版本");
			}
			formVo.setCurrentVersionUuid(currentVersionUuid);
		}else {//获取激活版本
			formVersion = formMapper.getActionFormVersionByFormUuid(uuid);
			if(formVersion == null) {
				throw new FormActiveVersionNotFoundExcepiton(uuid);
			}
			formVo.setCurrentVersionUuid(formVersion.getUuid());
		}
		//表单内容
		formVo.setFormConfig(formVersion.getFormConfig());
		//表单版本列表
		List<FormVersionVo> formVersionList = formMapper.getFormVersionSimpleByFormUuid(uuid);
		formVo.setVersionList(formVersionList);
		//引用数量
		int count = formMapper.getFormReferenceCount(uuid);
		formVo.setReferenceCount(count);
		return formVo;
	}

}
