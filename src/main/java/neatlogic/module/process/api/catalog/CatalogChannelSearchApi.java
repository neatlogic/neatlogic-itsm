package neatlogic.module.process.api.catalog;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.CatalogVo;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.CatalogMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.service.CatalogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class CatalogChannelSearchApi extends PrivateApiComponentBase {

	@Resource
	private CatalogService catalogService;

	@Resource
	private CatalogMapper catalogMapper;
	
	@Resource
	private ChannelMapper channelMapper;

	@Override
	public String getToken() {
		return "process/catalog/channel/search";
	}

	@Override
	public String getName() {
		return "nmpac.catalogchannelsearchapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "nmpac.catalogchannelsearchapi.input.param.desc.keyword"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "nmpac.catalogchannelsearchapi.input.param.desc.needpage"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "nmpac.catalogchannelsearchapi.input.param.desc.pagesize"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "nmpac.catalogchannelsearchapi.input.param.desc.currentpage")
		})
	@Output({
		@Param(explode=BasePageVo.class),
		@Param(name="treeList",explode=CatalogVo[].class,desc="nmpac.catalogchannelsearchapi.output.param.desc.treelist")
	})
	@Description(desc = "nmpac.catalogchannelsearchapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		String keyword = jsonObj.getString("keyword");
		BasePageVo basePageVo = JSON.toJavaObject(jsonObj, BasePageVo.class);
		//由于去除了root节点，所以要构造一个虚拟的root节点
		CatalogVo rootCatalog = catalogService.buildRootCatalog();
		List<CatalogVo> catalogList = catalogMapper.getCatalogListForTree(rootCatalog.getLft(), rootCatalog.getRht());
		List<ChannelVo> channelList = channelMapper.getChannelListForTree(null);
		List<CatalogVo> catalogChannelList = new ArrayList<>(catalogList.size() + channelList.size());
		for(CatalogVo catalogVo : catalogList) {
			if(StringUtils.isNotBlank(keyword) && !catalogVo.getName().toUpperCase().contains(keyword.toUpperCase())) {
				continue;
			}
			catalogChannelList.add(catalogVo);
		}
		for(ChannelVo channelVo : channelList) {
			if(StringUtils.isNotBlank(keyword) && !channelVo.getName().toUpperCase().contains(keyword.toUpperCase())) {
				continue;
			}
			CatalogVo catalogVo = new CatalogVo();
			catalogVo.setUuid(channelVo.getUuid());
			catalogVo.setName(channelVo.getName());
			catalogVo.setParentUuid(channelVo.getParentUuid());
			catalogVo.setType("channel");
			catalogChannelList.add(catalogVo);
		}
		catalogChannelList.sort((catalogVo1, catalogVo2) -> catalogVo1.getName().compareTo(catalogVo2.getName()));
		if(basePageVo.getNeedPage()) {
			int rowNum = catalogChannelList.size();
			int pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
			resultObj.put("currentPage", basePageVo.getCurrentPage());
			resultObj.put("pageSize", basePageVo.getPageSize());
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
			int fromIndex = basePageVo.getStartNum();
			if(fromIndex < rowNum) {
				int toIndex = fromIndex + basePageVo.getPageSize();
				toIndex = Math.min(toIndex, rowNum);
				resultObj.put("treeList", catalogChannelList.subList(fromIndex, toIndex));
			}else {
				resultObj.put("treeList", new ArrayList<>());
			}
		}else {
			resultObj.put("treeList", catalogChannelList);
		}
			
		return resultObj;
	}

}
