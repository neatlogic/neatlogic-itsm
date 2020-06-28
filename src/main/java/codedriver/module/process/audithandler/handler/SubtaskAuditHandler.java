package codedriver.module.process.audithandler.handler;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Objects;

import codedriver.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerBase;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.dto.ProcessTaskStepSubtaskVo;
@Service
public class SubtaskAuditHandler extends ProcessTaskStepAuditDetailHandlerBase {

	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.SUBTASK.getValue();
	}

	@Override
	protected void myHandle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		if(StringUtils.isNotBlank(processTaskStepAuditDetailVo.getNewContent())) {
			ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = JSON.parseObject(processTaskStepAuditDetailVo.getNewContent(), new TypeReference<ProcessTaskStepSubtaskVo>(){});
			ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepSubtaskVo.getContentHash());
			if(processTaskContentVo != null) {
				processTaskStepSubtaskVo.setContent(processTaskContentVo.getContent());
			}
			
			JSONObject content = new JSONObject();
			content.put("type", "content");
			content.put("newContent", processTaskStepSubtaskVo.getContent());
			
			JSONObject targetTime = new JSONObject();
			targetTime.put("type", "targetTime");
			targetTime.put("newContent", processTaskStepSubtaskVo.getTargetTime());

			JSONObject userName = new JSONObject();
			userName.put("type", "userName");
			userName.put("newContent", processTaskStepSubtaskVo.getUserName());

			if(StringUtils.isNotBlank(processTaskStepAuditDetailVo.getOldContent())) {
				ProcessTaskStepSubtaskVo oldProcessTaskStepSubtaskVo = JSON.parseObject(processTaskStepAuditDetailVo.getOldContent(), new TypeReference<ProcessTaskStepSubtaskVo>(){});		
				ProcessTaskContentVo oldContentVo = processTaskMapper.getProcessTaskContentByHash(oldProcessTaskStepSubtaskVo.getContentHash());
				if(oldContentVo != null) {
					oldProcessTaskStepSubtaskVo.setContent(oldContentVo.getContent());
				}
				if(!Objects.equal(oldProcessTaskStepSubtaskVo.getContent(), processTaskStepSubtaskVo.getContent())) {
					content.put("oldContent", oldProcessTaskStepSubtaskVo.getContent());
				}
				if(!Objects.equal(oldProcessTaskStepSubtaskVo.getTargetTime(), processTaskStepSubtaskVo.getTargetTime())) {
					targetTime.put("oldContent", oldProcessTaskStepSubtaskVo.getTargetTime());
				}
				if(!Objects.equal(oldProcessTaskStepSubtaskVo.getUserName(), processTaskStepSubtaskVo.getUserName())) {
					userName.put("oldContent", oldProcessTaskStepSubtaskVo.getUserName());
				}
			}

			JSONArray subtask = new JSONArray();
			subtask.add(content);
			subtask.add(targetTime);
			subtask.add(userName);
			processTaskStepAuditDetailVo.setOldContent(null);
			processTaskStepAuditDetailVo.setNewContent(JSON.toJSONString(subtask));
		}
	}

}
