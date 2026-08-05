package neatlogic.module.process.api.channeltype;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ChannelTypeVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelTypeSearchApi extends PrivateApiComponentBase {

	@Resource
	private ChannelTypeMapper channelTypeMapper;

	@Override
	public String getToken() {
		return "process/channeltype/search";
	}

	@Override
	public String getName() {
		return "nmpac.channeltypesearchapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "nmpac.channeltypesearchapi.input.param.desc.keyword"),
		@Param(name = "isActive", type = ApiParamType.ENUM, desc = "nmpac.channeltypesearchapi.input.param.desc.isactive", rule = "0,1"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "nmpac.channeltypesearchapi.input.param.desc.needpage"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "nmpac.channeltypesearchapi.input.param.desc.pagesize"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "nmpac.channeltypesearchapi.input.param.desc.currentpage")
	})
	@Output({
		@Param(name = "currentPage", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpac.channeltypesearchapi.output.param.desc.currentpage"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpac.channeltypesearchapi.output.param.desc.pagesize"),
		@Param(name = "pageCount", type = ApiParamType.INTEGER, isRequired =true, desc = "nmpac.channeltypesearchapi.output.param.desc.pagecount"),
		@Param(name = "rowNum", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpac.channeltypesearchapi.output.param.desc.rownum"),
		@Param(name = "tbodyList", explode = ChannelTypeVo[].class, desc = "nmpac.channeltypesearchapi.output.param.desc.tbodylist")
	})
	@Description(desc = "nmpac.channeltypesearchapi.getname")
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
