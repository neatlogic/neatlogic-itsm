package codedriver.module.process.api.score;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.score.ScoreTemplateMapper;
import codedriver.framework.process.dto.score.ScoreTemplateVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ScoreTemplateSearchForSelectApi extends PrivateApiComponentBase{

	@Autowired
	private ScoreTemplateMapper scoreTemplateMapper;

	@Override
	public String getToken() {
		return "score/template/search/forselect";
	}

	@Override
	public String getName() {
		return "查询评分模版_下拉框";
	}

	@Override
	public String getConfig() {
		return null;
	}


	@Input({
			@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss=true),
			@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
			@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
			@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
	})
	@Output({@Param(name = "scoreTemplateList",
			type = ApiParamType.JSONARRAY,
			explode = ValueTextVo[].class,
			desc = "选项列表")})
	@Description(desc = "查询评分模版_下拉框")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {

		ScoreTemplateVo scoreTemplateVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ScoreTemplateVo>() {});
		JSONObject returnObj = new JSONObject();
		if(scoreTemplateVo.getNeedPage()){
			int rowNum = scoreTemplateMapper.searchScoreTemplateCount(scoreTemplateVo);
			returnObj.put("pageSize", scoreTemplateVo.getPageSize());
			returnObj.put("currentPage", scoreTemplateVo.getCurrentPage());
			returnObj.put("rowNum", rowNum);
			returnObj.put("pageCount", PageUtil.getPageCount(rowNum, scoreTemplateVo.getPageSize()));
		}
		List<ValueTextVo> scoreTemplateList = scoreTemplateMapper.searchScoreTemplateForSelect(scoreTemplateVo);
		returnObj.put("scoreTemplateList", scoreTemplateList);
		return returnObj;
	}
}
