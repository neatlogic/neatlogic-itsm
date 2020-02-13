package codedriver.framework.process.workerdispatcher.core;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.module.process.dto.ProcessTaskStepVo;

public interface IWorkerDispatcher {
	/**
	 * @Author: chenqiwei
	 * @Time:Jun 30, 2019
	 * @Description: 返回类名
	 * @param @return
	 * @return String
	 */
	public String getHandler();

	/**
	 * @Author: chenqiwei
	 * @Time:Jun 30, 2019
	 * @Description: 返回中文名
	 * @param @return
	 * @return String
	 */
	public String getName();

	/**
	 * @Author: chenqiwei
	 * @Time:Aug 26, 2019
	 * @Description: 获取分派器配置
	 * @param @return
	 * @return JSONObject
	 */
	public JSONArray getConfig();

	/**
	 * @Author: chenqiwei
	 * @Time:Aug 26, 2019
	 * @Description: 获取帮助
	 * @param @return
	 * @return String
	 */
	public String getHelp();

	/**
	 * 
	 * @Author: chenqiwei
	 * @Time:Jun 30, 2019
	 * @Description: 返回处理人
	 * @param @return
	 * @return String
	 */
	public List<String> getWorker(ProcessTaskStepVo processTaskStepVo, JSONObject configObj);
}
