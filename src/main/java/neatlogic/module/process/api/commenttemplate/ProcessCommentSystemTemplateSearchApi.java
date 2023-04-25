package neatlogic.module.process.api.commenttemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.auth.PROCESS_COMMENT_TEMPLATE_MODIFY;
import neatlogic.framework.process.dao.mapper.ProcessCommentTemplateMapper;
import neatlogic.framework.process.dto.ProcessCommentTemplateVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessCommentSystemTemplateSearchApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessCommentTemplateMapper commentTemplateMapper;

    @Override
    public String getToken() {
        return "process/comment/system/template/search";
    }

    @Override
    public String getName() {
        return "查询系统回复模版";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = ProcessCommentTemplateVo.class, desc = "回复模版集合")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessCommentTemplateVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessCommentTemplateVo>() {
        });
//        vo.setType(ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue());

        if (!AuthActionChecker.check(PROCESS_COMMENT_TEMPLATE_MODIFY.class.getSimpleName())) {
            vo.setAuthList(Collections.singletonList(UserContext.get().getUserUuid()));
            vo.setType(ProcessCommentTemplateVo.TempalteType.CUSTOM.getValue());
        }
        vo.setSearchByLcu(1);
        JSONObject returnObj = new JSONObject();
        if (vo.getNeedPage()) {
            int rowNum = commentTemplateMapper.searchTemplateCount(vo);
            returnObj.put("pageSize", vo.getPageSize());
            returnObj.put("currentPage", vo.getCurrentPage());
            returnObj.put("rowNum", rowNum);
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, vo.getPageSize()));
        }
        List<ProcessCommentTemplateVo> tbodyList = commentTemplateMapper.searchTemplate(vo);
        returnObj.put("tbodyList", tbodyList);
        return returnObj;
    }
}
