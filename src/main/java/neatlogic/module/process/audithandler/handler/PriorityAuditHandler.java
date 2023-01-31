package neatlogic.module.process.audithandler.handler;

import neatlogic.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import neatlogic.framework.process.constvalue.ProcessTaskAuditDetailType;
import neatlogic.framework.process.dao.mapper.PriorityMapper;
import neatlogic.framework.process.dto.PriorityVo;
import neatlogic.framework.process.dto.ProcessTaskStepAuditDetailVo;
@Service
public class PriorityAuditHandler implements IProcessTaskStepAuditDetailHandler {

	@Autowired
	private PriorityMapper priorityMapper;
	
	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.PRIORITY.getValue();
	}

	@Override
	public int handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		String oldContent = processTaskStepAuditDetailVo.getOldContent();
		if(StringUtils.isNotBlank(oldContent)) {
			PriorityVo priorityVo = priorityMapper.getPriorityByUuid(oldContent);
			if(priorityVo != null) {
				processTaskStepAuditDetailVo.setOldContent(JSON.toJSONString(priorityVo));
			}
		}
		String newContent = processTaskStepAuditDetailVo.getNewContent();
		if(StringUtils.isNotBlank(newContent)) {
			PriorityVo priorityVo = priorityMapper.getPriorityByUuid(newContent);
			if(priorityVo != null) {
				processTaskStepAuditDetailVo.setNewContent(JSON.toJSONString(priorityVo));
			}
		}
		return 1;
	}

}
