package neatlogic.module.process.audithandler.handler;

import neatlogic.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import neatlogic.framework.integration.dao.mapper.IntegrationMapper;
import neatlogic.framework.integration.dto.IntegrationVo;
import neatlogic.framework.process.constvalue.ProcessTaskAuditDetailType;
import neatlogic.framework.process.dto.ProcessTaskActionVo;
import neatlogic.framework.process.dto.ProcessTaskStepAuditDetailVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyTriggerType;

@Service
public class RestfulActionAuditHandler implements IProcessTaskStepAuditDetailHandler {

	@Autowired
	private IntegrationMapper integrationMapper;
	
	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.RESTFULACTION.getValue();
	}

	@Override
	public int handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		if(StringUtils.isNotBlank(processTaskStepAuditDetailVo.getNewContent())) {
			ProcessTaskActionVo actionVo = JSON.parseObject(processTaskStepAuditDetailVo.getNewContent(), ProcessTaskActionVo.class);
			IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(actionVo.getIntegrationUuid());
			if(integrationVo != null) {
				actionVo.setIntegrationName(integrationVo.getName());
			}
			String triggerText = ProcessTaskStepNotifyTriggerType.getText(actionVo.getTrigger());
			if(StringUtils.isNotBlank(triggerText)) {
				actionVo.setTriggerText(triggerText);
			}
			if(actionVo.isSucceed()) {
				actionVo.setStatusText("已成功");
			}else {
				actionVo.setStatusText("已失败");
			}
			processTaskStepAuditDetailVo.setNewContent(JSON.toJSONString(actionVo));
		}
		return 1;
	}

}
