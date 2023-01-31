package neatlogic.module.process.api.score;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.score.ScoreTemplateMapper;
import neatlogic.framework.process.dto.score.ScoreTemplateVo;
import neatlogic.framework.process.exception.score.ScoreTemplateNotFoundException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = PROCESS_BASE.class)
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
