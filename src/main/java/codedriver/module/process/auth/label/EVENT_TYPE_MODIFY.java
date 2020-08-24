package codedriver.module.process.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class EVENT_TYPE_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "事件类型管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对事件类型进行添加、修改和删除";
	}

	@Override
	public String getAuthGroup() {
		return "framework";
	}
}
