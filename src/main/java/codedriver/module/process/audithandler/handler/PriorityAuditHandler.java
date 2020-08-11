package codedriver.module.process.audithandler.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import codedriver.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerBase;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
@Service
public class PriorityAuditHandler extends ProcessTaskStepAuditDetailHandlerBase {

	@Autowired
	private PriorityMapper priorityMapper;
	
	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.PRIORITY.getValue();
	}

	@Override
	protected int myHandle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
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
