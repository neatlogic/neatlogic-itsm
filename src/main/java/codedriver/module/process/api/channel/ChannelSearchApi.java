package codedriver.module.process.api.channel;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class ChannelSearchApi extends ApiComponentBase {
	
	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private TeamMapper teamMapper;
	
	@Override
	public String getToken() {
		return "process/channel/search";
	}

	@Override
	public String getName() {
		return "服务通道搜索接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字，匹配名称"),
		@Param(name = "parentUuid", type = ApiParamType.STRING, desc = "服务目录uuid"),
		@Param(name = "isFavorite", type = ApiParamType.ENUM, desc = "是否只查询已收藏的数据，1：已收藏，0：全部", rule = "0,1"),
		@Param(name = "isActive", type = ApiParamType.ENUM, desc = "是否激活", rule = "0,1"),
		@Param(name = "isAuthenticate", type = ApiParamType.ENUM, desc = "是否需要鉴权", rule = "0,1"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
		})
	@Output({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
		@Param(name="channelList",explode=ChannelVo[].class,desc="服务通道列表")
	})
	@Description(desc = "服务通道搜索接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		ChannelVo channelVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ChannelVo>() {});
		channelVo.setUserUuid(UserContext.get().getUserUuid(true));
		Integer isAuthenticate = jsonObj.getInteger("isAuthenticate");
		if(isAuthenticate != null && isAuthenticate.intValue() == 1) {
			List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
			//查出当前用户已授权的服务
			List<String> currentUserAuthorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), null);
			channelVo.setAuthorizedUuidList(currentUserAuthorizedChannelUuidList);
			channelVo.setIsActive(1);
		}
		if(channelVo.getNeedPage()) {
			int rowNum = channelMapper.searchChannelCount(channelVo);
			int pageCount = PageUtil.getPageCount(rowNum,channelVo.getPageSize());
			channelVo.setPageCount(pageCount);
			channelVo.setRowNum(rowNum);
			resultObj.put("currentPage",channelVo.getCurrentPage());
			resultObj.put("pageSize",channelVo.getPageSize());
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
		}			
		List<ChannelVo> channelList = channelMapper.searchChannelList(channelVo);		
		resultObj.put("channelList", channelList);
		return resultObj;
	}

}
