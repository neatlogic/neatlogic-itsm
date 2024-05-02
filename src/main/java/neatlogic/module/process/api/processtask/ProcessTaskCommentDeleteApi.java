package neatlogic.module.process.api.processtask;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskAuditDetailType;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.crossover.IProcessStepHandlerCrossoverUtil;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskStepContentVo;
import neatlogic.framework.process.dto.ProcessTaskStepReplyVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskStepCommentNotFoundException;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
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
	private IProcessStepHandlerUtil processStepHandlerUtil;

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
		@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "回复id"),
			@Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源")
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
        processTaskStepVo.getParamObj().putAll(jsonObj);
		processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.DELETECOMMENT);
        
        JSONObject resultObj = new JSONObject();
        List<String> typeList = new ArrayList<>();
        typeList.add(ProcessTaskOperationType.STEP_COMMENT.getValue());
        typeList.add(ProcessTaskOperationType.STEP_COMPLETE.getValue());
        typeList.add(ProcessTaskOperationType.STEP_BACK.getValue());
        typeList.add(ProcessTaskOperationType.PROCESSTASK_RETREAT.getValue());
        typeList.add(ProcessTaskOperationType.PROCESSTASK_TRANSFER.getValue());
		typeList.add(ProcessTaskOperationType.STEP_REAPPROVAL.getValue());
		typeList.add(ProcessTaskOperationType.PROCESSTASK_START.getValue());
        resultObj.put("commentList", processTaskService.getProcessTaskStepReplyListByProcessTaskStepId(replyVo.getProcessTaskStepId(), typeList));
        return resultObj;
	}

}
