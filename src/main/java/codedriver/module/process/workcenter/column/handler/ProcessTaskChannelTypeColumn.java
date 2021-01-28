package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.ChannelTypeSqlTable;
import codedriver.framework.process.workcenter.table.util.SqlTableUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ProcessTaskChannelTypeColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{

	@Autowired
	ChannelMapper channelMapper;
	
	@Override
	public String getName() {
		return "channeltype";
	}

	@Override
	public String getDisplayName() {
		return "服务类型";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		String channelTypeUuid = json.getString(this.getName());
		JSONObject channelTypeJson = new JSONObject();
		ChannelTypeVo channelType = channelMapper.getChannelTypeByUuid(channelTypeUuid);
		channelTypeJson.put("value", channelTypeUuid);
		if(channelType != null) {
			channelTypeJson.put("text", channelType.getName());
			channelTypeJson.put("color", channelType.getColor());

		}
		return channelTypeJson;
	}

	@Override
	public JSONObject getMyValueText(JSONObject json) {
		return (JSONObject) getMyValue(json);
	}
	
	@Override
	public Boolean allowSort() {
		return false;
	}

	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getSort() {
		return 8;
	}

	@Override
	public Object getSimpleValue(Object json) {
		String channelType = null;
		if(json != null){
			channelType = JSONObject.parseObject(json.toString()).getString("text");
		}
		return channelType;
	}

	@Override
	public Object getValue(ProcessTaskVo processTaskVo) {
		JSONObject channelTypeJson = new JSONObject();
		ChannelTypeVo channelTypeVo = processTaskVo.getChannelVo().getChannelTypeVo();
		channelTypeJson.put("value",channelTypeVo.getUuid());
		channelTypeJson.put("text",channelTypeVo.getName());
		channelTypeJson.put("color",channelTypeVo.getColor());
		return channelTypeJson;
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<TableSelectColumnVo>(){
			{
				add(new TableSelectColumnVo(new ChannelTypeSqlTable(), Arrays.asList(
						new SelectColumnVo(ChannelTypeSqlTable.FieldEnum.UUID.getValue(),"ChannelTypeUuid"),
						new SelectColumnVo(ChannelTypeSqlTable.FieldEnum.NAME.getValue(),"ChannelTypeName"),
						new SelectColumnVo(ChannelTypeSqlTable.FieldEnum.COLOR.getValue(),"ChannelTypeColor")
				)));
			}
		};
	}

	@Override
	public List<JoinTableColumnVo> getMyJoinTableColumnList() {
		return SqlTableUtil.getChannelTypeJoinTableSql();
	}
}
