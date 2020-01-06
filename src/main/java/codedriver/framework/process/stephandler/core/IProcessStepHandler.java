package codedriver.framework.process.stephandler.core;

import java.util.List;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import codedriver.framework.process.exception.ProcessTaskException;
import codedriver.module.process.constvalue.ProcessStepMode;
import codedriver.module.process.dto.ProcessTaskStepVo;

//需要把事务隔离级别调低，避免并发insert时因为gap lock导致deadlock
public interface IProcessStepHandler {
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
	 * @Author: chenqiwei
	 * @Time:Aug 6, 2019
	 * @Description: 获取处理页面
	 * @param @return
	 * @return String
	 */
	public String getEditPage();

	/**
	 * @Author: chenqiwei
	 * @Time:Aug 6, 2019
	 * @Description: 获取只读页面
	 * @param @return
	 * @return String
	 */
	public String getViewPage();

	/**
	 * 
	 * @Author: chenqiwei
	 * @Time:May 24, 2019
	 * @Description: 激活流程步骤
	 * @param @param
	 *            workflowStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED,
			isolation = Isolation.READ_COMMITTED)
	public int active(ProcessTaskStepVo processTaskStepVo);

	/**
	 * 
	 * @Author: chenqiwei
	 * @Time:May 24, 2019
	 * @Description: 分配处理人
	 * @param @param
	 *            workflowStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED,
			isolation = Isolation.READ_COMMITTED)
	public int assign(ProcessTaskStepVo processTaskStepVo) throws ProcessTaskException;

	/**
	 * 
	 * @Author: chenqiwei
	 * @Time:May 24, 2019
	 * @Description: 挂起流程步骤
	 * @param @param
	 *            workflowStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED,
			isolation = Isolation.READ_COMMITTED)
	public int hang(ProcessTaskStepVo processTaskStepVo);

	/**
	 * 
	 * @Author: chenqiwei
	 * @Time:May 24, 2019
	 * @Description: 开始流程步骤
	 * @param @param
	 *            workflowStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED,
			isolation = Isolation.READ_COMMITTED)
	public int start(ProcessTaskStepVo processTaskStepVo);

	/**
	 * @Author: chenqiwei
	 * @Time:Aug 6, 2019
	 * @Description: 处理流程步骤
	 * @param @param
	 *            processtaskStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED,
			isolation = Isolation.READ_COMMITTED)
	public int handle(ProcessTaskStepVo processtaskStepVo);

	/**
	 * @Author: chenqiwei
	 * @Time:Aug 5, 2019
	 * @Description: 接受流程步骤
	 * @param @param
	 *            processTaskStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED,
			isolation = Isolation.READ_COMMITTED)
	public int accept(ProcessTaskStepVo processTaskStepVo);

	/**
	 * @Author: chenqiwei
	 * @Time:Aug 7, 2019
	 * @Description: 转移流程步骤
	 * @param @param
	 *            processTaskStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED,
			isolation = Isolation.READ_COMMITTED)
	public int transfer(ProcessTaskStepVo processTaskStepVo);

	/**
	 * @Author: chenqiwei
	 * @Time:Oct 9, 2019
	 * @Description: 暂存步骤信息
	 * @param @param
	 *            processTaskStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED,
			isolation = Isolation.READ_COMMITTED)
	public int save(ProcessTaskStepVo processTaskStepVo);

	/**
	 * 
	 * @Author: chenqiwei
	 * @Time:May 24, 2019
	 * @Description: 完成流程步骤
	 * @param @param
	 *            workflowStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED,
			isolation = Isolation.READ_COMMITTED)
	public int complete(ProcessTaskStepVo processTaskStepVo);

	/**
	 * @Author: chenqiwei
	 * @Time:Sep 16, 2019
	 * @Description: 上一步发起的撤回动作
	 * @param @param
	 *            processTaskStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED,
			isolation = Isolation.READ_COMMITTED)
	public int retreat(ProcessTaskStepVo processTaskStepVo);

	/**
	 * 
	 * @Author: chenqiwei
	 * @Time:Jun 19, 2019
	 * @Description: 处理人终止流程步骤
	 * @param @param
	 *            workflowTaskStepUserVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED,
			isolation = Isolation.READ_COMMITTED)
	public int abort(ProcessTaskStepVo processTaskStepVo);

	/**
	 * 
	 * @Author: chenqiwei
	 * @Time:Aug 5, 2019
	 * @Description: 获取当前步骤满足流转条件的后置步骤
	 * @param @return
	 * @return List<ProcessTaskStepVo>
	 */
	public List<ProcessTaskStepVo> getNext(ProcessTaskStepVo processTaskStepVo);

	/**
	 * @Author: chenqiwei
	 * @Time:Aug 7, 2019
	 * @Description: 开始流程，将会创建一个作业
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED,
			isolation = Isolation.READ_COMMITTED)
	public int startProcess(ProcessTaskStepVo processTaskStepVo);

	/**
	 * @Author:
	 * @Time:
	 * @Description: 回退步骤
	 * @param @param
	 *            processTaskStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED,
			isolation = Isolation.READ_COMMITTED)
	public int back(ProcessTaskStepVo processTaskStepVo);

	/**
	 * @Author:
	 * @Time:
	 * @Description: 回复
	 * @param @param
	 *            processTaskStepVo
	 * @param @return
	 * @return int
	 */
	@Transactional(propagation = Propagation.REQUIRED,
			isolation = Isolation.READ_COMMITTED)
	public int comment(ProcessTaskStepVo processTaskStepVo);

}
