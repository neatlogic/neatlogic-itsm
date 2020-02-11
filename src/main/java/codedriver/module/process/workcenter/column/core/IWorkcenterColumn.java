package codedriver.module.process.workcenter.column.core;

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
	 * @Description: 获取显示值
	 * @Param: 
	 * @return: java.lang.Object
	 * @Date: 2020/2/2
	 */
	public Object getValue(MultiAttrsObject el) throws RuntimeException;

}
