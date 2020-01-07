package codedriver.module.process.service;

public interface WorktimeService {

	Long calculateTimeoutPoint(long startTime, long timeLimit, String worktimeUuid);
}
