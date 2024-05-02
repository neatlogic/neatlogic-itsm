package neatlogic.module.process.api.score;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.module.process.dao.mapper.score.ScoreTemplateMapper;
import neatlogic.framework.process.dto.score.ScoreTemplateVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ScoreTemplateSearchApi extends PrivateApiComponentBase{

	@Autowired
	private ScoreTemplateMapper scoreTemplateMapper;

	@Override
	public String getToken() {
		return "score/template/search";
	}

	@Override
	public String getName() {
		return "查询评分模版";
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
			explode = ScoreTemplateVo[].class,
			desc = "评分模版列表")})
	@Description(desc = "查询评分模版")
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
		List<ScoreTemplateVo> scoreTemplateList = scoreTemplateMapper.searchScoreTemplate(scoreTemplateVo);
		/** 查询关联的流程数量 */
		if(CollectionUtils.isNotEmpty(scoreTemplateList)){
			List<Long> idList = scoreTemplateList.stream().map(ScoreTemplateVo::getId).collect(Collectors.toList());
			List<ScoreTemplateVo> processCountByIdList = scoreTemplateMapper.getProcessCountByIdList(idList);
			Map<Long,ScoreTemplateVo> processCountMap = new HashMap<>();
			for(ScoreTemplateVo vo : processCountByIdList){
				processCountMap.put(vo.getId(),vo);
			}
			for(ScoreTemplateVo vo : scoreTemplateList){
				ScoreTemplateVo countVo = processCountMap.get(vo.getId());
				if(countVo != null){
					vo.setProcessCount(countVo.getProcessCount());
				}
			}
		}
		returnObj.put("scoreTemplateList", scoreTemplateList);
		return returnObj;
	}
}
