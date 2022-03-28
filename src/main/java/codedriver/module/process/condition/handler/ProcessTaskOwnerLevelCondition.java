package codedriver.module.process.condition.handler;

import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ConditionConfigType;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

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
    public String getHandler(FormConditionModel processWorkcenterConditionType) {
        return FormHandlerType.SELECT.toString();
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public JSONObject getConfig(ConditionConfigType type) {
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
    public Object valueConversionText(Object value, JSONObject config) {
        if (Objects.equals(value, "1")) {
            return "是";
        } else if (Objects.equals(value, "0")) {
            return "否";
        }
        return value;
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {

    }

    @Override
    public Object getConditionParamData(ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
        if (processTaskVo == null) {
            return null;
        }
        UserVo ownerVo = userMapper.getUserBaseInfoByUuid(processTaskVo.getOwner());
        if (ownerVo != null) {
            return ownerVo.getVipLevel();
        }
        return null;
    }
}
