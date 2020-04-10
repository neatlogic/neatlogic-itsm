package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ChannelPriorityVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.FormVersionVo;
import codedriver.framework.process.dto.ProcessStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessTaskConfigVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFileVo;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepFormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.form.FormActiveVersionNotFoundExcepiton;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;

@Service
public class ProcessTaskDraftGetApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private ProcessMapper processMapper;
	
	@Autowired
	private FileMapper fileMapper;
	
	@Autowired
	private FormMapper formMapper;
	
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
			//获取工单基本信息
			ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
			if(processTaskVo == null) {
				throw new ProcessTaskNotFoundException(processTaskId.toString());
			}

			//获取开始步骤信息
			List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskId, ProcessStepType.START.getValue());
			if(processTaskStepList.size() != 1) {
				throw new ProcessTaskRuntimeException("工单：'" + processTaskId + "'有" + processTaskStepList.size() + "个开始步骤");
			}
			Long startProcessTaskStepId = processTaskStepList.get(0).getId();
			//获取上报描述内容
			List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentProcessTaskStepId(startProcessTaskStepId);
			if(!processTaskStepContentList.isEmpty()) {
				ProcessTaskContentVo processTaskContentVo = processTaskMapper.getProcessTaskContentByHash(processTaskStepContentList.get(0).getContentHash());
				if(processTaskContentVo != null) {
					processTaskVo.setContent(processTaskContentVo.getContent());
				}
			}
			//获取上传附件uuid
			ProcessTaskFileVo processTaskFileVo = new ProcessTaskFileVo();
			processTaskFileVo.setProcessTaskId(processTaskId);
			processTaskFileVo.setProcessTaskStepId(startProcessTaskStepId);
			List<ProcessTaskFileVo> processTaskFileList = processTaskMapper.searchProcessTaskFile(processTaskFileVo);
			
			if(processTaskFileList.size() > 0) {
				List<String> fileUuidList = new ArrayList<>();
				List<FileVo> fileList = new ArrayList<>();
				for(ProcessTaskFileVo processTaskFile : processTaskFileList) {
					fileUuidList.add(processTaskFile.getFileUuid());
					FileVo fileVo = fileMapper.getFileByUuid(processTaskFile.getFileUuid());
					fileList.add(fileVo);
				}
				processTaskVo.setFileList(fileList);
			}
			//获取工单流程图信息
			ProcessTaskConfigVo processTaskConfig = processTaskMapper.getProcessTaskConfigByHash(processTaskVo.getConfigHash());
			if(processTaskConfig == null) {
				throw new ProcessTaskRuntimeException("没有找到工单：'" + processTaskId + "'的流程图配置信息");
			}
			processTaskVo.setConfig(processTaskConfig.getConfig());
			//获取工单表单信息
			ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
			if(processTaskFormVo != null && StringUtils.isNotBlank(processTaskFormVo.getFormContent())) {
				processTaskVo.setFormConfig(processTaskFormVo.getFormContent());
				List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskId);
				if(CollectionUtils.isNotEmpty(processTaskFormAttributeDataList)) {
					Map<String, Object> formAttributeDataMap = new HashMap<>();
					for(ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
						String data = processTaskFormAttributeDataVo.getData();
						if(data.startsWith("[") && data.endsWith("]")) {
							List<String> dataList = JSON.parseArray(data, String.class);
							formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), dataList);
						}else {
							formAttributeDataMap.put(processTaskFormAttributeDataVo.getAttributeUuid(), data);
						}
					}
					processTaskVo.setFormAttributeDataMap(formAttributeDataMap);
				}
			}			
			
			List<ProcessTaskStepFormAttributeVo> processTaskStepFormAttributeList = processTaskMapper.getProcessTaskStepFormAttributeByProcessTaskStepId(startProcessTaskStepId);
			if(processTaskStepFormAttributeList.size() > 0) {
				Map<String, String> formAttributeActionMap = new HashMap<>();
				for(ProcessTaskStepFormAttributeVo processTaskStepFormAttributeVo : processTaskStepFormAttributeList) {
					formAttributeActionMap.put(processTaskStepFormAttributeVo.getAttributeUuid(), processTaskStepFormAttributeVo.getAction());
				}
				processTaskService.setProcessTaskFormAttributeAction(processTaskVo, formAttributeActionMap, 1);
			}
			return processTaskVo;
		}else if(channelUuid != null){
			ProcessTaskVo processTaskVo = new ProcessTaskVo();
			ChannelVo channel = channelMapper.getChannelByUuid(channelUuid);
			if(channel == null) {
				throw new ChannelNotFoundException(channelUuid);
			}
			processTaskVo.setChannelUuid(channelUuid);
			processTaskVo.setProcessUuid(channel.getProcessUuid());
			processTaskVo.setWorktimeUuid(channel.getWorktimeUuid());
			List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(channelUuid);
			for(ChannelPriorityVo channelPriority : channelPriorityList) {
				if(channelPriority.getIsDefault().intValue() == 1) {
					processTaskVo.setPriorityUuid(channelPriority.getPriorityUuid());
				}
			}
			ProcessVo processVo = processMapper.getProcessByUuid(channel.getProcessUuid());
			if(processVo == null) {
				throw new ProcessNotFoundException(channel.getProcessUuid());
			}
			processTaskVo.setConfig(processVo.getConfig());
			
			if(StringUtils.isNotBlank(processVo.getFormUuid())) {
				FormVersionVo formVersion = formMapper.getActionFormVersionByFormUuid(processVo.getFormUuid());
				if(formVersion == null) {
					throw new FormActiveVersionNotFoundExcepiton(processVo.getFormUuid());
				}
				processTaskVo.setFormConfig(formVersion.getFormConfig());
				ProcessStepVo processStepVo = new ProcessStepVo();
				processStepVo.setProcessUuid(channel.getProcessUuid());
				processStepVo.setType(ProcessStepType.START.getValue());
				List<ProcessStepVo> processStepList = processMapper.searchProcessStep(processStepVo);
				if(processStepList.size() != 1) {
					throw new ProcessTaskRuntimeException("流程：'" + channel.getProcessUuid() + "'有" + processStepList.size() + "个开始步骤");
				}
				ProcessStepFormAttributeVo processStepFormAttributeVo = new ProcessStepFormAttributeVo();
				processStepFormAttributeVo.setProcessStepUuid(processStepList.get(0).getUuid());
				List<ProcessStepFormAttributeVo> processStepFormAttributeList = processMapper.getProcessStepFormAttributeByStepUuid(processStepFormAttributeVo);
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

}
