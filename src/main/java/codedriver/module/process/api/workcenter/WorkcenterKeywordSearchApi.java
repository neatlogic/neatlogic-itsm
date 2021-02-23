package codedriver.module.process.api.workcenter;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.elasticsearch.core.ElasticSearchHandlerFactory;
import codedriver.framework.elasticsearch.core.IElasticSearchHandler;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionFactory;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.elasticsearch.constvalue.ESHandler;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.condition.handler.ProcessTaskSerialNumberCondition;
import codedriver.module.process.condition.handler.ProcessTaskTitleCondition;
import codedriver.module.process.service.NewWorkcenterService;
import codedriver.module.process.service.WorkcenterService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;
import com.techsure.multiattrsearch.query.QueryResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkcenterKeywordSearchApi extends PrivateApiComponentBase {
    @Autowired
    WorkcenterMapper workcenterMapper;
    @Autowired
    WorkcenterService workcenterService;

    @Autowired
    NewWorkcenterService newWorkcenterService;

    @Override
    public String getToken() {
        return "workcenter/keyword/search";
    }

    @Override
    public String getName() {
        return "搜索工单中心关键字提示";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", isRequired = true),
        @Param(name = "pageSize", type = ApiParamType.STRING, desc = "选项显示数量"),
        @Param(name = "isCombine", type = ApiParamType.INTEGER, desc = "分开：1（pc端）；融合：0（移动端）；  默认1；")})
    @Output({@Param(name = "dataList", type = ApiParamType.JSONARRAY, desc = "展示的值")})
    @Description(desc = "工单中心关键字搜索提示接口，用于输入框输入关键字后，获取提示选项")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String keyword = jsonObj.getString("keyword");
        Integer isCombine = jsonObj.getInteger("isCombine");
        if (StringUtils.isEmpty(keyword)) {
            return CollectionUtils.EMPTY_COLLECTION;
        }
        if (isCombine != null && isCombine == 0) {
            List<IProcessTaskCondition> conditionList = new ArrayList<IProcessTaskCondition>();
            conditionList.add(ProcessTaskConditionFactory.getHandler(ProcessWorkcenterField.TITLE.getValue()));
            conditionList.add(ProcessTaskConditionFactory.getHandler(ProcessWorkcenterField.SERIAL_NUMBER.getValue()));
            return getKeywordOptionMB(conditionList, keyword, jsonObj.getInteger("pageSize"));
        }
        return newWorkcenterService.getKeywordOptionsPCNew(new WorkcenterVo(jsonObj));
    }

    /**
     * 根据关键字获取所有过滤选项 移动端
     * 
     * @param keyword
     * @return
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    private JSONArray getKeywordOptionMB(List<IProcessTaskCondition> conditionList, String keyword, Integer pageSize) {
        JSONArray returnArray = new JSONArray();
        WorkcenterVo workcenter = getKeywordConditionMB(conditionList, keyword);
        workcenter.setPageSize(pageSize);
        IElasticSearchHandler<WorkcenterVo, QueryResult> esHandler =
            ElasticSearchHandlerFactory.getHandler(ESHandler.PROCESSTASK.getValue());
        List<MultiAttrsObject> dataList = esHandler.search(workcenter).getData();
        if (!dataList.isEmpty()) {
            conditionList.add(ProcessTaskConditionFactory.getHandler(ProcessWorkcenterField.STATUS.getValue()));
            JSONObject titleObj = new JSONObject();
            JSONArray resultDateList = new JSONArray();
            for (MultiAttrsObject titleEl : dataList) {
                JSONObject data = new JSONObject();
                for (IProcessTaskCondition condition : conditionList) {
                    IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(condition.getName());
                    if (column == null) {
                        continue;
                    }
                    data.put(condition.getName(), column.getValue(titleEl));
                }
                resultDateList.add(data);
            }
            titleObj.put("dataList", resultDateList);
            returnArray.add(titleObj);
        }
        return returnArray;
    }

    /**
     * 拼接关键字过滤选项 移动端
     * 
     * @param conditionList
     *            搜索内容类型
     * @return
     */
    @Deprecated
    private WorkcenterVo getKeywordConditionMB(List<IProcessTaskCondition> conditionList, String keyword) {

        JSONObject searchObj = new JSONObject();
        JSONArray conditionGroupList = new JSONArray();
        JSONObject conditionGroup = new JSONObject();
        JSONArray conditionArray = new JSONArray();
        JSONArray conditionRelArray = new JSONArray();
        String fromUuid = null;
        for (int i = 0; i < conditionList.size(); i++) {
            IProcessTaskCondition con = conditionList.get(i);
            JSONObject conditionObj = new JSONObject();
            String uuid = UUID.randomUUID().toString().replace("-", "");
            conditionObj.put("uuid", uuid);
            conditionObj.put("name", con.getName());
            conditionObj.put("type", con.getType());
            JSONArray valueList = new JSONArray();
            valueList.add(keyword);
            conditionObj.put("valueList", valueList);
            conditionObj.put("expression", Expression.LIKE.getExpression());
            conditionArray.add(conditionObj);
            if (i != 0) {
                JSONObject conditionRel = new JSONObject();
                conditionRel.put("from", fromUuid);
                conditionRel.put("to", uuid);
                conditionRel.put("joinType", "or");
                conditionRelArray.add(conditionRel);
            } else {
                fromUuid = uuid;
            }

        }
        conditionGroup.put("conditionList", conditionArray);
        conditionGroup.put("conditionRelList", conditionRelArray);
        conditionGroupList.add(conditionGroup);
        searchObj.put("conditionGroupList", conditionGroupList);
        return new WorkcenterVo(searchObj);

    }



    /**
     * 根据单个关键字获取过滤选项 pc端
     * 
     * @param keyword
     * @return
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    private JSONArray getKeywordOptionPC(IProcessTaskCondition condition, String keyword, Integer pageSize) {
        JSONArray returnArray = new JSONArray();
        WorkcenterVo workcenter = getKeywordConditionPC(condition, keyword);
        workcenter.setPageSize(pageSize);
        IElasticSearchHandler<WorkcenterVo, QueryResult> esHandler =
            ElasticSearchHandlerFactory.getHandler(ESHandler.PROCESSTASK.getValue());
        List<MultiAttrsObject> dataList = esHandler.search(workcenter).getData();
        if (!dataList.isEmpty()) {
            JSONObject titleObj = new JSONObject();
            JSONArray titleDataList = new JSONArray();
            for (MultiAttrsObject titleEl : dataList) {
                IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(condition.getName());
                if (column == null) {
                    continue;
                }
                titleDataList.add(column.getValue(titleEl));
            }
            titleObj.put("dataList", titleDataList);
            titleObj.put("value", condition.getName());
            titleObj.put("text", condition.getDisplayName());
            returnArray.add(titleObj);
        }
        return returnArray;
    }

    /**
     * 拼接关键字过滤选项 pc端
     * 
     * @param keyword
     *            搜索内容类型
     * @return
     */
    @Deprecated
    private WorkcenterVo getKeywordConditionPC(IProcessTaskCondition condition, String keyword) {
        JSONObject searchObj = new JSONObject();
        JSONArray conditionGroupList = new JSONArray();
        JSONObject conditionGroup = new JSONObject();
        JSONArray conditionList = new JSONArray();
        JSONObject conditionObj = new JSONObject();
        conditionObj.put("name", condition.getName());
        conditionObj.put("type", condition.getType());
        JSONArray valueList = new JSONArray();
        valueList.add(keyword);
        conditionObj.put("valueList", valueList);
        conditionObj.put("expression", Expression.LIKE.getExpression());
        conditionList.add(conditionObj);
        conditionGroup.put("conditionList", conditionList);
        conditionGroupList.add(conditionGroup);
        searchObj.put("conditionGroupList", conditionGroupList);

        return new WorkcenterVo(searchObj);

    }
    /**
     * 根据关键字获取所有过滤选项 pc端
     *
     * @param keyword
     * @return
     */
    @Deprecated
    private JSONArray getKeywordOptionsPC(String keyword, Integer pageSize) {
        // 搜索标题
        JSONArray returnArray = getKeywordOptionPC(new ProcessTaskTitleCondition(), keyword, pageSize);
        // 搜索ID
        returnArray.addAll(getKeywordOptionPC(new ProcessTaskSerialNumberCondition(), keyword, pageSize));
        return returnArray;
    }
}
