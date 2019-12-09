package codedriver.framework.process.timeoutpolicy.handler;

import org.springframework.stereotype.Service;

import codedriver.framework.process.dto.ProcessTaskStepTimeoutPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.module.process.constvalue.TimeoutPolicy;
@Service
public class AdvancedTimeoutPolicyHandler implements ITimeoutPolicyHandler {
	public String getType() {
		return TimeoutPolicy.ADVANCED.getValue();
	}

	public Boolean execute(ProcessTaskStepTimeoutPolicyVo timeoutPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo) {
		return false;
	}
}
