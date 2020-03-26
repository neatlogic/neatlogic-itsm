package codedriver.module.process.workcenter.column.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.cache.WorkcenterColumnDataCache;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnBase;

@Component
public class ProcessTaskOwnerColumn extends WorkcenterColumnBase implements IWorkcenterColumn{

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
		String userId = json.getString(this.getName());
		String cacheKey = GroupSearch.USER+"#"+userId;
		String userName = (String) WorkcenterColumnDataCache.getItem(cacheKey);
		if(userName == null) {
			UserVo userVo =userMapper.getUserByUserId(userId);
			if(userVo != null) {
				userName = userVo.getUserName();
				WorkcenterColumnDataCache.addItem(cacheKey, userName);
			}
		}
		return userName;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

}
