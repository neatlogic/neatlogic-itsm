package codedriver.module.process.api.workcenter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.module.process.service.WorkcenterService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkcenterKeywordSearchApi extends PrivateApiComponentBase {
	@Autowired
	WorkcenterMapper workcenterMapper;
	@Autowired
	WorkcenterService workcenterService;
	
	@Override
	public String getToken() {
		return "workcenter/keyword/search";
	}

	@Override
	public String getName() {
		return "工单中心关键字搜索提示接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字",isRequired = true),
		@Param(name = "pageSize", type = ApiParamType.STRING, desc = "选项显示数量")
	})
	@Output({
		@Param(name="dataList", type = ApiParamType.JSONARRAY, desc="展示的值")
	})
	@Description(desc = "工单中心关键字搜索提示接口，用于输入框输入关键字后，获取提示选项")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String keyword = jsonObj.getString("keyword");
		if(StringUtils.isEmpty(keyword)) {
			return CollectionUtils.EMPTY_COLLECTION;
		}
		return workcenterService.getKeywordOptions(keyword,jsonObj.getInteger("pageSize"));
	}
}
