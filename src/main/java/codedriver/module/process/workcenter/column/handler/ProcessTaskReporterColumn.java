package codedriver.module.process.workcenter.column.handler;

import org.apache.commons.lang3.StringUtils;
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
public class ProcessTaskReporterColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{
	@Autowired
	UserMapper userMapper;
	@Override
	public String getName() {
		return "reporter";
	}

	@Override
	public String getDisplayName() {
		return "代报人";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		JSONObject userJson = new JSONObject();
		String userUuid = json.getString(this.getName());
		if(StringUtils.isNotBlank(userUuid)) {
			userUuid =userUuid.replaceFirst(GroupSearch.USER.getValuePlugin(), StringUtils.EMPTY);
		}
		UserVo userVo =userMapper.getUserBaseInfoByUuid(userUuid);
		if(userVo != null) {
			userJson.put("username", userVo.getUserName());
		}
		userJson.put("useruuid", userUuid);
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
		return 4;
	}

}
