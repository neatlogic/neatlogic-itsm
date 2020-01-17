package codedriver.module.process.service;

import java.util.List;

import codedriver.module.process.dto.WorktimeRangeVo;

public interface WorktimeService {

	Long calculateTimeoutPoint(long startTime, long timeLimit, String worktimeUuid);
	
	Long calculateCostTime(List<WorktimeRangeVo> worktimeRangeList);
}
