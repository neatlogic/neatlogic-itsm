package codedriver.module.process.api.score;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.score.ScoreTemplateMapper;
import codedriver.framework.process.dto.score.ScoreTemplateVo;
import codedriver.framework.process.exception.score.ScoreTemplateHasRefProcessException;
import codedriver.framework.process.exception.score.ScoreTemplateNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.auth.label.SCORE_TEMPLATE_MODIFY;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@AuthAction(action = SCORE_TEMPLATE_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class ScoreTemplateStatusUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private ScoreTemplateMapper scoreTemplateMapper;

    @Override
    public String getToken() {
        return "score/template/status/update";
    }

    @Override
    public String getName() {
        return "修改评分模版激活状态";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "id", type = ApiParamType.LONG,isRequired = true,desc = "评分模版ID"),
             @Param( name = "isActive", type = ApiParamType.INTEGER,isRequired = true,desc = "是否激活")
    })
    @Output({})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        Integer isActive = jsonObj.getInteger("isActive");

        ScoreTemplateVo scoreTemplateVo = new ScoreTemplateVo();
        scoreTemplateVo.setId(id);
        scoreTemplateVo.setIsActive(isActive);
        scoreTemplateVo.setLcu(UserContext.get().getUserUuid(true));

        if(scoreTemplateMapper.checkScoreTemplateExistsById(id) == null){
            throw new ScoreTemplateNotFoundException(id);
        }
        if(isActive == 0 && scoreTemplateMapper.getRefProcessCount(id) > 0){
            throw new ScoreTemplateHasRefProcessException(scoreTemplateMapper.getScoreTemplateById(id).getName());
        }
        scoreTemplateMapper.updateScoreTemplateStatus(scoreTemplateVo);
        return null;
    }
}
