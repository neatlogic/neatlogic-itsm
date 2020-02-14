package codedriver.module.process.dto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class WorkerDispatcherVo {
	@EntityField(name = "分派器名称",
			type = ApiParamType.STRING)
	private String name;
	@EntityField(name = "分派器处理类",
			type = ApiParamType.STRING)
	private String handler;
	@EntityField(name = "是否激活",
			type = ApiParamType.INTEGER)
	private Integer isActive;
	@EntityField(name = "输入帮助",
			type = ApiParamType.STRING)
	private String help;
	@EntityField(name = "分派器扩展配置",
			type = ApiParamType.STRING)
	private JSONArray config;
	@EntityField(name = "分派器所属模块",
			type = ApiParamType.STRING)
	private String moduleId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public Integer getIsActive() {
		return isActive;
	}

	public void setIsActive(Integer isActive) {
		this.isActive = isActive;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public JSONArray getConfig() {
		return config;
	}

	public void setConfig(JSONArray config) {
		this.config = config;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

}
