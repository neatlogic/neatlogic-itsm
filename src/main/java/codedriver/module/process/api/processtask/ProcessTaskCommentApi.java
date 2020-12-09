package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.process.dao.mapper.ProcessCommentTemplateMapper;
import codedriver.framework.process.dto.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.file.FileNotFoundException;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class ProcessTaskCommentApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Autowired
	private ProcessTaskStepDataMapper processTaskStepDataMapper;
	
	@Autowired
	private FileMapper fileMapper;

	@Autowired
    private ProcessCommentTemplateMapper commentTemplateMapper;
	
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
		@Param(name = "content", type = ApiParamType.STRING, desc = "描述"),
		@Param(name = "fileIdList", type=ApiParamType.JSONARRAY, desc = "附件id列表"),
		@Param(name = "commentTemplateId", type=ApiParamType.LONG, desc = "回复模版ID")
	})
	@Output({
		@Param(name = "commentList", explode = ProcessTaskStepReplyVo[].class, desc = "当前步骤评论列表")
	})
	@Description(desc = "工单回复接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        Long commentTemplateId = jsonObj.getLong("commentTemplateId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
		processTaskMapper.getProcessTaskLockById(processTaskId);
		ProcessTaskStepVo processTaskStepVo = processTaskVo.getCurrentProcessTaskStep();
		IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler(processTaskStepVo.getHandler());
		try {
	        handler.verifyOperationAuthoriy(processTaskVo, processTaskStepVo, ProcessTaskOperationType.COMMENT, true);
        }catch(ProcessTaskNoPermissionException e) {
            throw new PermissionDeniedException();
        }
		//删除暂存
		ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
		processTaskStepDataVo.setProcessTaskId(processTaskId);
		processTaskStepDataVo.setProcessTaskStepId(processTaskStepId);
		processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
		processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
		ProcessTaskStepDataVo stepDraftSaveData = processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
        if(stepDraftSaveData != null) {
            JSONObject dataObj = stepDraftSaveData.getData();
            if(MapUtils.isNotEmpty(dataObj)) {
                dataObj.remove("content");
                dataObj.remove("fileIdList");
            }
            if(MapUtils.isNotEmpty(dataObj)) {
                processTaskStepDataMapper.replaceProcessTaskStepData(stepDraftSaveData);
            }else {
                processTaskStepDataMapper.deleteProcessTaskStepData(stepDraftSaveData);             
            }
        }

		String content = jsonObj.getString("content");        
        List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("fileIdList")), Long.class);
        if(StringUtils.isBlank(content) && CollectionUtils.isEmpty(fileIdList)) {
            return null;
        }

        ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo();
        processTaskStepContentVo.setProcessTaskId(processTaskId);
        processTaskStepContentVo.setProcessTaskStepId(processTaskStepId);
        processTaskStepContentVo.setType(ProcessTaskOperationType.COMMENT.getValue());
        if (StringUtils.isNotBlank(content)) {
            ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
            processTaskMapper.replaceProcessTaskContent(contentVo);
            processTaskStepContentVo.setContentHash(contentVo.getHash());
        }
        processTaskMapper.insertProcessTaskStepContent(processTaskStepContentVo);

        /** 保存附件uuid **/
        if(CollectionUtils.isNotEmpty(fileIdList)) {
            ProcessTaskStepFileVo processTaskStepFileVo = new ProcessTaskStepFileVo();
            processTaskStepFileVo.setProcessTaskId(processTaskId);
            processTaskStepFileVo.setProcessTaskStepId(processTaskStepId);
            processTaskStepFileVo.setContentId(processTaskStepContentVo.getId());
            for (Long fileId : fileIdList) {
                if(fileMapper.getFileById(fileId) == null) {
                    throw new FileNotFoundException(fileId);
                }
                processTaskStepFileVo.setFileId(fileId);
                processTaskMapper.insertProcessTaskStepFile(processTaskStepFileVo);
            }
        }
        /** 记录回复模版使用次数 */
        if(commentTemplateId != null){
            ProcessCommentTemplateUseCountVo templateUseCount = commentTemplateMapper.getTemplateUseCount(commentTemplateId,UserContext.get().getUserUuid());
            if(templateUseCount != null){
                commentTemplateMapper.updateTemplateUseCount(commentTemplateId,UserContext.get().getUserUuid());
            }else{
                commentTemplateMapper.insertTemplateUseCount(commentTemplateId,UserContext.get().getUserUuid());
            }
        }
        
        //生成活动    
        processTaskStepVo.setParamObj(jsonObj);
        handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.COMMENT);
        
		JSONObject resultObj = new JSONObject();
        List<String> typeList = new ArrayList<>();
        typeList.add(ProcessTaskOperationType.COMMENT.getValue());
        typeList.add(ProcessTaskOperationType.COMPLETE.getValue());
        typeList.add(ProcessTaskOperationType.BACK.getValue());
        typeList.add(ProcessTaskOperationType.RETREAT.getValue());
        typeList.add(ProcessTaskOperationType.TRANSFER.getValue());
		resultObj.put("commentList", processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(processTaskStepId, typeList));
		return resultObj;
	}

}
