package codedriver.module.process.api.processtask;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class ProcessTaskDraftSaveApi extends ApiComponentBase  {

	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessMapper processMapper;
	
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
		@Param(name="title", type=ApiParamType.REGEX, rule="^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", desc = "标题"),
		@Param(name="owner", type=ApiParamType.STRING, desc="请求人"),
		@Param(name="priorityUuid", type=ApiParamType.STRING, desc="优先级uuid"),
		@Param(name="formAttributeDataList", type = ApiParamType.JSONARRAY, desc = "表单属性数据列表"),
		@Param(name="content", type=ApiParamType.STRING, xss = true, desc = "描述"),
		@Param(name="fileUuidList", type=ApiParamType.JSONARRAY, desc = "附件uuid列表")
	})
	@Output({
		@Param(name="Return", type = ApiParamType.LONG, desc="工单id")
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
		
		ProcessTaskStepVo startTaskStep = new ProcessTaskStepVo();
		startTaskStep.setProcessUuid(processUuid);
		
		Long processTaskId = jsonObj.getLong("processTaskId");
		if(processTaskId != null) {
			ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
			if(processTaskVo == null) {
				throw new ProcessTaskNotFoundException(processTaskId.toString());
			}
			List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
			if(processTaskStepList.size() != 1) {
				throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
			}
			startTaskStep = processTaskStepList.get(0);
		}else {
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
		if(handler != null) {
			startTaskStep.setParamObj(jsonObj);
			handler.saveDraft(startTaskStep);
		}else {
			throw new ProcessStepHandlerNotFoundException(startTaskStep.getHandler());
		}
		
		return startTaskStep.getProcessTaskId();
	}

}
