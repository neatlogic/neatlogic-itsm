package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ProcessTaskStatusColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{

	@Override
	public String getName() {
		return "status";
	}

	@Override
	public String getDisplayName() {
		return "工单状态";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		JSONObject statusJson = new JSONObject();
		String status = json.getString(this.getName());
		statusJson.put("value", status);
		statusJson.put("text", ProcessTaskStatus.getText(status));
		statusJson.put("color", ProcessTaskStatus.getColor(status));
		return statusJson;
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
		return 5;
	}

	@Override
	public Object getSimpleValue(Object json) {
		String status = null;
		if(json != null){
			status = JSONObject.parseObject(json.toString()).getString("text");
		}
		return status;
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<TableSelectColumnVo>(){
			{
				add(new TableSelectColumnVo(new ProcessTaskSqlTable(), Collections.singletonList(
						new SelectColumnVo(ProcessTaskSqlTable.FieldEnum.STATUS.getValue())
				)));
			}
		};
	}

}
