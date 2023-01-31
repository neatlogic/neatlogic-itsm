package neatlogic.module.process.api.channel;

import java.util.Iterator;
import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.CatalogService;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelSearchForSelectApi extends PrivateApiComponentBase {
	
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
		@Param(name = "uuidList", type = ApiParamType.JSONARRAY, desc = "服务uuid列表"),
		@Param(name = "defaultValue", type = ApiParamType.JSONARRAY,  desc = "用于回显的参数列表", xss = true),
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
		ChannelVo channelVo = JSONObject.toJavaObject(jsonObj, ChannelVo.class);
		channelVo.setUserUuid(UserContext.get().getUserUuid(true));
		Integer isAuthenticate = jsonObj.getInteger("isAuthenticate");
		if(Objects.equal(isAuthenticate, 1)) {
            //查出当前用户已授权的服务
            channelVo.setAuthorizedUuidList(catalogService.getCurrentUserAuthorizedChannelUuidList());
            channelVo.setIsActive(1);
        }
		//回显服务
		JSONArray defaultValue = channelVo.getDefaultValue();
        if(CollectionUtils.isNotEmpty(defaultValue)) {
            List<String> authorizationUuidList = channelVo.getAuthorizedUuidList();
            if(CollectionUtils.isNotEmpty(authorizationUuidList)) {
                Iterator<String> authUuidIterator = authorizationUuidList.iterator();
                while(authUuidIterator.hasNext()) {
                    String uuid = authUuidIterator.next();
                    if(!defaultValue.contains(uuid)) {
                        authUuidIterator.remove();
                    }
                }
            }else {
                channelVo.setAuthorizedUuidList(defaultValue.toJavaList(String.class));
            }
            channelVo.setNeedPage(false);
        }
		List<String> uuidList = channelVo.getUuidList();
        if (CollectionUtils.isNotEmpty(uuidList)) {
			List<String> authorizationUuidList = channelVo.getAuthorizedUuidList();
			if(CollectionUtils.isNotEmpty(authorizationUuidList)) {
				Iterator<String> authUuidIterator = authorizationUuidList.iterator();
				while(authUuidIterator.hasNext()) {
					String uuid = authUuidIterator.next();
					if(!uuidList.contains(uuid)) {
						authUuidIterator.remove();
					}
				}
			}else {
				channelVo.setAuthorizedUuidList(uuidList);
			}
			channelVo.setNeedPage(false);
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
