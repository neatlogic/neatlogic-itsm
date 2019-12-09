package codedriver.framework.process.timeoutpolicy.handler;

import org.springframework.transaction.annotation.Transactional;

import codedriver.module.process.dto.ProcessTaskStepTimeoutPolicyVo;
import codedriver.module.process.dto.ProcessTaskStepVo;

public interface ITimeoutPolicyHandler {
	public String getType();

	/**
	* @Author: chenqiwei
	* @Time:Sep 18, 2019
	* @Description: TODO 
	* @param @param workerPolicyVo
	* @param @param currentProcessTaskStepVo
	* @param @return 
	* @return Boolean 成功计算超时日期则返回true，否则返回false
	 */
	@Transactional
	public Boolean execute(ProcessTaskStepTimeoutPolicyVo timeoutPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo);
}
