package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepFileVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskContentUpdateApi extends PrivateApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
    
    @Autowired
    private ProcessTaskService processTaskService;
	
	@Autowired
	private FileMapper fileMapper;
	
	@Override
	public String getToken() {
		return "processtask/content/update";
	}

	@Override
	public String getName() {
		return "工单上报描述内容及附件更新接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "步骤id"),
		@Param(name = "content", type = ApiParamType.STRING, desc = "描述"),
		@Param(name = "fileIdList", type=ApiParamType.JSONARRAY, desc = "附件id列表")
	})
	@Description(desc = "工单上报描述内容及附件更新接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
		// 锁定当前流程
		processTaskMapper.getProcessTaskLockById(processTaskId);
		
		IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler();
		handler.verifyActionAuthoriy(processTaskId, processTaskStepId, ProcessTaskStepAction.UPDATE);
		
		//获取开始步骤id
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
		if(processTaskStepList.size() != 1) {
			throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
		}
		Long startProcessTaskStepId = processTaskStepList.get(0).getId();
		
		boolean isUpdate = false;
        //获取上传附件uuid
        List<Long> oldFileIdList = new ArrayList<>();
		//获取上报描述内容hash
		String oldContentHash = null;
		Long oldContentId = null;
		List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(processTaskStepId);
        for(ProcessTaskStepContentVo processTaskStepContent : processTaskStepContentList) {
            if (ProcessTaskStepAction.STARTPROCESS.getValue().equals(processTaskStepContent.getType())) {
                oldContentId = processTaskStepContent.getId();
                oldContentHash = processTaskStepContent.getContentHash();
                oldFileIdList = processTaskMapper.getFileIdListByContentId(processTaskStepContent.getId());
                break;
            }
        }

        /** 保存新附件uuid **/
        List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("fileIdList")), Long.class);
        if(fileIdList == null) {
            fileIdList = new ArrayList<>();
        }
		String content = jsonObj.getString("content");
		if(StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
			if(Objects.equals(oldContentHash, contentVo.getHash())) {
				jsonObj.remove("content");
			}else {
				isUpdate = true;
	            processTaskMapper.replaceProcessTaskContent(contentVo);
				jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldContentHash);
				if(oldContentId == null) {
				    processTaskMapper.insertProcessTaskStepContent(new ProcessTaskStepContentVo(processTaskId, startProcessTaskStepId, contentVo.getHash(), ProcessTaskStepAction.STARTPROCESS.getValue()));
				}else {
				    processTaskMapper.updateProcessTaskStepContentById(new ProcessTaskStepContentVo(oldContentId, contentVo.getHash()));
				}
			}
		}else if(oldContentHash != null){
			isUpdate = true;
			jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldContentHash);
			if(CollectionUtils.isEmpty(fileIdList)) {
	            processTaskMapper.deleteProcessTaskStepContentById(oldContentId);
			}else {
			    processTaskMapper.updateProcessTaskStepContentById(new ProcessTaskStepContentVo(oldContentId, null));
			}
		}else {
			jsonObj.remove("content");
		}
		
		
		if(Objects.equals(oldFileIdList, fileIdList)) {
			jsonObj.remove("fileIdList");
		}else {
            isUpdate = true;
            processTaskMapper.deleteProcessTaskStepFileByContentId(oldContentId);
			ProcessTaskContentVo processTaskContentVo = new ProcessTaskContentVo(JSON.toJSONString(oldFileIdList));
			processTaskMapper.replaceProcessTaskContent(processTaskContentVo);
			jsonObj.put(ProcessTaskAuditDetailType.FILE.getOldDataParamName(), processTaskContentVo.getHash());
			ProcessTaskStepFileVo processTaskStepFileVo = new ProcessTaskStepFileVo();
			processTaskStepFileVo.setProcessTaskId(processTaskId);
			processTaskStepFileVo.setProcessTaskStepId(startProcessTaskStepId);
			processTaskStepFileVo.setContentId(oldContentId);
			for (Long fileId : fileIdList) {
				if(fileMapper.getFileById(fileId) != null) {
					processTaskStepFileVo.setFileId(fileId);
					processTaskMapper.insertProcessTaskStepFile(processTaskStepFileVo);
				}
			}
		}

		//生成活动
		if(isUpdate) {	        
	        ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
	        processTaskStepVo.setProcessTaskId(processTaskId);
	        processTaskStepVo.setId(processTaskStepId);
			processTaskStepVo.setParamObj(jsonObj);
			handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.UPDATE);	
		}
		return null;
	}

}
