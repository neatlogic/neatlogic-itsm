package codedriver.module.process.dto;

public class ProcessTaskStepUserAuthorityVo {

	private boolean view;
	private boolean start;
	private boolean complete;
	private boolean transfer;
	private boolean abort;
	private boolean retreat;
	private boolean subProcess;
	private boolean back;
	private boolean comment;
	private boolean recover;
	
	public boolean isView() {
		return view;
	}
	public void setView(boolean view) {
		this.view = view;
	}
	public boolean isStart() {
		return start;
	}
	public void setStart(boolean start) {
		this.start = start;
	}
	public boolean isComplete() {
		return complete;
	}
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	public boolean isTransfer() {
		return transfer;
	}
	public void setTransfer(boolean transfer) {
		this.transfer = transfer;
	}
	public boolean isAbort() {
		return abort;
	}
	public void setAbort(boolean abort) {
		this.abort = abort;
	}
	public boolean isRetreat() {
		return retreat;
	}
	public void setRetreat(boolean retreat) {
		this.retreat = retreat;
	}
	public boolean isSubProcess() {
		return subProcess;
	}
	public void setSubProcess(boolean subProcess) {
		this.subProcess = subProcess;
	}
	public boolean isBack() {
		return back;
	}
	public void setBack(boolean back) {
		this.back = back;
	}
	public boolean isComment() {
		return comment;
	}
	public void setComment(boolean comment) {
		this.comment = comment;
	}
	public boolean isRecover() {
		return recover;
	}
	public void setRecover(boolean recover) {
		this.recover = recover;
	}
}
