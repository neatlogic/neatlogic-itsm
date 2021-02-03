package codedriver.module.process.audithandler.handler;

import java.util.List;

import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.dto.ProcessTaskVo;
@Service
public class ProcessTaskListAuditHandler implements IProcessTaskStepAuditDetailHandler {
    
    @Autowired
    private ProcessTaskMapper processTaskMapper;
    @Autowired
    private ChannelMapper channelMapper;
    @Autowired
    private ChannelTypeMapper channelTypeMapper;

	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.PROCESSTASKLIST.getValue();
	}

	@Override
	public int handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
	    JSONArray resultList = new JSONArray();
	    List<Long> processTaskIdList = JSON.parseArray(processTaskStepAuditDetailVo.getNewContent(), Long.class);
	    for(Long processTaskId : processTaskIdList) {
	        JSONObject resultObj = new JSONObject();
	        resultObj.put("processTaskId", processTaskId);
	        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
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
	        resultList.add(resultObj);
	    }
	    processTaskStepAuditDetailVo.setNewContent(JSON.toJSONString(resultList));
		return 1;
	}

}
