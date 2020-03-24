package codedriver.module.process.service;

import java.util.List;

import codedriver.framework.process.dto.WorktimeRangeVo;

public interface WorktimeService {

	long calculateTimeoutPoint(long startTime, long timeLimit, String worktimeUuid);
	
	long calculateCostTime(List<WorktimeRangeVo> worktimeRangeList);
}
