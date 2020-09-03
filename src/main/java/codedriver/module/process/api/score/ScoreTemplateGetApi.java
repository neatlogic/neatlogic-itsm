package codedriver.module.process.api.score;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.score.ScoreTemplateMapper;
import codedriver.framework.process.dto.ProcessTaskVo;
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
	@Autowired
	private ProcessTaskMapper processTaskMapper;

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
			@Param( name = "id", type = ApiParamType.LONG, isRequired = true,desc = "评分模版ID或者工单ID"),
			@Param( name = "type", type = ApiParamType.STRING, isRequired = true,desc = "区分入参【id】是评分模版ID还是工单ID。" +
					"score：表示入参id为评分模版ID；processtask：表示入参id为工单ID")
	})
	@Output({
			@Param(name = "id", type = ApiParamType.LONG, desc = "评分模版ID"),
			@Param(name = "name", type = ApiParamType.STRING, desc = "评分模版名称"),
			@Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否启用"),
			@Param(name = "description", type = ApiParamType.STRING, desc = "评分模版说明"),
			@Param(name = "dimensionList", type = ApiParamType.JSONARRAY, desc = "评分维度列表"),
	})
	@Description(desc = "获取评分模版")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject result = new JSONObject();
		ScoreTemplateVo scoreTemplate = null;
		Long scoreTemplateId = null;
		String type = jsonObj.getString("type");
		if(type.equals("score")){
			scoreTemplateId = jsonObj.getLong("id");
		}else if(type.equals("processtask")){
			ProcessTaskVo task = processTaskMapper.getProcessTaskById(jsonObj.getLong("id"));
			String processUuid = task.getProcessUuid();
			scoreTemplateId = scoreTemplateMapper.getScoreTemplateIdByProcessUuid(processUuid);
			//如果scoreTemplateId为空，则表明没有找到与工单相关联且启用的评分模版
			if(scoreTemplateId == null){
				return null;
			}
		}
		if(scoreTemplateMapper.checkScoreTemplateExistsById(scoreTemplateId) == null){
			throw new ScoreTemplateNotFoundException(scoreTemplateId);
		}
		scoreTemplate = scoreTemplateMapper.getScoreTemplateById(scoreTemplateId);

		result.put("scoreTemplate",scoreTemplate);
		return result;
	}
}
