package codedriver.module.process.workcenter.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.restful.annotation.EntityField;

public class WorkcenterVo extends BasePageVo implements Serializable{
	private static final long serialVersionUID = 1952066708451908924L;
	
	@EntityField(name = "工单中心分类uuid", type = ApiParamType.STRING)
	private String uuid;
	@EntityField(name = "工单中心分类名", type = ApiParamType.STRING)
	private String name;
	@EntityField(name = "类型，1：自定义分类，0：系统分类", type = ApiParamType.INTEGER)
	private Integer isPrivate;
	@JSONField(serialize = false)
	@EntityField(name = "排序", type = ApiParamType.INTEGER)
	private Integer sort;
	@EntityField(name = "数量", type = ApiParamType.INTEGER)
	private Integer count;
	@JSONField(serialize = false)
	@EntityField(name = "过滤条件", type = ApiParamType.STRING)
	private String conditionConfig;
	@JSONField(serialize = false)
	@EntityField(name = "显示的字段", type = ApiParamType.JSONARRAY)
	private JSONArray headerList;
	@EntityField(name = "角色列表", type = ApiParamType.JSONARRAY)
	private List<RoleVo> roleList;
	private List<WorkcenterConditionGroupVo> conditionGroupList;
	private List<WorkcenterConditionGroupRelVo> conditionGroupRelList;
	
	public WorkcenterVo() {
		super();
	}
	
	public WorkcenterVo(String _name,Integer _isPrivate,String _conditionConfig) {
		this.name =_name;
		this.isPrivate = _isPrivate;
		this.conditionConfig = _conditionConfig;
	}
	
	public WorkcenterVo(JSONObject jsonObj) {
		uuid = jsonObj.getString("uuid");
		headerList = jsonObj.getJSONArray("headerList");
		JSONArray conditionGroupArray = jsonObj.getJSONArray("conditionGroupList");
		if(conditionGroupArray.size() == 0) {
			 new ParamIrregularException("'conditionGroupList'参数不能为空数组");
		}
		for(Object conditionGroup:conditionGroupArray) {
			conditionGroupList.add(new WorkcenterConditionGroupVo((JSONObject) JSONObject.toJSON(conditionGroup)));
		}
		JSONArray conditionGroupRelArray = jsonObj.getJSONArray("conditionGroupRelList");
		for(Object conditionRelGroup:conditionGroupRelArray) {
			conditionGroupRelList.add(new WorkcenterConditionGroupRelVo((JSONObject) JSONObject.toJSON(conditionRelGroup)));
		}
	}
	public String getUuid() {
		if (StringUtils.isBlank(uuid)) {
			uuid = UUID.randomUUID().toString().replace("-", "");
		}
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public List<RoleVo> getRoleList() {
		return roleList;
	}
	public void setRoleList(List<RoleVo> roleList) {
		this.roleList = roleList;
	}
	public Integer getIsPrivate() {
		return isPrivate;
	}
	public void setIsPrivate(Integer isPrivate) {
		this.isPrivate = isPrivate;
	}
	public Integer getSort() {
		return sort;
	}
	public void setSort(Integer sort) {
		this.sort = sort;
	}
	public String getConditionConfig() {
		return conditionConfig;
	}
	public void setConditionConfig(String conditionConfig) {
		this.conditionConfig = conditionConfig;
	}
	public List<WorkcenterConditionGroupVo> getConditionGroupList() {
		return conditionGroupList;
	}
	public void setConditionGroupList(List<WorkcenterConditionGroupVo> conditionGroupList) {
		this.conditionGroupList = conditionGroupList;
	}
	public List<WorkcenterConditionGroupRelVo> getWorkcenterConditionGroupRelList() {
		return conditionGroupRelList;
	}
	public void setWorkcenterConditionGroupRelList(List<WorkcenterConditionGroupRelVo> conditionGroupRelList) {
		this.conditionGroupRelList = conditionGroupRelList;
	}
	public JSONArray getHeaderList() {
		return headerList;
	}
	public void setHeaderList(JSONArray headerArray) {
		this.headerList = headerArray;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	
	
}
