package codedriver.module.process.dto;

import java.util.List;

import codedriver.framework.common.dto.BasePageVo;

public class ChannelRoleVo extends BasePageVo{
	
	public static String ROLETYPE_REPORT = "report";
	public static String ROLETYPE_SELFREPORT = "selfreport";
	public static String ROLETYPE_SEARCH = "search";
	public static String ROLETYPE_REPLACE ="replace" ; 
	
	private String channelUuid;
	private String roleName;
	private String roleDesc;
	private String type;
	
	private String keyword;
	private Integer isSelect;
	private List<String> typeList;
	
	List<String> roleNameList;
	
	public String getChannelUuid() {
		return channelUuid;
	}
	public void setChannelUuid(String channelUuid) {
		this.channelUuid = channelUuid;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public String getRoleDesc() {
		return roleDesc;
	}
	public void setRoleDesc(String roleDesc) {
		this.roleDesc = roleDesc;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public Integer getIsSelect() {
		return isSelect;
	}
	public void setIsSelect(Integer isSelect) {
		this.isSelect = isSelect;
	}
	public List<String> getTypeList() {
		return typeList;
	}
	public void setTypeList(List<String> typeList) {
		this.typeList = typeList;
	}
	public List<String> getRoleNameList() {
		return roleNameList;
	}
	public void setRoleNameList(List<String> roleNameList) {
		this.roleNameList = roleNameList;
	}
}
