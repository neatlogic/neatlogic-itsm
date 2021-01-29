package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessTaskCurrentStepNameColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{
	@Autowired
	UserMapper userMapper;
	@Autowired
	RoleMapper roleMapper;
	@Autowired
	TeamMapper teamMapper;
	@Override
	public String getName() {
		return "currentstepname";
	}

	@Override
	public String getDisplayName() {
		return "当前步骤名";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		JSONArray stepArray = null;
		try {
		 stepArray = (JSONArray) json.getJSONArray(ProcessWorkcenterField.STEP.getValue());
		}catch(Exception ex){
			return "";
		}
		String processTaskStatus = json.getString("status");
		if(CollectionUtils.isEmpty(stepArray)) {
			return CollectionUtils.EMPTY_COLLECTION;
		}
		JSONArray stepResultArray = JSONArray.parseArray(stepArray.toJSONString());
		ListIterator<Object> stepIterator = stepResultArray.listIterator();
		List<String> stepNameList = new ArrayList<String>();
		while(stepIterator.hasNext()) {
			JSONObject currentStepJson = (JSONObject)stepIterator.next();
			String stepStatus =currentStepJson.getString("status");
			Integer isActive =currentStepJson.getInteger("isactive");
			if(ProcessTaskStatus.RUNNING.getValue().equals(processTaskStatus)&&(ProcessTaskStatus.DRAFT.getValue().equals(stepStatus)||(ProcessTaskStatus.PENDING.getValue().equals(stepStatus)&& isActive == 1)||ProcessTaskStatus.RUNNING.getValue().equals(stepStatus))) {
			    stepNameList.add(currentStepJson.getString("name"));
			}
		}
		return stepNameList;
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
		if(json != null && json instanceof List){
			@SuppressWarnings("unchecked")
            List<String> list = (List<String>) json;
			if(CollectionUtils.isNotEmpty(list)){
				return String.join(";",list);
			}
		}
		return null;
	}

	@Override
	public Object getValue(ProcessTaskVo processTaskVo) {
		List<ProcessTaskStepVo> stepVoList = processTaskVo.getStepList();
		List<String> stepNameList = new ArrayList<>();
		if(ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
			for (ProcessTaskStepVo stepVo : stepVoList) {
				if((ProcessTaskStatus.DRAFT.getValue().equals(stepVo.getStatus())||(ProcessTaskStatus.PENDING.getValue().equals(stepVo.getStatus())&& stepVo.getIsActive() == 1)||ProcessTaskStatus.RUNNING.getValue().equals(stepVo.getStatus()))) {
					stepNameList.add(stepVo.getName());
				}
			}
		}
		return stepNameList;
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<TableSelectColumnVo>(){
			{
				add(new TableSelectColumnVo(new ProcessTaskStepSqlTable(), Arrays.asList(
						new SelectColumnVo(ProcessTaskStepSqlTable.FieldEnum.ID.getValue(),"processTaskStepId"),
						new SelectColumnVo(ProcessTaskStepSqlTable.FieldEnum.PROCESSTASK_ID.getValue(),"processTaskId"),
						new SelectColumnVo(ProcessTaskStepSqlTable.FieldEnum.STATUS.getValue(),"processTaskStepStatus"),
						new SelectColumnVo(ProcessTaskStepSqlTable.FieldEnum.NAME.getValue(),"processTaskStepName"),
						new SelectColumnVo(ProcessTaskStepSqlTable.FieldEnum.IS_ACTIVE.getValue(),"processTaskStepIsActive")
				)));
			}
		};
	}

	@Override
	public List<JoinTableColumnVo> getMyJoinTableColumnList() {
		return new ArrayList<JoinTableColumnVo>() {
			{
				add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskStepSqlTable(), new HashMap<String, String>() {{
					put(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskStepSqlTable.FieldEnum.PROCESSTASK_ID.getValue());
				}}));
			}
		};
	}
}
