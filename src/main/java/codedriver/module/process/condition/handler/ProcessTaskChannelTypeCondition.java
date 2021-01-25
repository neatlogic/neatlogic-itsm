package codedriver.module.process.condition.handler;

import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.table.ChannelSqlTable;
import codedriver.framework.process.workcenter.table.ChannelTypeSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class ProcessTaskChannelTypeCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{

	@Autowired
	ChannelMapper channelMapper;
	
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
	public String getHandler(String processWorkcenterConditionType) {
		if(ProcessConditionModel.SIMPLE.getValue().equals(processWorkcenterConditionType)) {
			formHandlerType = FormHandlerType.CHECKBOX.toString();
		}
		return formHandlerType;
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
		List<ChannelTypeVo>  channellist = channelMapper.searchChannelTypeList(new ChannelTypeVo());
		JSONArray dataList = new JSONArray();
		for(ChannelTypeVo channelType:channellist) {
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
		if(value != null) {
			if(value instanceof String) {
				ChannelTypeVo channelTypeVo = channelMapper.getChannelTypeByUuid(value.toString());
				if(channelTypeVo != null) {
					return channelTypeVo.getName();
				}
			}else if(value instanceof List){
				List<String> valueList = JSON.parseArray(JSON.toJSONString(value), String.class);
				List<String> textList = new ArrayList<>();
				for(String valueStr : valueList) {
					ChannelTypeVo channelTypeVo = channelMapper.getChannelTypeByUuid(valueStr);
					if(channelTypeVo != null) {
						textList.add(channelTypeVo.getName());					
					}else {
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
		getSimpleSqlConditionWhere(conditionList.get(index), sqlSb,new ChannelTypeSqlTable().getShortName(),ChannelTypeSqlTable.FieldEnum.UUID.getValue());
	}

	@Override
	public List<JoinTableColumnVo> getMyJoinTableColumnList() {
		return new ArrayList<JoinTableColumnVo>() {
			{
				add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ChannelSqlTable(), new HashMap<String, String>() {{
					put(ProcessTaskSqlTable.FieldEnum.CHANNEL_UUID.getValue(), ChannelSqlTable.FieldEnum.UUID.getValue());
				}}));
				add(new JoinTableColumnVo(new ChannelSqlTable(), new ChannelTypeSqlTable(), new HashMap<String, String>() {{
					put(ChannelSqlTable.FieldEnum.CHANNEL_TYPE_UUID.getValue(), ChannelTypeSqlTable.FieldEnum.UUID.getValue());
				}}));
			}
		};
	}
}
