package codedriver.framework.process.exception.worktime;

import codedriver.framework.exception.core.ApiRuntimeException;

public class WorktimeConfigIllegalException extends ApiRuntimeException {

	private static final long serialVersionUID = -1703889762006158707L;

	private static String configTemplate = "{\"monday\":[{\"startTime\":\"9:00\",\"endTime\":\"12:00\"},{\"startTime\":\"14:00\",\"endTime\":\"18:00\"}]," + 
			"\"tuesday\":[{\"startTime\":\"9:00\",\"endTime\":\"12:00\"},{\"startTime\":\"14:00\",\"endTime\":\"18:00\"}]," + 
			"\"wednesday\":[{\"startTime\":\"9:00\",\"endTime\":\"12:00\"},{\"startTime\":\"14:00\",\"endTime\":\"18:00\"}]," + 
			"\"thursday\":[{\"startTime\":\"9:00\",\"endTime\":\"12:00\"},{\"startTime\":\"14:00\",\"endTime\":\"18:00\"}]," + 
			"\"friday\":[{\"startTime\":\"9:00\",\"endTime\":\"12:00\"},{\"startTime\":\"14:00\",\"endTime\":\"18:00\"}]," + 
			"\"saturday\":[{\"startTime\":\"9:00\",\"endTime\":\"12:00\"},{\"startTime\":\"14:00\",\"endTime\":\"18:00\"}]," + 
			"\"sunday\":[{\"startTime\":\"9:00\",\"endTime\":\"12:00\"},{\"startTime\":\"14:00\",\"endTime\":\"18:00\"}]" + 
			"}";
	public WorktimeConfigIllegalException(String msg) {
		super("config参数中\"" + msg+"\"不合法,正确的config参数格式是" + configTemplate);
	}
}
