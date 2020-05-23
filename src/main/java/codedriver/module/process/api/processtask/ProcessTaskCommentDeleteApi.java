package codedriver.module.process.api.processtask;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
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
public class ProcessTaskCommentDeleteApi extends ApiComponentBase {

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
		@Param(name = "commentList", explode = ProcessTaskStepCommentVo[].class, desc = "当前步骤评论列表")
	})
	@Description(desc = "工单回复删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		ProcessTaskStepCommentVo oldCommentVo = processTaskMapper.getProcessTaskStepCommentById(id);
		if(oldCommentVo == null) {
			throw new ProcessTaskStepCommentNotFoundException(id.toString());
		}
		if(Objects.equals(oldCommentVo.getIsDeletable(), 1)) {
			// 锁定当前流程
			processTaskMapper.getProcessTaskLockById(oldCommentVo.getProcessTaskId());
			processTaskMapper.deleteProcessTaskStepCommentById(id);
			
			processTaskService.parseProcessTaskStepComment(oldCommentVo);
			jsonObj.put(ProcessTaskAuditDetailType.CONTENT.getParamName(), oldCommentVo.getContentHash());
			jsonObj.put(ProcessTaskAuditDetailType.FILE.getParamName(), JSON.toJSONString(oldCommentVo.getFileUuidList()));
			
			//生成活动
			ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(oldCommentVo.getProcessTaskStepId());	
			processTaskStepVo.setParamObj(jsonObj);
			ProcessStepHandlerFactory.getHandler().activityAudit(processTaskStepVo, ProcessTaskStepAction.DELETECOMMENT);
			
			List<ProcessTaskStepCommentVo> processTaskStepCommentList = processTaskMapper.getProcessTaskStepCommentListByProcessTaskStepId(oldCommentVo.getProcessTaskStepId());
			for(ProcessTaskStepCommentVo processTaskStepComment : processTaskStepCommentList) {
				processTaskService.parseProcessTaskStepComment(processTaskStepComment);
			}
			JSONObject resultObj = new JSONObject();
			resultObj.put("commentList", processTaskStepCommentList);
			return resultObj;
		}else {
			throw new ProcessTaskNoPermissionException(ProcessTaskStepAction.DELETECOMMENT.getText());
		}
	}

}
