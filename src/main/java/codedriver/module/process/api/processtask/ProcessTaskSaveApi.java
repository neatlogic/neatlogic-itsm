package codedriver.module.process.api.processtask;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.exception.user.UserNotFoundException;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.priority.PriorityNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.dto.ProcessStepVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskVo;

public class ProcessTaskSaveApi extends ApiComponentBase  {

	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private PriorityMapper priorityMapper;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessMapper processMapper;
	
	@Override
	public String getToken() {
		return "processtask/save";
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
//  			"uuid": "属性uuid",
//  			"handler": "formselect",
//  			"isRequired": true,
//  			"dataList": [
//  				"value"
//  			]
//  		}
//  	]                			
	@Input({
		@Param(name="processTaskId", type = ApiParamType.LONG, desc="工单id"),
		@Param(name="channelUuid", type= ApiParamType.STRING, isRequired=true, desc="服务uuid"),
		@Param(name="title", type=ApiParamType.REGEX, rule="^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", desc = "标题"),
		@Param(name="reporter", type=ApiParamType.STRING, desc="请求人"),
		@Param(name="priorityUuid", type=ApiParamType.STRING, desc="优先级uuid"),
		@Param(name="formAttributeDataList", type = ApiParamType.JSONARRAY, desc = "表单属性数据列表"),
		@Param(name="content", type=ApiParamType.STRING, xss = true, desc = "描述"),
		@Param(name="fileUuidList", type=ApiParamType.JSONARRAY, desc = "附件uuid列表")
	})
	@Output({
		@Param(name="processTaskId", type = ApiParamType.LONG, desc="工单id")
	})
	@Description(desc = "工单上报暂存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String channelUuid = jsonObj.getString("channelUuid");
		if(channelMapper.checkChannelIsExists(channelUuid) == 0) {
			throw new ChannelNotFoundException(channelUuid);
		}
		String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
		String priorityUuid = jsonObj.getString("priorityUuid");
		if(priorityMapper.checkPriorityIsExists(priorityUuid) == 0) {
			throw new PriorityNotFoundException(priorityUuid);
		}
		String reporter = jsonObj.getString("reporter");
		if(reporter != null) {
			if(userMapper.getUserBaseInfoByUserId(reporter) == null) {
				throw new UserNotFoundException(reporter);
			}
		}
		String owner = UserContext.get().getUserId();
		String title = jsonObj.getString("title");
		String content = jsonObj.getString("content");
		List<String> fileIdList = JSON.parseArray(jsonObj.getString("fileUuidList"), String.class);
		JSON formAttributeDataList = jsonObj.getJSONArray("formAttributeDataList");
		Long processTaskId = jsonObj.getLong("processTaskId");
		if(processTaskId != null) {
			ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
			if(processTaskVo == null) {
				throw new ProcessTaskNotFoundException(processTaskId.toString());
			}
			processTaskVo.setTitle(title);
			processTaskVo.setChannelUuid(channelUuid);
			processTaskVo.setPriorityUuid(priorityUuid);
			processTaskVo.setReporter(reporter);
			processTaskVo.setProcessUuid(processUuid);
			processTaskVo.setOwner(owner);
			List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
			ProcessTaskStepVo startStep = processTaskStepList.get(0);
		}else {
			ProcessTaskVo processTaskVo = new ProcessTaskVo();
			processTaskVo.setTitle(title);
			processTaskVo.setChannelUuid(channelUuid);
			processTaskVo.setPriorityUuid(priorityUuid);
			processTaskVo.setReporter(reporter);
			processTaskVo.setProcessUuid(processUuid);
			processTaskVo.setOwner(owner);
			List<ProcessStepVo> processStepList = processMapper.getProcessStepDetailByProcessUuid(processUuid);
			/** 写入所有步骤信息 **/
			if (processStepList != null && processStepList.size() > 0) {
				
			}
		}
		return null;
	}

}
