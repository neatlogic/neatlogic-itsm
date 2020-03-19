package codedriver.module.process.constvalue;

public enum ProcessTaskStepAction {
	VIEW("view", "查看节点信息"),
	STARTPROCESS("startprocess", "上报"),
	START("start", "开始"),
	ACTIVE("active", "激活"),
	COMPLETE("complete", "流转"),
	RETREAT("retreat", "撤回"),
	ACCEPT("accept", "接受"),
	ABORT("abort", "取消"),
	RECOVER("recover", "恢复"),
	TRANSFER("transfer", "转交"),
	BACK("back", "回退"),
	SAVE("save", "暂存"),
	UPDATE("update", "修改上报内容"),//包括标题、优先级、描述
	UPDATETITLE("updateTitle", "更新标题"),
	UPDATEPRIORITY("updatePriority", "更新优先级"),
	UPDATECONTENT("updateContent", "更新上报描述内容"),
	COMMENT("comment", "回复");
	private String status;
	private String text;

	private ProcessTaskStepAction(String _status, String _text) {
		this.status = _status;
		this.text = _text;
	}

	public String getValue() {
		return status;
	}

	public String getText() {
		return text;
	}

	public static String getValue(String _status) {
		for (ProcessTaskStepAction s : ProcessTaskStepAction.values()) {
			if (s.getValue().equals(_status)) {
				return s.getValue();
			}
		}
		return null;
	}

	public static String getText(String _status) {
		for (ProcessTaskStepAction s : ProcessTaskStepAction.values()) {
			if (s.getValue().equals(_status)) {
				return s.getText();
			}
		}
		return "";
	}

}
