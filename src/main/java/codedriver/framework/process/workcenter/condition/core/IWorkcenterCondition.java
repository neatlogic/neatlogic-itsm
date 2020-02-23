package codedriver.framework.process.workcenter.condition.core;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

import codedriver.module.process.constvalue.ProcessExpression;

public interface IWorkcenterCondition {
	
	/**
	 * @Description: 条件英文名
	 * @Param:
	 * @return: java.lang.String
	 * @Date: 2020/2/11
	 */
	public String getName();
	
	/**
	 * @Description: 条件显示名
	 * @Param:
	 * @return: java.lang.String
	 * @Date: 2020/2/11
	 */
	public String getDisplayName();

	/**
	 * @Description: 获取控件类型
	 * @Param: 
	 * @return: java.lang.String
	 * @Date: 2020/2/11
	 */
	public String getHandler();
	
	/**
	 * @Description: 获取类型
	 * @Param: 
	 * @return: java.lang.String
	 * @Date: 2020/2/11
	 */
	public String getType();
	
	/**
	 * @Description: 获取控件配置
	 * @Param: 
	 * @return: com.alibaba.fastjson.JSONObject
	 * @Date: 2020/2/11
	 */
	public JSONObject getConfig();
	
	/**
	 * @Description: 获取控件页面显示排序，越小越靠前
	 * @Param: 
	 * @return: java.lang.Integer
	 * @Date: 2020/2/11
	 */
	public Integer getSort();
	
	/**
	 * @Description: 后端查询es使用
	 * @Param: 
	 * @return: java.lang.Integer
	 * @Date: 2020/2/11
	 */
	public List<ProcessExpression> getExpressionList();

}
