package codedriver.module.process.condition.handler;

import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ConditionConfigType;
import codedriver.framework.process.constvalue.ProcessFieldType;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ProcessTaskContentCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    @Override
    public String getName() {
        return "content";
    }

    @Override
    public String getDisplayName() {
        return "上报内容";
    }

    @Override
    public String getHandler(String processWorkcenterConditionType) {
        return FormHandlerType.INPUT.toString();
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public JSONObject getConfig(ConditionConfigType type) {
        JSONObject config = new JSONObject();
        config.put("type", "text");
        config.put("value", "");
        config.put("defaultValue", "");
        config.put("maxlength", 50);
//		config.put("name", "");
//		config.put("label", "");
        return config;
    }

    @Override
    public Integer getSort() {
        return 3;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.STRING;
    }

    @Override
    public Expression getExpression() {
        return Expression.LIKE;
    }

    @Override
    public List<Expression> getExpressionList() {
        return Arrays.asList(Expression.LIKE, Expression.NOTLIKE);
    }

    @Override
    protected String getMyEsWhere(Integer index, List<ConditionVo> conditionList) {
        ConditionVo condition = conditionList.get(index);
        String where = "(";
        if (condition.getValueList() instanceof String) {
            Object value = condition.getValueList();
            where += String.format(Expression.getExpressionEs(condition.getExpression()), this.getEsName(), String.format("'%s'", value));
        } else if (condition.getValueList() instanceof List) {
            List<String> keywordList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
            if (keywordList.size() == 1) {
                Object value = keywordList.get(0);
                where += String.format(Expression.getExpressionEs(condition.getExpression()), this.getEsName(), String.format("'%s'", value));
            } else {
                for (int i = 0; i < keywordList.size(); i++) {
                    if (i != 0) {
                        where += " or ";
                    }
                    where += String.format(Expression.getExpressionEs(condition.getExpression()), this.getEsName(), String.format("'%s'", keywordList.get(i)));
                }
            }
        }

        return where + ")";
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        return value;
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {

    }
}
