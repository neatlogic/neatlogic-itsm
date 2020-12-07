package codedriver.module.process.api.commenttemplate;

import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.dao.mapper.ProcessCommentTemplateMapper;
import codedriver.framework.process.dto.ProcessCommentTemplateVo;
import codedriver.framework.process.exception.commenttemplate.ProcessCommentTemplateNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.auth.label.PROCESS_COMMENT_TEMPLATE_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return "删除回复模版";
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
        ProcessCommentTemplateVo vo = commentTemplateMapper.getTemplateById(id);
        /** 没有权限则不允许删除系统模版 */
        if(ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue().equals(vo.getType()) && !AuthActionChecker.check(PROCESS_COMMENT_TEMPLATE_MODIFY.class.getSimpleName())){
            throw new PermissionDeniedException();
        }
        commentTemplateMapper.deleteTemplate(id);
        commentTemplateMapper.deleteTemplateAuthority(id);
        return null;
    }
}
