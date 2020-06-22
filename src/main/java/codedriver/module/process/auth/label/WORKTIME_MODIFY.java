package codedriver.module.process.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class WORKTIME_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "服务窗口管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对服务窗口添加、修改和删除";
	}

	@Override
	public String getAuthGroup() {
		return "process";
	}

}
