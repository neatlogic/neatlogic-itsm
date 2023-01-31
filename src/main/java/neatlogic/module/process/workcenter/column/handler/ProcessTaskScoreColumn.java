package neatlogic.module.process.workcenter.column.handler;

import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dao.mapper.score.ProcessTaskScoreMapper;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.dto.score.ProcessTaskScoreVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.dto.SelectColumnVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskScoreSqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ProcessTaskScoreColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{

	@Resource
	private ProcessTaskMapper processTaskMapper;

	@Resource
	private ProcessTaskScoreMapper processTaskScoreMapper;

	@Resource
	private SelectContentByHashMapper selectContentByHashMapper;

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
		List<ProcessTaskScoreVo> processTaskScoreVos = processTaskScoreMapper.getProcessTaskScoreWithContentHashByProcessTaskId(processTaskVo.getId());
		if(CollectionUtils.isNotEmpty(processTaskScoreVos)) {
			float total = 0;
			for (ProcessTaskScoreVo processTaskScoreVo : processTaskScoreVos) {
				total += processTaskScoreVo.getScore();
			}
			obj.put("value",Math.round(total / processTaskScoreVos.size()));//平均分数
			obj.put("content",selectContentByHashMapper.getProcessTaskContentStringByHash(processTaskScoreVos.get(0).getContentHash()));
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
