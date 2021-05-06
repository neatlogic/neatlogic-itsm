package codedriver.module.process.api.channeltype;

import java.util.List;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dto.ChannelTypeVo;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelTypeSearchApi extends PrivateApiComponentBase {

	@Autowired
	private ChannelTypeMapper channelTypeMapper;

	@Override
	public String getToken() {
		return "process/channeltype/search";
	}

	@Override
	public String getName() {
		return "服务类型列表搜索接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字，匹配名称"),
		@Param(name = "isActive", type = ApiParamType.ENUM, desc = "是否激活", rule = "0,1"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
	})
	@Output({
		@Param(name = "currentPage", type = ApiParamType.INTEGER, isRequired = true, desc = "当前页码"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, isRequired = true, desc = "页大小"),
		@Param(name = "pageCount", type = ApiParamType.INTEGER, isRequired =true, desc = "总页数"),
		@Param(name = "rowNum", type = ApiParamType.INTEGER, isRequired = true, desc = "总行数"),
		@Param(name = "tbodyList", explode = ChannelTypeVo[].class, desc = "服务类型列表")
	})
	@Description(desc = "服务类型列表搜索接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ChannelTypeVo channelTypeVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ChannelTypeVo>() {});
		
		JSONObject resultObj = new JSONObject();
		if(channelTypeVo.getNeedPage()) {
			int rowNum = channelTypeMapper.searchChannelTypeCount(channelTypeVo);
			int pageCount = PageUtil.getPageCount(rowNum, channelTypeVo.getPageSize());
			channelTypeVo.setPageCount(pageCount);
			channelTypeVo.setRowNum(rowNum);
			resultObj.put("currentPage", channelTypeVo.getCurrentPage());
			resultObj.put("pageSize", channelTypeVo.getPageSize());
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
		}
		List<ChannelTypeVo> channelTypeList = channelTypeMapper.searchChannelTypeList(channelTypeVo);
		resultObj.put("tbodyList", channelTypeList);
		return resultObj;
	}

}
