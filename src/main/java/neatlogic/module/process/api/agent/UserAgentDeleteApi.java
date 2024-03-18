/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.api.agent;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import javax.annotation.Resource;

@Deprecated
//@Service
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = PROCESS_BASE.class)
public class UserAgentDeleteApi extends PrivateApiComponentBase {

	@Resource
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
//		userMapper.deleteUserAgent(UserContext.get().getUserUuid());
		return null;
	}

}
