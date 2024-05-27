package neatlogic.module.process.condition.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.FormHandlerType;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.dto.condition.ConditionVo;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.IProcessTaskSource;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.constvalue.ProcessTaskSourceFactory;
import neatlogic.framework.process.dto.SqlDecoratorVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.table.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ProcessTaskInvokeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    @Override
    public String getName() {
        return "invoke";
    }

    @Override
    public String getDisplayName() {
        return "工单来源";
    }

    @Override
    public String getHandler(FormConditionModel processWorkcenterConditionType) {
        return FormHandlerType.SELECT.toString();
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public JSONObject getConfig(ConditionConfigType type) {
        JSONArray dataList = new JSONArray();
        List<IProcessTaskSource> processTaskSources = ProcessTaskSourceFactory.getSourceList();
        if (CollectionUtils.isNotEmpty(processTaskSources)) {
            for (IProcessTaskSource processTaskSource : processTaskSources) {
                dataList.add(new ValueTextVo(processTaskSource.getValue(), processTaskSource.getText()));
            }
        }

        JSONObject config = new JSONObject();
        config.put("type", FormHandlerType.SELECT.toString());
        config.put("search", false);
        config.put("multiple", true);
        config.put("value", "");
        config.put("defaultValue", "");
        config.put("dataList", dataList);
        config.put("firstSelect", false);
        /** 以下代码是为了兼容旧数据结构，前端有些地方还在用 **/
        config.put("isMultiple", true);
        return config;
    }

    @Override
    public Integer getSort() {
        return 24;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        List<String> valueList = null;
        if (value != null) {
            if (value instanceof String) {
                valueList = Collections.singletonList(value.toString());
            } else if (value instanceof List) {
                valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
            }
            List<String> textList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(valueList)) {
                for (String valueStr : valueList) {
                    IProcessTaskSource processTaskSource = ProcessTaskSourceFactory.getHandler(valueStr);
                    if (processTaskSource != null) {
                        String text = processTaskSource.getText();
                        if (text != null) {
                            textList.add(text);
                        } else {
                            textList.add(valueStr);
                        }
                    }
                }
            }
            return String.join("、", textList);
        }
        return value;
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {
        getSimpleSqlConditionWhere(conditionList.get(index), sqlSb, new ProcessTaskInvokeSqlTable().getShortName(), ProcessTaskInvokeSqlTable.FieldEnum.SOURCE.getValue());
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList(SqlDecoratorVo sqlDecoratorVo) {
        return new ArrayList<JoinTableColumnVo>() {
            {
                add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskInvokeSqlTable(), new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskInvokeSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
                }}));
            }
        };
    }
}
