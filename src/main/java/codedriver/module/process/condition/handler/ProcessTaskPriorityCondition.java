package codedriver.module.process.condition.handler;

import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.framework.process.constvalue.ConditionConfigType;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessTaskPriorityCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {
    @Autowired
    private PriorityMapper priorityMapper;

    private String formHandlerType = FormHandlerType.SELECT.toString();

    @Override
    public String getName() {
        return "priority";
    }

    @Override
    public String getDisplayName() {
        return "优先级";
    }

	@Override
	public String getHandler(FormConditionModel formConditionModel) {
        if (FormConditionModel.SIMPLE == formConditionModel) {
            formHandlerType = FormHandlerType.CHECKBOX.toString();
        } else {
            formHandlerType = FormHandlerType.SELECT.toString();
        }
		return formHandlerType;
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

    @Override
    public JSONObject getConfig(ConditionConfigType type) {
        PriorityVo priorityVo = new PriorityVo();
        priorityVo.setIsActive(1);
        List<PriorityVo> priorityList = priorityMapper.searchPriorityList(priorityVo);
        JSONArray dataList = new JSONArray();
        for (PriorityVo priority : priorityList) {
            dataList.add(new ValueTextVo(priority.getUuid(), priority.getName()));
        }
        JSONObject config = new JSONObject();
        config.put("type", formHandlerType);
        config.put("search", false);
        config.put("multiple", true);
        config.put("value", "");
        config.put("defaultValue", new ArrayList<String>());
        config.put("dataList", dataList);
        /** 以下代码是为了兼容旧数据结构，前端有些地方还在用 **/
        config.put("isMultiple", true);
        return config;
    }

    @Override
    public Integer getSort() {
        return 5;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        if (value != null) {
            if (value instanceof String) {
                PriorityVo priorityVo = priorityMapper.getPriorityByUuid(value.toString());
                if (priorityVo != null) {
                    return priorityVo.getName();
                }
            } else if (value instanceof List) {
                List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                List<String> textList = new ArrayList<>();
                for (String valueStr : valueList) {
                    PriorityVo priorityVo = priorityMapper.getPriorityByUuid(valueStr);
                    if (priorityVo != null) {
                        textList.add(priorityVo.getName());
                    } else {
                        textList.add(valueStr);
                    }
                }
                return String.join("、", textList);
            }
        }
        return value;
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {
        getSimpleSqlConditionWhere(conditionList.get(index), sqlSb, new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.PRIORITY_UUID.getValue());
    }

    @Override
    public Object getConditionParamData(ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
        if (processTaskVo == null) {
            return null;
        }
        return processTaskVo.getPriorityUuid();
    }
}
