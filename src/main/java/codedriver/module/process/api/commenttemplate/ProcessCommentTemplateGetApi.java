package codedriver.module.process.api.commenttemplate;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessCommentTemplateMapper;
import codedriver.framework.process.dto.ProcessCommentTemplateVo;
import codedriver.framework.process.exception.commenttemplate.ProcessCommentTemplateNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessCommentTemplateGetApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessCommentTemplateMapper commentTemplateMapper;

    @Override
    public String getToken() {
        return "process/comment/template/get";
    }

    @Override
    public String getName() {
        return "获取系统回复模版";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param( name = "id", type = ApiParamType.LONG, desc = "回复模版ID")})
    @Output({
            @Param( name = "template", explode = ProcessCommentTemplateVo.class, desc = "回复模版")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        Long id = jsonObj.getLong("id");
        if(commentTemplateMapper.checkTemplateExistsById(id) == 0){
            throw new ProcessCommentTemplateNotFoundException(id);
        }
        ProcessCommentTemplateVo vo = commentTemplateMapper.getTemplateById(id);
        returnObj.put("template", vo);
        return returnObj;
    }
}
