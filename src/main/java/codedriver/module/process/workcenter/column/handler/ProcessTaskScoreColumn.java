package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.JoinOnVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.ProcessTaskScoreSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ProcessTaskScoreColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getName() {
		return "score";
	}

	/** 此列在工单中心不需要中文名，也不需要可拖拽，所以displayName为空且disable为true */
	@Override
	public String getDisplayName() {
		return "";
	}

	@Override
	public Boolean getDisabled() {
		return true;
	}

	/*@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		JSONObject obj = new JSONObject();
		Long id = json.getLong(new ProcessTaskIdColumn().getName());
		String scoreInfo = processTaskMapper.getProcessTaskScoreInfoById(id);
		if(StringUtils.isNotBlank(scoreInfo)){
			float total = 0;
			JSONObject scoreObj = JSON.parseObject(scoreInfo);
			JSONArray dimensionList = scoreObj.getJSONArray("dimensionList");
			if(CollectionUtils.isNotEmpty(dimensionList)){
				for(int i = 0;i < dimensionList.size();i++){
					total += dimensionList.getJSONObject(i).getIntValue("score");
				}
				obj.put("value",Math.round(total / dimensionList.size()));
			}
			obj.put("content",scoreObj.getString("content"));
		}
		return obj;
	}*/

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
		return null;
	}

	@Override
	public Integer getSort() {
		return -2;
	}

	/*@Override
	public Object getSimpleValue(Object json) {
		return null;
	}*/

	@Override
	public Object getValue(ProcessTaskVo processTaskVo) {
		JSONObject obj = new JSONObject();
		String scoreInfo = processTaskMapper.getProcessTaskScoreInfoById(processTaskVo.getId());
		if(StringUtils.isNotBlank(scoreInfo)){
			float total = 0;
			JSONObject scoreObj = JSON.parseObject(scoreInfo);
			JSONArray dimensionList = scoreObj.getJSONArray("dimensionList");
			if(CollectionUtils.isNotEmpty(dimensionList)){
				for(int i = 0;i < dimensionList.size();i++){
					total += dimensionList.getJSONObject(i).getIntValue("score");
				}
				obj.put("value",Math.round(total / dimensionList.size()));
			}
			obj.put("content",scoreObj.getString("content"));
		}
		return obj;
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<TableSelectColumnVo>(){
			{
				add(new TableSelectColumnVo(new ProcessTaskScoreSqlTable(), Collections.singletonList(new SelectColumnVo(ProcessTaskScoreSqlTable.FieldEnum.SCORE.getValue()))));
			}
		};
	}

	@Override
	public Boolean getMyIsShow() {
        return false;
    }

	@Override
	public List<JoinTableColumnVo> getMyJoinTableColumnList() {
		return new ArrayList<JoinTableColumnVo>() {
			{
				add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskScoreSqlTable(), new ArrayList<JoinOnVo>() {{
					add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskScoreSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
				}}));
			}
		};
	}
}
