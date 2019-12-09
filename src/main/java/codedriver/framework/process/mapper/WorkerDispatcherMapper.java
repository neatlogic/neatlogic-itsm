package codedriver.framework.process.mapper;

import java.util.List;

import codedriver.framework.process.dto.WorkerDispatcherVo;

public interface WorkerDispatcherMapper {

	public List<WorkerDispatcherVo> getAllActiveWorkerDispatcher();

	public int resetWorkerDispatcher();

	public int replaceWorkerDispatcher(WorkerDispatcherVo workerDispatcherVo);
}
