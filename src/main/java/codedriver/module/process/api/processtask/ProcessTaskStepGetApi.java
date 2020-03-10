package codedriver.module.process.api.processtask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.module.process.constvalue.ProcessTaskStatus;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.constvalue.UserType;
import codedriver.module.process.dto.ProcessTaskContentVo;
import codedriver.module.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.module.process.dto.ProcessTaskStepAuditVo;
import codedriver.module.process.dto.ProcessTaskStepContentVo;
import codedriver.module.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepUserVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;
@Service
public class ProcessTaskStepGetApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getToken() {
		return "processtask/step/get";
	}

	@Override
	public String getName() {
		return "工单步骤基本信息获取接口";
	}
	
	
	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "工单步骤id")
	})
	@Output({
		@Param(explode = ProcessTaskStepVo.class, desc = "工单步骤信息")
	})
	@Description(desc = "工单步骤基本信息获取接口，当前步骤名称、激活时间、状态、处理人、协助处理人、处理时效、表单属性显示控制等")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		//获取步骤信息
		ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
		if(processTaskStepVo == null) {
			throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
		}
		//状态名
		processTaskStepVo.setStatusText(ProcessTaskStatus.getText(processTaskStepVo.getStatus()));
		//处理人列表
		List<ProcessTaskStepUserVo> majorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, UserType.MAJOR.getValue());
		if(CollectionUtils.isNotEmpty(majorUserList)) {
			processTaskStepVo.setMajorUserList(majorUserList);
		}
		List<ProcessTaskStepUserVo> minorUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, UserType.MINOR.getValue());
		if(CollectionUtils.isNotEmpty(minorUserList)) {
			processTaskStepVo.setMinorUserList(minorUserList);
		}
		List<ProcessTaskStepUserVo> agentUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepId, UserType.AGENT.getValue());
		if(CollectionUtils.isNotEmpty(agentUserList)) {
			processTaskStepVo.setAgentUserList(agentUserList);
		}
		//表单属性显示控制
		List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList = processTaskMapper.getProcessTaskStepFormAttributeByProcessTaskStepId(processTaskStepId);
		if(processTaskStepFormAttributeList.size() > 0) {
			Map<String, String> formAttributeActionMap = new HashMap<>();
			for(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo : processTaskStepFormAttributeList) {
				formAttributeActionMap.put(processTaskStepFormAttributeVo.getAttributeUuid(), processTaskStepFormAttributeVo.getAction());
			}
			processTaskStepVo.setFormAttributeActionMap(formAttributeActionMap);
		}
		//回复框内容和附件暂存回显
		ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
		processTaskStepAuditVo.setProcessTaskStepId(processTaskStepId);
		processTaskStepAuditVo.setAction(ProcessTaskStepAction.SAVE.getValue());
		processTaskStepAuditVo.setUserId(UserContext.get().getUserId());
		List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(processTaskStepAuditVo);
		if(CollectionUtils.isNotEmpty(processTaskStepAuditList)) {
			List<ProcessTaskStepAuditDetailVo> processTaskStepAuditDetailListt = processTaskStepAuditList.get(0).getAuditDetailList();
			for(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo : processTaskStepAuditDetailListt) {
				if(ProcessTaskAuditDetailType.CONTENT.getValue().equals(processTaskStepAuditDetailVo.getType())) {
					ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepAuditDetailVo.getNewContent());
					if(processTaskContentVo != null) {
						
					}
				}else if(ProcessTaskAuditDetailType.FILE.getValue().equals(processTaskStepAuditDetailVo.getType())){
					
				}
			}
		}

		return null;
	}

}
