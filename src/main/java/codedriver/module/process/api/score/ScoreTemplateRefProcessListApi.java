package codedriver.module.process.api.score;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.score.ScoreTemplateMapper;
import codedriver.framework.process.dto.score.ScoreTemplateVo;
import codedriver.framework.process.exception.score.ScoreTemplateNotFoundException;
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
public class ScoreTemplateRefProcessListApi extends PrivateApiComponentBase{

	@Autowired
	private ScoreTemplateMapper scoreTemplateMapper;

	@Override
	public String getToken() {
		return "score/template/process/list";
	}

	@Override
	public String getName() {
		return "查询评分模版关联的流程";
	}

	@Override
	public String getConfig() {
		return null;
	}


	@Input({
			@Param(name = "id", type = ApiParamType.LONG, isRequired = true,desc = "评分模版ID"),
			@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
			@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
			@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
	})
	@Output({@Param(name = "processList",
			type = ApiParamType.JSONARRAY,
			explode = ValueTextVo[].class,
			desc = "关联的流程列表")})
	@Description(desc = "查询评分模版关联的流程")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject returnObj = new JSONObject();
		ScoreTemplateVo scoreTemplateVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ScoreTemplateVo>() {});
		if(scoreTemplateMapper.checkScoreTemplateExistsById(scoreTemplateVo.getId()) == null){
			throw new ScoreTemplateNotFoundException(scoreTemplateVo.getId());
		}
		if(scoreTemplateVo.getNeedPage()){
			int rowNum = scoreTemplateMapper.getRefProcessCount(scoreTemplateVo.getId());
			returnObj.put("pageSize", scoreTemplateVo.getPageSize());
			returnObj.put("currentPage", scoreTemplateVo.getCurrentPage());
			returnObj.put("rowNum", rowNum);
			returnObj.put("pageCount", PageUtil.getPageCount(rowNum, scoreTemplateVo.getPageSize()));
		}
		List<ValueTextVo> processList = scoreTemplateMapper.getRefProcessList(scoreTemplateVo);
		returnObj.put("processList", processList);
		return returnObj;
	}
}
