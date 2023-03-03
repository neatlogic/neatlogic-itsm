package neatlogic.module.process.audithandler.handler;

import neatlogic.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import neatlogic.framework.process.dao.mapper.ChannelTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.process.constvalue.ProcessTaskAuditDetailType;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ChannelTypeVo;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.dto.ProcessTaskStepAuditDetailVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
@Service
public class ProcessTaskAuditHandler implements IProcessTaskStepAuditDetailHandler {
	
    @Autowired
    private ProcessTaskMapper processTaskMapper;
    @Autowired
    private ChannelMapper channelMapper;
    @Autowired
    private ChannelTypeMapper channelTypeMapper;

	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.PROCESSTASK.getValue();
	}

	@Override
	public int handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
	    JSONObject resultObj = new JSONObject();
	    Long fromProcessTaskId = Long.valueOf(processTaskStepAuditDetailVo.getNewContent());
	    resultObj.put("processTaskId", fromProcessTaskId);
	    ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(fromProcessTaskId);
	    if(processTaskVo != null) {
	        resultObj.put("title", processTaskVo.getTitle());
	        resultObj.put("serialNumber", processTaskVo.getSerialNumber());
	        ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
	        if(channelVo != null) {
	            ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid());
	            if(channelTypeVo != null) {
	                resultObj.put("prefix", channelTypeVo.getPrefix());
	            }
	        }
	    }
	    processTaskStepAuditDetailVo.setNewContent(resultObj.toJSONString());
		return 1;
	}

}
