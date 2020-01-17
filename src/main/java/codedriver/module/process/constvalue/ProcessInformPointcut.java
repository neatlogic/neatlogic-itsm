package codedriver.module.process.constvalue;

import java.util.ArrayList;
import java.util.List;

import codedriver.module.process.dto.ProcessInformPointcutVo;

public enum ProcessInformPointcut {

	ACTIVE("active", "激活"),
	START("start", "开始"),
	BACK("back", "回退"),
	COMPLETE("complete", "完成");
	
	private String value;
	private String name;
	
	private static List<ProcessInformPointcutVo> processInformPointcutList;
	
	private ProcessInformPointcut(String value, String name) {
		this.value = value;
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public String getName() {
		return name;
	}
	
	public static String getValue(String value) {
		for(ProcessInformPointcut processInformPointcut : ProcessInformPointcut.values()) {
			if(processInformPointcut.getValue().equals(value)) {
				return processInformPointcut.getValue();
			}
		}
		return null;
	}
	
	public static String getName(String value) {
		for(ProcessInformPointcut processInformPointcut : ProcessInformPointcut.values()) {
			if(processInformPointcut.getValue().equals(value)) {
				return processInformPointcut.getName();
			}
		}
		return "";
	}
	
	public static List<ProcessInformPointcutVo> getProcessInformPointcutList(){
		if(processInformPointcutList == null) {
			processInformPointcutList = new ArrayList<>();
			for(ProcessInformPointcut processInformPointcut : ProcessInformPointcut.values()) {
				processInformPointcutList.add(new ProcessInformPointcutVo(processInformPointcut.getValue(), processInformPointcut.getName()));
			}
		}
		return processInformPointcutList;
	}
}
