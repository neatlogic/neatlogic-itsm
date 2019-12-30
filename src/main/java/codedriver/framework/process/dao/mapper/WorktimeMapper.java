package codedriver.framework.process.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import codedriver.module.process.dto.WorktimeDefineVo;
import codedriver.module.process.dto.WorktimeDetailVo;
import codedriver.module.process.dto.WorktimeVo;

public interface WorktimeMapper {
	
	WorktimeVo getWorktimeByUuid(String uuid);

	int checkWorktimeIsExists(String uuid);
	
	List<WorktimeVo> searchWorktimeList(WorktimeVo worktimeVo);
	
	List<WorktimeDetailVo> getWorktimeDetailListByWorktimeUuid(String worktimeUuid);
	
	List<String> getWorktimeDateList(@Param("worktimeUuid")String worktimeUuid, @Param("workYear")Integer workYear);
	
	int insertWorktime(WorktimeVo worktimeVo);
	
	int insertBatchWorktimeDefine(List<WorktimeDefineVo> worktimeDefineList);
	
	int insertBatchWorktimeDetail(List<WorktimeDetailVo> worktimeDetailList);
	
	int updateWorktime(WorktimeVo worktimeVo);
	
	int deleteWorktimeByUuid(String uuid);
	
	int deleteWorktimeDefineByWorktimeUuid(String worktimeUuid);
	
	int deleteWorktimeDetail(@Param("worktimeUuid")String worktimeUuid, @Param("workYear")Integer workYear);
}
