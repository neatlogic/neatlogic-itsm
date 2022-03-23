package codedriver.module.process.api.workcenter;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.exception.workcenter.WorkcenterNotFoundException;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.NewWorkcenterService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkcenterDataSearchApi extends PrivateApiComponentBase {
    @Resource
    WorkcenterMapper workcenterMapper;

    @Resource
    NewWorkcenterService newWorkcenterService;

    @Override
    public String getToken() {
        return "workcenter/search";
    }

    @Override
    public String getName() {
        return "工单中心搜索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "分类uuid,有则去数据库获取对应分类的条件，无则根据传的过滤条件查询"),
            @Param(name = "isMeWillDo", type = ApiParamType.INTEGER, desc = "是否带我处理的，1：是；0：否"),
            @Param(name = "conditionGroupList", type = ApiParamType.JSONARRAY, desc = "条件组条件", isRequired = false),
            @Param(name = "conditionGroupRelList", type = ApiParamType.JSONARRAY, desc = "条件组连接类型", isRequired = false),
            @Param(name = "sortList", type = ApiParamType.JSONARRAY, desc = "排序", isRequired = false),
            @Param(name = "headerList", type = ApiParamType.JSONARRAY, desc = "显示的字段", isRequired = false),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数", isRequired = false),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目", isRequired = false)
    })
    @Output({
            @Param(name = "headerList", type = ApiParamType.JSONARRAY, desc = "展示的字段"),
            @Param(name = "dataList", type = ApiParamType.JSONARRAY, desc = "展示的值"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, desc = "总数"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页数"),
            @Param(name = "pageCount", type = ApiParamType.INTEGER, desc = "总页数"),
    })
    @Description(desc = "工单中心搜索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        //Long startTime  = System.currentTimeMillis();
        if (jsonObj.containsKey("uuid")) {
            String uuid = jsonObj.getString("uuid");
            Integer currentPage = jsonObj.getInteger("currentPage");
            Integer pageSize = jsonObj.getInteger("pageSize");
            JSONArray sortList = jsonObj.getJSONArray("sortList");
            WorkcenterVo workcenter = workcenterMapper.getWorkcenterByUuid(uuid);
            if (workcenter != null) {
                jsonObj = JSONObject.parseObject(workcenter.getConditionConfig());
                jsonObj.put("uuid", uuid);
                jsonObj.put("currentPage", currentPage);
                jsonObj.put("pageSize", pageSize);
                if (CollectionUtils.isNotEmpty(sortList)) {
                    jsonObj.put("sortList", sortList);
                }
            }else{
                throw new WorkcenterNotFoundException(uuid);
            }
        }
        WorkcenterVo workcenterVo = new WorkcenterVo(jsonObj);
        //System.out.println((System.currentTimeMillis() - startTime) + " ##start workcenter-thead:-------------------------------------------------------------------------------");
        //workcenterVo.setSqlFieldType(FieldTypeEnum.DISTINCT_ID.getValue());
        return newWorkcenterService.doSearch(workcenterVo);
        //return workcenterService.doSearch(workcenterVo);
    }

}
