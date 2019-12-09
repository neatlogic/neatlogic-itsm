package codedriver.framework.process.attribute.core;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.dto.AttributeVo;
import codedriver.framework.process.dto.ProcessTaskAttributeDataVo;
import codedriver.framework.process.exception.AttributeValidException;

public interface IAttributeHandler {
	public String getType();

	public boolean valid(ProcessTaskAttributeDataVo processTaskAttrDataVo, JSONObject configObj) throws AttributeValidException;

	public String getConfigPage();

	public String getInputPage();

	public String getViewPage();

	/**
	 * @Author: chenqiwei
	 * @Time:Aug 8, 2019
	 * @Description: 获取组件数据，例如下拉框的数据
	 * @param @param
	 *            attributeVo
	 * @param @param
	 *            paramMap 通过request传过来的queryString
	 * @param @return
	 * @return Object
	 */
	public Object getData(AttributeVo attributeVo, Map<String, String[]> paramMap);

	/**
	 * @Author: chenqiwei
	 * @Time:Sep 11, 2019
	 * @Description: 根据原始数据获取value值
	 * @param @param
	 *            dataObj
	 * @param @return
	 * @return String
	 */
	public List<String> getValueList(Object dataObj);

	/**
	 * @Author: chenqiwei
	 * @Time:Sep 12, 2019
	 * @Description: 返回显示文本
	 * @param @param
	 *            dataObj
	 * @param @return
	 * @return String
	 */
	public String getText(Object dataObj);
}
