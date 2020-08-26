package codedriver.module.process.api.processtask;

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepFileVo;
import codedriver.framework.process.dto.ProcessTaskStepReplyVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepCommentNotFoundException;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskCommentEditApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Autowired
	private FileMapper fileMapper;

	@Override
	public String getToken() {
		return "processtask/comment/edit";
	}

	@Override
	public String getName() {
		return "工单回复编辑接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "回复id"),
		@Param(name = "content", type = ApiParamType.STRING, desc = "描述"),
		@Param(name = "fileIdList", type=ApiParamType.JSONARRAY, desc = "附件id列表")
	})
	@Output({
		@Param(name = "commentList", explode = ProcessTaskStepReplyVo[].class, desc = "当前步骤评论列表")
	})
	@Description(desc = "工单回复编辑接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		ProcessTaskStepContentVo processTaskStepContentVo= processTaskMapper.getProcessTaskStepContentById(id);
        if(processTaskStepContentVo == null) {
            throw new ProcessTaskStepCommentNotFoundException(id.toString());
        }
        ProcessTaskStepReplyVo oldReplyVo = new ProcessTaskStepReplyVo(processTaskStepContentVo);
		if(Objects.equals(oldReplyVo.getIsEditable(), 0)) {
            throw new ProcessTaskNoPermissionException(ProcessTaskStepAction.EDITCOMMENT.getText());	
		}
		// 锁定当前流程
        processTaskMapper.getProcessTaskLockById(oldReplyVo.getProcessTaskId());
        processTaskService.parseProcessTaskStepReply(oldReplyVo);
        
        ProcessTaskStepReplyVo processTaskStepCommentVo = new ProcessTaskStepReplyVo();
        processTaskStepCommentVo.setId(oldReplyVo.getId());
        processTaskStepCommentVo.setProcessTaskId(oldReplyVo.getProcessTaskId());
        processTaskStepCommentVo.setProcessTaskStepId(oldReplyVo.getProcessTaskStepId());
        processTaskStepCommentVo.setLcu(UserContext.get().getUserUuid(true));
        boolean isUpdate = false;
        List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("fileIdList")), Long.class);
        String content = jsonObj.getString("content");
        if(StringUtils.isNotBlank(content)) {
            ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
            if(Objects.equals(oldReplyVo.getContentHash(), contentVo.getHash())) {
                jsonObj.remove("content");
            }else {
                isUpdate = true;
                processTaskMapper.replaceProcessTaskContent(contentVo);
                jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldReplyVo.getContentHash());
                processTaskMapper.updateProcessTaskStepContentById(new ProcessTaskStepContentVo(oldReplyVo.getId(), contentVo.getHash()));
            }
        }else if(oldReplyVo.getContentHash() != null) {
            isUpdate = true;
            jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldReplyVo.getContentHash());
            if(CollectionUtils.isEmpty(fileIdList)) {
                processTaskMapper.deleteProcessTaskStepContentById(oldReplyVo.getId());
            }else {
                processTaskMapper.updateProcessTaskStepContentById(new ProcessTaskStepContentVo(oldReplyVo.getId(), null));
            }
        }else {
            jsonObj.remove("content");
        }
        
        if(Objects.equals(oldReplyVo.getFileIdList(), fileIdList)) {
            jsonObj.remove("fileIdList");
        }else {
            isUpdate = true;
            processTaskMapper.deleteProcessTaskStepFileByContentId(oldReplyVo.getId());
            ProcessTaskContentVo fileIdListContentVo = new ProcessTaskContentVo(JSON.toJSONString(oldReplyVo.getFileIdList()));
            processTaskMapper.replaceProcessTaskContent(fileIdListContentVo);
            jsonObj.put(ProcessTaskAuditDetailType.FILE.getOldDataParamName(), fileIdListContentVo.getHash());
            /** 保存附件uuid **/
            ProcessTaskStepFileVo processTaskStepFileVo = new ProcessTaskStepFileVo();
            processTaskStepFileVo.setProcessTaskId(oldReplyVo.getProcessTaskId());
            processTaskStepFileVo.setProcessTaskStepId(oldReplyVo.getProcessTaskStepId());
            processTaskStepFileVo.setContentId(oldReplyVo.getId());
            if(CollectionUtils.isNotEmpty(fileIdList)) {
                for(Long fileId : fileIdList) {
                    FileVo fileVo = fileMapper.getFileById(fileId);
                    if(fileVo == null) {
                        throw new ProcessTaskRuntimeException("上传附件id:'" + fileId + "'不存在");
                    }
                    processTaskStepFileVo.setFileId(fileId);
                    processTaskMapper.insertProcessTaskStepFile(processTaskStepFileVo);
                }
            }
        }

        if(isUpdate) {
            //生成活动
            ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
            processTaskStepVo.setProcessTaskId(oldReplyVo.getProcessTaskId());
            processTaskStepVo.setId(oldReplyVo.getProcessTaskStepId());
            processTaskStepVo.setParamObj(jsonObj);
            ProcessStepUtilHandlerFactory.getHandler().activityAudit(processTaskStepVo, ProcessTaskAuditType.EDITCOMMENT);
        }
        
        JSONObject resultObj = new JSONObject();
        resultObj.put("commentList", processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(oldReplyVo.getProcessTaskStepId()));
        return resultObj;
	}
}
