package codedriver.module.process.audithandler.handler;

import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dto.ChannelTypeRelationVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
@Service
public class ChannelTypeRelationAuditHandler implements IProcessTaskStepAuditDetailHandler {
	
    @Autowired
    private ChannelTypeMapper channelTypeMapper;
    
	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.CHANNELTYPERELATION.getValue();
	}

	@Override
	public int handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
	    Long channelTypeRelationId = Long.valueOf(processTaskStepAuditDetailVo.getNewContent());
	    ChannelTypeRelationVo channelTypeRelationVo = channelTypeMapper.getChannelTypeRelationById(channelTypeRelationId);
		if(channelTypeRelationVo != null) {
		    processTaskStepAuditDetailVo.setNewContent(channelTypeRelationVo.getName());
		}
	    return 1;
	}

}
