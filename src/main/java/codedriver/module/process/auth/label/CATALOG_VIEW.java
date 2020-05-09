package codedriver.module.process.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class CATALOG_VIEW extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "服务目录查看权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对服务目录页面查看权限";
	}

	@Override
	public String getAuthGroup() {
		return "process";
	}

}
