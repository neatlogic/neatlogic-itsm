package codedriver.framework.process.workcenter.column.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.techsure.multiattrsearch.MultiAttrsObject;

import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.dao.cache.WorkcenterColumnDataCache;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.module.process.constvalue.ProcessWorkcenterCondition;

@Component
public class ProcessTaskOwnerColumn implements IWorkcenterColumn{

	@Autowired
	UserMapper userMapper;
	@Override
	public String getName() {
		return ProcessWorkcenterCondition.OWNER.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterCondition.OWNER.getName();
	}

	@Override
	public Object getValue(MultiAttrsObject el) throws RuntimeException {
		String userId = el.getString(this.getName());
		String userName = (String) WorkcenterColumnDataCache.getItem(userId);
		if(userName == null) {
			UserVo userVo =userMapper.getUserByUserId(userId);
			if(userVo != null) {
				userName = userVo.getUserName();
				WorkcenterColumnDataCache.addItem(userId, userName);
			}
		}
		return userName;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}

}
