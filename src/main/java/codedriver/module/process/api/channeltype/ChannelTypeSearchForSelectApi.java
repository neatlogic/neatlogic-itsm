package codedriver.module.process.api.channeltype;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelTypeSearchForSelectApi extends ApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;

	@Override
	public String getToken() {
		return "process/channeltype/search/forselect";
	}

	@Override
	public String getName() {
		return "查询服务类型列表_下拉框";
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
		@Param(name = "list", explode = ValueTextVo[].class, desc = "服务类型列表")
	})
	@Description(desc = "查询服务类型列表_下拉框")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ChannelTypeVo channelTypeVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ChannelTypeVo>() {});
		
		JSONObject resultObj = new JSONObject();
		if(channelTypeVo.getNeedPage()) {
			int rowNum = channelMapper.searchChannelTypeCount(channelTypeVo);
			int pageCount = PageUtil.getPageCount(rowNum, channelTypeVo.getPageSize());
			channelTypeVo.setPageCount(pageCount);
			channelTypeVo.setRowNum(rowNum);
			resultObj.put("currentPage", channelTypeVo.getCurrentPage());
			resultObj.put("pageSize", channelTypeVo.getPageSize());
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
		}
		List<ValueTextVo> channelTypeList = channelMapper.searchChannelTypeListForSelect(channelTypeVo);
		resultObj.put("list", channelTypeList);
		return resultObj;
	}

}
