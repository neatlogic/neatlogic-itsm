package codedriver.module.process.api.catalog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class CatalogChannelSearchApi extends ApiComponentBase {

	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private ChannelMapper channelMapper;

	@Override
	public String getToken() {
		return "process/catalog/channel/search";
	}

	@Override
	public String getName() {
		return "目录及服务列表搜索接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字，匹配名称"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
		})
	@Output({
		@Param(explode=BasePageVo.class),
		@Param(name="treeList",explode=CatalogVo[].class,desc="目录及服务列表")
	})
	@Description(desc = "目录及服务列表搜索接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		String keyword = jsonObj.getString("keyword");
		BasePageVo basePageVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<BasePageVo>() {});
		List<CatalogVo> catalogList = catalogMapper.getCatalogListForTree(null);
		List<ChannelVo> channelList = channelMapper.getChannelListForTree(null);
		List<CatalogVo> catalogChannelList = new ArrayList<>(catalogList.size() + channelList.size());
		for(CatalogVo catalogVo : catalogList) {
			if(!CatalogVo.ROOT_UUID.equals(catalogVo.getUuid())) {
				continue;
			}
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
			int toIndex = fromIndex + basePageVo.getPageSize();
			toIndex = rowNum < toIndex ? rowNum : toIndex;
			catalogChannelList = catalogChannelList.subList(fromIndex, toIndex);
		}
		
		resultObj.put("treeList", catalogChannelList);
		return resultObj;
	}

}
