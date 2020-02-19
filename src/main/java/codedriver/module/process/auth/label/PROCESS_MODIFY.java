package codedriver.module.process.auth.label;

import codedriver.framework.auth.core.AuthBase;
import codedriver.framework.auth.core.AuthGroupEnum;

public class PROCESS_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "流程管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对流程进行添加、修改和删除";
	}

	@Override
	public String getAuthGroup() {
		return AuthGroupEnum.PROCESS.getValue();
	}
}
