package neatlogic.module.process.api.agent;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.exception.user.AgentIsUserSelfException;
import neatlogic.framework.exception.user.UserAgentLoopException;
import neatlogic.framework.exception.user.UserAgentRepeatException;
import neatlogic.framework.exception.user.UserHasAgentException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Deprecated
//@Service
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class UserAgentSaveApi extends PrivateApiComponentBase {

	@Autowired
	private UserMapper userMapper;
	
	@Override
	public String getToken() {
		return "user/agent/save";
	}

	@Override
	public String getName() {
		return "保存用户授权代理";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "agentUuid", type = ApiParamType.STRING, isRequired = true,desc = "代理人uuid")
	})
	@Output({})
	@Description(desc = "保存用户授权代理")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
//		UserAgentVo userAgentVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<UserAgentVo>() {});
//		if(UserContext.get().getUserUuid().equals(userAgentVo.getAgentUuid())){
//			throw new AgentIsUserSelfException();
//		}
//		/** 检查当前用户是否已存在代理对象 */
//		if(userMapper.checkUserExistsInUserAgent(UserContext.get().getUserUuid()) > 0){
//			throw new UserHasAgentException();
//		}
//		/** 检查目标代理对象是否已经代理了其他用户 */
//		if(userMapper.checkAgentExistsInUserAgent(userAgentVo.getAgentUuid()) > 0){
//			UserVo agent = userMapper.getUserByUuid(userAgentVo.getAgentUuid());
//			throw new UserAgentRepeatException(agent.getUserId());
//		}
//		/** 如果A已经是B的代理人，则不允许A设置代理人为B */
//		if(userMapper.checkExistsAgentLoop(userAgentVo.getAgentUuid(),UserContext.get().getUserUuid()) > 0){
//			UserVo user = userMapper.getUserByUuid(UserContext.get().getUserUuid());
//			UserVo agent = userMapper.getUserByUuid(userAgentVo.getAgentUuid());
//			throw new UserAgentLoopException(user.getUserId(),agent.getUserId());
//		}
//		userAgentVo.setUserUuid(UserContext.get().getUserUuid());
//		userAgentVo.setFunc("processtask");
//		userMapper.insertUserAgent(userAgentVo);
		return null;
	}

}
