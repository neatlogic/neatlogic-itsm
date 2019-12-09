package codedriver.framework.process.attribute.component;

import java.util.UUID;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.attribute.core.AttributeComponentBase;
import codedriver.module.process.constvalue.AttributeHandler;

public class SingleUserComponent extends AttributeComponentBase {

	public static void main(String[] a) {
		System.out.println(UUID.randomUUID().toString());
	}

	@Override
	public String getUuid() {
		return "4bb2005fb937433e93d53bb8f4790fa0";
	}

	@Override
	public String getHandler() {
		return AttributeHandler.USER.getValue();
	}

	@Override
	public String getLabel() {
		return "单个用户";
	}

	@Override
	public JSONObject getConfigObj() {
		return new JSONObject();
	}

	@Override
	public String getUnit() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}
}
