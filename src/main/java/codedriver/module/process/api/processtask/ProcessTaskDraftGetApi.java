package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ChannelPriorityVo;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.FormAttributeVo;
import codedriver.framework.process.dto.FormVersionVo;
import codedriver.framework.process.dto.ProcessStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
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
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.CatalogService;
import codedriver.module.process.service.ProcessTaskService;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskDraftGetApi extends PrivateApiComponentBase {

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

    @Autowired
    private SelectContentByHashMapper selectContentByHashMapper;
	
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
		@Param(name="copyProcessTaskId", type = ApiParamType.LONG, desc="复制工单id，从复制上报进入上报页时，传copyProcessTaskId"),
		@Param(name="channelUuid", type = ApiParamType.STRING, desc="服务uuid，从服务目录进入上报页时，传channelUuid"),
        @Param(name="fromProcessTaskId", type = ApiParamType.LONG, desc="来源工单id，从转报进入上报页时，传fromProcessTaskId"),
		@Param(name="channelTypeRelationId", type = ApiParamType.LONG, desc="关系类型id，从转报进入上报页时，传channelTypeRelationId")
	})
	@Output({
		@Param(explode = ProcessTaskVo.class, desc = "工单信息")
	})
	@Description(desc = "工单详情数据获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		Long copyProcessTaskId = jsonObj.getLong("copyProcessTaskId");
		String channelUuid = jsonObj.getString("channelUuid");
		if(processTaskId != null) {
		    processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
		    ProcessTaskVo processTaskVo = processTaskService.getProcessTaskDetailById(processTaskId);
	        /** 判断当前用户是否拥有channelUuid服务的上报权限 **/
	        if(!catalogService.channelIsAuthority(processTaskVo.getChannelUuid())){
	            //throw new ProcessTaskNoPermissionException("上报");
	            throw new PermissionDeniedException();
	        }
	        
	        String owner = processTaskVo.getOwner();
	        if(StringUtils.isNotBlank(owner)) {
	            owner = GroupSearch.USER.getValuePlugin() + owner;
	            processTaskVo.setOwner(owner);              
	        }
		    
		    ProcessTaskStepVo startProcessTaskStepVo = getStartProcessTaskStepByProcessTaskId(processTaskId);
			//获取须指派的步骤列表				
			startProcessTaskStepVo.setAssignableWorkerStepList(processTaskService.getAssignableWorkerStepList(startProcessTaskStepVo.getProcessTaskId(), startProcessTaskStepVo.getProcessStepUuid()));
			processTaskVo.setStartProcessTaskStep(startProcessTaskStepVo);
			
			if(StringUtils.isNotBlank(processTaskVo.getFormConfig())) {
			    List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList = processTaskMapper.getProcessTaskStepFormAttributeByProcessTaskStepId(startProcessTaskStepVo.getId());
	            if(CollectionUtils.isNotEmpty(processTaskStepFormAttributeList)) {
	                Map<String, String> formAttributeActionMap = new HashMap<>();
	                for(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo : processTaskStepFormAttributeList) {
	                    formAttributeActionMap.put(processTaskStepFormAttributeVo.getAttributeUuid(), processTaskStepFormAttributeVo.getAction());
	                }
	                processTaskService.setProcessTaskFormAttributeAction(processTaskVo, formAttributeActionMap, 1);
	                startProcessTaskStepVo.setStepFormConfig(processTaskStepFormAttributeList);
	            }
			}
			
			return processTaskVo;
		}else if(copyProcessTaskId != null){
		    ProcessTaskVo oldProcessTaskVo = processTaskService.checkProcessTaskParamsIsLegal(copyProcessTaskId);
            /** 判断当前用户是否拥有channelUuid服务的上报权限 **/
            if(!catalogService.channelIsAuthority(oldProcessTaskVo.getChannelUuid())){
                //throw new ProcessTaskNoPermissionException("上报");
                throw new PermissionDeniedException();
            }
            //获取旧工单表单信息
            ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(copyProcessTaskId);
            if(processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContentHash())) {
                String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
                if(StringUtils.isNotBlank(formContent)) {
                    oldProcessTaskVo.setFormConfig(formContent);            
                    List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(copyProcessTaskId);
                    for(ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
                        oldProcessTaskVo.getFormAttributeDataMap().put(processTaskFormAttributeDataVo.getAttributeUuid(), processTaskFormAttributeDataVo.getDataObj());
                    }
                }
            }
            
            ChannelVo channel = channelMapper.getChannelByUuid(oldProcessTaskVo.getChannelUuid());
            ChannelTypeVo channelTypeVo = channelMapper.getChannelTypeByUuid(channel.getChannelTypeUuid());
            ProcessVo processVo = processMapper.getProcessByUuid(channel.getProcessUuid());
            ProcessTaskVo processTaskVo = new ProcessTaskVo();
            processTaskVo.setIsAutoGenerateId(false);
            processTaskVo.setChannelType(new ChannelTypeVo(channelTypeVo));
            processTaskVo.setChannelUuid(oldProcessTaskVo.getChannelUuid());
            processTaskVo.setProcessUuid(channel.getProcessUuid());
            processTaskVo.setWorktimeUuid(channel.getWorktimeUuid());
            processTaskVo.setConfig(processVo.getConfig());
            processTaskVo.setTitle(oldProcessTaskVo.getTitle());
            List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(oldProcessTaskVo.getChannelUuid());
            for(ChannelPriorityVo channelPriority : channelPriorityList) {
                if(oldProcessTaskVo.getPriorityUuid().equals(channelPriority.getPriorityUuid())) {
                    processTaskVo.setPriorityUuid(channelPriority.getPriorityUuid());
                    break;
                }else if(channelPriority.getIsDefault().intValue() == 1) {
                    processTaskVo.setPriorityUuid(channelPriority.getPriorityUuid());
                }
            }
            
            ProcessTaskStepVo oldStartProcessTaskStepVo = getStartProcessTaskStepByProcessTaskId(copyProcessTaskId);
            
            ProcessStepVo processStepVo = new ProcessStepVo();
            processStepVo.setProcessUuid(channel.getProcessUuid());
            processStepVo.setType(ProcessStepType.START.getValue());
            List<ProcessStepVo> processStepList = processMapper.searchProcessStep(processStepVo);
            if(processStepList.size() != 1) {
                throw new ProcessTaskRuntimeException("流程：'" + channel.getProcessUuid() + "'有" + processStepList.size() + "个开始步骤");
            }
            ProcessTaskStepVo startProcessTaskStepVo = new ProcessTaskStepVo(processStepList.get(0));
            startProcessTaskStepVo.setIsAutoGenerateId(false);
            startProcessTaskStepVo.setComment(oldStartProcessTaskStepVo.getComment());
            startProcessTaskStepVo.setHandlerStepInfo(oldStartProcessTaskStepVo.getHandlerStepInfo());
            //获取须指派的步骤列表    
            startProcessTaskStepVo.setAssignableWorkerStepList(processTaskService.getAssignableWorkerStepList(channel.getProcessUuid(), startProcessTaskStepVo.getProcessStepUuid()));
            
            processTaskVo.setStartProcessTaskStep(startProcessTaskStepVo);
            
            if(StringUtils.isNotBlank(processVo.getFormUuid())) {
                FormVersionVo formVersion = formMapper.getActionFormVersionByFormUuid(processVo.getFormUuid());
                if(formVersion == null) {
                    throw new FormActiveVersionNotFoundExcepiton(processVo.getFormUuid());
                }
                processTaskVo.setFormConfig(formVersion.getFormConfig());
                if(StringUtils.isNotBlank(oldProcessTaskVo.getFormConfig())) {
                    transferFormAttributeValue(oldProcessTaskVo, processTaskVo);
                }
                List<ProcessStepFormAttributeVo> processStepFormAttributeList = processMapper.getProcessStepFormAttributeByStepUuid(startProcessTaskStepVo.getProcessStepUuid());
                if(CollectionUtils.isNotEmpty(processStepFormAttributeList)) {
                    Map<String, String> formAttributeActionMap = new HashMap<>();
                    List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList = new ArrayList<>();
                    for(ProcessStepFormAttributeVo processStepFormAttribute : processStepFormAttributeList) {
                        formAttributeActionMap.put(processStepFormAttribute.getAttributeUuid(), processStepFormAttribute.getAction());
                        processTaskStepFormAttributeList.add(new ProcessTaskStepFormAttributeVo(processStepFormAttribute));
                    }
                    processTaskService.setProcessTaskFormAttributeAction(processTaskVo, formAttributeActionMap, 1);
                    startProcessTaskStepVo.setStepFormConfig(processTaskStepFormAttributeList);
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
				//throw new ProcessTaskNoPermissionException("上报");
				throw new PermissionDeniedException();
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
			processTaskVo.setChannelType(new ChannelTypeVo(channelTypeVo));
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
			
			//获取须指派的步骤列表 	
			startProcessTaskStepVo.setAssignableWorkerStepList(processTaskService.getAssignableWorkerStepList(channel.getProcessUuid(), startProcessTaskStepVo.getProcessStepUuid()));
			
			processTaskVo.setStartProcessTaskStep(startProcessTaskStepVo);

			Long fromProcessTaskId = jsonObj.getLong("fromProcessTaskId");
			if(fromProcessTaskId != null) {
			    processTaskVo.getTranferReportProcessTaskList().add(processTaskService.getFromProcessTasById(fromProcessTaskId));
			}
			if(StringUtils.isNotBlank(processVo.getFormUuid())) {
				FormVersionVo formVersion = formMapper.getActionFormVersionByFormUuid(processVo.getFormUuid());
				if(formVersion == null) {
					throw new FormActiveVersionNotFoundExcepiton(processVo.getFormUuid());
				}
				processTaskVo.setFormConfig(formVersion.getFormConfig());
				if(CollectionUtils.isNotEmpty(processTaskVo.getTranferReportProcessTaskList()) && StringUtils.isNotBlank(processTaskVo.getTranferReportProcessTaskList().get(0).getFormConfig())) {
				    transferFormAttributeValue(processTaskVo.getTranferReportProcessTaskList().get(0), processTaskVo);
				}
				List<ProcessStepFormAttributeVo> processStepFormAttributeList = processMapper.getProcessStepFormAttributeByStepUuid(startProcessTaskStepVo.getProcessStepUuid());
				if(CollectionUtils.isNotEmpty(processStepFormAttributeList)) {
					Map<String, String> formAttributeActionMap = new HashMap<>();
					List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList = new ArrayList<>();
					for(ProcessStepFormAttributeVo processStepFormAttribute : processStepFormAttributeList) {
						formAttributeActionMap.put(processStepFormAttribute.getAttributeUuid(), processStepFormAttribute.getAction());
						processTaskStepFormAttributeList.add(new ProcessTaskStepFormAttributeVo(processStepFormAttribute));
					}
					processTaskService.setProcessTaskFormAttributeAction(processTaskVo, formAttributeActionMap, 1);
					startProcessTaskStepVo.setStepFormConfig(processTaskStepFormAttributeList);
				}
			}
			return processTaskVo;
		}else {
			throw new ProcessTaskRuntimeException("参数'processTaskId'、'copyProcessTaskId'和'channelUuid'，至少要传一个");
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

	private void transferFormAttributeValue(ProcessTaskVo fromProcessTaskVo, ProcessTaskVo toProcessTaskVo) {
	    Map<String, String> labelUuidMap = new HashMap<>();
	    FormVersionVo fromFormVersion = new FormVersionVo();
	    fromFormVersion.setFormConfig(fromProcessTaskVo.getFormConfig());
	    List<FormAttributeVo> fromFormAttributeList = fromFormVersion.getFormAttributeList();
	    for(FormAttributeVo formAttributeVo : fromFormAttributeList) {
	        labelUuidMap.put(formAttributeVo.getLabel(), formAttributeVo.getUuid());
	    }
	    Map<String, Object> formAttributeDataMap = fromProcessTaskVo.getFormAttributeDataMap();
	    FormVersionVo toFormVersion = new FormVersionVo();
	    toFormVersion.setFormConfig(toProcessTaskVo.getFormConfig());
	    for(FormAttributeVo formAttributeVo : toFormVersion.getFormAttributeList()) {
	        String fromFormAttributeUuid = labelUuidMap.get(formAttributeVo.getLabel());
	        if(StringUtils.isNotBlank(fromFormAttributeUuid)) {
	            Object data = formAttributeDataMap.get(fromFormAttributeUuid);
	            if(data != null) {
	                toProcessTaskVo.getFormAttributeDataMap().put(formAttributeVo.getUuid(), data);
	            }
	        }
	    }
	}
}
