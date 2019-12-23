package codedriver.module.process.dto;

import java.util.List;

public interface ITree {
	String ROOT_UUID = "0";
	
	String getUuid();
	void setUuid(String uuid);
	String getName();
	void setName(String name);
	String getParentUuid();
	void setParentUuid(String parentUuid);
	boolean isSelected();
	void setSelected(boolean selected);
	List<ITree> getChildren();
	void setChildren(List<ITree> children);
	ITree getParent();
	void setParent(ITree parent);
	void setOpenCascade(boolean open);
	void setSelectedCascade(boolean selected);
	Integer getSort();
	void setSort(Integer sort);	
	String getType();
}
