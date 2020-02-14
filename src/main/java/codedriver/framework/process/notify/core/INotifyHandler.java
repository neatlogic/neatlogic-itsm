package codedriver.framework.process.notify.core;

import org.springframework.util.ClassUtils;

import codedriver.module.process.notify.dto.NotifyVo;

public interface INotifyHandler {
	/**
	 * @Description: 处理通知
	 * @Param: [informVo]
	 * @return: void
	 */
	public void execute(NotifyVo notifyVo);

	/**
	 * @Author: chenqiwei
	 * @Time:Jan 25, 2020
	 * @Description: 插件名称
	 * @param @return
	 * @return String
	 */
	public String getName();

	/**
	 * 
	 * @Author: chenqiwei
	 * @Time:Feb 12, 2020
	 * @Description: TODO
	 * @param @return
	 * @return String
	 */
	public default String getClassName() {
		return ClassUtils.getUserClass(this.getClass()).getName();
	}

}
