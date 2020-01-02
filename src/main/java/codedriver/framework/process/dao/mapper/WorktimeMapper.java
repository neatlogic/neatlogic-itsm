package codedriver.framework.process.dao.mapper;

import java.util.List;

import codedriver.module.process.dto.WorktimeRangeVo;
import codedriver.module.process.dto.WorktimeVo;

public interface WorktimeMapper {
	
	WorktimeVo getWorktimeByUuid(String uuid);

	int checkWorktimeNameIsRepeat(WorktimeVo worktimeVo);
	
	int checkWorktimeIsExists(String uuid);
	
	List<WorktimeVo> searchWorktimeList(WorktimeVo worktimeVo);
	
	List<WorktimeRangeVo> getWorktimeRangeListByWorktimeUuid(String worktimeUuid);
	
	List<String> getWorktimeDateList(WorktimeRangeVo worktimeRangeVo);
	
	WorktimeRangeVo getRecentWorktimeRange(WorktimeRangeVo worktimeRangeVo);
	
	long calculateCostTime(WorktimeRangeVo worktimeRangeVo);
	
	int insertWorktime(WorktimeVo worktimeVo);
	
	int insertBatchWorktimeRange(List<WorktimeRangeVo> worktimeRangeList);
	
	int updateWorktime(WorktimeVo worktimeVo);
	
	int deleteWorktimeByUuid(String uuid);
	
	int deleteWorktimeRange(WorktimeRangeVo worktimeRangeVo);
}
