package codedriver.module.process.matrixattribute.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.matrixattribute.core.IMatrixAttributeHandler;
@Component
public class TeamMatrixHandler implements IMatrixAttributeHandler {

	@Autowired
	private TeamMapper teamMapper;
	
	@Override
	public String getType() {
		return ProcessMatrixAttributeType.TEAM.getValue();
	}

	@Override
	public JSONObject getData(ProcessMatrixAttributeVo processMatrixAttributeVo, String value) {
		JSONObject resultObj = new JSONObject();
		resultObj.put("type", ProcessMatrixAttributeType.TEAM.getValue());
		String[] split = value.split("#");
		resultObj.put("value", split[1]);
		TeamVo teamVo = teamMapper.getTeamByUuid(split[1]);
		if(teamVo != null) {
			resultObj.put("text", teamVo.getName());
		}else {
			resultObj.put("text", split[1]);
		}
		return resultObj;
	}

}
