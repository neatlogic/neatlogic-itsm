package codedriver.module.process.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class CATALOG_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "服务目录管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对服务目录添加、修改和删除";
	}

	@Override
	public String getAuthGroup() {
		return "process";
	}

}
