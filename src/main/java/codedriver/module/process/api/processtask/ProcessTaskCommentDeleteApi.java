package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepReplyVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.processtask.ProcessTaskStepCommentNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandlerUtil;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskCommentDeleteApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;

	@Autowired
	private IProcessStepHandlerUtil IProcessStepHandlerUtil;

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
		IProcessStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.DELETECOMMENT);
        
        JSONObject resultObj = new JSONObject();
        List<String> typeList = new ArrayList<>();
        typeList.add(ProcessTaskOperationType.STEP_COMMENT.getValue());
        typeList.add(ProcessTaskOperationType.STEP_COMPLETE.getValue());
        typeList.add(ProcessTaskOperationType.STEP_BACK.getValue());
        typeList.add(ProcessTaskOperationType.STEP_RETREAT.getValue());
        typeList.add(ProcessTaskOperationType.STEP_TRANSFER.getValue());
        resultObj.put("commentList", processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(replyVo.getProcessTaskStepId(), typeList));
        return resultObj;
	}

}
