package codedriver.framework.process.timeoutpolicy.handler;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.dto.ProcessTaskAttributeValueVo;
import codedriver.framework.process.dto.ProcessTaskStepTimeoutPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.mapper.ProcessTaskMapper;
import codedriver.module.process.constvalue.TimeoutPolicy;

@Service
public class SimpleTimeoutPolicyHandler implements ITimeoutPolicyHandler {
	@Autowired
	private ProcessTaskMapper processTaskMapper;

	public String getType() {
		return TimeoutPolicy.SIMPLE.getValue();
	}

	public Boolean execute(ProcessTaskStepTimeoutPolicyVo timeoutPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo) {
		if (timeoutPolicyVo.getConfigObj() != null) {
			String uuid = timeoutPolicyVo.getConfigObj().getString("uuid");
			String targetValue = timeoutPolicyVo.getConfigObj().getString("targetvalue");
			if (StringUtils.isNotBlank(uuid)) {
				List<ProcessTaskAttributeValueVo> valueList = processTaskMapper.getProcessTaskAttributeValue(currentProcessTaskStepVo.getProcessTaskId(), uuid);
				if (valueList != null && valueList.size() > 0) {
					for (ProcessTaskAttributeValueVo valueVo : valueList) {
						if (valueVo.getValue().equalsIgnoreCase(targetValue)) {
							processTaskMapper.updateProcessTaskStepExpireTime(currentProcessTaskStepVo);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
