package codedriver.module.process.api.channel;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelRoleVo;
import codedriver.framework.process.exception.channel.ChannelIllegalParameterException;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@Transactional
public class ChannelRoleSaveApi extends ApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;
	@Autowired
	private RoleMapper roleMapper;
	
	@Override
	public String getToken() {
		return "process/channel/role/save";
	}

	@Override
	public String getName() {
		return "服务通道授权控制接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "channelUuid", type = ApiParamType.STRING, isRequired= true, desc = "服务通道uuid"),
		@Param(name = "roleNameList", type = ApiParamType.JSONARRAY, isRequired= true, desc = "角色名列表"),
		@Param(name = "type", type = ApiParamType.ENUM, isRequired= true, desc = "权限类型", rule = "report,selfreport,replace,search"),
		@Param(name = "action", type = ApiParamType.ENUM, isRequired= true, desc = "1：授权，0：取消", rule = "0,1")
		})
	@Description(desc = "服务通道授权控制接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ChannelRoleVo channelRole = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ChannelRoleVo>() {});
		if(channelMapper.checkChannelIsExists(channelRole.getChannelUuid()) == 0) {
			throw new ChannelNotFoundException(channelRole.getChannelUuid());
		}
		int action = jsonObj.getIntValue("action");
		List<String> roleNameList = channelRole.getRoleNameList();
		if(roleNameList != null && roleNameList.size() > 0) {
			for(String roleName : roleNameList) {
				if(roleMapper.getRoleByRoleName(roleName) == null) {
					throw new ChannelIllegalParameterException("角色：'" + roleName + "'不存在");
				}
				channelRole.setRoleName(roleName);
				if(action == 1) {					
					channelMapper.replaceChannelRole(channelRole);
				}else {
					channelMapper.deleteChannelRole(channelRole);
				}
			}
		}
		
		return null;
	}

}
