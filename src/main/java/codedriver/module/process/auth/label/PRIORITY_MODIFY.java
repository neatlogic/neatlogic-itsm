package codedriver.module.process.auth.label;

import codedriver.framework.auth.core.AuthBase;
import codedriver.framework.process.constvalue.ModuleEnum;

public class PRIORITY_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "优先级管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对优先级添加、修改和删除";
	}

	@Override
	public String getAuthGroup() {
		return ModuleEnum.PROCESS.getValue();
	}

}
