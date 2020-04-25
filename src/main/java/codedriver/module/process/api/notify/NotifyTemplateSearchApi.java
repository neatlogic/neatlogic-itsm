package codedriver.module.process.api.notify;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.notify.NotifyMapper;
import codedriver.framework.process.notify.core.NotifyDefaultTemplateFactory;
import codedriver.framework.process.notify.dto.NotifyTemplateVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class NotifyTemplateSearchApi extends ApiComponentBase {

	@Autowired
	private NotifyMapper notifyMapper;

	@Override
	public String getToken() {
		return "notify/template/search";
	}

	@Override
	public String getName() {
		return "通知模板列表查询接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "keyword",
					type = ApiParamType.STRING,
					isRequired = false,
					desc = "模板名称模糊匹配"),
			@Param(name = "type",
					type = ApiParamType.STRING,
					isRequired = false,
					desc = "类型"),
			@Param(name = "needPage",
					type = ApiParamType.BOOLEAN,
					desc = "是否需要分页，默认true"),
			@Param(name = "currentPage",
					type = ApiParamType.INTEGER,
					isRequired = false,
					desc = "当前页码"),
			@Param(name = "pageSize",
					type = ApiParamType.INTEGER,
					isRequired = false,
					desc = "页大小") })
	@Output({
			@Param(explode = BasePageVo.class),
			@Param(name = "tbodyList",
					explode = NotifyTemplateVo[].class,
					desc = "通知模板列表") })
	@Description(desc = "通知模板列表查询接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		NotifyTemplateVo notifyTemplateVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<NotifyTemplateVo>() {});
		List<NotifyTemplateVo> defaultTemplateList = NotifyDefaultTemplateFactory.getDefaultTemplateList(notifyTemplateVo);

		List<NotifyTemplateVo> notifyTemplateList = new ArrayList<>();
		if (notifyTemplateVo.getNeedPage()) {
			int rowNum = notifyMapper.searchNotifyTemplateCount(notifyTemplateVo);
			int pageCount = PageUtil.getPageCount(rowNum + defaultTemplateList.size(), notifyTemplateVo.getPageSize());
			resultObj.put("currentPage", notifyTemplateVo.getCurrentPage());
			resultObj.put("pageSize", notifyTemplateVo.getPageSize());
			resultObj.put("pageCount", pageCount);
			resultObj.put("rowNum", rowNum + defaultTemplateList.size());
			int startNum = notifyTemplateVo.getStartNum();
			int count = rowNum - startNum;
			if(count >= notifyTemplateVo.getPageSize()) {
				notifyTemplateList = notifyMapper.searchNotifyTemplate(notifyTemplateVo);
			}else if(count > 0 && count < notifyTemplateVo.getPageSize()) {
				notifyTemplateList = notifyMapper.searchNotifyTemplate(notifyTemplateVo);
				notifyTemplateList.addAll(defaultTemplateList.subList(0, notifyTemplateVo.getPageSize() - count));
			}else {
				notifyTemplateList.addAll(defaultTemplateList.subList(Math.abs(count), count + notifyTemplateVo.getPageSize()));
			}
		}else {
			notifyTemplateList = notifyMapper.searchNotifyTemplate(notifyTemplateVo);
			notifyTemplateList.addAll(defaultTemplateList);
		}
		resultObj.put("tbodyList", notifyTemplateList);
		return resultObj;
	}

}
