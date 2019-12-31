package codedriver.module.process.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.exception.WorktimeNotFoundException;
import codedriver.module.process.dto.WorktimeDetailVo;

@Service
@Transactional
public class WorktimeServiceImpl implements WorktimeService{

	@Autowired
	private WorktimeMapper worktimeMapper;
	
	@Override
	public Long calculateTimeoutPoint(long startTime, long timeLimit, String worktimeUuid) {		
		if(worktimeMapper.checkWorktimeIsExists(worktimeUuid) == 0) {
			throw new WorktimeNotFoundException(worktimeUuid);
		}
		if(timeLimit == 0) {
			return startTime;
		}
		WorktimeDetailVo worktimeDetailVo = new WorktimeDetailVo();
		WorktimeDetailVo recentWorktimeDetail = null;
		long workStart = 0;
		long workEnd = 0;
		long duration = 0;
		while(true) {
			worktimeDetailVo.setWorktimeUuid(worktimeUuid);
			worktimeDetailVo.setWorkStart(startTime);
			recentWorktimeDetail = worktimeMapper.getRecentWorktimeDetail(worktimeDetailVo);
			if(recentWorktimeDetail == null) {
				return startTime;
			}
			workStart = recentWorktimeDetail.getWorkStart();
			workEnd = recentWorktimeDetail.getWorkEnd();
			if(workStart > startTime) {
				startTime = workStart;
			}
			duration = workEnd - startTime;
			if(duration > timeLimit) {
				return startTime + timeLimit;
			}else {
				timeLimit -= duration;
				startTime = workEnd;
			}
		}
	}

}
