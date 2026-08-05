package neatlogic.module.process.api.commenttemplate;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.core.AuthActionChecker;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.UserType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.auth.PROCESS_COMMENT_TEMPLATE_MODIFY;
import neatlogic.framework.process.dto.ProcessCommentTemplateSearchVo;
import neatlogic.framework.process.dto.ProcessCommentTemplateVo;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.process.dao.mapper.process.ProcessCommentTemplateMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessCommentTemplateSearchApi extends PrivateApiComponentBase {

    @Resource
    private ProcessCommentTemplateMapper commentTemplateMapper;


    @Override
    public String getToken() {
        return "process/comment/template/search";
    }

    @Override
    public String getName() {
        return "nmpac.processcommenttemplatesearchapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "nmpac.processcommenttemplatesearchapi.input.param.desc.keyword"),
            @Param(name = "type", type = ApiParamType.ENUM, rule = "system,custom", desc = "nmpac.processcommenttemplatesearchapi.input.param.desc.type"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "nmpac.processcommenttemplatesearchapi.input.param.desc.needpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "nmpac.processcommenttemplatesearchapi.input.param.desc.pagesize"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "nmpac.processcommenttemplatesearchapi.input.param.desc.currentpage")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = ProcessCommentTemplateVo.class, desc = "nmpac.processcommenttemplatesearchapi.output.param.desc.tbodylist")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        /*
        回复模板管理页、工单处理页回复模板、流程编辑页回复模板下拉框的取数逻辑：
        1.当前用户拥有PROCESS_COMMENT_TEMPLATE_MODIFY（系统回复模版管理权限）时，可以看到本人创建的模板和所有的系统模板；
        2.当前用户没有PROCESS_COMMENT_TEMPLATE_MODIFY（系统回复模版管理权限）时，可以看到本人创建的模板和有授权的系统模板；
         */
        ProcessCommentTemplateSearchVo searchVo = jsonObj.toJavaObject(ProcessCommentTemplateSearchVo.class);
        searchVo.setUserUuid(UserContext.get().getUserUuid());
        if (AuthActionChecker.check(PROCESS_COMMENT_TEMPLATE_MODIFY.class)) {
            searchVo.setIsHasModifyAuthority(1);
        } else {
            List<String> uuidList = UserContext.get().getUuidList();
            uuidList.add(UserType.ALL.getValue());
            searchVo.setUuidList(uuidList);
        }
        int rowNum = commentTemplateMapper.searchCommentTemplateCount(searchVo);
        if (rowNum == 0) {
            return TableResultUtil.getResult(new ArrayList<>(), searchVo);
        }
        searchVo.setRowNum(rowNum);
        List<ProcessCommentTemplateVo> tbodyList = commentTemplateMapper.searchCommentTemplateList(searchVo);
        if (CollectionUtils.isNotEmpty(tbodyList)) {
            //有系统模版管理权限才能编辑系统模版
            for (ProcessCommentTemplateVo processCommentTemplateVo : tbodyList) {
                if (ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue().equals(processCommentTemplateVo.getType())) {
                    if (AuthActionChecker.check(PROCESS_COMMENT_TEMPLATE_MODIFY.class.getSimpleName())) {
                        processCommentTemplateVo.setIsEditable(1);
                    } else {
                        processCommentTemplateVo.setIsEditable(0);
                    }
                } else if (ProcessCommentTemplateVo.TempalteType.CUSTOM.getValue().equals(processCommentTemplateVo.getType())) {
                    processCommentTemplateVo.setIsEditable(1);
                }
            }
        }
        return TableResultUtil.getResult(tbodyList, searchVo);
    }
}
