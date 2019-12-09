package codedriver.framework.process.workerpolicy.handler;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;

public interface IWorkerPolicyHandler {
	public String getType();

	/**
	 * @Author: chenqiwei
	 * @Time:Sep 18, 2019
	 * @Description: TODO
	 * @param @param
	 *            workerPolicyVo
	 * @param @param
	 *            currentProcessTaskStepVo
	 * @param @return
	 * @return Boolean 成功分配到处理人则返回true，分配不到则返回false
	 */
	@Transactional
	public List<ProcessTaskStepWorkerVo> execute(ProcessTaskStepWorkerPolicyVo workerPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo);
}
