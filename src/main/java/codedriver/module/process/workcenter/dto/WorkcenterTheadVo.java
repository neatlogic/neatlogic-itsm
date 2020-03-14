package codedriver.module.process.workcenter.dto;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.restful.annotation.EntityField;
import codedriver.module.process.constvalue.ProcessWorkcenterType;

public class WorkcenterTheadVo {

	@JSONField(serialize = false)
	@EntityField(name = "工单中心分类唯一标识段", type = ApiParamType.STRING)
	private String workcenterUuid;
	@EntityField(name = "字段名（表单属性则存属性uuid）", type = ApiParamType.STRING)
	private String name ;
	@EntityField(name = "字段中文名", type = ApiParamType.STRING)
	private String displayName ;
	@EntityField(name = "字段排序", type = ApiParamType.INTEGER)
	private Integer sort ;
	@EntityField(name = "字段宽度", type = ApiParamType.INTEGER)
	private Integer width ;
	@EntityField(name = "字段是否展示", type = ApiParamType.INTEGER)
	private Integer isShow ;
	@JSONField(serialize = false)
	@EntityField(name = "所属用户", type = ApiParamType.STRING)
	private String userId;
	@EntityField(name = "字段类型", type = ApiParamType.STRING)
	private String type ;
	
	public WorkcenterTheadVo(JSONObject obj) {
		this.name = obj.getString("name");
		this.sort = obj.getInteger("sort");
		this.width = obj.getInteger("width");
		this.isShow = obj.getInteger("isShow");
		this.userId = UserContext.get().getUserId();
		this.type = obj.getString("type");
	}
	
	public WorkcenterTheadVo(IWorkcenterColumn column) {
		this.name = column.getName();
		this.sort = null;
		this.width = null;
		this.isShow = 0;
		this.userId = UserContext.get().getUserId();
		this.type = FormType.Process.getValue();
	}
	
	public WorkcenterTheadVo(String _workcenterUuid,String _userId) {
		this.workcenterUuid = _workcenterUuid;
		this.userId = _userId;
	}
		
	public WorkcenterTheadVo() {
		
	}
	public String getWorkcenterUuid() {
		return workcenterUuid;
	}
	public void setWorkcenterUuid(String workcenterUuid) {
		this.workcenterUuid = workcenterUuid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getSort() {
		return sort;
	}
	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getIsShow() {
		return isShow;
	}

	public void setIsShow(Integer isShow) {
		this.isShow = isShow;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public enum FormType {
		Process("process", "工单固有属性"), FORM("form", "表单属性");
		private String value;
		private String name;

		private FormType(String _value, String _name) {
			this.value = _value;
			this.name = _name;
		}

		public String getValue() {
			return value;
		}

		public String getName() {
			return name;
		}

		public static String getValue(String _value) {
			for (ProcessWorkcenterType s : ProcessWorkcenterType.values()) {
				if (s.getValue().equals(_value)) {
					return s.getValue();
				}
			}
			return null;
		}

		public static String getName(String _value) {
			for (ProcessWorkcenterType s : ProcessWorkcenterType.values()) {
				if (s.getValue().equals(_value)) {
					return s.getName();
				}
			}
			return "";
		}

	}
}
