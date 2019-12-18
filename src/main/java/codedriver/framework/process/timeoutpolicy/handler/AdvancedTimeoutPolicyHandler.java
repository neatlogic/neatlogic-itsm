package codedriver.framework.process.timeoutpolicy.handler;

import org.springframework.stereotype.Service;

import codedriver.module.process.constvalue.TimeoutPolicy;
import codedriver.module.process.dto.ProcessTaskStepTimeoutPolicyVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
@Service
public class AdvancedTimeoutPolicyHandler implements ITimeoutPolicyHandler {
	public String getType() {
		return TimeoutPolicy.ADVANCED.getValue();
	}

	public String getName() {
		return TimeoutPolicy.ADVANCED.getText();
	}
	
	public Boolean execute(ProcessTaskStepTimeoutPolicyVo timeoutPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo) {
		return false;
	}
}
