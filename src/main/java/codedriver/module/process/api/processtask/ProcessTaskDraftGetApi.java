package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.WorkerPolicy;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ChannelPriorityVo;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.FormVersionVo;
import codedriver.framework.process.dto.ProcessStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.channeltype.ChannelTypeNotFoundException;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.form.FormActiveVersionNotFoundExcepiton;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.CatalogService;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskDraftGetApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private ProcessMapper processMapper;
	
	@Autowired
	private FormMapper formMapper;
	
	@Autowired
	private CatalogService catalogService;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Override
	public String getToken() {
		return "processtask/draft/get";
	}

	@Override
	public String getName() {
		return "工单草稿数据获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name="processTaskId", type = ApiParamType.LONG, desc="工单id，从工单中心进入上报页时，传processTaskId"),
		@Param(name="channelUuid", type = ApiParamType.STRING, desc="服务uuid，从服务目录进入上报页时，传channelUuid")
	})
	@Output({
		@Param(explode = ProcessTaskVo.class, desc = "工单信息")
	})
	@Description(desc = "工单详情数据获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		String channelUuid = jsonObj.getString("channelUuid");
		if(processTaskId != null) {
		    processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
		    ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(processTaskId);
	        ChannelVo channel = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
	        if(channel == null) {
	            throw new ChannelNotFoundException(processTaskVo.getChannelUuid());
	        }
	        /** 判断当前用户是否拥有channelUuid服务的上报权限 **/
	        if(!catalogService.channelIsAuthority(processTaskVo.getChannelUuid())){
	            throw new ProcessTaskNoPermissionException("上报");
	        }

	        ChannelTypeVo channelTypeVo = channelMapper.getChannelTypeByUuid(channel.getChannelTypeUuid());
	        if(channelTypeVo == null) {
	            throw new ChannelTypeNotFoundException(channel.getChannelTypeUuid());
	        }
	        
	        String owner = processTaskVo.getOwner();
	        if(StringUtils.isNotBlank(owner)) {
	            owner = GroupSearch.USER.getValuePlugin() + owner;
	            processTaskVo.setOwner(owner);              
	        }
		    
		    ProcessTaskStepVo startProcessTaskStepVo = getStartProcessTaskStepByProcessTaskId(processTaskId);
			//获取可分配处理人的步骤列表				
			startProcessTaskStepVo.setAssignableWorkerStepList(processTaskService.getAssignableWorkerStepListByProcessTaskIdAndProcessStepUuid(startProcessTaskStepVo.getProcessTaskId(), startProcessTaskStepVo.getProcessStepUuid()));
			processTaskVo.setStartProcessTaskStep(startProcessTaskStepVo);
			
			if(StringUtils.isNotBlank(processTaskVo.getFormConfig())) {
			    List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList = processTaskMapper.getProcessTaskStepFormAttributeByProcessTaskStepId(startProcessTaskStepVo.getId());
	            if(CollectionUtils.isNotEmpty(processTaskStepFormAttributeList)) {
	                Map<String, String> formAttributeActionMap = new HashMap<>();
	                for(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo : processTaskStepFormAttributeList) {
	                    formAttributeActionMap.put(processTaskStepFormAttributeVo.getAttributeUuid(), processTaskStepFormAttributeVo.getAction());
	                }
	                processTaskService.setProcessTaskFormAttributeAction(processTaskVo, formAttributeActionMap, 1);
	            }
			}
			return processTaskVo;
		}else if(channelUuid != null){
			ChannelVo channel = channelMapper.getChannelByUuid(channelUuid);
			if(channel == null) {
				throw new ChannelNotFoundException(channelUuid);
			}
			/** 判断当前用户是否拥有channelUuid服务的上报权限 **/
			if(!catalogService.channelIsAuthority(channelUuid)){
				throw new ProcessTaskNoPermissionException("上报");
			}
			ChannelTypeVo channelTypeVo = channelMapper.getChannelTypeByUuid(channel.getChannelTypeUuid());
			if(channelTypeVo == null) {
				throw new ChannelTypeNotFoundException(channel.getChannelTypeUuid());
			}

			ProcessVo processVo = processMapper.getProcessByUuid(channel.getProcessUuid());
			if(processVo == null) {
				throw new ProcessNotFoundException(channel.getProcessUuid());
			}
			ProcessTaskVo processTaskVo = new ProcessTaskVo();
			processTaskVo.setIsAutoGenerateId(false);
			processTaskVo.setChannelType(channelTypeVo);
			processTaskVo.setChannelUuid(channelUuid);
			processTaskVo.setProcessUuid(channel.getProcessUuid());
			processTaskVo.setWorktimeUuid(channel.getWorktimeUuid());
			List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(channelUuid);
			for(ChannelPriorityVo channelPriority : channelPriorityList) {
				if(channelPriority.getIsDefault().intValue() == 1) {
					processTaskVo.setPriorityUuid(channelPriority.getPriorityUuid());
				}
			}
			processTaskVo.setConfig(processVo.getConfig());
			
			ProcessStepVo processStepVo = new ProcessStepVo();
			processStepVo.setProcessUuid(channel.getProcessUuid());
			processStepVo.setType(ProcessStepType.START.getValue());
			List<ProcessStepVo> processStepList = processMapper.searchProcessStep(processStepVo);
			if(processStepList.size() != 1) {
				throw new ProcessTaskRuntimeException("流程：'" + channel.getProcessUuid() + "'有" + processStepList.size() + "个开始步骤");
			}
			ProcessTaskStepVo startProcessTaskStepVo = new ProcessTaskStepVo(processStepList.get(0));
			startProcessTaskStepVo.setIsAutoGenerateId(false);
			
			//获取可分配处理人的步骤列表	
			List<ProcessStepWorkerPolicyVo> processStepWorkerPolicyList = processMapper.getProcessStepWorkerPolicyListByProcessUuid(channel.getProcessUuid());
			if(CollectionUtils.isNotEmpty(processStepWorkerPolicyList)) {
				List<ProcessTaskStepVo> assignableWorkerStepList = new ArrayList<>();
				for(ProcessStepWorkerPolicyVo workerPolicyVo : processStepWorkerPolicyList) {
					if(WorkerPolicy.PRESTEPASSIGN.getValue().equals(workerPolicyVo.getPolicy())) {
						List<String> processStepUuidList = JSON.parseArray(workerPolicyVo.getConfigObj().getString("processStepUuidList"), String.class);
						for(String processStepUuid : processStepUuidList) {
							if(startProcessTaskStepVo.getProcessStepUuid().equals(processStepUuid)) {
								ProcessStepVo processStep = processMapper.getProcessStepByUuid(workerPolicyVo.getProcessStepUuid());
								ProcessTaskStepVo assignableWorkerStep = new ProcessTaskStepVo(processStep);
								assignableWorkerStep.setIsAutoGenerateId(false);
								assignableWorkerStep.setIsRequired(workerPolicyVo.getConfigObj().getInteger("isRequired"));
								assignableWorkerStepList.add(assignableWorkerStep);
							}
						}
					}
				}
				startProcessTaskStepVo.setAssignableWorkerStepList(assignableWorkerStepList);
			}
			
			processTaskVo.setStartProcessTaskStep(startProcessTaskStepVo);

			if(StringUtils.isNotBlank(processVo.getFormUuid())) {
				FormVersionVo formVersion = formMapper.getActionFormVersionByFormUuid(processVo.getFormUuid());
				if(formVersion == null) {
					throw new FormActiveVersionNotFoundExcepiton(processVo.getFormUuid());
				}
				processTaskVo.setFormConfig(formVersion.getFormConfig());
				
				List<ProcessStepFormAttributeVo> processStepFormAttributeList = processMapper.getProcessStepFormAttributeByStepUuid(startProcessTaskStepVo.getProcessStepUuid());
				if(CollectionUtils.isNotEmpty(processStepFormAttributeList)) {
					Map<String, String> formAttributeActionMap = new HashMap<>();
					for(ProcessStepFormAttributeVo processStepFormAttribute : processStepFormAttributeList) {
						formAttributeActionMap.put(processStepFormAttribute.getAttributeUuid(), processStepFormAttribute.getAction());
					}
					processTaskService.setProcessTaskFormAttributeAction(processTaskVo, formAttributeActionMap, 1);
				}
			}
			return processTaskVo;
		}else {
			throw new ProcessTaskRuntimeException("参数'processTaskId'和'channelUuid'，至少要传一个");
		}
	}
	
	private ProcessTaskStepVo getStartProcessTaskStepByProcessTaskId(Long processTaskId) {
        //获取开始步骤id
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
        if(processTaskStepList.size() != 1) {
            throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
        }

        ProcessTaskStepVo startProcessTaskStepVo = processTaskStepList.get(0);
        processTaskService.setProcessTaskStepConfig(startProcessTaskStepVo);

        startProcessTaskStepVo.setComment(processTaskService.getProcessTaskStepContentAndFileByProcessTaskStepIdId(startProcessTaskStepVo.getId()));
        /** 当前步骤特有步骤信息 **/
        IProcessStepUtilHandler startProcessStepUtilHandler = ProcessStepUtilHandlerFactory.getHandler(startProcessTaskStepVo.getHandler());
        if(startProcessStepUtilHandler == null) {
            throw new ProcessStepHandlerNotFoundException(startProcessTaskStepVo.getHandler());
        }
        startProcessTaskStepVo.setHandlerStepInfo(startProcessStepUtilHandler.getHandlerStepInitInfo(startProcessTaskStepVo));
        return startProcessTaskStepVo;
    }
}
