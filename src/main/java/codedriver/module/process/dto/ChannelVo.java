package codedriver.module.process.dto;

import java.util.List;
import java.util.UUID;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.restful.annotation.EntityField;

public class ChannelVo extends BasePageVo implements ITree{
	
	@EntityField(name = "服务通道uuid", type = ApiParamType.STRING)
	private String uuid;
	
	@EntityField(name = "服务通道名称", type = ApiParamType.STRING)
	private String name;
	
	@EntityField(name = "是否启用，0：禁用，1：启用", type = ApiParamType.INTEGER)
	private Integer isActive;
	
	@EntityField(name = "描述", type = ApiParamType.STRING)
	private String desc;
	
	@EntityField(name = "图标", type = ApiParamType.STRING)
	private String icon;
	
	@EntityField(name = "颜色", type = ApiParamType.STRING)
	private String color;
	
	@EntityField(name = "服务目录uuid", type = ApiParamType.STRING)
	private String parentUuid;
	
	@EntityField(name = "是否收藏，0：未收藏，1：已收藏", type = ApiParamType.INTEGER)
	private Integer isFavorite;
	
	@EntityField(name = "是否已选中，false：未选中，true：已选中", type = ApiParamType.BOOLEAN)
	private boolean selected = false;
	
	@EntityField(name = "类型", type = ApiParamType.STRING)
	private String type = "channel";
	
	private transient ITree parent;
	
	private transient Integer sort;
	
	private transient String keyword;
	
	private transient String userId;
	
	private transient String nextUuid;
	
	@Override
	public String getUuid() {
		if(uuid == null) {
			uuid = UUID.randomUUID().toString().replace("-", "");
		}
		return uuid;
	}
	@Override
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getIsActive() {
		return isActive;
	}
	
	public void setIsActive(Integer isActive) {
		this.isActive = isActive;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public String getIcon() {
		return icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public String getColor() {
		return color;
	}
	
	public void setColor(String color) {
		this.color = color;
	}
	@Override
	public String getParentUuid() {
		return parentUuid;
	}
	@Override
	public void setParentUuid(String parentUuid) {
		this.parentUuid = parentUuid;
	}
	
	public Integer getIsFavorite() {
		return isFavorite;
	}
	public void setIsFavorite(Integer isFavorite) {
		this.isFavorite = isFavorite;
	}
	
	@Override
	public boolean isSelected() {
		return selected;
	}
	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public String getKeyword() {
		return keyword;
	}
	
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	@Override
	public String getNextUuid() {
		return nextUuid;
	}
	@Override
	public void setNextUuid(String nextUuid) {
		this.nextUuid = nextUuid;
	}
	@Override
	public Integer getSort() {
		return sort;
	}
	@Override
	public void setSort(Integer sort) {
		this.sort = sort;
	}
	
	@Override
	public List<ITree> getChildren() {
		return null;
	}
	@Override
	public void setChildren(List<ITree> children) {
		
	}
	@Override
	public ITree getParent() {
		return parent;
	}
	@Override
	public void setParent(ITree parent) {
		this.parent = parent;		
	}
	@Override
	public void setOpenCascade(boolean open) {
		if(parent != null) {
			parent.setOpenCascade(open);
		}		
	}
	@Override
	public void setSelectedCascade(boolean selected) {
		this.selected = selected;
		if(parent != null) {
			parent.setSelectedCascade(selected);
		}
	}
	@Override
	public String getType() {
		return type;
	}
	
}
