package codedriver.module.process.api.score;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.score.ScoreTemplateMapper;
import codedriver.framework.process.dto.score.ScoreTemplateDimensionVo;
import codedriver.framework.process.dto.score.ScoreTemplateVo;
import codedriver.framework.process.exception.score.ScoreTemplateNameRepeatException;
import codedriver.framework.process.exception.score.ScoreTemplateNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class ScoreTemplateSaveApi extends PrivateApiComponentBase {

    @Autowired
    private ScoreTemplateMapper scoreTemplateMapper;

    @Override
    public String getToken() {
        return "score/template/save";
    }

    @Override
    public String getName() {
        return "保存评分模版";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "id", type = ApiParamType.LONG, desc = "评分模版ID"),
             @Param( name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", desc = "评分模版名称", isRequired = true, xss = true),
             @Param( name = "description", type = ApiParamType.STRING, desc = "评分模版说明"),
             @Param( name = "isActive", type = ApiParamType.INTEGER,desc = "是否激活"),
             @Param( name = "dimensionArray", type = ApiParamType.JSONARRAY, isRequired = true,desc = "评分维度列表，格式:[{\"name\":\"t1\",\"description\":\"d1\"},{\"name\":\"t2\",\"description\":\"d2\"}]")
    })
    @Output({
            @Param( name = "scoreTemplate", explode = ScoreTemplateVo.class, desc = "评分模版")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        Long id = jsonObj.getLong("id");
        String name = jsonObj.getString("name");
        String description = jsonObj.getString("description");
        Integer isActive = jsonObj.getInteger("isActive");
        JSONArray dimensionArray = jsonObj.getJSONArray("dimensionArray");
        List<ScoreTemplateDimensionVo> dimensionList = JSON.parseArray(dimensionArray.toJSONString(), ScoreTemplateDimensionVo.class);

        ScoreTemplateVo scoreTemplateVo = new ScoreTemplateVo();
        scoreTemplateVo.setId(id);
        scoreTemplateVo.setName(name);
        scoreTemplateVo.setDescription(description);
        scoreTemplateVo.setIsActive(isActive);
        scoreTemplateVo.setLcu(UserContext.get().getUserUuid(true));

        if (id != null){
            if(scoreTemplateMapper.checkScoreTemplateExistsById(id) == null){
                throw new ScoreTemplateNotFoundException(id);
            }
        	if(scoreTemplateMapper.checkScoreTemplateNameIsRepeat(scoreTemplateVo) > 0) {
        		throw new ScoreTemplateNameRepeatException(scoreTemplateVo.getName());
        	}
            scoreTemplateMapper.updateScoreTemplate(scoreTemplateVo);
            scoreTemplateMapper.deleteScoreTemplateDimension(scoreTemplateVo.getId());
        }else {
            scoreTemplateVo.setFcu(UserContext.get().getUserUuid(true));
            if(scoreTemplateMapper.checkScoreTemplateNameIsRepeat(scoreTemplateVo) > 0) {
            	throw new ScoreTemplateNameRepeatException(scoreTemplateVo.getName());
        	}
            scoreTemplateMapper.insertScoreTemplate(scoreTemplateVo);
        }
        for(ScoreTemplateDimensionVo vo : dimensionList){
            vo.setScoreTemplateId(scoreTemplateVo.getId());
            scoreTemplateMapper.insertScoreTemplateDimension(vo);
        }
        returnObj.put("scoreTemplate", scoreTemplateVo);
        return returnObj;
    }
}
