package codedriver.framework.process.attribute.core;

import com.alibaba.fastjson.JSONObject;

public abstract class AttributeComponentBase {
	public abstract String getUuid();

	public abstract String getHandler();

	public abstract String getLabel();

	public abstract JSONObject getConfigObj();

	public abstract String getUnit();

	public abstract String getDescription();
}
