package codedriver.module.process.dto;

import java.util.List;
import java.util.UUID;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.restful.annotation.EntityField;

public class CatalogVo extends BasePageVo implements ITree{
	
	@EntityField(name = "服务目录uuid", type = ApiParamType.STRING)
	private String uuid;
	
	@EntityField(name = "服务目录名称", type = ApiParamType.STRING)
	private String name;
	
	@EntityField(name = "服务目录父级uuid", type = ApiParamType.STRING)
	private String parentUuid;
	
	@EntityField(name = "是否启用，0：禁用，1：启用", type = ApiParamType.INTEGER)
	private Integer isActive;
	
	@EntityField(name = "图标", type = ApiParamType.STRING)
	private String icon;
	
	@EntityField(name = "颜色", type = ApiParamType.STRING)
	private String color;
	
	@EntityField(name = "描述", type = ApiParamType.STRING)
	private String desc;
	
	@EntityField(name = "是否打开，false：未选中，true：已选中", type = ApiParamType.BOOLEAN)
	private boolean open = false;
	
	@EntityField(name = "是否已选中，false：未选中，true：已选中", type = ApiParamType.BOOLEAN)
	private boolean selected = false;
	
	@EntityField(name = "子目录或通道", type = ApiParamType.JSONARRAY)
	private List<ITree> children;
	
	@EntityField(name = "类型", type = ApiParamType.STRING)
	private String type = "catalog";
	
	private transient ITree parent;
	
	private transient String nextUuid;
	
	private transient Integer sort;
	
	public CatalogVo() {
	}
	
	public CatalogVo(String uuid) {
		this.uuid = uuid;
	}
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
	@Override
	public String getParentUuid() {
		return parentUuid;
	}
	@Override
	public void setParentUuid(String parentUuid) {
		this.parentUuid = parentUuid;
	}
	
	public Integer getIsActive() {
		return isActive;
	}
	
	public void setIsActive(Integer isActive) {
		this.isActive = isActive;
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
	
	public String getDesc() {
		return desc;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}
	@Override
	public boolean isSelected() {
		return selected;
	}
	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	@Override
	public List<ITree> getChildren() {
		return children;
	}
	@Override
	public void setChildren(List<ITree> children) {
		this.children = children;
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
		this.open = open;
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
	public String getType() {
		return type;
	}

}
