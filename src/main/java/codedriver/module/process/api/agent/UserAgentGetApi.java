package codedriver.module.process.api.agent;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Deprecated
//@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class UserAgentGetApi extends PrivateApiComponentBase {

	@Autowired
	private UserMapper userMapper;
	
	@Override
	public String getToken() {
		return "user/agent/get";
	}

	@Override
	public String getName() {
		return "获取用户授权代理";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({})
	@Output({
			@Param(name = "uuid", type = ApiParamType.STRING, desc = "用户uuid"),
			@Param(name = "userId", type = ApiParamType.STRING, desc = "用户userId"),
			@Param(name = "userName", type = ApiParamType.STRING, desc = "用户名"),
			@Param(name = "email", type = ApiParamType.STRING, desc = "邮箱"),
			@Param(name = "phone", type = ApiParamType.STRING, desc = "电话"),
			@Param(name = "pinyin", type = ApiParamType.STRING, desc = "拼音"),
			@Param(name = "isActive", type = ApiParamType.STRING, desc = "是否激活"),
			@Param(name = "userInfo", type = ApiParamType.STRING, desc = "额外属性"),
			@Param(name = "teamList", type = ApiParamType.STRING, desc = "分组列表"),
			@Param(name = "roleList", type = ApiParamType.STRING, desc = "角色列表"),
			@Param(name = "userAuthList", type = ApiParamType.STRING, desc = "权限列表"),
			@Param(name = "roleUuidList", type = ApiParamType.STRING, desc = "角色uuid列表"),
			@Param(name = "teamUuidList", type = ApiParamType.STRING, desc = "分组uuid列表")
	})
	@Description(desc = "获取用户授权代理")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
//		UserVo agent = userMapper.getUserAgent(UserContext.get().getUserUuid());
//		if(agent != null){
//			List<UserAuthVo> userAuthList = userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(UserContext.get().getUserUuid()));
//			agent.setUserAuthList(userAuthList);
//		}
//		JSONObject result = new JSONObject();
//		result.put("agent",agent);
//		return result;
		return null;
	}

}
