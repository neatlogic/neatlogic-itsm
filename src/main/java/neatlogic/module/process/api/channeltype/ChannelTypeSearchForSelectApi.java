package neatlogic.module.process.api.channeltype;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Objects;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
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

import neatlogic.framework.util.$;
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelTypeSearchForSelectApi extends PrivateApiComponentBase {

	@Resource
	private ChannelTypeMapper channelTypeMapper;

	@Override
	public String getToken() {
		return "process/channeltype/search/forselect";
	}

	@Override
	public String getName() {
		return "nmpac.channeltypesearchforselectapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "nmpac.channeltypesearchforselectapi.input.param.desc.keyword"),
		@Param(name = "isActive", type = ApiParamType.ENUM, desc = "nmpac.channeltypesearchforselectapi.input.param.desc.isactive", rule = "0,1"),
		@Param(name = "needAllOption", type = ApiParamType.ENUM, desc = "nmpac.channeltypesearchforselectapi.input.param.desc.needalloption", rule = "0,1"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "nmpac.channeltypesearchforselectapi.input.param.desc.needpage"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "nmpac.channeltypesearchforselectapi.input.param.desc.pagesize"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "nmpac.channeltypesearchforselectapi.input.param.desc.currentpage")
	})
	@Output({
		@Param(name = "currentPage", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpac.channeltypesearchforselectapi.output.param.desc.currentpage"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpac.channeltypesearchforselectapi.output.param.desc.pagesize"),
		@Param(name = "pageCount", type = ApiParamType.INTEGER, isRequired =true, desc = "nmpac.channeltypesearchforselectapi.output.param.desc.pagecount"),
		@Param(name = "rowNum", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpac.channeltypesearchforselectapi.output.param.desc.rownum"),
		@Param(name = "list", explode = ValueTextVo[].class, desc = "nmpac.channeltypesearchforselectapi.output.param.desc.list")
	})
	@Description(desc = "nmpac.channeltypesearchforselectapi.getname")
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
		List<ValueTextVo> channelTypeList = channelTypeMapper.searchChannelTypeListForSelect(channelTypeVo);
		Integer needAllOption = jsonObj.getInteger("needAllOption");
		if(Objects.equal(needAllOption, 1)) {
		    channelTypeList.add(0, new ValueTextVo("all", $.t("nmpac.channeltypesearchforselectapi.runtime.label.1")));
		}
		resultObj.put("list", channelTypeList);
		return resultObj;
	}

}
