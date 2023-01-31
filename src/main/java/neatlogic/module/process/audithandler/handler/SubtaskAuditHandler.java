package neatlogic.module.process.audithandler.handler;

import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Objects;

import neatlogic.framework.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dto.ProcessTaskStepAuditDetailVo;
import neatlogic.framework.process.dto.ProcessTaskStepSubtaskVo;
//@Service
@Deprecated
public class SubtaskAuditHandler implements IProcessTaskStepAuditDetailHandler {
    @Autowired
    private SelectContentByHashMapper selectContentByHashMapper;
    
	@Override
	public String getType() {
//		return ProcessTaskAuditDetailType.SUBTASK.getValue();
		return null;
	}

	@Override
	public int handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		if(StringUtils.isNotBlank(processTaskStepAuditDetailVo.getNewContent())) {
			ProcessTaskStepSubtaskVo processTaskStepSubtaskVo = JSON.parseObject(processTaskStepAuditDetailVo.getNewContent(), new TypeReference<ProcessTaskStepSubtaskVo>(){});
			processTaskStepSubtaskVo.setContent(selectContentByHashMapper.getProcessTaskContentStringByHash(processTaskStepSubtaskVo.getContentHash()));
			JSONObject content = new JSONObject();
			content.put("type", "content");
			content.put("typeName", "描述");
			content.put("newContent", processTaskStepSubtaskVo.getContent());
			content.put("changeType", "new");
			
			JSONObject targetTime = new JSONObject();
			targetTime.put("type", "targetTime");
			targetTime.put("typeName", "期望时间");
			targetTime.put("newContent", processTaskStepSubtaskVo.getTargetTime());
			targetTime.put("changeType", "new");

			JSONObject userName = new JSONObject();
			userName.put("type", "worker");
			userName.put("typeName", "处理人");
			JSONObject newUserObj = new JSONObject();
			newUserObj.put("initType", GroupSearch.USER.getValue());
			newUserObj.put("uuid", processTaskStepSubtaskVo.getUserUuid());
			newUserObj.put("name", processTaskStepSubtaskVo.getUserName());
			userName.put("newContent", newUserObj);
			userName.put("changeType", "new");

			if(StringUtils.isNotBlank(processTaskStepAuditDetailVo.getOldContent())) {
				ProcessTaskStepSubtaskVo oldProcessTaskStepSubtaskVo = JSON.parseObject(processTaskStepAuditDetailVo.getOldContent(), new TypeReference<ProcessTaskStepSubtaskVo>(){});		
				oldProcessTaskStepSubtaskVo.setContent(selectContentByHashMapper.getProcessTaskContentStringByHash(oldProcessTaskStepSubtaskVo.getContentHash()));
				if(!Objects.equal(oldProcessTaskStepSubtaskVo.getContent(), processTaskStepSubtaskVo.getContent())) {
					content.put("oldContent", oldProcessTaskStepSubtaskVo.getContent());
					content.put("changeType", "update");
				}
				if(!Objects.equal(oldProcessTaskStepSubtaskVo.getTargetTime(), processTaskStepSubtaskVo.getTargetTime())) {
					targetTime.put("oldContent", oldProcessTaskStepSubtaskVo.getTargetTime());
					targetTime.put("changeType", "update");
				}
				if(!Objects.equal(oldProcessTaskStepSubtaskVo.getUserName(), processTaskStepSubtaskVo.getUserName())) {
					JSONObject oldUserObj = new JSONObject();
					oldUserObj.put("initType", GroupSearch.USER.getValue());
					oldUserObj.put("uuid", oldProcessTaskStepSubtaskVo.getUserUuid());
					oldUserObj.put("name", oldProcessTaskStepSubtaskVo.getUserName());
					userName.put("oldContent", oldUserObj);
					userName.put("changeType", "update");
				}
			}

			JSONArray subtask = new JSONArray();
			subtask.add(content);
			subtask.add(targetTime);
			subtask.add(userName);
			processTaskStepAuditDetailVo.setOldContent(null);
			processTaskStepAuditDetailVo.setNewContent(JSON.toJSONString(subtask));
		}
		return 1;
	}

}
