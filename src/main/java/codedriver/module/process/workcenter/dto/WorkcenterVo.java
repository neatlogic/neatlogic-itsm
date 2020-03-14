package codedriver.module.process.workcenter.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.restful.annotation.EntityField;

public class WorkcenterVo extends BasePageVo implements Serializable{
	private static final long serialVersionUID = 1952066708451908924L;
	
	@EntityField(name = "工单中心分类uuid", type = ApiParamType.STRING)
	private String uuid;
	@EntityField(name = "工单中心分类名", type = ApiParamType.STRING)
	private String name;
	@EntityField(name = "custom类型,工单中心分类所属人", type = ApiParamType.ENUM)
	private String owner;
	@EntityField(name = "default:默认出厂  system：系统分类  custom：自定义分类", type = ApiParamType.ENUM)
	private String type;
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
	@JSONField(serialize = false)
	private List<WorkcenterRoleVo> workcenterRoleList;
	@EntityField(name = "角色列表", type = ApiParamType.JSONARRAY)
	private List<String> valueList;
	@EntityField(name = "是否拥有编辑权限", type = ApiParamType.JSONARRAY)
	private Integer isCanEdit;
	@EntityField(name = "是否拥有授权权限", type = ApiParamType.JSONARRAY)
	private Integer isCanRole;
	
	private List<WorkcenterConditionGroupVo> conditionGroupList;
	private List<WorkcenterConditionGroupRelVo> conditionGroupRelList;
	
	
	//params
	private String userId;
	private List<String> roleNameList;
	private List<String> channelUuidList;
	
	public WorkcenterVo() {
	}
	
	public WorkcenterVo(String _name) {
		this.name =_name;
	}
	
	public WorkcenterVo(String _userId,List<String> _roleNameList,String _owner) {
		this.userId = _userId;
		this.roleNameList = _roleNameList;
		this.owner = _owner;
	}
	
	public WorkcenterVo(JSONObject jsonObj) {
		uuid = jsonObj.getString("uuid");
		//headerList = jsonObj.getJSONArray("headerList");
		JSONArray conditionGroupArray = jsonObj.getJSONArray("conditionGroupList");
		if(conditionGroupArray.size() == 0) {
			 new ParamIrregularException("'conditionGroupList'参数不能为空数组");
		}
		conditionGroupList = new ArrayList<WorkcenterConditionGroupVo>();
		channelUuidList = new ArrayList<String>();
		for(Object conditionGroup:conditionGroupArray) {
			JSONObject conditionGroupJson = (JSONObject) JSONObject.toJSON(conditionGroup);
			JSONArray channelArray =conditionGroupJson.getJSONArray("channelUuidList");
			List<String> channelUuidListTmp = new ArrayList<String>();
			if(CollectionUtils.isNotEmpty(channelArray)) {
				channelUuidListTmp = JSONObject.parseArray(channelArray.toJSONString(),String.class);
			}
			channelUuidList.addAll(channelUuidListTmp);
			conditionGroupList.add(new WorkcenterConditionGroupVo(conditionGroupJson));
		}
		JSONArray conditionGroupRelArray = jsonObj.getJSONArray("conditionGroupRelList");
		conditionGroupRelList = new ArrayList<WorkcenterConditionGroupRelVo>();
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
	
	public List<WorkcenterRoleVo> getWorkcenterRoleList() {
		return workcenterRoleList;
	}

	public void setWorkcenterRoleList(List<WorkcenterRoleVo> workcenterRoleList) {
		this.workcenterRoleList = workcenterRoleList;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public List<String> getValueList() {
		if(valueList == null) {
			valueList = new ArrayList<String>();
			for(WorkcenterRoleVo workcenterRoleVo : this.workcenterRoleList) {
				if(workcenterRoleVo.getRoleName() !=null) {
					valueList.add("role#" + workcenterRoleVo.getRoleName());
				}else if(workcenterRoleVo.getUserId() !=null){
					valueList.add("user#" + workcenterRoleVo.getUserId());
				}
			}
		}
		return valueList;
	}

	public Integer getIsCanEdit() {
		return isCanEdit;
	}

	public void setIsCanEdit(Integer isCanEdit) {
		this.isCanEdit = isCanEdit;
	}

	public Integer getIsCanRole() {
		return isCanRole;
	}

	public void setIsCanRole(Integer isCanRole) {
		this.isCanRole = isCanRole;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<String> getRoleNameList() {
		return roleNameList;
	}

	public void setRoleNameList(List<String> roleNameList) {
		this.roleNameList = roleNameList;
	}

	public List<String> getChannelUuidList() {
		return channelUuidList;
	}

	public void setChannelUuidList(List<String> channelUuidList) {
		this.channelUuidList = channelUuidList;
	}


}
