package codedriver.module.process.api.processtask;

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepCommentNotFoundException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
@Service
@Transactional
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
		@Param(name = "content", type = ApiParamType.STRING, xss = true, desc = "描述"),
		@Param(name = "fileUuidList", type=ApiParamType.JSONARRAY, desc = "附件uuid列表")
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
				processTaskMapper.replaceProcessTaskContent(contentVo);
				jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), contentVo.getHash());
				processTaskStepCommentVo.setContentHash(contentVo.getHash());
			}
			
			String fileUuidListStr = jsonObj.getString("fileUuidList");
			if(StringUtils.isNotBlank(fileUuidListStr)) {
				List<String> fileUuidList = JSON.parseArray(fileUuidListStr, String.class);
				if(CollectionUtils.isNotEmpty(fileUuidList)) {
					for(String fileUuid : fileUuidList) {
						FileVo fileVo = fileMapper.getFileByUuid(fileUuid);
						if(fileVo == null) {
							throw new ProcessTaskRuntimeException("上传附件uuid:'" + fileUuid + "'不存在");
						}
					}
					ProcessTaskContentVo fileUuidListContentVo = new ProcessTaskContentVo(fileUuidListStr);
					processTaskMapper.replaceProcessTaskContent(fileUuidListContentVo);
					processTaskStepCommentVo.setFileUuidListHash(fileUuidListContentVo.getHash());
				}
			}
			processTaskMapper.updateProcessTaskStepCommentById(processTaskStepCommentVo);
			
			processTaskService.parseProcessTaskStepComment(oldCommentVo);
			jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getOldDataParamName(), oldCommentVo.getContentHash());
			jsonObj.put(ProcessTaskAuditDetailType.FILE.getOldDataParamName(), JSON.toJSONString(oldCommentVo.getFileUuidList()));
			
			//生成活动
			ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(oldCommentVo.getProcessTaskStepId());	
			processTaskStepVo.setParamObj(jsonObj);
			ProcessStepHandlerFactory.getHandler().activityAudit(processTaskStepVo, ProcessTaskStepAction.EDITCOMMENT);
			
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
