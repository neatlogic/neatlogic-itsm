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
//		ProcessTaskStepFormAttributeVo formAttributeVo = new ProcessTaskStepFormAttributeVo();
//		formAttributeVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
//		List<ProcessTaskStepFormAttributeVo> formAttributeList = processTaskMapper.getProcessTaskStepFormAttributeByStepId(formAttributeVo);
//		currentProcessTaskStepVo.setFormAttributeList(formAttributeList);
//		if (formAttributeList != null && formAttributeList.size() > 0) {
//			JSONArray attributeObjList = null;
//			if (paramObj != null && paramObj.containsKey("formAttributeValueList") && paramObj.get("formAttributeValueList") instanceof JSONArray) {
//				attributeObjList = paramObj.getJSONArray("formAttributeValueList");
//			}
//			for (ProcessTaskStepFormAttributeVo attribute : formAttributeList) {
//				if (attribute.getIsEditable().equals(1)) {
//					if (attributeObjList != null && attributeObjList.size() > 0) {
//						for (int i = 0; i < attributeObjList.size(); i++) {
//							JSONObject attrObj = attributeObjList.getJSONObject(i);
//							if (attrObj.getString("uuid").equals(attribute.getAttributeUuid())) {
//								ProcessTaskFormAttributeDataVo attributeData = new ProcessTaskFormAttributeDataVo();
//								attributeData.setData(attrObj.getString("data"));
//								attributeData.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
//								attributeData.setAttributeUuid(attribute.getAttributeUuid());
//								processTaskMapper.replaceProcessTaskFormAttributeData(attributeData);
//								// 放进去方便基类记录日志
//								attribute.setAttributeData(attributeData);
//
//								break;
//							}
//						}
//					}
//					IAttributeHandler attributeHandler = AttributeHandlerFactory.getHandler(attribute.getHandler());
//					if (attributeHandler != null) {
//						try {
//							attributeHandler.valid(attribute.getAttributeData(), attribute.getConfigObj());
//						} catch (Exception ex) {
//							throw new ProcessTaskRuntimeException(ex);
//						}
//					}
//				}
//			}
//		}

		/** 保存内容 **/
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
