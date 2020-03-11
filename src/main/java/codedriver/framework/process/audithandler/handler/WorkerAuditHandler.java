package codedriver.framework.process.audithandler.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.module.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.module.process.dto.ProcessTaskStepAuditDetailVo;
@Service
public class WorkerAuditHandler implements IProcessTaskStepAuditDetailHandler{
	
	@Autowired
	private UserMapper userMapper;
	
	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.WORKER.getValue();
	}

	@Override
	public void handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		String oldContent = processTaskStepAuditDetailVo.getOldContent();
		if(StringUtils.isNotBlank(oldContent)) {
			UserVo userVo = userMapper.getUserByUserId(oldContent);
			if(userVo != null) {
				Map<String, String> map = new HashMap<>();
				map.put("userId", userVo.getUserId());
				map.put("userName", userVo.getUserName());
				processTaskStepAuditDetailVo.setOldContent(JSON.toJSONString(map));
			}
		}
		String newContent = processTaskStepAuditDetailVo.getNewContent();
		if(StringUtils.isNotBlank(newContent)) {
			UserVo userVo = userMapper.getUserByUserId(newContent);
			if(userVo != null) {
				Map<String, String> map = new HashMap<>();
				map.put("userId", userVo.getUserId());
				map.put("userName", userVo.getUserName());
				processTaskStepAuditDetailVo.setNewContent(JSON.toJSONString(map));
			}
		}
	}

}
