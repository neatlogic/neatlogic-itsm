package codedriver.module.process.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.process.exception.channel.ChannelUnsupportedOperationException;
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
	
	private String processUuid;
	
	private String worktimeUuid;
	
	private Long time;
	
	private List<String> priorityUuidList;
	
	private String defaultPriorityUuid;
	
	private transient int childrenCount = 0;
	
	private transient List<Integer> sortList;
	
	private transient List<String> nameList;
	
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
		throw new ChannelUnsupportedOperationException(this.uuid, "设置子节点");
	}
	@Override
	public ITree getParent() {
		return parent;
	}
	@Override
	public void setParent(ITree parent) {
		this.parent = parent;
		parent.addChild(this);
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
	public String getProcessUuid() {
		return processUuid;
	}
	public void setProcessUuid(String processUuid) {
		this.processUuid = processUuid;
	}
	public String getWorktimeUuid() {
		return worktimeUuid;
	}
	public void setWorktimeUuid(String worktimeUuid) {
		this.worktimeUuid = worktimeUuid;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public List<String> getPriorityUuidList() {
		return priorityUuidList;
	}
	public void setPriorityUuidList(List<String> priorityUuidList) {
		this.priorityUuidList = priorityUuidList;
	}
	public String getDefaultPriorityUuid() {
		return defaultPriorityUuid;
	}
	public void setDefaultPriorityUuid(String defaultPriorityUuid) {
		this.defaultPriorityUuid = defaultPriorityUuid;
	}
	@Override
	public int getChildrenCount() {
		return 0;
	}
	@Override
	public void setChildrenCount(int count) {
		throw new ChannelUnsupportedOperationException(this.uuid, "设置子节点个数");
	}
	@Override
	public List<Integer> getSortList() {
		if(sortList != null) {
			return sortList;
		}
		if(parent != null) {
			sortList = new ArrayList<>(parent.getSortList());			
		}else {
			sortList = new ArrayList<>();
		}		
		sortList.add(sort);
		return sortList;
	}
	@Override
	public void setSortList(List<Integer> sortList) {
		this.sortList = sortList;
		
	}
	@Override
	public boolean addChild(ITree child) {
		throw new ChannelUnsupportedOperationException(this.uuid, "添加子节点");
	}
	@Override
	public boolean removeChild(ITree child) {
		throw new ChannelUnsupportedOperationException(this.uuid, "删除子节点");
	}
	@Override
	public List<String> getNameList() {
		if(nameList != null) {
			return nameList;
		}
		if(parent != null && !ITree.ROOT_UUID.equals(parent.getUuid())) {
			nameList = new ArrayList<>(parent.getNameList());
		}else {
			nameList = new ArrayList<>();
		}
		nameList.add(name);
		return nameList;
	}

	@Override
	public void setNameList(List<String> nameList) {
		this.nameList = nameList;		
	}
	@Override
	public boolean isAncestorOrSelf(String uuid) {
		if(this.uuid.equals(uuid)) {
			return true;
		}
		if(parent == null) {
			return false;
		}	
		return parent.isAncestorOrSelf(uuid);
	}	
}
