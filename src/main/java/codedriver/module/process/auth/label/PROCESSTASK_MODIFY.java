package codedriver.module.process.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class PROCESSTASK_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "工单管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对工单隐藏/显示和删除";
	}

	@Override
	public String getAuthGroup() {
		return "process";
	}

}
