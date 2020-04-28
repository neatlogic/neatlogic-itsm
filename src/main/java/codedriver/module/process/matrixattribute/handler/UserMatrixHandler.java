package codedriver.module.process.matrixattribute.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.matrixattribute.core.IMatrixAttributeHandler;
@Component
public class UserMatrixHandler implements IMatrixAttributeHandler {

	@Autowired
	private UserMapper userMapper;
	
	@Override
	public String getType() {
		return ProcessMatrixAttributeType.USER.getValue();
	}

	@Override
	public JSONObject getData(ProcessMatrixAttributeVo processMatrixAttributeVo, String value) {
		JSONObject resultObj = new JSONObject();
		resultObj.put("type", ProcessMatrixAttributeType.USER.getValue());
		String[] split = value.split("#");
		resultObj.put("value", split[1]);
		UserVo userVo = userMapper.getUserBaseInfoByUserId(split[1]);
		if(userVo != null) {
			resultObj.put("text", userVo.getUserName());
		}else {
			resultObj.put("text", split[1]);
		}
		return resultObj;
	}

}
