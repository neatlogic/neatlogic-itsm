/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.api.agent;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
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
		return "nmpaa.useragentgetapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({
			@Param(name = "uuid", type = ApiParamType.STRING, desc = "nmpaa.useragentgetapi.output.param.desc.uuid"),
			@Param(name = "userId", type = ApiParamType.STRING, desc = "nmpaa.useragentgetapi.output.param.desc.userid"),
			@Param(name = "userName", type = ApiParamType.STRING, desc = "nmpaa.useragentgetapi.output.param.desc.username"),
			@Param(name = "email", type = ApiParamType.STRING, desc = "nmpaa.useragentgetapi.output.param.desc.email"),
			@Param(name = "phone", type = ApiParamType.STRING, desc = "nmpaa.useragentgetapi.output.param.desc.phone"),
			@Param(name = "pinyin", type = ApiParamType.STRING, desc = "nmpaa.useragentgetapi.output.param.desc.pinyin"),
			@Param(name = "isActive", type = ApiParamType.STRING, desc = "nmpaa.useragentgetapi.output.param.desc.isactive"),
			@Param(name = "userInfo", type = ApiParamType.STRING, desc = "nmpaa.useragentgetapi.output.param.desc.userinfo"),
			@Param(name = "teamList", type = ApiParamType.STRING, desc = "nmpaa.useragentgetapi.output.param.desc.teamlist"),
			@Param(name = "roleList", type = ApiParamType.STRING, desc = "nmpaa.useragentgetapi.output.param.desc.rolelist"),
			@Param(name = "userAuthList", type = ApiParamType.STRING, desc = "nmpaa.useragentgetapi.output.param.desc.userauthlist"),
			@Param(name = "roleUuidList", type = ApiParamType.STRING, desc = "nmpaa.useragentgetapi.output.param.desc.roleuuidlist"),
			@Param(name = "teamUuidList", type = ApiParamType.STRING, desc = "nmpaa.useragentgetapi.output.param.desc.teamuuidlist")
	})
	@Description(desc = "nmpaa.useragentgetapi.getname")
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
