package codedriver.module.process.auth.label;

import codedriver.framework.auth.core.AuthBase;
import codedriver.framework.common.constvalue.ModuleEnum;

public class WORKCENTER_VIEW extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "工单中心查看权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对工单中心查看";
	}

	@Override
	public String getAuthGroup() {
		return ModuleEnum.PROCESS.getValue();
	}

}
