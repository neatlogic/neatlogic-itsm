package codedriver.module.process.api.commenttemplate;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.dao.mapper.ProcessCommentTemplateMapper;
import codedriver.framework.process.dto.ProcessCommentTemplateVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.auth.label.PROCESS_COMMENT_TEMPLATE_MODIFY;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessCommentTemplateSearchForTaskApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessCommentTemplateMapper commentTemplateMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public String getToken() {
        return "process/comment/template/search/fortask";
    }

    @Override
    public String getName() {
        return "查询处理页回复模版";
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
            @Param(explode= BasePageVo.class),
            @Param(name = "tbodyList", explode = ProcessCommentTemplateVo.class, desc = "回复模版集合")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessCommentTemplateVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessCommentTemplateVo>() {});
        /** 根据当前用户所在组、角色筛选其能看到的模版 */
        List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(UserContext.get().getUserUuid());
        List<String> roleUuidList = userMapper.getRoleUuidListByUserUuid(UserContext.get().getUserUuid());
        List<String> uuidList = new ArrayList<>();
        uuidList.addAll(teamUuidList);
        uuidList.addAll(roleUuidList);
        uuidList.add(UserContext.get().getUserUuid());
        uuidList.add(UserType.ALL.getValue());
        vo.setAuthList(uuidList);
        JSONObject returnObj = new JSONObject();
        if (vo.getNeedPage()) {
            int rowNum = commentTemplateMapper.searchTemplateCountForTask(vo);
            returnObj.put("pageSize", vo.getPageSize());
            returnObj.put("currentPage", vo.getCurrentPage());
            returnObj.put("rowNum", rowNum);
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, vo.getPageSize()));
        }
        List<ProcessCommentTemplateVo> tbodyList = commentTemplateMapper.searchTemplateForTask(vo);
        if(CollectionUtils.isNotEmpty(tbodyList)){
            /** 有系统模版管理权限才能编辑系统模版 */
            tbodyList.stream().forEach(o -> {
                if((ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue().equals(o.getType())
                && AuthActionChecker.check(PROCESS_COMMENT_TEMPLATE_MODIFY.class.getSimpleName()))
                || ProcessCommentTemplateVo.TempalteType.CUSTOM.getValue().equals(o.getType())){
                    o.setIsEditable(1);
                }else{
                    o.setIsEditable(0);
                }
            });
        }
        returnObj.put("tbodyList", tbodyList);
        return returnObj;
    }
}
