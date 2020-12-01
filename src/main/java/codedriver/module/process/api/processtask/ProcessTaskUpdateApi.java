package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTagVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepReplyVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskTagVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.priority.PriorityNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskUpdateApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private PriorityMapper priorityMapper;
	
	@Autowired
    private ProcessMapper processMapper;
    
    @Autowired
    private ProcessTaskService processTaskService;

	@Override
	public String getToken() {
		return "processtask/update";
	}

	@Override
	public String getName() {
		return "更新工单信息";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "步骤id"),
		@Param(name = "title", type = ApiParamType.STRING, xss = true, maxLength = 80, desc = "标题"),
		@Param(name = "priorityUuid", type = ApiParamType.STRING, desc = "优先级uuid"),
		@Param(name = "content", type = ApiParamType.STRING, desc = "描述"),
		@Param(name = "tagList", type = ApiParamType.JSONARRAY, desc = "标签列表"),
		@Param(name = "fileIdList", type=ApiParamType.JSONARRAY, desc = "附件id列表")
	})
	@Description(desc = "更新工单信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
		
		//获取开始步骤id
		List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
		if(processTaskStepList.size() != 1) {
			throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
		}
		Long startProcessTaskStepId = processTaskStepList.get(0).getId();
				
		IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler();
        try {
            handler.verifyOperationAuthoriy(processTaskVo, ProcessTaskOperationType.UPDATE, true);
        }catch(ProcessTaskNoPermissionException e) {
            throw new PermissionDeniedException();
        }
		// 锁定当前流程
		processTaskMapper.getProcessTaskLockById(processTaskId);
		
		boolean isUpdate = false;
		String oldTitle = processTaskVo.getTitle();	
		String title = jsonObj.getString("title");
		if(StringUtils.isNotBlank(title)&&!title.equals(oldTitle)) {	
			isUpdate = true;
			processTaskVo.setTitle(title);
			ProcessTaskContentVo oldTitleContentVo = new ProcessTaskContentVo(oldTitle);
			processTaskMapper.replaceProcessTaskContent(oldTitleContentVo);
			jsonObj.put(ProcessTaskAuditDetailType.TITLE.getOldDataParamName(), oldTitleContentVo.getHash());
		}else {
			jsonObj.remove("title");
		}
		

		String priorityUuid = jsonObj.getString("priorityUuid");
		String oldPriorityUuid = processTaskVo.getPriorityUuid();
		if(StringUtils.isNotBlank(priorityUuid)&&!priorityUuid.equals(oldPriorityUuid)) {	
		    if(priorityMapper.checkPriorityIsExists(priorityUuid) == 0) {
	            throw new PriorityNotFoundException(priorityUuid);
	        }
			isUpdate = true;
			processTaskVo.setPriorityUuid(priorityUuid);
			ProcessTaskContentVo oldPriorityUuidContentVo = new ProcessTaskContentVo(oldPriorityUuid);
			processTaskMapper.replaceProcessTaskContent(oldPriorityUuidContentVo);
			jsonObj.put(ProcessTaskAuditDetailType.PRIORITY.getOldDataParamName(), oldPriorityUuidContentVo.getHash());
		}else {
			jsonObj.remove("priorityUuid");
		}
		if(isUpdate) {
			processTaskMapper.updateProcessTaskTitleOwnerPriorityUuid(processTaskVo);
		}
		
		//跟新标签
        List<String> tagNameList = JSONObject.parseArray(JSON.toJSONString(jsonObj.getJSONArray("tagList")), String.class);
        if(tagNameList != null) {
            List<ProcessTagVo> oldTagList = processTaskMapper.getProcessTaskTagListByProcessTaskId(processTaskId);
            processTaskMapper.deleteProcessTaskTagByProcessTaskId(processTaskId);
            if(CollectionUtils.isNotEmpty(tagNameList)) {
                List<ProcessTagVo> existTagList = processMapper.getProcessTagByNameList(tagNameList);
                List<String> existTagNameList = existTagList.stream().map(ProcessTagVo::getName).collect(Collectors.toList());
                List<String> notExistTagList = ListUtils.removeAll(tagNameList, existTagNameList);
                for(String tagName : notExistTagList) {
                    ProcessTagVo tagVo = new ProcessTagVo(tagName);
                    processMapper.insertProcessTag(tagVo);
                    existTagList.add(tagVo);
                }
                List<ProcessTaskTagVo> processTaskTagVoList = new ArrayList<ProcessTaskTagVo>();
                for(ProcessTagVo processTagVo : existTagList) {
                    processTaskTagVoList.add(new ProcessTaskTagVo(processTaskId,processTagVo.getId()));
                }
                processTaskMapper.insertProcessTaskTag(processTaskTagVoList);
            }
            int diffCount = tagNameList.stream().filter(a->!oldTagList.stream().map(b -> b.getName()).collect(Collectors.toList()).contains(a)).collect(Collectors.toList()).size();
            if(tagNameList.size() != oldTagList.size() || diffCount >0) {
                List<String> oldTagNameList = new ArrayList<String>();
                for(ProcessTagVo tag:oldTagList) {
                    oldTagNameList.add(tag.getName());
                }
                ProcessTaskContentVo oldTagContentVo = new ProcessTaskContentVo(String.join(",", oldTagNameList));
                processTaskMapper.replaceProcessTaskContent(oldTagContentVo);
                if(StringUtils.isNotBlank(oldTagContentVo.getHash())) {
                    jsonObj.put(ProcessTaskAuditDetailType.TAGLIST.getOldDataParamName(), oldTagContentVo.getHash());
                    jsonObj.put(ProcessTaskAuditDetailType.TAGLIST.getParamName(), String.join(",", tagNameList));
                }
                isUpdate = true;
            }else {
                jsonObj.remove(ProcessTaskAuditDetailType.TAGLIST.getParamName());
            }
        }     
        
		ProcessTaskStepReplyVo oldReplyVo = null;
        List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(startProcessTaskStepId);
        for(ProcessTaskStepContentVo processTaskStepContent : processTaskStepContentList) {
            if (ProcessTaskOperationType.STARTPROCESS.getValue().equals(processTaskStepContent.getType())) {
                oldReplyVo = new ProcessTaskStepReplyVo(processTaskStepContent);
                break;
            }
        }
        if(oldReplyVo == null) {
            oldReplyVo = new ProcessTaskStepReplyVo();
            oldReplyVo.setProcessTaskId(processTaskId);
            oldReplyVo.setProcessTaskStepId(startProcessTaskStepId);
        }
        Boolean tmpIsUpdate = processTaskService.saveProcessTaskStepReply(jsonObj, oldReplyVo) ;
        if(tmpIsUpdate) {
            isUpdate = true;
        }
        
		//生成活动
		if(isUpdate) {
		    ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
		    processTaskStepVo.setProcessTaskId(processTaskId);
		    processTaskStepVo.setId(processTaskStepId);
			processTaskStepVo.setParamObj(jsonObj);
			handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.UPDATE);
			handler.calculateSla(new ProcessTaskVo(processTaskId), false);
		}
		
		return null;
	}

}
