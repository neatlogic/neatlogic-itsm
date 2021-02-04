package codedriver.module.process.audithandler.handler;

import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.dto.ProcessTaskVo;
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
