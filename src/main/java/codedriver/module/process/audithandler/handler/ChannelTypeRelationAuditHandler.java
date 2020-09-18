package codedriver.module.process.audithandler.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerBase;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelTypeRelationVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
@Service
public class ChannelTypeRelationAuditHandler extends ProcessTaskStepAuditDetailHandlerBase {
	
    @Autowired
    private ChannelMapper channelMapper;
    
	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.CHANNELTYPERELATION.getValue();
	}

	@Override
	protected int myHandle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
	    Long channelTypeRelationId = Long.valueOf(processTaskStepAuditDetailVo.getNewContent());
	    ChannelTypeRelationVo channelTypeRelationVo = channelMapper.getChannelTypeRelationById(channelTypeRelationId);
		if(channelTypeRelationVo != null) {
		    processTaskStepAuditDetailVo.setNewContent(channelTypeRelationVo.getName());
		}
	    return 1;
	}

}
