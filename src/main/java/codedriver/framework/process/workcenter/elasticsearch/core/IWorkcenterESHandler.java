package codedriver.framework.process.workcenter.elasticsearch.core;

import java.util.List;

public interface IWorkcenterESHandler {

	/**
	 * 处理类
	 * @return
	 */
	public String getHandler();
	
	/**
	 * 处理类名
	 * @return
	 */
	public String getHandlerName();
	
	/**
	 * 执行动作
	 */
	public void doService(List<Object> params);
}
