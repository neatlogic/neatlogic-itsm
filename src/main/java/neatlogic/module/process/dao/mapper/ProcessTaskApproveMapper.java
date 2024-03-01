package neatlogic.module.process.dao.mapper;

import neatlogic.framework.process.crossover.IProcessTaskApproveCrossoverMapper;

public interface ProcessTaskApproveMapper extends IProcessTaskApproveCrossoverMapper {

    String getProcessTaskApproveEntityConfigByProcessTaskId(Long processTaskId);
}
