package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.dto.FormAttributeVo;
import codedriver.module.process.dto.FormVersionVo;
import codedriver.module.process.dto.ProcessTaskContentVo;
import codedriver.module.process.dto.ProcessTaskFileVo;
import codedriver.module.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.module.process.dto.ProcessTaskFormVo;
import codedriver.module.process.dto.ProcessTaskStepContentVo;
import codedriver.module.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;

@Service
public class ProcessTaskDraftGetApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getToken() {
		return "processtask/draft/get";
	}

	@Override
	public String getName() {
		return "工单草稿数据获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name="processTaskId", type = ApiParamType.LONG, isRequired = true, desc="工单id")
	})
	@Output({
		@Param(name = "title", type = ApiParamType.STRING, desc = "标题"),
		@Param(name = "owner", type = ApiParamType.STRING, desc = "请求人"),
		@Param(name = "channelUuid", type = ApiParamType.STRING, desc = "服务通道"),
		@Param(name = "priorityUuid", type = ApiParamType.STRING, desc = "优先级uuid"),
		@Param(name = "content", type = ApiParamType.STRING, desc = "描述"),
		@Param(name = "fileUuidList", type = ApiParamType.STRING, desc = "附件uuid列表"),
		@Param(name = "formVersionVo", type = ApiParamType.STRING, desc = "表单"),
		@Param(name = "formAttributeDataMap", type = ApiParamType.STRING, desc = "表单属性数据"),
		@Param(name = "formAttributeActionMap", type = ApiParamType.STRING, desc = "表单属性显示控制"),
	})
	@Description(desc = "工单详情数据获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		resultObj.put("title", processTaskVo.getTitle());
		resultObj.put("owner", processTaskVo.getOwner());
		resultObj.put("channelUuid", processTaskVo.getChannelUuid());
		resultObj.put("priorityUuid", processTaskVo.getPriorityUuid());
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
		if(processTaskStepList.size() != 1) {
			throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
		}
		Long startProcessTaskStepId = processTaskStepList.get(0).getId();
		List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentProcessTaskStepId(startProcessTaskStepId);
		if(!processTaskStepContentList.isEmpty()) {
			ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepContentList.get(0).getContentHash());
			if(processTaskContentVo != null) {
				resultObj.put("content", processTaskContentVo.getContent());
			}
		}
		ProcessTaskFileVo processTaskFileVo = new ProcessTaskFileVo();
		processTaskFileVo.setProcessTaskId(processTaskId);
		processTaskFileVo.setProcessTaskStepId(startProcessTaskStepId);
		List<ProcessTaskFileVo> processTaskFileList = processTaskMapper.searchProcessTaskFile(processTaskFileVo);
		
		if(processTaskFileList.size() > 0) {
			List<String> fileUuidList = new ArrayList<>();
			for(ProcessTaskFileVo processTaskFile : processTaskFileList) {
				fileUuidList.add(processTaskFile.getFileUuid());
			}
			resultObj.put("fileUuidList", fileUuidList);
		}
		
		ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
		if(processTaskFormVo == null) {
			return resultObj;
		}
		if(StringUtils.isBlank(processTaskFormVo.getFormContent())) {
			return resultObj;
		}
		FormVersionVo formVersionVo = new FormVersionVo();
		formVersionVo.setFormConfig(processTaskFormVo.getFormContent());
		formVersionVo.setFormUuid(processTaskFormVo.getFormUuid());
		List<FormAttributeVo> formAttributeList =formVersionVo.getFormAttributeList();
		if(formAttributeList == null || formAttributeList.isEmpty()) {
			return resultObj;
		}
		resultObj.put("formVersionVo", formVersionVo);
		List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskId);
		if(processTaskFormAttributeDataList.size() > 0) {
			Map<String, String> formAttributeDataMap = new HashMap<>();
			for(ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
				formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getData());
			}
			resultObj.put("formAttributeDataMap", formAttributeDataMap);
		}
		
		List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList = processTaskMapper.getProcessTaskStepFormAttributeByProcessTaskStepId(startProcessTaskStepId);
		if(processTaskStepFormAttributeList.size() > 0) {
			Map<String, String> formAttributeActionMap = new HashMap<>();
			for(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo : processTaskStepFormAttributeList) {
				formAttributeActionMap.put(processTaskStepFormAttributeVo.getAttributeUuid(), processTaskStepFormAttributeVo.getAction());
			}
			resultObj.put("formAttributeActionMap", formAttributeActionMap);
		}
		return resultObj;
	}

}
