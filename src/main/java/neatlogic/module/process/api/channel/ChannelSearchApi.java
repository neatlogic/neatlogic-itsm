package neatlogic.module.process.api.channel;

import java.util.ArrayList;
import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.ChannelTypeMapper;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Objects;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;
import neatlogic.module.process.service.CatalogService;

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

	@Override
	public String getToken() {
		return "process/channel/search";
	}

	@Override
	public String getName() {
		return "nmpac.channelsearchapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "common.keyword"),
		@Param(name = "parentUuid", type = ApiParamType.STRING, desc = "term.itsm.cataloguuid"),
		@Param(name = "isFavorite", type = ApiParamType.ENUM, desc = "nmpac.channelsearchapi.input.param.desc.isfavorite", rule = "0,1", help = "1：已收藏，0：全部"),
		@Param(name = "isActive", type = ApiParamType.ENUM, desc = "common.isactive", rule = "0,1"),
		@Param(name = "isAuthenticate", type = ApiParamType.ENUM, desc = "common.isauthenticate", rule = "0,1"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "common.isneedpage"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
        @Param(name = "channelTypeRelationId", type = ApiParamType.LONG, desc = "term.itsm.channeltyperelationid"),
        @Param(name = "channelUuid", type = ApiParamType.STRING, desc = "term.itsm.channeluuid")
		})
	@Output({
		@Param(explode = BasePageVo.class),
		@Param(name="channelList",explode=ChannelVo[].class,desc="common.tbodylist")
	})
	@Description(desc = "nmpac.channelsearchapi.getname")
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
			AuthenticationInfoVo authenticationInfoVo = UserContext.get().getAuthenticationInfoVo();
		    List<String> authorizedChannelUuidList = channelMapper.getAuthorizedChannelUuidList(UserContext.get().getUserUuid(true), authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), null);
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
