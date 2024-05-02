package neatlogic.module.process.audithandler.handler;

import neatlogic.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import neatlogic.framework.process.constvalue.ProcessTaskAuditDetailType;
import neatlogic.framework.process.dto.ChannelTypeRelationVo;
import neatlogic.framework.process.dto.ProcessTaskStepAuditDetailVo;
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
