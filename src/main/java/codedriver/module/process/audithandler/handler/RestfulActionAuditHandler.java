package codedriver.module.process.audithandler.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerBase;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dto.ActionVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.notify.core.NotifyTriggerType;

@Service
public class RestfulActionAuditHandler extends ProcessTaskStepAuditDetailHandlerBase {

	@Autowired
	private IntegrationMapper integrationMapper;
	
	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.RESTFULACTION.getValue();
	}

	@Override
	protected int myHandle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		if(StringUtils.isNotBlank(processTaskStepAuditDetailVo.getNewContent())) {
			ActionVo actionVo = JSON.parseObject(processTaskStepAuditDetailVo.getNewContent(), ActionVo.class);
			IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(actionVo.getIntegrationUuid());
			if(integrationVo != null) {
				actionVo.setIntegrationName(integrationVo.getName());
			}
			String triggerText = NotifyTriggerType.getText(actionVo.getTrigger());
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
