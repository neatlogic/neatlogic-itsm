package codedriver.module.process.condition.handler;

import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.module.process.workcenter.core.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class ProcessTaskStartTimeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String getName() {
        return "starttime";
    }

    @Override
    public String getDisplayName() {
        return "上报时间";
    }

    @Override
    public String getHandler(String processWorkcenterConditionType) {
        return FormHandlerType.DATE.toString();
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public JSONObject getConfig() {
        JSONObject config = new JSONObject();
        config.put("type", "datetimerange");
        config.put("value", "");
        config.put("defaultValue", "");
        config.put("format", "yyyy-MM-dd HH:mm:ss");
        config.put("valueType", "timestamp");
        return config;
    }

    @Override
    public Integer getSort() {
        return 4;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.DATE;
    }

    @Override
    protected String getMyEsWhere(Integer index, List<ConditionVo> conditionList) {
        ConditionVo condition = conditionList.get(index);
        return getDateEsWhere(condition, conditionList);
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        if (value != null) {
            if (value instanceof String) {
                return simpleDateFormat.format(new Date(Integer.parseInt(value.toString())));
            } else if (value instanceof List) {
                List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                List<String> textList = new ArrayList<>();
                for (String valueStr : valueList) {
                    textList.add(simpleDateFormat.format(new Date(Long.parseLong(valueStr))));
                }
                return String.join("-", textList);
            }
        }
        return value;
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {
        getDateSqlWhere(conditionList.get(index),sqlSb,new ProcessTaskSqlTable().getShortName(),ProcessTaskSqlTable.FieldEnum.START_TIME.getValue());
    }
}
