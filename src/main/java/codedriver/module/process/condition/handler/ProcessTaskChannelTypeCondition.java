package codedriver.module.process.condition.handler;

import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ConditionConfigType;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.ChannelTypeSqlTable;
import codedriver.framework.process.workcenter.table.util.SqlTableUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessTaskChannelTypeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    @Autowired
    ChannelTypeMapper channelTypeMapper;

    private String formHandlerType = FormHandlerType.SELECT.toString();

    @Override
    public String getName() {
        return "channeltype";
    }

    @Override
    public String getDisplayName() {
        return "服务类型";
    }

	@Override
	public String getHandler(FormConditionModel processWorkcenterConditionType) {
		if(FormConditionModel.SIMPLE == processWorkcenterConditionType) {
			formHandlerType = FormHandlerType.CHECKBOX.toString();
		}
		return formHandlerType;
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

    @Override
    public JSONObject getConfig(ConditionConfigType type) {
        ChannelTypeVo searchVo = new ChannelTypeVo();
        searchVo.setIsActive(1);
        List<ChannelTypeVo> channelList = channelTypeMapper.searchChannelTypeList(searchVo);
        JSONArray dataList = new JSONArray();
        for (ChannelTypeVo channelType : channelList) {
            dataList.add(new ValueTextVo(channelType.getUuid(), channelType.getName()));
        }
        JSONObject config = new JSONObject();
        config.put("type", formHandlerType);
        config.put("search", false);
        config.put("multiple", true);
        config.put("value", "");
        config.put("defaultValue", "");
        config.put("dataList", dataList);
        /** 以下代码是为了兼容旧数据结构，前端有些地方还在用 **/
        config.put("isMultiple", true);
        return config;
    }

    @Override
    public Integer getSort() {
        return 6;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        if (value != null) {
            if (value instanceof String) {
                ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(value.toString());
                if (channelTypeVo != null) {
                    return channelTypeVo.getName();
                }
            } else if (value instanceof List) {
                List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
                List<String> textList = new ArrayList<>();
                for (String valueStr : valueList) {
                    ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(valueStr);
                    if (channelTypeVo != null) {
                        textList.add(channelTypeVo.getName());
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
        getSimpleSqlConditionWhere(conditionList.get(index), sqlSb, new ChannelTypeSqlTable().getShortName(), ChannelTypeSqlTable.FieldEnum.UUID.getValue());
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList(WorkcenterVo workcenterVo) {
        return SqlTableUtil.getChannelTypeJoinTableSql();
    }
}
