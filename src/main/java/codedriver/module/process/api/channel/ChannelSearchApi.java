package codedriver.module.process.api.channel;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Objects;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;
import codedriver.module.process.service.CatalogService;
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelSearchApi extends PrivateApiComponentBase {
	
	@Autowired
	private ChannelMapper channelMapper;

	@Autowired
	private ChannelTypeMapper channelTypeMapper;

	@Autowired
	private CatalogService catalogService;

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
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
        @Param(name = "channelTypeRelationId", type = ApiParamType.LONG, desc = "服务类型关系id"),
        @Param(name = "channelUuid", type = ApiParamType.STRING, desc = "服务uuid")
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
		resultObj.put("channelList", new ArrayList<>());
		ChannelVo channelVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ChannelVo>() {});
		/** 查询所有收藏的服务时，parentUuid设为空即可 **/
		if(channelVo.getIsFavorite() != null && channelVo.getIsFavorite() == 1 && "0".equals(channelVo.getParentUuid())){
			channelVo.setParentUuid(null);
		}
		channelVo.setUserUuid(UserContext.get().getUserUuid(true));
		boolean hasData = true;
		Integer isAuthenticate = jsonObj.getInteger("isAuthenticate");
		if(Objects.equal(isAuthenticate, 1)) {
		    List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid(true));
		    List<String> authorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), teamUuidList, UserContext.get().getRoleUuidList(), null);
		    if(CollectionUtils.isNotEmpty(authorizedChannelUuidList)) {
		        String channelUuid = jsonObj.getString("channelUuid");
	            if(StringUtils.isNotBlank(channelUuid) && channelMapper.checkChannelIsExists(channelUuid) == 0) {
	                throw new ChannelNotFoundException(channelUuid);
	            }
	            Long channelTypeRelationId = jsonObj.getLong("channelTypeRelationId");
	            if(channelTypeRelationId != null && channelTypeMapper.checkChannelTypeRelationIsExists(channelTypeRelationId) == 0) {
	                throw new ChannelTypeRelationNotFoundException(channelTypeRelationId);
	            }
	            if(StringUtils.isNotBlank(channelUuid) && channelTypeRelationId != null) {
	                List<String> channelRelationTargetChannelUuidList = catalogService.getChannelRelationTargetChannelUuidList(channelUuid, channelTypeRelationId);
	                if(CollectionUtils.isNotEmpty(channelRelationTargetChannelUuidList)) {
	                    channelVo.setAuthorizedUuidList(ListUtils.retainAll(authorizedChannelUuidList, channelRelationTargetChannelUuidList));
	                }                
	            }else {
	                channelVo.setAuthorizedUuidList(authorizedChannelUuidList);
	            }
		    }	    	        
			//查出当前用户已授权的服务
//			channelVo.setAuthorizedUuidList(catalogService.getCurrentUserAuthorizedChannelUuidList());
			channelVo.setIsActive(1);
			hasData = CollectionUtils.isNotEmpty(channelVo.getAuthorizedUuidList());
		}
		if(hasData) {
		    int pageCount = 0;
		    if(channelVo.getNeedPage()) {
	            int rowNum = channelMapper.searchChannelCount(channelVo);
	            pageCount = PageUtil.getPageCount(rowNum,channelVo.getPageSize());
	            resultObj.put("currentPage",channelVo.getCurrentPage());
	            resultObj.put("pageSize",channelVo.getPageSize());
	            resultObj.put("pageCount", pageCount);
	            resultObj.put("rowNum", rowNum);
	        }           
		    if(!channelVo.getNeedPage() || channelVo.getCurrentPage() <= pageCount) {
	            List<ChannelVo> channelList = channelMapper.searchChannelList(channelVo);       
	            resultObj.put("channelList", channelList);
		    }
		}		
		return resultObj;
	}

}
