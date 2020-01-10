package codedriver.module.process.dto;

import java.util.List;

public interface ITree extends Comparable<ITree> {
	public final static String ROOT_UUID = "0";
	
	public String getUuid();
	public void setUuid(String uuid);
	public String getName();
	public void setName(String name);
	public String getParentUuid();
	public void setParentUuid(String parentUuid);
	public boolean isSelected();
	public void setSelected(boolean selected);
	public List<ITree> getChildren();
	public void setChildren(List<ITree> children);
	public ITree getParent();
	public void setParent(ITree parent);
	public void setOpenCascade(boolean open);
	public void setSelectedCascade(boolean selected);
	public Integer getSort();
	public void setSort(Integer sort);	
	public String getType();
	public int getChildrenCount();
	public void setChildrenCount(int count);
	public List<Integer> getSortList();
	public void setSortList(List<Integer> sortList);
	public boolean addChild(ITree child);
	public boolean removeChild(ITree child);
	public List<String> getNameList();
	public void setNameList(List<String> nameList);
	public boolean isAncestorOrSelf(String uuid);
	@Override
	public default int compareTo(ITree other) {
		List<Integer> sortList1 = getSortList();
		List<Integer> sortList2 = other.getSortList();
		int size1 = sortList1.size();
		int size2 = sortList2.size();
		int minIndex = 0;
		int resultDefault = 0;
		if(size1 > size2) {
			minIndex = size2;
			resultDefault = 1;
		}else {
			minIndex = size1;
			resultDefault = -1;
		}
		for(int i = 0; i < minIndex; i++) {
			if(sortList1.get(i).equals(sortList2.get(i))) {
				continue;
			}else {
				return sortList1.get(i) - sortList2.get(i);
			}
		}
		return resultDefault;
	}
}
