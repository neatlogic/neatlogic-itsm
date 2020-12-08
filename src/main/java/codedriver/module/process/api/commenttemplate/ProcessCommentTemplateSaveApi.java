package codedriver.module.process.api.commenttemplate;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.dao.mapper.ProcessCommentTemplateMapper;
import codedriver.framework.process.dto.ProcessCommentTemplateAuthVo;
import codedriver.framework.process.dto.ProcessCommentTemplateVo;
import codedriver.framework.process.exception.commenttemplate.ProcessCommentTemplateNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.auth.label.PROCESS_COMMENT_TEMPLATE_MODIFY;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class ProcessCommentTemplateSaveApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessCommentTemplateMapper commentTemplateMapper;

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
            @Param(name = "content", type = ApiParamType.STRING, desc = "内容", isRequired = true, xss = true),
            @Param(name = "type", type = ApiParamType.ENUM, rule = "system,custom",desc = "类型，新增时必填(system:系统模版;custom:自定义模版)"),
            @Param(name = "authList", type = ApiParamType.JSONARRAY, desc = "授权对象，可多选，type为system时必填，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]")
    })
    @Output({
            @Param(name = "id",type = ApiParamType.LONG,desc = "回复模版id")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        Long id = jsonObj.getLong("id");
        String content = jsonObj.getString("content");
        String type = jsonObj.getString("type");
        JSONArray authList = jsonObj.getJSONArray("authList");

        ProcessCommentTemplateVo vo = new ProcessCommentTemplateVo();
        vo.setId(id);
        vo.setContent(content);
        vo.setLcu(UserContext.get().getUserUuid(true));
        if (id != null){
            if(commentTemplateMapper.checkTemplateExistsById(id) == 0){
                throw new ProcessCommentTemplateNotFoundException(id);
            }
            /** 没有权限则不允许编辑系统模版 */
            ProcessCommentTemplateVo _vo = commentTemplateMapper.getTemplateById(id);
            if(ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue().equals(_vo.getType()) && !AuthActionChecker.check(PROCESS_COMMENT_TEMPLATE_MODIFY.class.getSimpleName())){
                throw new PermissionDeniedException();
            }
            vo.setType(_vo.getType());
            commentTemplateMapper.updateTemplate(vo);
            commentTemplateMapper.deleteTemplateAuthority(id);
        }else {
            vo.setType(type);
            /** 没有权限则不允许创建系统模版 */
            if(ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue().equals(vo.getType()) && !AuthActionChecker.check(PROCESS_COMMENT_TEMPLATE_MODIFY.class.getSimpleName())){
                throw new PermissionDeniedException();
            }
            vo.setFcu(UserContext.get().getUserUuid(true));
            commentTemplateMapper.insertTemplate(vo);
        }
        List<ProcessCommentTemplateAuthVo> list = new ArrayList<>();
        if(ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue().equals(vo.getType()) && CollectionUtils.isNotEmpty(authList)){
            for(Object obj : authList) {
                String[] split = obj.toString().split("#");
                if(GroupSearch.getGroupSearch(split[0]) != null) {
                    ProcessCommentTemplateAuthVo auth = new ProcessCommentTemplateAuthVo();
                    auth.setCommentTemplateId(vo.getId());
                    auth.setType(split[0]);
                    auth.setUuid(split[1]);
                    list.add(auth);
                }
            }
        }else if(ProcessCommentTemplateVo.TempalteType.CUSTOM.getValue().equals(vo.getType())){
            ProcessCommentTemplateAuthVo auth = new ProcessCommentTemplateAuthVo();
            auth.setCommentTemplateId(vo.getId());
            auth.setType(GroupSearch.USER.getValue());
            auth.setUuid(UserContext.get().getUserUuid());
            list.add(auth);
        }
        if(CollectionUtils.isNotEmpty(list)){
            commentTemplateMapper.batchInsertAuthority(list);
        }

        returnObj.put("id", vo.getId());
        return returnObj;
    }
}
