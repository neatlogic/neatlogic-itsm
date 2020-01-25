package codedriver.framework.process.notify.core;

import codedriver.module.process.notify.dto.NotifyVo;

public interface INotifyHandler {
	/**
	 * @Description: 处理通知
	 * @Param: [informVo]
	 * @return: void
	 */
	public void execute(NotifyVo notifyVo);

	/**
	 * @Description: 插件ID
	 * @Param: []
	 * @return: java.lang.String
	 */
	public String getId();
	
	/**
	* @Author: chenqiwei
	* @Time:Jan 25, 2020
	* @Description: 插件名称 
	* @param @return 
	* @return String
	 */
	public String getName();

	/**
	 * @Description: 内容默认模板
	 * @Param: []
	 * @return: java.lang.String
	 */
	public String getTemplateContent();

	/**
	 * @Description: 标题默认模板
	 * @Param: []
	 * @return: java.lang.String
	 */
	public String getTemplateTitle();
}
