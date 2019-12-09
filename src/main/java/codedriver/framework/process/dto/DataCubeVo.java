package codedriver.framework.process.dto;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;

import codedriver.framework.common.dto.BasePageVo;

public class DataCubeVo extends BasePageVo implements Serializable {
	/**
	 * @Fields serialVersionUID : TODO
	 */
	private static final long serialVersionUID = -8581361535630166663L;
	private String uuid;
	private String name;
	private String type;
	private String typeName;
	private String value;
	private JSONArray valueList;
	private String sql;
	private String url;
	private List<DataCubeFieldVo> fieldList;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public JSONArray getValueList() {
		if (StringUtils.isNotBlank(value)) {
			try {
				valueList = JSONArray.parseArray(value);
			} catch (Exception ex) {

			}
		}
		return valueList;
	}

	public void setValueList(JSONArray valueList) {
		this.valueList = valueList;
	}

	public List<DataCubeFieldVo> getFieldList() {
		return fieldList;
	}

	public void setFieldList(List<DataCubeFieldVo> fieldList) {
		this.fieldList = fieldList;
	}

}
