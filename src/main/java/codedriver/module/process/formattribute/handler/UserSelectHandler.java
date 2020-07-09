package codedriver.module.process.formattribute.handler;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessFormHandler;
import codedriver.framework.process.dto.AttributeDataVo;
import codedriver.framework.process.exception.form.AttributeValidException;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;
@Component
public class UserSelectHandler implements IFormAttributeHandler {

	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private TeamMapper teamMapper;
	
	@Autowired
	private RoleMapper roleMapper;
	
	@Override
	public String getType() {
		return ProcessFormHandler.FORMUSERSELECT.getHandler();
	}

	@Override
	public boolean valid(AttributeDataVo attributeDataVo, JSONObject configObj) throws AttributeValidException {
		return false;
	}

	@Override
	public Object getValue(AttributeDataVo attributeDataVo, JSONObject configObj) {
		String value = attributeDataVo.getData();
		List<String> valueList = null;
		boolean isMultiple = configObj.getBooleanValue("isMultiple");
		if(isMultiple) {
			valueList = JSON.parseArray(value, String.class);
			if(CollectionUtils.isNotEmpty(valueList)) {
				StringBuilder result = new StringBuilder();
				for(String key : valueList) {
					result.append("、");
					result.append(parse(key));
				}
				return result.toString().substring(1);
			}
		}else {
			if(StringUtils.isNotBlank(value)) {
				return parse(value);
			}
		}
		return value;
	}

	private String parse(String key) {
		if(key.contains("#")) {
			String[] split = key.split("#");
			if(GroupSearch.COMMON.getValue().equals(split[0])) {
				return UserType.getText(split[1]);
			}else if(GroupSearch.USER.getValue().equals(split[0])) {
				UserVo user = userMapper.getUserBaseInfoByUuid(split[1]);
				if(user != null) {
					return user.getUserName();
				}else {
					return split[1];
				}
			}else if(GroupSearch.TEAM.getValue().equals(split[0])) {
				TeamVo team = teamMapper.getTeamByUuid(split[1]);
				if(team != null) {
					return team.getName();
				}else {
					return split[1];
				}
			}else if(GroupSearch.ROLE.getValue().equals(split[0])) {
				RoleVo role = roleMapper.getRoleByUuid(split[1]);
				if(role != null) {
					return role.getName();
				}else {
					return split[1];
				}
			}
		}
		return key;
	}
}
