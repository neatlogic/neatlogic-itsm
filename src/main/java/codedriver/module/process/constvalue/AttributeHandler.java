package codedriver.module.process.constvalue;

public enum AttributeHandler {
	TEXT("text", "单行文本","单行文本输入，输入长度不能超过200字符。"),
	LONGTEXT("longtext","多行文本","多行文本输入，输入长度不限制，但输入值不可用于搜索。"), 
	DECIMAL("decimal", "小数","小数输入，可加入单位。"), 
	INTEGER("integer", "整数","整数输入，可加入单位。"),
	CALENDAR("calendar","日历","日历输入，可调整格式"),
	SELECT("select","单选下拉","下拉菜单列表，只能选择一项"),
	MSELECT("mselect","多选下拉","下拉菜单列表，可以选择多项"),
	RADIO("radio","单选","单选框，只能选择一项"),
	CHECKBOX("checkbox","多选","多选框，可以选择多项"),
	INPUTSELECT("inputselect","可搜索下拉","可搜索下拉菜单列表，根据关键字到指定接口获取选项，支持单选或多选"),
	FILE("file","附件","上传附件"),
	USER("user","单个用户","单个用户选择组件"),
	MUSER("muser","多个用户","多个用户选择组件"),
	TEAM("team","单个组织","单个组织选择组件"),
	MTEAM("mteam","多个组织","多个组织选择组件"),
	CUSTOM("custom","自定义","自定义组件，需要自己编写模板");
	private String value;
	private String name;
	private String description;

	private AttributeHandler(String _value, String _name, String _description) {
		this.value = _value;
		this.name = _name;
		this.description = _description;
	}

	public String getValue() {
		return value;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}

	public static String getValue(String _value) {
		for (AttributeHandler s : AttributeHandler.values()) {
			if (s.getValue().equals(_value)) {
				return s.getValue();
			}
		}
		return null;
	}

	public static String getName(String _value) {
		for (AttributeHandler s : AttributeHandler.values()) {
			if (s.getValue().equals(_value)) {
				return s.getName();
			}
		}
		return "";
	}
	
	public static String getDescription(String _value) {
		for (AttributeHandler s : AttributeHandler.values()) {
			if (s.getValue().equals(_value)) {
				return s.getDescription();
			}
		}
		return "";
	}
}
