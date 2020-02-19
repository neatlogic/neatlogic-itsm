package codedriver.module.process.workcenter.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.restful.annotation.EntityField;

public class WorkcenterVo extends BasePageVo implements Serializable{
	private static final long serialVersionUID = 1952066708451908924L;
	
	@EntityField(name = "工单中心分类uuid", type = ApiParamType.STRING)
	private String uuid;
	@EntityField(name = "工单中心分类名", type = ApiParamType.STRING)
	private String name;
	@EntityField(name = "类型，1：自定义分类，0：系统分类", type = ApiParamType.INTEGER)
	private Integer isPrivate;
	@EntityField(name = "排序", type = ApiParamType.INTEGER)
	private Integer sort;
	@EntityField(name = "过滤条件", type = ApiParamType.STRING)
	private String conditionConfig;
	@EntityField(name = "显示的字段", type = ApiParamType.JSONARRAY)
	private JSONArray headerArray;
	@EntityField(name = "角色列表", type = ApiParamType.JSONARRAY)
	private List<RoleVo> roleList;
	private List<WorkcenterConditionGroupVo> WorkcenterConditionGroupList;
	private List<WorkcenterConditionGroupRelVo> WorkcenterConditionGroupRelList;
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
	public List<WorkcenterConditionGroupVo> getWorkcenterConditionGroupList() {
		return WorkcenterConditionGroupList;
	}
	public void setWorkcenterConditionGroupList(List<WorkcenterConditionGroupVo> workcenterConditionGroupList) {
		WorkcenterConditionGroupList = workcenterConditionGroupList;
	}
	public List<WorkcenterConditionGroupRelVo> getWorkcenterConditionGroupRelList() {
		return WorkcenterConditionGroupRelList;
	}
	public void setWorkcenterConditionGroupRelList(List<WorkcenterConditionGroupRelVo> workcenterConditionGroupRelList) {
		WorkcenterConditionGroupRelList = workcenterConditionGroupRelList;
	}
	public JSONArray getHeaderArray() {
		return headerArray;
	}
	public void setHeaderArray(JSONArray headerArray) {
		this.headerArray = headerArray;
	}
	
	
}
