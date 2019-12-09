package codedriver.framework.process.attribute.component;

import java.util.UUID;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.attribute.core.AttributeComponentBase;
import codedriver.module.process.constvalue.AttributeHandler;

public class SingleTeamComponent extends AttributeComponentBase {

	public static void main(String[] a) {
		System.out.println(UUID.randomUUID().toString());
	}

	@Override
	public String getUuid() {
		return "128e135acefb4e44b932e15062c4e5b2";
	}

	@Override
	public String getHandler() {
		return AttributeHandler.TEAM.getValue();
	}

	@Override
	public String getLabel() {
		return "单个组";
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
