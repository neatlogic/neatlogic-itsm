package neatlogic.module.process.api.priority;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.PriorityVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.PriorityMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class PrioritySearchForSelectApi extends PrivateApiComponentBase {

	@Resource
	private PriorityMapper priorityMapper;
	
	@Override
	public String getToken() {
		return "process/priority/search/forselect";
	}

	@Override
	public String getName() {
		return "nmpap.prioritysearchforselectapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "nmpap.prioritysearchforselectapi.input.param.desc.keyword"),
		@Param(name = "isActive", type = ApiParamType.ENUM, desc = "nmpap.prioritysearchforselectapi.input.param.desc.isactive", rule = "0,1"),
		@Param(name = "channelUuid", type = ApiParamType.STRING, desc = "nmpap.prioritysearchforselectapi.input.param.desc.channeluuid"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "nmpap.prioritysearchforselectapi.input.param.desc.needpage"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "nmpap.prioritysearchforselectapi.input.param.desc.pagesize"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "nmpap.prioritysearchforselectapi.input.param.desc.currentpage")
		})
	@Output({
		@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="nmpap.prioritysearchforselectapi.output.param.desc.currentpage"),
		@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="nmpap.prioritysearchforselectapi.output.param.desc.pagesize"),
		@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="nmpap.prioritysearchforselectapi.output.param.desc.pagecount"),
		@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="nmpap.prioritysearchforselectapi.output.param.desc.rownum"),
		@Param(name="list",explode=ValueTextVo[].class,desc="nmpap.prioritysearchforselectapi.output.param.desc.list")
	})
	@Description(desc = "nmpap.prioritysearchforselectapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		PriorityVo priorityVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<PriorityVo>() {});
		
		JSONObject resultObj = new JSONObject();
		if(priorityVo.getNeedPage()) {
			int rowNum = priorityMapper.searchPriorityCount(priorityVo);
			int pageCount = PageUtil.getPageCount(rowNum, priorityVo.getPageSize());
			priorityVo.setPageCount(pageCount);
			priorityVo.setRowNum(rowNum);
			resultObj.put("currentPage", priorityVo.getCurrentPage());
			resultObj.put("pageSize", priorityVo.getPageSize());
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum);
		}
		List<ValueTextVo> priorityList = priorityMapper.searchPriorityListForSelect(priorityVo);
		resultObj.put("list", priorityList);
		return resultObj;
	}

}
