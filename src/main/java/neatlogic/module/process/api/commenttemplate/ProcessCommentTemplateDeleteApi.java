package neatlogic.module.process.api.commenttemplate;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.auth.PROCESS_COMMENT_TEMPLATE_MODIFY;
import neatlogic.module.process.dao.mapper.process.ProcessCommentTemplateMapper;
import neatlogic.framework.process.dto.ProcessCommentTemplateVo;
import neatlogic.framework.process.exception.commenttemplate.ProcessCommentTemplateNotFoundException;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessCommentTemplateDeleteApi extends PrivateApiComponentBase {

    @Resource
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
        ProcessCommentTemplateVo vo = commentTemplateMapper.getTemplateById(id);
        if(vo == null){
            throw new ProcessCommentTemplateNotFoundException(id);
        }
        /** 没有权限则不允许删除系统模版 */
        if(ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue().equals(vo.getType()) && !AuthActionChecker.check(PROCESS_COMMENT_TEMPLATE_MODIFY.class.getSimpleName())){
            throw new PermissionDeniedException(PROCESS_COMMENT_TEMPLATE_MODIFY.class);
        }
        commentTemplateMapper.deleteTemplate(id);
        commentTemplateMapper.deleteTemplateAuthority(id);
        commentTemplateMapper.deleteTemplateUsecount(id);
        return null;
    }
}
