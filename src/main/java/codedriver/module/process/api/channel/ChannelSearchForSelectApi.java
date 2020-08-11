package codedriver.module.process.api.channel;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.CatalogService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelSearchForSelectApi extends ApiComponentBase {
	
	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private CatalogService catalogService;
	
	@Override
	public String getToken() {
		return "process/channel/search/forselect";
	}

	@Override
	public String getName() {
		return "查询服务通道_下拉框";
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
		@Param(name="list",explode=ValueTextVo[].class,desc="服务通道列表")
	})
	@Description(desc = "查询服务通道_下拉框")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		ChannelVo channelVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ChannelVo>() {});
		channelVo.setUserUuid(UserContext.get().getUserUuid(true));
		Integer isAuthenticate = jsonObj.getInteger("isAuthenticate");
		if(Objects.equal(isAuthenticate, 1)) {
			//查出当前用户已授权的服务
			channelVo.setAuthorizedUuidList(catalogService.getCurrentUserAuthorizedChannelUuidList());
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
		List<ValueTextVo> channelList = channelMapper.searchChannelListForSelect(channelVo);
		resultObj.put("list", channelList);
		return resultObj;
	}

}
