package codedriver.module.process.api.score;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.score.ScoreTemplateMapper;
import codedriver.framework.process.dto.score.ScoreTemplateVo;
import codedriver.framework.process.exception.score.ScoreTemplateNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ScoreTemplateGetApi extends PrivateApiComponentBase{

	@Autowired
	private ScoreTemplateMapper scoreTemplateMapper;

	@Override
	public String getToken() {
		return "score/template/get";
	}

	@Override
	public String getName() {
		return "获取评分模版";
	}

	@Override
	public String getConfig() {
		return null;
	}


	@Input({
			@Param( name = "id", type = ApiParamType.LONG, isRequired = true,desc = "评分模版ID")
	})
	@Output({
			@Param(explode = ScoreTemplateVo.class, desc = "评分模版")
	})
	@Description(desc = "获取评分模版")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
        Long scoreTemplateId = jsonObj.getLong("id");
		ScoreTemplateVo scoreTemplate = scoreTemplateMapper.getScoreTemplateById(scoreTemplateId);
		if(scoreTemplate == null) {
            throw new ScoreTemplateNotFoundException(scoreTemplateId);
		}
		return scoreTemplate;
	}
}
