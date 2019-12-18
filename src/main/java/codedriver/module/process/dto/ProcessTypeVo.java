package codedriver.module.process.dto;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class ProcessTypeVo {

	@EntityField(name = "流程类型id", type = ApiParamType.LONG)
	private Long id;
	@EntityField(name = "流程类型名称", type = ApiParamType.STRING)
	private String name;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
