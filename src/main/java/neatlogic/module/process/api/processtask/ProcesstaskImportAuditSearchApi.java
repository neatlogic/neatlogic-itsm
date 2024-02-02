package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.process.auth.BATCH_REPORT_PROCESS_TASK;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskImportAuditVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = BATCH_REPORT_PROCESS_TASK.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcesstaskImportAuditSearchApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "processtask/import/audit/search";
    }

    @Override
    public String getName() {
        return "查询工单导入记录";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword",
                    type = ApiParamType.STRING,
                    desc = "关键词",
                    xss = true),
            @Param(name = "status",
                    type = ApiParamType.INTEGER,
                    desc = "上报状态"),
            @Param(name = "currentPage",
                    type = ApiParamType.INTEGER,
                    desc = "当前页"),
            @Param(name = "pageSize",
                    type = ApiParamType.INTEGER,
                    desc = "每页数据条目"),
            @Param(name = "needPage",
                    type = ApiParamType.BOOLEAN,
                    desc = "是否需要分页，默认true")
    })
    @Output({
            @Param(name = "auditList",
                    type = ApiParamType.JSONARRAY,
                    explode = ProcessTaskImportAuditVo[].class,
                    desc = "工单导入记录"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "查询工单导入记录")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessTaskImportAuditVo auditVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessTaskImportAuditVo>() {
        });
        JSONObject returnObj = new JSONObject();
        if (auditVo.getNeedPage()) {
            int rowNum = processTaskMapper.searchProcessTaskImportAuditCount(auditVo);
            returnObj.put("pageSize", auditVo.getPageSize());
            returnObj.put("currentPage", auditVo.getCurrentPage());
            returnObj.put("rowNum", rowNum);
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, auditVo.getPageSize()));
        }
        List<ProcessTaskImportAuditVo> auditList = processTaskMapper.searchProcessTaskImportAudit(auditVo);
        if (CollectionUtils.isNotEmpty(auditList)) {
            for (ProcessTaskImportAuditVo importAuditVo : auditList) {
                UserVo user = userMapper.getUserByUserId(importAuditVo.getOwner());
                if (user == null) {
                    user = userMapper.getUserBaseInfoByUuid(importAuditVo.getOwner());
                }
                importAuditVo.setOwnerVo(user);
            }
        }
        returnObj.put("auditList", auditList);
        return returnObj;
    }

}
