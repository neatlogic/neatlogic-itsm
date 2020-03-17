package codedriver.framework.process.stephandler.component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.module.process.constvalue.ProcessStepHandler;
import codedriver.module.process.constvalue.ProcessStepMode;
import codedriver.module.process.dto.ChannelPriorityVo;
import codedriver.module.process.dto.ProcessStepVo;
import codedriver.module.process.dto.ProcessTaskContentVo;
import codedriver.module.process.dto.ProcessTaskFileVo;
import codedriver.module.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.module.process.dto.ProcessTaskStepContentVo;
import codedriver.module.process.dto.ProcessTaskStepUserVo;
import codedriver.module.process.dto.ProcessTaskStepVo;
import codedriver.module.process.dto.ProcessTaskStepWorkerVo;
import codedriver.module.process.dto.ProcessTaskVo;

@Service
public class StartProcessComponent extends ProcessStepHandlerBase {

	@Override
	public String getHandler() {
		return ProcessStepHandler.START.getHandler();
	}

	@Override
	public String getType() {
		return ProcessStepHandler.START.getType();
	}

	@Override
	public ProcessStepMode getMode() {
		return ProcessStepMode.AT;
	}

	@Override
	public String getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return ProcessStepHandler.START.getName();
	}

	@Override
	public int getSort() {
		return 3;
	}

	@Override
	protected int myActive(ProcessTaskStepVo processTaskStepVo) {
		return 0;
	}

	@Override
	protected int myStart(ProcessTaskStepVo processTaskStepVo) {
		return 0;
	}

	@Override
	public Boolean isAllowStart() {
		return true;
	}

	@Override
	public List<ProcessTaskStepVo> myGetNext(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		List<ProcessTaskStepVo> returnNextStepList = new ArrayList<>();
		List<ProcessTaskStepVo> nextStepList = processTaskMapper.getToProcessTaskStepByFromId(currentProcessTaskStepVo.getId());
		if (nextStepList.size() == 1) {
			return nextStepList;
		} else if (nextStepList.size() > 1) {
			JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
			if (paramObj != null && paramObj.containsKey("nextStepId")) {
				Long nextStepId = paramObj.getLong("nextStepId");
				for (ProcessTaskStepVo processTaskStepVo : nextStepList) {
					if (processTaskStepVo.getId().equals(nextStepId)) {
						returnNextStepList.add(processTaskStepVo);
						break;
					}
				}
			} else {
				throw new ProcessTaskException("找到多个后续节点");
			}
		}
		return returnNextStepList;
	}

	@Override
	protected int myStartProcess(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {	
		return 0;
	}

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	protected int myHandle(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myComplete(ProcessTaskStepVo currentProcessTaskStepVo) {
		baseInfoValid(currentProcessTaskStepVo);
		DataValid.formAttributeDataValid(currentProcessTaskStepVo);
		return 0;
	}

	@Override
	protected int myRetreat(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myAbort(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myBack(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myHang(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myRecover(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myTransfer(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList, List<ProcessTaskStepUserVo> userList) throws ProcessTaskException {
		return 0;
	}

	@Override
	public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj) {
		// TODO Auto-generated method stub

	}
	private boolean baseInfoValid(ProcessTaskStepVo currentProcessTaskStepVo) {
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
		Pattern titlePattern = Pattern.compile("^[A-Za-z_\\d\\u4e00-\\u9fa5]+$");
		if(!titlePattern.matcher(processTaskVo.getTitle()).matches()) {
			throw new ProcessTaskRuntimeException("工单标题格式不对");
		}
		if(StringUtils.isBlank(processTaskVo.getOwner())) {
			throw new ProcessTaskRuntimeException("工单请求人不能为空");
		}
		if(userMapper.getUserBaseInfoByUserId(processTaskVo.getOwner()) == null) {
			throw new ProcessTaskRuntimeException("工单请求人账号:'" + processTaskVo.getOwner() + "'不存在");
		}
		if(StringUtils.isBlank(processTaskVo.getPriorityUuid())) {
			throw new ProcessTaskRuntimeException("工单优先级不能为空");
		}
		List<ChannelPriorityVo> channelPriorityList = channelMapper.getChannelPriorityListByChannelUuid(processTaskVo.getChannelUuid());
		List<String> priorityUuidlist = new ArrayList<>(channelPriorityList.size());
		for(ChannelPriorityVo channelPriorityVo : channelPriorityList) {
			priorityUuidlist.add(channelPriorityVo.getPriorityUuid());
		}
		if(!priorityUuidlist.contains(processTaskVo.getPriorityUuid())) {
			throw new ProcessTaskRuntimeException("工单优先级与服务优先级级不匹配");
		}
//		List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentProcessTaskStepId(currentProcessTaskStepVo.getId());
//		if(processTaskStepContentList.isEmpty()) {
//			throw new ProcessTaskRuntimeException("工单描述不能为空");
//		}
		ProcessTaskFileVo processTaskFileVo = new ProcessTaskFileVo();
		processTaskFileVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
		processTaskFileVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
		List<ProcessTaskFileVo> processTaskFileList = processTaskMapper.searchProcessTaskFile(processTaskFileVo);
		if(processTaskFileList.size() > 0) {
			for(ProcessTaskFileVo processTaskFile : processTaskFileList) {
				//TODO 验证附件uuid是否存在
				if(fileMapper.getFileByUuid(processTaskFile.getFileUuid()) == null) {
					throw new ProcessTaskRuntimeException("上传附件uuid:'" + processTaskFile.getFileUuid() + "'不存在");
				}
			}
		}
		return true;
	}

	@Override
	protected int mySaveDraft(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
		
		/** 更新工单信息**/
		ProcessTaskVo processTaskVo = new ProcessTaskVo();
		processTaskVo.setId(currentProcessTaskStepVo.getProcessTaskId());
		processTaskVo.setTitle(paramObj.getString("title"));
		processTaskVo.setOwner(paramObj.getString("owner"));
		processTaskVo.setPriorityUuid(paramObj.getString("priorityUuid"));
		processTaskMapper.updateProcessTaskTitleOwnerPriorityUuid(processTaskVo);
		
		/** 写入当前步骤的表单属性值 **/
		JSONArray formAttributeDataList = paramObj.getJSONArray("formAttributeDataList");
		if(formAttributeDataList != null && formAttributeDataList.size() > 0) {
			for(int i = 0; i < formAttributeDataList.size(); i++) {
				JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
				ProcessTaskFormAttributeDataVo attributeData = new ProcessTaskFormAttributeDataVo();
				String dataList = formAttributeDataObj.getString("dataList");
				if(StringUtils.isBlank(dataList)) {
					continue;
				}
				attributeData.setData(dataList);
				attributeData.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
				attributeData.setAttributeUuid(formAttributeDataObj.getString("attributeUuid"));
				attributeData.setType(formAttributeDataObj.getString("handler"));
				processTaskMapper.replaceProcessTaskFormAttributeData(attributeData);
			}
		}

		/** 保存描述内容 **/
		String content = paramObj.getString("content");
		if (StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
			processTaskMapper.replaceProcessTaskContent(contentVo);
			processTaskMapper.replaceProcessTaskStepContent(new ProcessTaskStepContentVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), contentVo.getHash()));
		}
		
		/** 保存附件uuid **/
		ProcessTaskFileVo processTaskFileVo = new ProcessTaskFileVo();
		processTaskFileVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
		processTaskFileVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
		processTaskMapper.deleteProcessTaskFile(processTaskFileVo);
		String fileUuidListStr = paramObj.getString("fileUuidList");
		if(StringUtils.isNotBlank(fileUuidListStr)) {
			List<String> fileUuidList = JSON.parseArray(fileUuidListStr, String.class);
			for(String fileUuid : fileUuidList) {
				processTaskFileVo.setFileUuid(fileUuid);
				processTaskMapper.insertProcessTaskFile(processTaskFileVo);
			}
		}
		return 1;
	}
}
