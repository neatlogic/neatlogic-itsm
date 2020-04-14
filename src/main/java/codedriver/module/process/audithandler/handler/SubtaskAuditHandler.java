package codedriver.module.process.audithandler.handler;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
@Service
public class SubtaskAuditHandler implements IProcessTaskStepAuditDetailHandler {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.SUBTASK.getValue();
	}

	@Override
	public void handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		ProcessTaskStepSubtaskVo oldProcessTaskStepSubtaskVo = null;
		if(StringUtils.isNotBlank(processTaskStepAuditDetailVo.getOldContent())) {
			oldProcessTaskStepSubtaskVo = JSON.parseObject(processTaskStepAuditDetailVo.getOldContent(), new TypeReference<ProcessTaskStepSubtaskVo>(){});		
			ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(oldProcessTaskStepSubtaskVo.getContentHash());
			if(processTaskContentVo != null) {
				oldProcessTaskStepSubtaskVo.setContent(processTaskContentVo.getContent());
			}
		}
		if(StringUtils.isNotBlank(processTaskStepAuditDetailVo.getNewContent())) {
			ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = JSON.parseObject(processTaskStepAuditDetailVo.getNewContent(), new TypeReference<ProcessTaskStepSubtaskVo>(){});
			ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepSubtaskVo.getContentHash());
			if(processTaskContentVo != null) {
				processTaskStepSubtaskVo.setContent(processTaskContentVo.getContent());
			}
			JSONArray subtask = new JSONArray();
			JSONObject content = new JSONObject();
			content.put("type", "content");
			content.put("newContent", processTaskStepSubtaskVo.getContent());
			if(oldProcessTaskStepSubtaskVo != null) {
				content.put("oldContent", oldProcessTaskStepSubtaskVo.getContent());
			}
			subtask.add(content);
			JSONObject targetTime = new JSONObject();
			content.put("type", "targetTime");
			targetTime.put("newContent", processTaskStepSubtaskVo.getTargetTime());
			if(oldProcessTaskStepSubtaskVo != null) {
				targetTime.put("oldContent", oldProcessTaskStepSubtaskVo.getTargetTime());
			}
			subtask.add(targetTime);
			JSONObject userName = new JSONObject();
			content.put("type", "userName");
			userName.put("newContent", processTaskStepSubtaskVo.getUserName());
			if(oldProcessTaskStepSubtaskVo != null) {
				userName.put("oldContent", oldProcessTaskStepSubtaskVo.getUserName());
			}
			subtask.add(userName);
			processTaskStepAuditDetailVo.setOldContent(null);
			processTaskStepAuditDetailVo.setNewContent(JSON.toJSONString(subtask));
		}
	}

}
