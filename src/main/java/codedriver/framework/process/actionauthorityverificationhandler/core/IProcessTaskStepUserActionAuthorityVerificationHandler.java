package codedriver.framework.process.actionauthorityverificationhandler.core;

public interface IProcessTaskStepUserActionAuthorityVerificationHandler {

	public String getAction();
	
	public boolean test(Long processTaskId, Long processTaskStepId);
}
