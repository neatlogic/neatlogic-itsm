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
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepCommentNotFoundException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
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
		@Param(name = "commentList", explode = ProcessTaskStepCommentVo[].class, desc = "当前步骤评论列表")
	})
	@Description(desc = "工单回复编辑接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		ProcessTaskStepCommentVo oldCommentVo = processTaskMapper.getProcessTaskStepCommentById(id);
		if(oldCommentVo == null) {
			throw new ProcessTaskStepCommentNotFoundException(id.toString());
		}
		if(Objects.equals(oldCommentVo.getIsEditable(), 1)) {
			// 锁定当前流程
			processTaskMapper.getProcessTaskLockById(oldCommentVo.getProcessTaskId());
			ProcessTaskStepCommentVo processTaskStepCommentVo = new ProcessTaskStepCommentVo();
			processTaskStepCommentVo.setId(oldCommentVo.getId());
			processTaskStepCommentVo.setProcessTaskId(oldCommentVo.getProcessTaskId());
			processTaskStepCommentVo.setProcessTaskStepId(oldCommentVo.getProcessTaskStepId());
			processTaskStepCommentVo.setLcu(UserContext.get().getUserUuid(true));
			
			String content = jsonObj.getString("content");
			if(StringUtils.isNotBlank(content)) {
				ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
				processTaskStepCommentVo.setContentHash(contentVo.getHash());
			}

			List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("fileIdList")), Long.class);
			if(CollectionUtils.isNotEmpty(fileIdList)) {
				for(Long fileId : fileIdList) {
					FileVo fileVo = fileMapper.getFileById(fileId);
					if(fileVo == null) {
						throw new ProcessTaskRuntimeException("上传附件id:'" + fileId + "'不存在");
					}
				}
				ProcessTaskContentVo fileIdListContentVo = new ProcessTaskContentVo(JSON.toJSONString(fileIdList));
				processTaskMapper.replaceProcessTaskContent(fileIdListContentVo);
				processTaskStepCommentVo.setFileIdListHash(fileIdListContentVo.getHash());
			}

			processTaskMapper.updateProcessTaskStepCommentById(processTaskStepCommentVo);
			
			jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldCommentVo.getContentHash());
			jsonObj.put(ProcessTaskAuditDetailType.FILE.getOldDataParamName(), oldCommentVo.getFileIdListHash());
			
			//生成活动
			ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(oldCommentVo.getProcessTaskStepId());	
			processTaskStepVo.setParamObj(jsonObj);
			ProcessStepHandlerFactory.getHandler().activityAudit(processTaskStepVo, ProcessTaskAuditType.EDITCOMMENT);
			
			List<ProcessTaskStepCommentVo> processTaskStepCommentList = processTaskMapper.getProcessTaskStepCommentListByProcessTaskStepId(oldCommentVo.getProcessTaskStepId());
			for(ProcessTaskStepCommentVo processTaskStepComment : processTaskStepCommentList) {
				processTaskService.parseProcessTaskStepComment(processTaskStepComment);
			}
			JSONObject resultObj = new JSONObject();
			resultObj.put("commentList", processTaskStepCommentList);
			return resultObj;
		}else {
			throw new ProcessTaskNoPermissionException(ProcessTaskStepAction.EDITCOMMENT.getText());
		}
	}
}
