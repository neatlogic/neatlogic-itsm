package codedriver.framework.process.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import codedriver.module.process.dto.WorktimeRangeVo;
import codedriver.module.process.dto.WorktimeVo;

public interface WorktimeMapper {
	
	public WorktimeVo getWorktimeByUuid(String uuid);

	public int checkWorktimeNameIsRepeat(WorktimeVo worktimeVo);
	
	public int checkWorktimeIsExists(String uuid);
	
	public int searchWorktimeCount(WorktimeVo worktimeVo);
	
	public List<WorktimeVo> searchWorktimeList(WorktimeVo worktimeVo);
	
	public List<WorktimeRangeVo> getWorktimeRangeListByWorktimeUuid(String worktimeUuid);
	
	public List<String> getWorktimeDateList(WorktimeRangeVo worktimeRangeVo);
	
	public WorktimeRangeVo getRecentWorktimeRange(WorktimeRangeVo worktimeRangeVo);
	
	public long calculateCostTime(@Param("startTime") long startTime, @Param("endTime") long endTime, @Param("worktimeUuid") String worktimeUuid);
	
	public int insertWorktime(WorktimeVo worktimeVo);
	
	public int insertBatchWorktimeRange(List<WorktimeRangeVo> worktimeRangeList);
	
	public int updateWorktime(WorktimeVo worktimeVo);
	
	public int deleteWorktimeByUuid(String uuid);
	
	public int deleteWorktimeRange(WorktimeRangeVo worktimeRangeVo);
}
