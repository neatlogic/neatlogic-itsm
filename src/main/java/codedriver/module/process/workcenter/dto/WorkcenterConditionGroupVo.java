package codedriver.module.process.workcenter.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.exception.type.ParamIrregularException;

public class WorkcenterConditionGroupVo implements Serializable{
	private static final long serialVersionUID = 8392325201425982471L;
	
	private String uuid;
	private List<WorkcenterConditionVo> conditionList;
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
		for(Object condition:conditionArray) {
			conditionList.add(new WorkcenterConditionVo((JSONObject) JSONObject.toJSON(condition)));
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


	
	
}
