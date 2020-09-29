package codedriver.module.process.api.agent;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.DELETE)
public class UserAgentDeleteApi extends PrivateApiComponentBase {

	@Autowired
	private UserMapper userMapper;
	
	@Override
	public String getToken() {
		return "user/agent/delete";
	}

	@Override
	public String getName() {
		return "删除用户授权代理";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({})
	@Output({})
	@Description(desc = "删除用户授权代理")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		userMapper.deleteUserAgent(UserContext.get().getUserUuid());
		return null;
	}

}