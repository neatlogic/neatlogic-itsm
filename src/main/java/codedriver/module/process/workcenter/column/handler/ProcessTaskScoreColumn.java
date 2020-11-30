package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

	@Override
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
		return null;
	}

	@Override
	public Integer getSort() {
		return -2;
	}

	@Override
	public Object getSimpleValue(Object json) {
		return null;
	}
}
