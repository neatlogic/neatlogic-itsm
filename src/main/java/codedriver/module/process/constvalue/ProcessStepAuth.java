package codedriver.module.process.constvalue;

import java.util.ArrayList;
import java.util.List;

import codedriver.module.process.dto.ProcessStepAuthVo;

public enum ProcessStepAuth {

	VIEW("view", "查看"),
	ABORT("abort", "终止流程"),
	TRANSFTER("transfter", "转交"),
	MODIFY("modify", "修改"),
	REMINDER("reminder", "催单");
	
	private String value;
	private String name;
	
	private static List<ProcessStepAuthVo> processStepAuthTypeList;
	
	private ProcessStepAuth(String value, String name) {
		this.value = value;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public static String getValue(String value) {
		for(ProcessStepAuth authType : ProcessStepAuth.values()) {
			if(authType.getValue().equals(value)) {
				return authType.getValue();
			}
		}
		return null;
	}
	
	public static String getName(String value) {
		for(ProcessStepAuth auth : ProcessStepAuth.values()) {
			if(auth.getValue().equals(value)) {
				return auth.getName();
			}
		}
		return "";
	}
	
	public static List<ProcessStepAuthVo> getProcessStepAuthList(){
		if(processStepAuthTypeList == null) {
			processStepAuthTypeList = new ArrayList<>();
			for(ProcessStepAuth auth : ProcessStepAuth.values()) {
				processStepAuthTypeList.add(new ProcessStepAuthVo(auth.getValue(), auth.getName()));
			}			
		}
		return processStepAuthTypeList;
	}
}
