package codedriver.framework.process.stephandler.core;

import java.util.List;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.module.process.constvalue.ProcessStepMode;
import codedriver.module.process.dto.ProcessStepVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;
import codedriver.module.process.dto.ProcessTaskVo;

//需要把事务隔离级别调低，避免并发insert时因为gap lock导致deadlock
public interface IProcessStepHandler {
	/**
	 * @Author: chenqiwei
	 * @Time:Jan 20, 2020
	 * @Description: 返回控件
	 * @param @return
	 * @return String
	 */
	public String getHandler();
	
	/**
	 * @Author: chenqiwei
	 * @Time:Jan 20, 2020
	 * @Description: 返回控件类型，目前只有start,end,process和converge四种类型
	 * @param @return
	 * @return String
	 */
	public String getType();

	/**
	 * @Author: chenqiwei
	 * @Time:Jan 5, 2020
	 * @Description: 自动模式还是手动模式，自动模式引擎会自动触发handle动作
	 * @param @return
	 * @return String
	 */
	public ProcessStepMode getMode();

	public String getIcon();

	public String getName();

	public int getSort();

	/**
	 * @Author: chenqiwei
	 * @Time:Sep 16, 2019
	 * @Description: 是否异步步骤
	 * @param @return
	 * @return boolean
	 */
	public boolean isAsync();

	/**
	 * @Author: chenqiwei
	 * @Time:Aug 6, 2019
	 * @Description: 是否允许设为开始节点
	 * @param @return
	 * @return Boolean
	 */
	public Boolean isAllowStart();

	/**
	 * 
	 * @Author: chenqiwei
	 * @Time:May 24, 2019
	 * @Description: 激活流程步骤
	 * @param @param  workflowStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int active(ProcessTaskStepVo currentProcessTaskStepVo);

	/**
	 * 
	 * @Author: chenqiwei
	 * @Time:May 24, 2019
	 * @Description: 分配处理人
	 * @param @param  workflowStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int assign(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException;

	/**
	 * 
	 * @Author: chenqiwei
	 * @Time:May 24, 2019
	 * @Description: 挂起流程步骤
	 * @param @param  workflowStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int hang(ProcessTaskStepVo currentProcessTaskStepVo);

	/**
	 * 
	 * @Author: chenqiwei
	 * @Time:May 24, 2019
	 * @Description: 开始流程步骤
	 * @param @param  workflowStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int start(ProcessTaskStepVo currentProcessTaskStepVo);

	/**
	 * @Author: chenqiwei
	 * @Time:Aug 6, 2019
	 * @Description: 处理流程步骤
	 * @param @param  processtaskStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int handle(ProcessTaskStepVo currentProcessTaskStepVo);

	/**
	 * @Author: chenqiwei
	 * @Time:Aug 5, 2019
	 * @Description: 接受流程步骤
	 * @param @param  processTaskStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int accept(ProcessTaskStepVo currentProcessTaskStepVo);

	/**
	 * @Author: chenqiwei
	 * @Time:Jan 7, 2020
	 * @Description: 转交步骤处理人
	 * @param @param  currentProcessTaskStepVo
	 * @param @param  workerList
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int transfer(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList);

	/**
	 * 
	 * @Author: chenqiwei
	 * @Time:May 24, 2019
	 * @Description: 完成流程步骤
	 * @param @param  workflowStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int complete(ProcessTaskStepVo currentProcessTaskStepVo);

	/**
	 * @Author: chenqiwei
	 * @Time:Sep 16, 2019
	 * @Description: 上一步发起的撤回动作
	 * @param @param  processTaskStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int retreat(ProcessTaskStepVo currentProcessTaskStepVo);

	/***
	 * @Author: chenqiwei
	 * @Time:Jan 7, 2020
	 * @Description: 终止流程
	 * @param @param  processTaskStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int abortProcessTask(ProcessTaskVo currentProcessTaskVo);

	/***
	 * @Author: chenqiwei
	 * @Time:Jan 7, 2020
	 * @Description: 终止流程步骤
	 * @param @param  processTaskStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int abort(ProcessTaskStepVo currentProcessTaskStepVo);

	/***
	 * @Author: chenqiwei
	 * @Time:Jan 7, 2020
	 * @Description: 恢复已终止流程
	 * @param @param  currentProcessTaskVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int recoverProcessTask(ProcessTaskVo currentProcessTaskVo);

	/***
	 * @Author: chenqiwei
	 * @Time:Jan 7, 2020
	 * @Description: 恢复终止流程步骤
	 * @param @param  processTaskStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int recover(ProcessTaskStepVo currentProcessTaskStepVo);

	/**
	 * 
	 * @Author: chenqiwei
	 * @Time:Aug 5, 2019
	 * @Description: 获取当前步骤满足流转条件的后置步骤
	 * @param @return
	 * @return List<ProcessTaskStepVo>
	 */
	public List<ProcessTaskStepVo> getNext(ProcessTaskStepVo currentProcessTaskStepVo);
	
	/**
	 * @Author: 
	 * @Time:
	 * @Description: 保存工单草稿，将会创建一个工单，工单状态为草稿状态
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int saveDraft(ProcessTaskStepVo currentProcessTaskStepVo);
	
	/**
	 * @Author: chenqiwei
	 * @Time:Aug 7, 2019
	 * @Description: 开始流程，将会创建一个作业
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int startProcess(ProcessTaskStepVo currentProcessTaskStepVo);

	/**
	 * @Author:
	 * @Time:
	 * @Description: 回退步骤
	 * @param @param  processTaskStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public int back(ProcessTaskStepVo currentProcessTaskStepVo);

	/**
	 * @Author: chenqiwei
	 * @Time:Feb 10, 2020
	 * @Description: 组装步骤节点信息
	 * @param @param processStepVo
	 * @param @param stepConfigObj
	 * @return void
	 */
	public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj);
}
