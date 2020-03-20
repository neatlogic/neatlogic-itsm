package codedriver.module.process.dto.condition;

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

public class ConditionGroupVo implements Serializable{
	private static final long serialVersionUID = 8392325201425982471L;
	
	private String uuid;
	private List<ConditionVo> conditionList;
	private Map<String, ConditionVo> conditionMap;
	private List<ConditionRelVo> conditionRelList;
	private List<String> channelUuidList;
	
	
	
	public ConditionGroupVo() {
		super();
	}

	public ConditionGroupVo(JSONObject jsonObj) {
		this.uuid = jsonObj.getString("uuid");
		JSONArray conditionArray =jsonObj.getJSONArray("conditionList");
		if(conditionArray.size() == 0) {
			 new ParamIrregularException("'conditionList'参数不能为空数组");
		}
		JSONArray channelArray =jsonObj.getJSONArray("channelUuidList");
		if(CollectionUtils.isNotEmpty(channelArray)) {
			channelUuidList = JSONObject.parseArray(channelArray.toJSONString(),String.class);
		}
		conditionList = new ArrayList<ConditionVo>();
		conditionMap = new HashMap<String, ConditionVo>();
		for(Object condition:conditionArray) {
			ConditionVo conditionVo = new ConditionVo((JSONObject) JSONObject.toJSON(condition));
			conditionList.add(conditionVo);
			conditionMap.put(conditionVo.getUuid(), conditionVo);
		}
		JSONArray conditionRelArray = jsonObj.getJSONArray("conditionRelList");
		if(CollectionUtils.isNotEmpty(conditionRelArray)) {
			conditionRelList = new ArrayList<ConditionRelVo>();
			for(Object conditionRel:conditionRelArray) {
				conditionRelList.add(new ConditionRelVo((JSONObject) JSONObject.toJSON(conditionRel)));
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

	public List<ConditionVo> getConditionList() {
		return conditionList;
	}

	public void setConditionList(List<ConditionVo> conditionList) {
		this.conditionList = conditionList;
	}

	public List<ConditionRelVo> getConditionRelList() {
		return conditionRelList;
	}

	public void setConditionRelList(List<ConditionRelVo> conditionRelList) {
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
			for(ConditionRelVo conditionRelVo : conditionRelList) {
				script.append(conditionMap.get(conditionRelVo.getFrom()).predicate(currentProcessTaskStepVo));
				script.append("and".equals(conditionRelVo.getJoinType()) ? " && " : " || ");
				toUuid = conditionRelVo.getTo();
			}
			script.append(conditionMap.get(toUuid).predicate(currentProcessTaskStepVo));
			script.append(")");
			return script.toString();
		}else {
			ConditionVo conditionVo = conditionList.get(0);
			return String.valueOf(conditionVo.predicate(currentProcessTaskStepVo));
		}		
	}
}
