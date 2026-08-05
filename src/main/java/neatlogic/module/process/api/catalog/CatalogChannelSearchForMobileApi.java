package neatlogic.module.process.api.catalog;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.CatalogVo;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.exception.catalog.CatalogNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.CatalogMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.service.CatalogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class CatalogChannelSearchForMobileApi extends PrivateApiComponentBase {

	@Resource
	private CatalogMapper catalogMapper;

	@Resource
	private ChannelMapper channelMapper;

	@Resource
	private CatalogService catalogService;

	@Override
	public String getToken() {
		return "/catalog/channel/search/mobile";
	}

	@Override
	public String getName() {
		return "nmpac.catalogchannelsearchformobileapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "catalogUuid", type = ApiParamType.STRING, desc = "nmpac.catalogchannelsearchformobileapi.input.param.desc.cataloguuid" , isRequired = true)
	})
	@Output({
		@Param(explode=BasePageVo.class),
		@Param(name="treeList",explode=CatalogVo[].class,desc="nmpac.catalogchannelsearchformobileapi.output.param.desc.treelist")
	})
	@Description(desc = "nmpac.catalogchannelsearchformobileapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		JSONArray listArray = new JSONArray();
		String catalogUuid = jsonObj.getString("catalogUuid");
		ChannelVo paramChannel = new ChannelVo();
		//如果服务目录catalogUuid == 0，则展示所有一级服务目录,否则仅展示对应的catalogUuid
		if(catalogUuid.equals(CatalogVo.ROOT_UUID)) {
			catalogUuid = null;
    		//找到root目录下所有有权限的一级服务目录
    		List<CatalogVo> firstCatalogList = catalogService.getCatalogByCatalogParentUuid(CatalogVo.ROOT_UUID);
    		//获取以上一级服务目录下的所有有权限的服务目录&&服务
    		for(CatalogVo firstCatalog: firstCatalogList) {
    			listArray.add(catalogService.getCatalogChannelByCatalogUuid(firstCatalog,true));
    		}
		}else {
		    CatalogVo catalog = catalogMapper.getCatalogByUuid(catalogUuid);
		    if(catalog == null) {
	            throw new CatalogNotFoundException(catalogUuid);
	        }
		    listArray = catalogService.getCatalogChannelByCatalogUuid(catalog,true).getJSONArray("list");
		}
		resultObj.put("list", listArray);
		/* 获取对应目录下的收藏服务 **/
		paramChannel.setIsFavorite(1);
		paramChannel.setUserUuid(UserContext.get().getUserUuid(true));
		//查出当前用户已授权的服务
		paramChannel.setAuthorizedUuidList(catalogService.getCurrentUserAuthorizedChannelUuidList());
		paramChannel.setIsActive(1);
		paramChannel.setParentUuid(catalogUuid);
		resultObj.put("favoriteList", channelMapper.searchChannelList(paramChannel));

		return resultObj;
	}

}
