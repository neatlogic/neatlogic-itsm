package codedriver.module.process.api.channel;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ChannelRoleVo;

@Service
@Transactional
public class ChannelRoleSearchApi extends ApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;
	
	@Override
	public String getToken() {
		return "channel/role/search";
	}

	@Override
	public String getName() {
		return "服务通道授权信息查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "channelUuid", type = ApiParamType.STRING, isRequired = true, desc = "服务通道uuid"),
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "模糊匹配角色名"),
		@Param(name = "isSelect", type = ApiParamType.ENUM, isRequired = false, desc = "1:已选择，0：未选择，不传查全部"),
		@Param(name = "typeList", type = ApiParamType.JSONARRAY, isRequired = false, desc = "权限类型，多选列表"),
		@Param(name = "typeList[0]", type = ApiParamType.ENUM, isRequired = false, desc = "权限类型", rule = "report,selfreport,replace,search"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
		})
	@Output({
		@Param(name = "channelUuid", type = ApiParamType.STRING, isRequired = true, desc = "服务通道uuid"),
		@Param(name = "roleName", type = ApiParamType.STRING, isRequired = true, desc = "角色名"),
		@Param(name = "roleDesc", type = ApiParamType.STRING, isRequired = true, desc = "角色描述"),
		@Param(name = "typeList", type = ApiParamType.JSONARRAY, isRequired = false, desc = "已选权限类型列表")
	})
	@Description(desc = "服务通道授权信息查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		ChannelRoleVo channelRoleVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ChannelRoleVo>() {});
		if(channelRoleVo.getNeedPage()) {
			int rowNum = channelMapper.searchChannelRoleNameCount(channelRoleVo);
			int pageCount = PageUtil.getPageCount(rowNum, channelRoleVo.getPageSize());
			channelRoleVo.setPageCount(pageCount);
			channelRoleVo.setRowNum(rowNum);
			resultObj.put("currentPage", channelRoleVo.getCurrentPage());
			resultObj.put("pageSize", channelRoleVo.getPageSize());
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
		}
		List<String> roleNameList = channelMapper.searchChannelRoleNameList(channelRoleVo);
		List<ChannelRoleVo> channelRoleList = channelMapper.searchChannelRoleList(roleNameList, channelRoleVo.getChannelUuid());
		return channelRoleList;
	}

}
