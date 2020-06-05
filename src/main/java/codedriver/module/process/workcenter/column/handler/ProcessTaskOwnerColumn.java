package codedriver.module.process.workcenter.column.handler;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;

@Component
public class ProcessTaskOwnerColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{
	@Autowired
	UserMapper userMapper;
	@Override
	public String getName() {
		return "owner";
	}

	@Override
	public String getDisplayName() {
		return "上报人";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		JSONObject userJson = new JSONObject();
		String userUuid = json.getString(this.getName());
		UserVo userVo =userMapper.getUserBaseInfoByUuid(userUuid.replaceFirst(GroupSearch.USER.getValuePlugin(), StringUtils.EMPTY));
		if(userVo != null) {
			userJson.put("username", userVo.getUserName());
		}
		userJson.put("useruuid", userUuid);
		return userJson;
	}
	
	@Override
	public JSONObject getMyValueText(JSONObject json) {
		JSONObject userJson = new JSONObject();
		String userUuid = json.getString(this.getName());
		UserVo userVo =userMapper.getUserBaseInfoByUuid(userUuid.replaceFirst(GroupSearch.USER.getValuePlugin(), StringUtils.EMPTY));
		if(userVo != null) {
			userJson.put("text", userVo.getUserName());
			userJson.put("value", userVo.getUserId());
		}
		return userJson;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getSort() {
		return 3;
	}

}
