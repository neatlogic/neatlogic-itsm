package codedriver.framework.process.stephandler.component;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.module.process.constvalue.ProcessStepHandler;
import codedriver.module.process.constvalue.ProcessStepMode;
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
	public List<ProcessTaskStepVo> myGetNext(ProcessTaskStepVo processTaskStepVo) {
		return null;
	}

	@Override
	protected int myStartProcess(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
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
				attributeData.setData(formAttributeDataObj.getString("dataList"));
				attributeData.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
				attributeData.setAttributeUuid(formAttributeDataObj.getString("uuid"));
				attributeData.setType(formAttributeDataObj.getString("handler"));
				processTaskMapper.replaceProcessTaskFormAttributeData(attributeData);
			}
		}

		/** 保存描述内容 **/
		String content = paramObj.getString("content");
		if (StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
			processTaskMapper.replaceProcessTaskContent(contentVo);
			processTaskMapper.insertProcessTaskStepContent(new ProcessTaskStepContentVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), contentVo.getHash()));
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
		DataValid.baseInfoValid(currentProcessTaskStepVo);
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

}
