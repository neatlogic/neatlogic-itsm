package codedriver.module.process.api.processtask;

import java.util.List;

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
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class ProcessTaskCommentApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Autowired
	private ProcessTaskStepDataMapper processTaskStepDataMapper;
	
	@Autowired
	private FileMapper fileMapper;
	
	@Override
	public String getToken() {
		return "processtask/comment";
	}

	@Override
	public String getName() {
		return "工单回复接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "步骤id"),
//		@Param(name = "formAttributeDataList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "表单属性数据列表"),
//		@Param(name = "hidecomponentList", type = ApiParamType.JSONARRAY, isRequired = false, desc = "联动隐藏表单属性列表"),
		@Param(name = "content", type = ApiParamType.STRING, desc = "描述"),
		@Param(name = "fileIdList", type=ApiParamType.JSONARRAY, desc = "附件id列表")
	})
	@Output({
		@Param(name = "commentList", explode = ProcessTaskStepCommentVo[].class, desc = "当前步骤评论列表")
	})
	@Description(desc = "工单回复接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
		processTaskMapper.getProcessTaskLockById(processTaskId);

		IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler();
		handler.verifyActionAuthoriy(processTaskId, processTaskStepId, ProcessTaskStepAction.COMMENT);
		
		//删除暂存
		ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
		processTaskStepDataVo.setProcessTaskId(processTaskId);
		processTaskStepDataVo.setProcessTaskStepId(processTaskStepId);
		processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
		processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
		processTaskStepDataMapper.deleteProcessTaskStepData(processTaskStepDataVo);

		ProcessTaskStepCommentVo processTaskStepCommentVo = null;
		
		String content = jsonObj.getString("content");
		if(StringUtils.isNotBlank(content)) {
			processTaskStepCommentVo = new ProcessTaskStepCommentVo();
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
			processTaskMapper.replaceProcessTaskContent(contentVo);
			processTaskStepCommentVo.setContentHash(contentVo.getHash());
		}
		
		List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("fileIdList")), Long.class);
		if(CollectionUtils.isNotEmpty(fileIdList)) {
			if(processTaskStepCommentVo == null) {
				processTaskStepCommentVo = new ProcessTaskStepCommentVo();
			}
			for(Long fileId : fileIdList) {
				if(fileMapper.getFileById(fileId) == null) {
					throw new ProcessTaskRuntimeException("上传附件id:'" + fileId + "'不存在");
				}
			}
			ProcessTaskContentVo fileIdListContentVo = new ProcessTaskContentVo(JSON.toJSONString(fileIdList));
			processTaskMapper.replaceProcessTaskContent(fileIdListContentVo);
			processTaskStepCommentVo.setFileIdListHash(fileIdListContentVo.getHash());
		}
		if(processTaskStepCommentVo != null) {
			processTaskStepCommentVo.setProcessTaskId(processTaskId);
			processTaskStepCommentVo.setProcessTaskStepId(processTaskStepId);
			processTaskStepCommentVo.setFcu(UserContext.get().getUserUuid(true));
			processTaskMapper.insertProcessTaskStepComment(processTaskStepCommentVo);

			//生成活动	
			ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
			processTaskStepVo.setProcessTaskId(processTaskId);
			processTaskStepVo.setId(processTaskStepId);
			processTaskStepVo.setParamObj(jsonObj);
			handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.COMMENT);
		}
		JSONObject resultObj = new JSONObject();
		resultObj.put("commentList", processTaskService.getProcessTaskStepCommentListByProcessTaskStepId(processTaskStepId));
		return resultObj;
	}

}
