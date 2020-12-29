package codedriver.module.process.api.commenttemplate;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ProcessCommentTemplateMapper;
import codedriver.framework.process.dto.ProcessCommentTemplateVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessCommentSystemTemplateSearchForSelectApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessCommentTemplateMapper commentTemplateMapper;

    @Override
    public String getToken() {
        return "process/comment/system/template/search/forselect";
    }

    @Override
    public String getName() {
        return "查询系统回复模版_下拉框";
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
            @Param(name = "tbodyList", explode = ValueTextVo.class, desc = "回复模版集合")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessCommentTemplateVo vo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessCommentTemplateVo>() {});
        vo.setType(ProcessCommentTemplateVo.TempalteType.SYSTEM.getValue());
        JSONObject returnObj = new JSONObject();
        if (vo.getNeedPage()) {
            int rowNum = commentTemplateMapper.searchTemplateCount(vo);
            returnObj.put("pageSize", vo.getPageSize());
            returnObj.put("currentPage", vo.getCurrentPage());
            returnObj.put("rowNum", rowNum);
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, vo.getPageSize()));
        }
        List<ValueTextVo> tbodyList = commentTemplateMapper.searchTemplateForSelect(vo);
        returnObj.put("tbodyList", tbodyList);
        return returnObj;
    }
}
