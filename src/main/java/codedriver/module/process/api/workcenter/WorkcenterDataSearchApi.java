package codedriver.module.process.api.workcenter;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.WorkcenterService;

@Service
public class WorkcenterDataSearchApi extends ApiComponentBase {
	@Autowired
	WorkcenterMapper workcenterMapper;
	@Autowired
	WorkcenterService workcenterService;
	
	@Override
	public String getToken() {
		return "workcenter/search";
	}

	@Override
	public String getName() {
		return "工单中心搜索接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "分类uuid,有则去数据库获取对应分类的条件，无则根据传的过滤条件查询"),
		@Param(name = "isMeWillDo", type = ApiParamType.INTEGER, desc = "是否带我处理的，1：是；0：否"),
		@Param(name = "conditionGroupList", type = ApiParamType.JSONARRAY, desc = "条件组条件", isRequired = false),
		@Param(name = "conditionGroupRelList", type = ApiParamType.JSONARRAY, desc = "条件组连接类型", isRequired = false),
		@Param(name = "headerList", type = ApiParamType.JSONARRAY, desc = "显示的字段", isRequired = false),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数", isRequired = false),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目", isRequired = false)
	})
	@Output({
		@Param(name="headerList", type = ApiParamType.JSONARRAY, desc="展示的字段"),
		@Param(name="dataList", type = ApiParamType.JSONARRAY, desc="展示的值"),
		@Param(name="rowNum", type = ApiParamType.INTEGER, desc="总数"),
		@Param(name="pageSize", type = ApiParamType.INTEGER, desc="每页数据条目"),
		@Param(name="currentPage", type = ApiParamType.INTEGER, desc="当前页数"),
		@Param(name="pageCount", type = ApiParamType.INTEGER, desc="总页数"),
	})
	@Description(desc = "工单中心搜索接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		if(jsonObj.containsKey("uuid")) {
			String uuid = jsonObj.getString("uuid");
			Integer currentPage = jsonObj.getInteger("currentPage");
			Integer pageSize = jsonObj.getInteger("pageSize");
			Integer isMeWillDo = jsonObj.getInteger("isMeWillDo");
			List<WorkcenterVo> workcenterList = workcenterMapper.getWorkcenterByNameAndUuid(null, uuid);
			if(CollectionUtils.isNotEmpty(workcenterList)) {
				jsonObj = JSONObject.parseObject(workcenterList.get(0).getConditionConfig());
				jsonObj.put("uuid", uuid);
				jsonObj.put("currentPage", currentPage);
				jsonObj.put("pageSize", pageSize);
				jsonObj.put("isMeWillDo", isMeWillDo);
			}
		}
		return workcenterService.doSearch(new WorkcenterVo(jsonObj));
	}

}
