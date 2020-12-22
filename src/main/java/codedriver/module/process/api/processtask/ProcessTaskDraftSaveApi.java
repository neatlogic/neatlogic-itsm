package codedriver.module.process.api.processtask;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.NO_AUTH;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.CatalogService;
import codedriver.module.process.service.ProcessTaskService;
@Service
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = NO_AUTH.class)
public class ProcessTaskDraftSaveApi extends PrivateApiComponentBase  {

	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessTaskService processTaskService;
	
	@Autowired
	private ProcessMapper processMapper;
	
	@Autowired
    private ProcessTaskStepDataMapper processTaskStepDataMapper;
    
    @Autowired
    private CatalogService catalogService;
	
	@Override
	public String getToken() {
		return "processtask/draft/save";
	}

	@Override
	public String getName() {
		return "工单上报暂存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
//	"formAttributeDataList": [
//  		{
//  			"attributeUuid": "属性uuid",
//  			"handler": "formselect",
//  			"dataList": [
//  				"value"
//  			]
//  		}
//  	]                			
	@Input({
		@Param(name="processTaskId", type = ApiParamType.LONG, desc="工单id"),
		@Param(name="channelUuid", type= ApiParamType.STRING, isRequired=true, desc="服务uuid"),
		@Param(name="title", type=ApiParamType.STRING, maxLength = 80, desc = "标题"),
		@Param(name="owner", type=ApiParamType.STRING, desc="请求人"),
		@Param(name="priorityUuid", type=ApiParamType.STRING, desc="优先级uuid"),
		@Param(name="formAttributeDataList", type = ApiParamType.JSONARRAY, desc = "表单属性数据列表"),
		@Param(name="hidecomponentList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "隐藏表单属性列表"),
		@Param(name="readcomponentList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "只读表单属性列表"),
		@Param(name="content", type=ApiParamType.STRING, desc = "描述"),
		@Param(name="fileIdList", type=ApiParamType.JSONARRAY, desc = "附件id列表"),
		@Param(name="tagList", type=ApiParamType.JSONARRAY, desc = "标签列表"),
		@Param(name="handlerStepInfo", type=ApiParamType.JSONOBJECT, desc="处理器特有的步骤信息"),
        @Param(name="fromProcessTaskId", type = ApiParamType.LONG, desc="来源工单id，从转报进入上报页时，传fromProcessTaskId"),
        @Param(name="channelTypeRelationId", type = ApiParamType.LONG, desc="关系类型id，从转报进入上报页时，传channelTypeRelationId")
	})
	@Output({
		@Param(name="processTaskId", type = ApiParamType.LONG, desc="工单id"),
		@Param(name="processTaskStepId", type = ApiParamType.LONG, desc="步骤id")
	})
	@Description(desc = "工单上报暂存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String channelUuid = jsonObj.getString("channelUuid");
		if(channelMapper.checkChannelIsExists(channelUuid) == 0) {
			throw new ChannelNotFoundException(channelUuid);
		}
		String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
		if(processMapper.checkProcessIsExists(processUuid) == 0) {
			throw new ProcessNotFoundException(processUuid);
		}
		String owner = jsonObj.getString("owner");
		if(StringUtils.isNotBlank(owner) && owner.contains("#")) {
			owner = owner.split("#")[1];
			jsonObj.put("owner", owner);
		}
		ProcessTaskStepVo startTaskStep = new ProcessTaskStepVo();
		startTaskStep.setProcessUuid(processUuid);
		
		Long processTaskId = jsonObj.getLong("processTaskId");
		if(processTaskId != null) {
			processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
			List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
			if(processTaskStepList.size() != 1) {
				throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
			}
			startTaskStep = processTaskStepList.get(0);
		}else {
            /** 判断当前用户是否拥有channelUuid服务的上报权限 **/
            if(!catalogService.channelIsAuthority(channelUuid, UserContext.get().getUserUuid(true))){
                //throw new ProcessTaskNoPermissionException("上报");
                throw new PermissionDeniedException();
            }
			List<ProcessStepVo> processStepList = processMapper.getProcessStepDetailByProcessUuid(processUuid);
			if(CollectionUtils.isNotEmpty(processStepList)) {
				for(ProcessStepVo processStepVo : processStepList) {
					if(processStepVo.getType().equals(ProcessStepType.START.getValue())) {
						startTaskStep.setHandler(processStepVo.getHandler());
					}
				}
			}
		}
		
		IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(startTaskStep.getHandler());
		if(handler == null) {
			throw new ProcessStepHandlerNotFoundException(startTaskStep.getHandler());
		}

        startTaskStep.setParamObj(jsonObj);
        handler.saveDraft(startTaskStep);
        if(processTaskMapper.checkProcessTaskhasForm(startTaskStep.getProcessTaskId()) > 0) {
            // 保存组件联动导致隐藏的属性uuid列表
            ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
            processTaskStepDataVo.setProcessTaskId(startTaskStep.getProcessTaskId());
            processTaskStepDataVo.setProcessTaskStepId(startTaskStep.getId());
            processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
            processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
            processTaskStepDataMapper.deleteProcessTaskStepData(processTaskStepDataVo);
            processTaskStepDataVo.setData(jsonObj.toJSONString());
            processTaskStepDataVo.setIsAutoGenerateId(true);
            processTaskStepDataMapper.replaceProcessTaskStepData(processTaskStepDataVo);
        }
		JSONObject resultObj = new JSONObject();
		resultObj.put("processTaskId", startTaskStep.getProcessTaskId());
		resultObj.put("processTaskStepId", startTaskStep.getId());
		return resultObj;
	}

}
