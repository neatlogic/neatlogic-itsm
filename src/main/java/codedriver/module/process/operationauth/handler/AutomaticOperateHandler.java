package codedriver.module.process.operationauth.handler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerBase;
import codedriver.framework.process.operationauth.core.OperationAuthHandlerType;

@Component
public class AutomaticOperateHandler extends OperationAuthHandlerBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public Map<String, Boolean> getOperateMap(Long processTaskId, Long processTaskStepId) {
		// TODO Auto-generated method stub
		Set<String> list = new HashSet<>();
		processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
		list.add("AUTOMATIC");
		list.add("AUTOMATIC2");
		list.add("AUTOMATIC3");
		list.add("AUTOMATIC4");
		return null;
	}

	@Override
	public OperationAuthHandlerType getHandler() {
		return OperationAuthHandlerType.AUTOMATIC;
	}

}
