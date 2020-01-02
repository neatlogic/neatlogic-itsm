package codedriver.module.process.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.exception.WorktimeNotFoundException;
import codedriver.module.process.dto.WorktimeRangeVo;

@Service
@Transactional
public class WorktimeServiceImpl implements WorktimeService{

	@Autowired
	private WorktimeMapper worktimeMapper;
	
	@Override
	public Long calculateTimeoutPoint(long activeTime, long timeLimit, String worktimeUuid) {		
		if(worktimeMapper.checkWorktimeIsExists(worktimeUuid) == 0) {
			throw new WorktimeNotFoundException(worktimeUuid);
		}
		if(timeLimit == 0) {
			return activeTime;
		}
		WorktimeRangeVo worktimeRangeVo = new WorktimeRangeVo();
		WorktimeRangeVo recentWorktimeRange = null;
		long startTime = 0;
		long endTime = 0;
		long duration = 0;
		while(true) {
			worktimeRangeVo.setWorktimeUuid(worktimeUuid);
			worktimeRangeVo.setStartTime(activeTime);
			recentWorktimeRange = worktimeMapper.getRecentWorktimeRange(worktimeRangeVo);
			if(recentWorktimeRange == null) {
				return activeTime;
			}
			startTime = recentWorktimeRange.getStartTime();
			endTime = recentWorktimeRange.getEndTime();
			if(startTime > activeTime) {
				activeTime = startTime;
			}
			duration = endTime - activeTime;
			if(duration > timeLimit) {
				return activeTime + timeLimit;
			}else {
				timeLimit -= duration;
				activeTime = endTime;
			}
		}
	}

}
