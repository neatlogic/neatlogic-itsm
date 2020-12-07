package codedriver.module.process.api.commenttemplate;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessCommentTemplateMapper;
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
import org.springframework.transaction.annotation.Transactional;

@AuthAction(name = "PROCESS_COMMENT_TEMPLATE_MODIFY")
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class ProcessCommentTemplateDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessCommentTemplateMapper commentTemplateMapper;

    @Override
    public String getToken() {
        return "process/comment/template/delete";
    }

    @Override
    public String getName() {
        return "删除系统回复模版";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param( name = "id", type = ApiParamType.LONG, isRequired = true,desc = "回复模版ID")})
    @Output({})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        if(commentTemplateMapper.checkTemplateExistsById(id) == 0){
            throw new ProcessCommentTemplateNotFoundException(id);
        }
        commentTemplateMapper.deleteTemplate(id);
        commentTemplateMapper.deleteTemplateAuthority(id);
        return null;
    }
}
