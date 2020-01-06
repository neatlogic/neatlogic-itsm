package codedriver.framework.process.dao.mapper;

import java.util.List;

import codedriver.module.process.dto.PriorityVo;

public interface PriorityMapper {

	public int searchPriorityCount(PriorityVo priorityVo);

	public List<PriorityVo> searchPriorityList(PriorityVo priorityVo);

}
