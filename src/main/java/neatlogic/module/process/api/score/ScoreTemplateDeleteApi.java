package neatlogic.module.process.api.score;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.dao.mapper.score.ScoreTemplateMapper;
import neatlogic.framework.process.exception.score.ScoreTemplateHasRefProcessException;
import neatlogic.framework.process.exception.score.ScoreTemplateNotFoundException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.process.auth.SCORE_TEMPLATE_MODIFY;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@AuthAction(action = SCORE_TEMPLATE_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.DELETE)
public class ScoreTemplateDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private ScoreTemplateMapper scoreTemplateMapper;

    @Override
    public String getToken() {
        return "score/template/delete";
    }

    @Override
    public String getName() {
        return "删除评分模版";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "id", type = ApiParamType.LONG, desc = "评分模版ID" ,isRequired = true)
    })
    @Output({})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");

        if(scoreTemplateMapper.checkScoreTemplateExistsById(id) == null){
            throw new ScoreTemplateNotFoundException(id);
        }
        if(scoreTemplateMapper.getRefProcessCount(id) > 0){
            throw new ScoreTemplateHasRefProcessException(scoreTemplateMapper.getScoreTemplateById(id).getName());
        }
        scoreTemplateMapper.deleteScoreTemplate(id);
        return null;
    }
}
