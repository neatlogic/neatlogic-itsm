package codedriver.module.process.api.commenttemplate;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@AuthAction(name = "PROCESS_COMMENT_TEMPLATE_MODIFY")
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
        return "保存系统回复模版";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "回复模版ID"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "内容", isRequired = true, xss = true),
            @Param(name = "authList", type = ApiParamType.JSONARRAY, desc = "授权对象，可多选，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]", isRequired = true)
    })
    @Output({
            @Param(name = "id",type = ApiParamType.LONG,desc = "回复模版id")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        Long id = jsonObj.getLong("id");
        String content = jsonObj.getString("content");
        JSONArray authList = jsonObj.getJSONArray("authList");

        ProcessCommentTemplateVo vo = new ProcessCommentTemplateVo();
        vo.setId(id);
        vo.setContent(content);

        if (id != null){
            if(commentTemplateMapper.checkTemplateExistsById(id) == 0){
                throw new ProcessCommentTemplateNotFoundException(id);
            }
            commentTemplateMapper.updateTemplate(vo);
            commentTemplateMapper.deleteTemplateAuthority(id);
        }else {
            vo.setFcu(UserContext.get().getUserUuid(true));
            vo.setLcu(UserContext.get().getUserUuid(true));
            commentTemplateMapper.insertTemplate(vo);
        }

        if(CollectionUtils.isNotEmpty(authList)){
            List<ProcessCommentTemplateAuthVo> list = new ArrayList<>();
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
            commentTemplateMapper.batchInsertAuthority(list);
        }

        returnObj.put("id", vo.getId());
        return returnObj;
    }
}
