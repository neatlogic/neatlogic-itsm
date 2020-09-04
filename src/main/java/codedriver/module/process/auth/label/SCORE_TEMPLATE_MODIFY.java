package codedriver.module.process.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class SCORE_TEMPLATE_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "评分模版管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对评分模版进行添加、修改和删除";
	}

	@Override
	public String getAuthGroup() {
		return "process";
	}
}
