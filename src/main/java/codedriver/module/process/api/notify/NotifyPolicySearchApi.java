package codedriver.module.process.api.notify;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dto.NotifyPolicyVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class NotifyPolicySearchApi  extends ApiComponentBase {

	@Override
	public String getToken() {
		return "process/notify/policy/search";
	}

	@Override
	public String getName() {
		return "通知策略管理列表搜索接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字搜索"),
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否分页"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条数")
	})
	@Output({
		@Param(explode=BasePageVo.class),
		@Param(name = "tbodyList", explode = NotifyPolicyVo[].class, desc = "通知策略列表")
	})
	@Description(desc = "通知策略管理列表搜索接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return null;
	}
	
	@Override
	public Object myDoTest(JSONObject jsonObj) {
		List<NotifyPolicyVo> notifyPolicyList = NotifyPolicyVo.notifyPolicyList;
		JSONObject resultObj = new JSONObject();
		BasePageVo basePageVo = JSON.toJavaObject(jsonObj, BasePageVo.class);
		List<NotifyPolicyVo> tbodyList = new ArrayList<>();
		if(StringUtils.isNoneBlank(basePageVo.getKeyword())) {
			for(NotifyPolicyVo notifyPolicy : notifyPolicyList) {
				if(notifyPolicy.getName().equalsIgnoreCase(basePageVo.getKeyword())) {
					tbodyList.add(notifyPolicy);
				}
			}
		}else {
			tbodyList = notifyPolicyList;
		}
		
		if(basePageVo.getNeedPage()) {
			int rowNum = tbodyList.size();
			resultObj.put("currentPage", basePageVo.getCurrentPage());
			resultObj.put("pageSize", basePageVo.getPageSize());
			resultObj.put("pageCount", PageUtil.getPageCount(rowNum, basePageVo.getPageSize()));
			resultObj.put("rowNum", rowNum);
		}
		resultObj.put("tbodyList", tbodyList);
		return resultObj;
	}

}
