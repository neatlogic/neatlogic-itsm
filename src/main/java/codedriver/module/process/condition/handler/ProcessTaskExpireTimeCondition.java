package codedriver.module.process.condition.handler;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.constvalue.ProcessFieldType;

@Component
public class ProcessTaskExpireTimeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    private String formHandlerType = FormHandlerType.SELECT.toString();

    @Override
    public String getName() {
        return "expiretime";
    }

    @Override
    public String getDisplayName() {
        return "是否超时";
    }

    @Override
    public String getHandler(String processWorkcenterConditionType) {
        if (ProcessConditionModel.SIMPLE.getValue().equals(processWorkcenterConditionType)) {
            formHandlerType = FormHandlerType.RADIO.toString();
        }
        return formHandlerType;
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public String getMyEsName() {
        return String.format(" %s.%s", getType(), "expiretime.slaTimeVo.expireTimeLong");
    }

    @Override
    public JSONObject getConfig() {
        JSONArray dataList = new JSONArray();
        dataList.add(new ValueTextVo("1", "是"));
        // TODO es 封装暂时不支持 判断空key
        // dataList.add(new ValueTextVo("0", "否"));

        JSONObject config = new JSONObject();
        config.put("type", formHandlerType);
        config.put("search", false);
        config.put("multiple", false);
        config.put("value", "");
        config.put("defaultValue", "");
        config.put("dataList", dataList);
        /** 以下代码是为了兼容旧数据结构，前端有些地方还在用 **/
        config.put("isMultiple", false);
        return config;
    }

    @Override
    public Integer getSort() {
        return 10;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
    }

    @Override
    protected String getMyEsWhere(Integer index, List<ConditionVo> conditionList) {
        String where = StringUtils.EMPTY;
        ConditionVo condition = conditionList.get(index);
        List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
        Object value = valueList.get(0);
        if ("1".equals(value)) {
            // 超时单
            where = String.format(Expression.LESSTHAN.getExpressionEs(), this.getEsName(), System.currentTimeMillis());
                //+ " and "  +String.format(Expression.INCLUDE.getExpressionEs(),((IProcessTaskCondition)ConditionHandlerFactory.getHandler(ProcessWorkcenterField.STATUS.getValue())).getEsName(), "'running'");
        } else { // TODO es 封装暂时不支持 判断空key
            // where =
            // String.format(Expression.EQUAL.getExpressionEs(),ProcessWorkcenterField.getConditionValue(condition.getName())+".slaTimeVo.expireTimeLong",null)
            // + " or ";
            // where +=
            // String.format(Expression.LESSTHAN.getExpressionEs(),ProcessWorkcenterField.getConditionValue(condition.getName())+".slaTimeVo.expireTimeLong",System.currentTimeMillis());
        }

        return where;
    }

    @Override
    public String getMyEsOrderBy(String sortType) {
        String orderBy = StringUtils.EMPTY;
        if ("DESC".equalsIgnoreCase(sortType.trim())) {
            sortType = "ASC";
        } else {
            sortType = "DESC";
        }
        if (StringUtils.isBlank(orderBy)) {
            orderBy = String.format(" %s %s ", this.getEsName(), sortType.toUpperCase());
        }
        return orderBy;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        return null;
    }
}