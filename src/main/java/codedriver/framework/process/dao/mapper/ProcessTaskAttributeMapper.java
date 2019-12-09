package codedriver.framework.process.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import codedriver.module.process.dto.ProcessTaskStepAttributeVo;

public interface ProcessTaskAttributeMapper {
	public List<ProcessTaskStepAttributeVo> getProcessAttributeListByProcessTaskAndStepId(@Param("processTaskId") Long processTaskId, @Param("processTaskStepId") Long processTaskStepId);
}
