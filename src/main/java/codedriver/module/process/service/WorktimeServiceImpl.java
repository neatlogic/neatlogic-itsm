package codedriver.module.process.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.exception.WorktimeNotFoundException;
import codedriver.module.process.dto.WorktimeRangeVo;

@Service
public class WorktimeServiceImpl implements WorktimeService {

	@Autowired
	private WorktimeMapper worktimeMapper;

	@Override
	public long calculateTimeoutPoint(long activeTime, long timeLimit, String worktimeUuid) {
		if (worktimeMapper.checkWorktimeIsExists(worktimeUuid) == 0) {
			throw new WorktimeNotFoundException(worktimeUuid);
		}
		if (timeLimit == 0) {
			return activeTime;
		}
		WorktimeRangeVo worktimeRangeVo = new WorktimeRangeVo();
		WorktimeRangeVo recentWorktimeRange = null;
		long startTime = 0;
		long endTime = 0;
		long duration = 0;
		while (true) {
			worktimeRangeVo.setWorktimeUuid(worktimeUuid);
			worktimeRangeVo.setStartTime(activeTime);
			recentWorktimeRange = worktimeMapper.getRecentWorktimeRange(worktimeRangeVo);
			if (recentWorktimeRange == null) {
				return activeTime;
			}
			startTime = recentWorktimeRange.getStartTime();
			endTime = recentWorktimeRange.getEndTime();
			if (startTime > activeTime) {
				activeTime = startTime;
			}
			duration = endTime - activeTime;
			if (duration >= timeLimit) {
				return activeTime + timeLimit;
			} else {
				timeLimit -= duration;
				activeTime = endTime;
			}
		}
	}

	@Override
	public long calculateCostTime(List<WorktimeRangeVo> worktimeRangeList) {
		if (worktimeRangeList == null || worktimeRangeList.isEmpty()) {
			return 0L;
		}
		
		// 先按开始时间从小到大排序
		Collections.sort(worktimeRangeList, new Comparator<WorktimeRangeVo>() {
			@Override
			public int compare(WorktimeRangeVo o1, WorktimeRangeVo o2) {
				return o1.getStartTime().compareTo(o2.getStartTime());
			}
		});
		// 保存删除重复时间后的列表
		List<WorktimeRangeVo> deduplicationList = new ArrayList<>();
		long startTime = -1L;
		long endTime = -1L;
		WorktimeRangeVo worktimeRange = null;
		String worktimeUuid = null;
		for (WorktimeRangeVo worktimeRangeVo: worktimeRangeList) {
			if (startTime == -1L) {
				worktimeUuid = worktimeRangeVo.getWorktimeUuid();
				startTime = worktimeRangeVo.getStartTime();
			}
			if (endTime == -1L) {
				endTime = worktimeRangeVo.getEndTime();
			} else if (endTime >= worktimeRangeVo.getEndTime()) {// 如果上一段时间的结束时间大于等于这段的结束时间，说明上一段包含这段时间范围，抛弃这段

			} else if (endTime >= worktimeRangeVo.getStartTime()) {// 如果上一段时间的结束时间大于等于下一段的开始时间，就拼成一段时间
				endTime = worktimeRangeVo.getEndTime();
			} else {// 保存上一段
				worktimeRange = new WorktimeRangeVo();
				worktimeRange.setStartTime(startTime);
				worktimeRange.setEndTime(endTime);
				deduplicationList.add(worktimeRange);
				// 开始新一段
				startTime = worktimeRangeVo.getStartTime();
				endTime = worktimeRangeVo.getEndTime();
			}
		}
		// 最后一段
		worktimeRange = new WorktimeRangeVo();
		worktimeRange.setStartTime(startTime);
		worktimeRange.setEndTime(endTime);
		deduplicationList.add(worktimeRange);
		
		long sum = 0L;
		for (WorktimeRangeVo worktimeRangeVo : deduplicationList) {
			sum += worktimeMapper.calculateCostTime(worktimeUuid, worktimeRangeVo.getStartTime(), worktimeRangeVo.getEndTime());
		}
		return sum;
	}

}
