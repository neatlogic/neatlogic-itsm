package codedriver.module.process.dto;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class ProcessDraftVo {
	
	@EntityField(name = "草稿uuid", type = ApiParamType.STRING)
	private String uuid;
	
	@EntityField(name = "流程uuid", type = ApiParamType.STRING)
	private String processUuid;

	@EntityField(name = "流程名称", type = ApiParamType.STRING)
	private String name;

	@EntityField(name = "流程图配置", type = ApiParamType.STRING)
	private String config;
	
	private transient String fcu;
	
	private transient String md5;

	public synchronized String getUuid() {
		if (StringUtils.isBlank(uuid)) {
			uuid = UUID.randomUUID().toString().replace("-", "");
		}
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getProcessUuid() {
		return processUuid;
	}

	public void setProcessUuid(String processUuid) {
		this.processUuid = processUuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public String getFcu() {
		return fcu;
	}

	public void setFcu(String fcu) {
		this.fcu = fcu;
	}

	public String getMd5() {
		if(md5 != null) {
			return md5;
		}
		if (StringUtils.isBlank(config)) {
			return null;
		}
		md5 = "{MD5}" + DigestUtils.md5DigestAsHex(config.getBytes());
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}
}
