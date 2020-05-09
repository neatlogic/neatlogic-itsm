package codedriver.module.process.auth.label;

import codedriver.framework.auth.core.AuthBase;
public class SLA_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "SLA管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对SLA进行管理";
	}

	@Override
	public String getAuthGroup() {
		return "process";
	}

}
