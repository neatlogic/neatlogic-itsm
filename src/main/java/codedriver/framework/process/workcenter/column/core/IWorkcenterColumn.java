package codedriver.framework.process.workcenter.column.core;

import com.techsure.multiattrsearch.MultiAttrsObject;

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
	 * @Description: 字段是否允许排序
	 * @Param:
	 * @return: java.lang.String
	 * @Date: 2020/2/2
	 */
	public Boolean allowSort();

	/**
	 * @Description: 获取显示值
	 * @Param: 
	 * @return: java.lang.Object
	 * @Date: 2020/2/2
	 */
	public Object getValue(MultiAttrsObject el) throws RuntimeException;

}
