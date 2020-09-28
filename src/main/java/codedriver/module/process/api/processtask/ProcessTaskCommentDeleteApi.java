package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepReplyVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.processtask.ProcessTaskStepCommentNotFoundException;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.module.process.service.ProcessTaskService;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class ProcessTaskCommentDeleteApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;

	@Override
	public String getToken() {
		return "processtask/comment/delete";
	}

	@Override
	public String getName() {
		return "工单回复删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "回复id")
	})
	@Output({
		@Param(name = "commentList", explode = ProcessTaskStepReplyVo[].class, desc = "当前步骤评论列表")
	})
	@Description(desc = "工单回复删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		ProcessTaskStepContentVo processTaskStepContentVo= processTaskMapper.getProcessTaskStepContentById(id);
		if(processTaskStepContentVo == null) {
			throw new ProcessTaskStepCommentNotFoundException(id.toString());
		}
		ProcessTaskStepReplyVo replyVo = new ProcessTaskStepReplyVo(processTaskStepContentVo);
		if(Objects.equals(replyVo.getIsDeletable(), 0)) {
            // throw new ProcessTaskNoPermissionException(ProcessTaskOperationType.DELETECOMMENT.getText());
		    throw new PermissionDeniedException();
		}
		// 锁定当前流程
        processTaskMapper.getProcessTaskLockById(replyVo.getProcessTaskId());
        
        processTaskService.parseProcessTaskStepReply(replyVo);
        jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), replyVo.getContent());
        jsonObj.put(ProcessTaskAuditDetailType.FILE.getParamName(), JSON.toJSONString(replyVo.getFileIdList()));

        processTaskMapper.deleteProcessTaskStepContentById(id);
        processTaskMapper.deleteProcessTaskStepFileByContentId(id);
        //生成活动
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(replyVo.getProcessTaskStepId());    
        processTaskStepVo.setParamObj(jsonObj);
        ProcessStepUtilHandlerFactory.getHandler().activityAudit(processTaskStepVo, ProcessTaskAuditType.DELETECOMMENT);
        
        JSONObject resultObj = new JSONObject();
        List<String> typeList = new ArrayList<>();
        typeList.add(ProcessTaskOperationType.COMMENT.getValue());
        typeList.add(ProcessTaskOperationType.COMPLETE.getValue());
        typeList.add(ProcessTaskOperationType.BACK.getValue());
        typeList.add(ProcessTaskOperationType.RETREAT.getValue());
        typeList.add(ProcessTaskOperationType.TRANSFER.getValue());
        resultObj.put("commentList", processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(replyVo.getProcessTaskStepId(), typeList));
        return resultObj;
	}

}
