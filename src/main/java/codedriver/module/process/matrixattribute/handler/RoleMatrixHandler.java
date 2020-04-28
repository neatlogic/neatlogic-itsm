package codedriver.module.process.matrixattribute.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.matrixattribute.core.IMatrixAttributeHandler;
@Component
public class RoleMatrixHandler implements IMatrixAttributeHandler {

	@Autowired
	private RoleMapper roleMapper;
	
	@Override
	public String getType() {
		return ProcessMatrixAttributeType.ROLE.getValue();
	}

	@Override
	public JSONObject getData(ProcessMatrixAttributeVo processMatrixAttributeVo, String value) {
		JSONObject resultObj = new JSONObject();
		resultObj.put("type", ProcessMatrixAttributeType.ROLE.getValue());
		String[] split = value.split("#");
		resultObj.put("value", split[1]);
		RoleVo roleVo = roleMapper.getRoleByRoleName(split[1]);
		if(roleVo != null) {
			resultObj.put("text", roleVo.getDescription());
		}else {
			resultObj.put("text", split[1]);
		}
		return resultObj;
	}

}
