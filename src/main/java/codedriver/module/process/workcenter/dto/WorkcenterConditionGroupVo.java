package codedriver.module.process.workcenter.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.module.process.dto.ProcessTaskStepVo;

public class WorkcenterConditionGroupVo implements Serializable{
	private static final long serialVersionUID = 8392325201425982471L;
	
	private String uuid;
	private List<WorkcenterConditionVo> conditionList;
	private Map<String, WorkcenterConditionVo> conditionMap;
	private List<WorkcenterConditionRelVo> conditionRelList;
	private List<String> channelUuidList;
	
	
	
	public WorkcenterConditionGroupVo() {
		super();
	}

	public WorkcenterConditionGroupVo(JSONObject jsonObj) {
		this.uuid = jsonObj.getString("uuid");
		JSONArray conditionArray =jsonObj.getJSONArray("conditionList");
		if(conditionArray.size() == 0) {
			 new ParamIrregularException("'conditionList'参数不能为空数组");
		}
		JSONArray channelArray =jsonObj.getJSONArray("channelUuidList");
		if(CollectionUtils.isNotEmpty(channelArray)) {
			channelUuidList = JSONObject.parseArray(channelArray.toJSONString(),String.class);
		}
		conditionList = new ArrayList<WorkcenterConditionVo>();
		conditionMap = new HashMap<String, WorkcenterConditionVo>();
		for(Object condition:conditionArray) {
			WorkcenterConditionVo conditionVo = new WorkcenterConditionVo((JSONObject) JSONObject.toJSON(condition));
			conditionList.add(conditionVo);
			conditionMap.put(conditionVo.getUuid(), conditionVo);
		}
		JSONArray conditionRelArray = jsonObj.getJSONArray("conditionRelList");
		if(CollectionUtils.isNotEmpty(conditionRelArray)) {
			conditionRelList = new ArrayList<WorkcenterConditionRelVo>();
			for(Object conditionRel:conditionRelArray) {
				conditionRelList.add(new WorkcenterConditionRelVo((JSONObject) JSONObject.toJSON(conditionRel)));
			}
		}
		
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUuid() {
		/*if(uuid == null) {
			uuid = UUID.randomUUID().toString().replace("-", "");
		}*/
		return uuid;
	}

	public List<WorkcenterConditionVo> getConditionList() {
		return conditionList;
	}

	public void setConditionList(List<WorkcenterConditionVo> conditionList) {
		this.conditionList = conditionList;
	}

	public List<WorkcenterConditionRelVo> getConditionRelList() {
		return conditionRelList;
	}

	public void setConditionRelList(List<WorkcenterConditionRelVo> conditionRelList) {
		this.conditionRelList = conditionRelList;
	}

	public List<String> getChannelUuidList() {
		return channelUuidList;
	}

	public void setChannelUuidList(List<String> channelUuidList) {
		this.channelUuidList = channelUuidList;
	}

	public String buildScript(ProcessTaskStepVo currentProcessTaskStepVo) {	
		if(!CollectionUtils.isEmpty(conditionRelList)) {
			StringBuilder script = new StringBuilder();
			script.append("(");
			String toUuid = null;
			for(WorkcenterConditionRelVo conditionRelVo : conditionRelList) {
				script.append(conditionMap.get(conditionRelVo.getFrom()).predicate(currentProcessTaskStepVo));
				script.append("and".equals(conditionRelVo.getJoinType()) ? " && " : " || ");
				toUuid = conditionRelVo.getTo();
			}
			script.append(conditionMap.get(toUuid).predicate(currentProcessTaskStepVo));
			script.append(")");
			return script.toString();
		}else {
			WorkcenterConditionVo conditionVo = conditionList.get(0);
			return String.valueOf(conditionVo.predicate(currentProcessTaskStepVo));
		}		
	}
}
