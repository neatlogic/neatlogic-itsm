package codedriver.module.process.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class WORKCENTER_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "工单中心管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对工单中心系统类型添加、修改和删除";
	}

	@Override
	public String getAuthGroup() {
		return "process";
	}

}
