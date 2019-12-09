package codedriver.framework.process.attribute.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.attribute.core.IAttributeHandler;
import codedriver.framework.process.dto.AttributeVo;
import codedriver.framework.process.dto.ProcessTaskAttributeDataVo;
import codedriver.framework.process.exception.AttributeValidException;
import codedriver.module.process.constvalue.AttributeHandler;

@Component
public class UserHandler implements IAttributeHandler {

	@Autowired
	private UserMapper userMapper;

	@Override
	public String getType() {
		return AttributeHandler.USER.getValue();
	}

	@Override
	public boolean valid(ProcessTaskAttributeDataVo processTaskAttributeDataVo, JSONObject configObj) throws AttributeValidException {
		return true;
	}

	@Override
	public String getConfigPage() {
		return "process.attribute.handler.user.userconfig";
	}

	@Override
	public String getInputPage() {
		return "process.attribute.handler.user.userinput";
	}

	@Override
	public String getViewPage() {
		return "process.attribute.handler.user.userview";
	}

	@Override
	public Object getData(AttributeVo attributeVo, Map<String, String[]> paramMap) {
		UserVo userVo = new UserVo();
		userVo.setPageSize(20);
		if (paramMap != null && !paramMap.isEmpty()) {
			if (paramMap.containsKey("name") && paramMap.get("name").length > 0 && StringUtils.isNotBlank(paramMap.get("name")[0])) {
				userVo.setKeyword(paramMap.get("name")[0]);
			}
		}
		List<UserVo> userList = userMapper.searchUser(userVo);
		JSONArray returnList = new JSONArray();
		for (UserVo user : userList) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("value", user.getUserId());
			jsonObj.put("text", user.getUserName() + "[" + user.getUserId() + "]");
			returnList.add(jsonObj);
		}
		return returnList;
	}

	@Override
	public List<String> getValueList(Object data) {
		List<String> valueList = new ArrayList<>();
		try {
			JSONObject jsonObj = JSONObject.parseObject(data.toString());
			if (jsonObj.containsKey("value")) {
				valueList.add(jsonObj.getString("value"));
			}
		} catch (Exception ex) {

		}
		return valueList;
	}

	@Override
	public String getText(Object data) {
		try {
			JSONObject jsonObj = JSONObject.parseObject(data.toString());
			if (jsonObj.containsKey("text")) {
				return jsonObj.getString("text");
			}
		} catch (Exception ex) {

		}
		return "";
	}

}
