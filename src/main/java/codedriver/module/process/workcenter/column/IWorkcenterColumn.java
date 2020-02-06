package codedriver.module.process.workcenter.column;

import com.alibaba.fastjson.JSONObject;

public interface IWorkcenterColumn {
	
	/**
	 * @Description: 字段英文名
	 * @Param:
	 * @return: java.lang.String
	 * @Date: 2020/2/2
	 */
	public String getName();
	
	/**
	 * @Description: 字段显示名
	 * @Param:
	 * @return: java.lang.String
	 * @Date: 2020/2/2
	 */
	public String getDisplayName();

	/**
	 * @Description: 获取字段类路径
	 * @Param: 
	 * @return: java.lang.String
	 * @Date: 2020/2/2
	 */
	public String getHandlerName();

	/**
	 * @Description: 是否允许排序
	 * @Param: 
	 * @return: java.lang.Integer
	 * @Date: 2020/2/2
	 */
	public Integer allowSort();
	
	/**
	 * @Description: 字段排序，越小越靠前
	 * @Param: 
	 * @return: java.lang.Integer
	 * @Date: 2020/2/2
	 */
	public Integer getSort();

	/**
	 * @Description: 获取显示值
	 * @Param: 
	 * @return: java.lang.Object
	 * @Date: 2020/2/2
	 */
	public Object getValue() throws RuntimeException;
	
	/**
	 * @Description: 获取过滤条件
	 * @Param: 
	 * @return: com.alibaba.fastjson.JSONObject
	 * @Date: 2020/2/2
	 */
	public abstract JSONObject getCondition(JSONObject conditionObj);

	


}
