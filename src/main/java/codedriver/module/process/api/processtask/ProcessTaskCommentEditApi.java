package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepReplyVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepCommentNotFoundException;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskCommentEditApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;

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
            throw new ProcessTaskNoPermissionException(ProcessTaskOperationType.EDITCOMMENT.getText());	
		}
		// 锁定当前流程
        processTaskMapper.getProcessTaskLockById(oldReplyVo.getProcessTaskId());
        
        boolean isUpdate = processTaskService.saveProcessTaskStepReply(jsonObj, oldReplyVo);
        if(isUpdate) {
            //生成活动
            ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
            processTaskStepVo.setProcessTaskId(oldReplyVo.getProcessTaskId());
            processTaskStepVo.setId(oldReplyVo.getProcessTaskStepId());
            processTaskStepVo.setParamObj(jsonObj);
            ProcessStepUtilHandlerFactory.getHandler().activityAudit(processTaskStepVo, ProcessTaskAuditType.EDITCOMMENT);
        }
        
        JSONObject resultObj = new JSONObject();
        List<String> typeList = new ArrayList<>();
        typeList.add(ProcessTaskOperationType.COMMENT.getValue());
        typeList.add(ProcessTaskOperationType.COMPLETE.getValue());
        typeList.add(ProcessTaskOperationType.BACK.getValue());
        typeList.add(ProcessTaskOperationType.RETREAT.getValue());
        typeList.add(ProcessTaskOperationType.TRANSFER.getValue());
        resultObj.put("commentList", processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(oldReplyVo.getProcessTaskStepId(), typeList));
        return resultObj;
	}
}
