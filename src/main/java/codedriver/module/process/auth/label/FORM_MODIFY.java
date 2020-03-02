package codedriver.module.process.auth.label;

import codedriver.framework.auth.core.AuthBase;
import codedriver.framework.common.constvalue.ModuleEnum;

public class FORM_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "表单管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对表单进行添加、修改和删除";
	}

	@Override
	public String getAuthGroup() {
		return ModuleEnum.PROCESS.getValue();
	}

}
