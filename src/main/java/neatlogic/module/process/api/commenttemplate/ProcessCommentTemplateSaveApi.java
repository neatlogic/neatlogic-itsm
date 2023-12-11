package neatlogic.module.process.api.commenttemplate;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.auth.PROCESS_COMMENT_TEMPLATE_MODIFY;
import neatlogic.framework.process.dao.mapper.ProcessCommentTemplateMapper;
import neatlogic.framework.process.dto.ProcessCommentTemplateVo;
import neatlogic.framework.process.exception.commenttemplate.ProcessCommentTemplateNameRepeatException;
import neatlogic.framework.process.exception.commenttemplate.ProcessCommentTemplateNotFoundException;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessCommentTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessCommentTemplateSaveApi extends PrivateApiComponentBase {

    @Resource
    private ProcessCommentTemplateMapper commentTemplateMapper;

    @Resource
    private ProcessCommentTemplateService processCommentTemplateService;

    @Override
    public String getToken() {
        return "process/comment/template/save";
    }

    @Override
    public String getName() {
        return "保存回复模版";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "回复模版ID"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "名称", isRequired = true),
            @Param(name = "content", type = ApiParamType.STRING, desc = "内容", isRequired = true),
            @Param(name = "type", type = ApiParamType.ENUM, rule = "system,custom", desc = "类型，新增时必填(system:系统模版;custom:自定义模版)"),
            @Param(name = "authList", type = ApiParamType.JSONARRAY, desc = "授权对象，可多选，type为system时必填，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]")
    })
    @Output({
            @Param(name = "id", type = ApiParamType.LONG, desc = "回复模版id")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        Long id = jsonObj.getLong("id");
        if (id != null) {
            ProcessCommentTemplateVo _vo = commentTemplateMapper.getTemplateById(id);
            if (_vo == null) {
                throw new ProcessCommentTemplateNotFoundException(id);
            }
            /** 没有权限则不允许编辑系统模版 */
            if (ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue().equals(_vo.getType()) && !AuthActionChecker.check(PROCESS_COMMENT_TEMPLATE_MODIFY.class.getSimpleName())) {
                throw new PermissionDeniedException(PROCESS_COMMENT_TEMPLATE_MODIFY.class);
            }
        } else {
            String type = jsonObj.getString("type");
            /** 没有权限则不允许创建系统模版 */
            if (ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue().equals(type) && !AuthActionChecker.check(PROCESS_COMMENT_TEMPLATE_MODIFY.class.getSimpleName())) {
                throw new PermissionDeniedException(PROCESS_COMMENT_TEMPLATE_MODIFY.class);
            }
        }
        ProcessCommentTemplateVo template = jsonObj.toJavaObject(ProcessCommentTemplateVo.class);
        processCommentTemplateService.saveTemplate(template);
        returnObj.put("id", template.getId());
        return returnObj;
    }

    public IValid name() {
        return value -> {
            ProcessCommentTemplateVo vo = value.toJavaObject(ProcessCommentTemplateVo.class);
            if (commentTemplateMapper.checkTemplateNameIsRepeat(vo) > 0) {
                return new FieldValidResultVo(new ProcessCommentTemplateNameRepeatException(vo.getName()));
            }
            return new FieldValidResultVo();
        };
    }
}
