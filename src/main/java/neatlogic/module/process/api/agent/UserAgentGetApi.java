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
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class UserAgentGetApi extends PrivateApiComponentBase {

	@Resource
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
