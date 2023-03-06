package neatlogic.module.process.api.commenttemplate;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.UserType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dto.AuthenticationInfoVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.ProcessCommentTemplateMapper;
import neatlogic.framework.process.dto.ProcessCommentTemplateVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.process.auth.PROCESS_COMMENT_TEMPLATE_MODIFY;
import neatlogic.framework.service.AuthenticationInfoService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessCommentTemplateSearchForTaskApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessCommentTemplateMapper commentTemplateMapper;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

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
        AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(UserContext.get().getUserUuid(true));
        List<String> uuidList = new ArrayList<>();
        uuidList.addAll(authenticationInfoVo.getTeamUuidList());
        uuidList.addAll(authenticationInfoVo.getRoleUuidList());
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