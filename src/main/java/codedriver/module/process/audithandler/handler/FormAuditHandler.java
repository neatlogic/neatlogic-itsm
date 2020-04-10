package codedriver.module.process.audithandler.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.formattribute.core.FormAttributeHandlerFactory;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;
@Service
public class FormAuditHandler implements IProcessTaskStepAuditDetailHandler {

	private final static Logger logger = LoggerFactory.getLogger(FormAuditHandler.class);
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.FORM.getValue();
	}

	@Override
	public void handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		List<ProcessTaskFormAttributeDataVo> oldProcessTaskFormAttributeDataList = JSON.parseArray(processTaskStepAuditDetailVo.getOldContent(), ProcessTaskFormAttributeDataVo.class);
		if(oldProcessTaskFormAttributeDataList == null) {
			oldProcessTaskFormAttributeDataList = new ArrayList<>();
		}

		List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = JSON.parseArray(processTaskStepAuditDetailVo.getNewContent(), ProcessTaskFormAttributeDataVo.class);
		if(processTaskFormAttributeDataList == null) {
			processTaskFormAttributeDataList = new ArrayList<>();
		}
		Iterator<ProcessTaskFormAttributeDataVo> iterator = processTaskFormAttributeDataList.iterator();
		//循环删除相同的
		while(iterator.hasNext()) {
			ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo = iterator.next();
			int index = oldProcessTaskFormAttributeDataList.indexOf(processTaskFormAttributeDataVo);
			if(index != -1) {
				iterator.remove();
				oldProcessTaskFormAttributeDataList.remove(index);
			}
		}
		if(CollectionUtils.isNotEmpty(processTaskFormAttributeDataList) || CollectionUtils.isNotEmpty(oldProcessTaskFormAttributeDataList)) {
			int oldDataSize = oldProcessTaskFormAttributeDataList.size();
			int newDataSize = processTaskFormAttributeDataList.size();
			int maxSize = Math.max(oldDataSize, newDataSize);
			List<String> oldContentList = new ArrayList<>(maxSize);
			List<String> newContentList = new ArrayList<>(maxSize);
			Long processTaskId = null;
			if(oldDataSize > 0) {
				processTaskId = oldProcessTaskFormAttributeDataList.get(0).getProcessTaskId();
			}else {
				processTaskId = processTaskFormAttributeDataList.get(0).getProcessTaskId();
			}
			ProcessTaskFormVo processTaskForm = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
			if(processTaskForm != null && StringUtils.isNotBlank(processTaskForm.getFormContent())) {
				try {
					JSONObject formConfig = JSON.parseObject(processTaskForm.getFormContent());
					JSONArray controllerList = formConfig.getJSONArray("controllerList");
					if(CollectionUtils.isNotEmpty(controllerList)) {
						Map<String, JSONObject> attributeConfigMap = new HashMap<>();
						for(int i = 0; i < controllerList.size(); i++) {
							JSONObject attributeObj = controllerList.getJSONObject(i);
							attributeConfigMap.put(attributeObj.getString("uuid"), attributeObj.getJSONObject("config"));
						}
						
						for(ProcessTaskFormAttributeDataVo attributeDataVo : oldProcessTaskFormAttributeDataList) {
							IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(attributeDataVo.getType());
							if(handler != null) {
								oldContentList.add(handler.getValue(attributeDataVo, attributeConfigMap.get(attributeDataVo.getAttributeUuid())));
							}else {
								oldContentList.add(attributeDataVo.getData());
							}
						}
						for(ProcessTaskFormAttributeDataVo attributeDataVo : processTaskFormAttributeDataList) {
							IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(attributeDataVo.getType());
							if(handler != null) {
								newContentList.add(handler.getValue(attributeDataVo, attributeConfigMap.get(attributeDataVo.getAttributeUuid())));
							}else {
								newContentList.add(attributeDataVo.getData());
							}
						}
					}
				}catch(Exception ex) {
					logger.error("hash为" + processTaskForm.getFormContentHash() + "的processtask_form内容不是合法的JSON格式", ex);
				}
				
			}else {
				oldContentList = oldProcessTaskFormAttributeDataList.stream().map(ProcessTaskFormAttributeDataVo::getData).collect(Collectors.toList());
				newContentList = processTaskFormAttributeDataList.stream().map(ProcessTaskFormAttributeDataVo::getData).collect(Collectors.toList());
			}

			for(int i = oldContentList.size(); i < maxSize; i++) {
				oldContentList.add("");
			}
			processTaskStepAuditDetailVo.setOldContent(JSON.toJSONString(oldContentList));

			for(int i = newContentList.size(); i < maxSize; i++) {
				newContentList.add("");
			}
			processTaskStepAuditDetailVo.setNewContent(JSON.toJSONString(newContentList));
		}
	}

}
