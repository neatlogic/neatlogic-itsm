package codedriver.module.process.constvalue;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.common.dto.ValueTextVo;

public enum ProcessTaskStatus {
	RUNNING("running", "处理中"),
	ABORTED("aborted", "已终止"),
	SUCCEED("succeed", "已成功"),
	PENDING("pending", "待处理"),
	FAILED("failed", "已失败"),
	ABORTING("aborting", "终止中"),
	BACK("back", "已回退"),
	HANG("hang", "已挂起");

	private String status;
	private String text;

	private static List<ValueTextVo> processTaskStatusList;
	private ProcessTaskStatus(String _status, String _text) {
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
		for (ProcessTaskStatus s : ProcessTaskStatus.values()) {
			if (s.getValue().equals(_status)) {
				return s.getValue();
			}
		}
		return null;
	}

	public static String getText(String _status) {
		for (ProcessTaskStatus s : ProcessTaskStatus.values()) {
			if (s.getValue().equals(_status)) {
				return s.getText();
			}
		}
		return "";
	}

	public static List<ValueTextVo> getProcessTaskStatusList(){
		if(processTaskStatusList == null) {
			processTaskStatusList = new ArrayList<>();
			for (ProcessTaskStatus s : ProcessTaskStatus.values()) {
				processTaskStatusList.add(new ValueTextVo(s.getValue(), s.getText()));
			}
		}
		return processTaskStatusList;
	}
}
