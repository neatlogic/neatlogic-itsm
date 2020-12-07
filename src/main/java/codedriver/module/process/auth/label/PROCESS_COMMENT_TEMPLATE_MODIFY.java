package codedriver.module.process.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class PROCESS_COMMENT_TEMPLATE_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "系统回复模版管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对系统回复模版添加、修改和删除";
	}

	@Override
	public String getAuthGroup() {
		return "process";
	}

}
