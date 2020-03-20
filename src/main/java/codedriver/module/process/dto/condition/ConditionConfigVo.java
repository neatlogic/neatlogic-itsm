package codedriver.module.process.dto.condition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.dto.BasePageVo;
import codedriver.module.process.dto.ProcessTaskStepVo;

public class ConditionConfigVo extends BasePageVo implements Serializable {

	private static final long serialVersionUID = 5439300427812355573L;

	private List<ConditionGroupVo> conditionGroupList;
	private Map<String, ConditionGroupVo> conditionGroupMap;
	private List<ConditionGroupRelVo> conditionGroupRelList;
	
	public ConditionConfigVo() {
	}
	public ConditionConfigVo(JSONObject jsonObj) {
		JSONArray conditionGroupArray = jsonObj.getJSONArray("conditionGroupList");
		if(CollectionUtils.isNotEmpty(conditionGroupArray)) {
			conditionGroupList = new ArrayList<ConditionGroupVo>();
			conditionGroupMap = new HashMap<String, ConditionGroupVo>();
			for(Object conditionGroup:conditionGroupArray) {
				JSONObject conditionGroupJson = (JSONObject) JSONObject.toJSON(conditionGroup);
				ConditionGroupVo conditionGroupVo = new ConditionGroupVo(conditionGroupJson);
				conditionGroupList.add(conditionGroupVo);
				conditionGroupMap.put(conditionGroupVo.getUuid(), conditionGroupVo);
			}
			JSONArray conditionGroupRelArray = jsonObj.getJSONArray("conditionGroupRelList");
			if(CollectionUtils.isNotEmpty(conditionGroupRelArray)) {
				conditionGroupRelList = new ArrayList<ConditionGroupRelVo>();
				for(Object conditionRelGroup:conditionGroupRelArray) {
					conditionGroupRelList.add(new ConditionGroupRelVo((JSONObject) JSONObject.toJSON(conditionRelGroup)));
				}
			}
		}
	}
	public List<ConditionGroupVo> getConditionGroupList() {
		return conditionGroupList;
	}
	public void setConditionGroupList(List<ConditionGroupVo> conditionGroupList) {
		this.conditionGroupList = conditionGroupList;
	}
	public Map<String, ConditionGroupVo> getConditionGroupMap() {
		return conditionGroupMap;
	}
	public void setConditionGroupMap(Map<String, ConditionGroupVo> conditionGroupMap) {
		this.conditionGroupMap = conditionGroupMap;
	}
	public List<ConditionGroupRelVo> getConditionGroupRelList() {
		return conditionGroupRelList;
	}
	public void setConditionGroupRelList(List<ConditionGroupRelVo> conditionGroupRelList) {
		this.conditionGroupRelList = conditionGroupRelList;
	}

	public String buildScript(ProcessTaskStepVo currentProcessTaskStepVo) {		
		if(!CollectionUtils.isEmpty(conditionGroupRelList)) {
			StringBuilder script = new StringBuilder();
			script.append("(");
			String toUuid = null;
			for(ConditionGroupRelVo conditionGroupRelVo : conditionGroupRelList) {
				script.append(conditionGroupMap.get(conditionGroupRelVo.getFrom()).buildScript(currentProcessTaskStepVo));
				script.append("and".equals(conditionGroupRelVo.getJoinType()) ? " && " : " || ");
				toUuid = conditionGroupRelVo.getTo();
			}
			script.append(conditionGroupMap.get(toUuid).buildScript(currentProcessTaskStepVo));
			script.append(")");
			return script.toString();
		}else {
			ConditionGroupVo conditionGroupVo = conditionGroupList.get(0);
			return conditionGroupVo.buildScript(currentProcessTaskStepVo);
		}
	}
}
