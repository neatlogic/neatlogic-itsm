/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.api.agent;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import javax.annotation.Resource;

@Deprecated
//@Service
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class UserAgentSaveApi extends PrivateApiComponentBase {

	@Resource
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
