package codedriver.module.process.condition.handler;

import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcessTaskOwnerLevelCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {


    @Override
    public String getName() {
        return "ownerlevel";
    }

    @Override
    public String getDisplayName() {
        return "上报人是否VIP";
    }

    @Override
    public String getHandler(String processWorkcenterConditionType) {
        return FormHandlerType.SELECT.toString();
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public JSONObject getConfig() {
        JSONObject config = new JSONObject();
        config.put("type", FormHandlerType.SELECT.toString());
//		config.put("multiple", true);
//		config.put("isMultiple", true);//为兼容旧数据结构
        JSONArray dataList = new JSONArray();
        dataList.add(new ValueTextVo("1", "是"));
        dataList.add(new ValueTextVo("0", "否"));
        config.put("dataList", dataList);
        return config;
    }

    @Override
    public Integer getSort() {
        return 19;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
    }

    @Override
    protected String getMyEsWhere(Integer index, List<ConditionVo> conditionList) {
        return null;
    }


    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        return null;
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {

    }
}
