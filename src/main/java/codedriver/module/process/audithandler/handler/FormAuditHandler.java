package codedriver.module.process.audithandler.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerBase;
import codedriver.framework.process.constvalue.ProcessFormHandler;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.formattribute.core.FormAttributeHandlerFactory;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;
@Service
public class FormAuditHandler extends ProcessTaskStepAuditDetailHandlerBase {

	private final static Logger logger = LoggerFactory.getLogger(FormAuditHandler.class);
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.FORM.getValue();
	}

	@Override
	protected void myHandle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
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
			}else if(ProcessFormHandler.FORMDIVIDER.getHandler().equals(processTaskFormAttributeDataVo.getType())) {//删除分割线
				iterator.remove();
			}
		}
		processTaskStepAuditDetailVo.setOldContent(null);
		processTaskStepAuditDetailVo.setNewContent(null);
		if(CollectionUtils.isNotEmpty(processTaskFormAttributeDataList)) {
			Map<String, JSONObject> attributeConfigMap = new HashMap<>();
			Map<String, String> attributeLabelMap = new HashMap<>();
			Long processTaskId = processTaskFormAttributeDataList.get(0).getProcessTaskId();
			ProcessTaskFormVo processTaskForm = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
			if(processTaskForm != null && StringUtils.isNotBlank(processTaskForm.getFormContent())) {
				try {
					JSONObject formConfig = JSON.parseObject(processTaskForm.getFormContent());
					JSONArray controllerList = formConfig.getJSONArray("controllerList");
					if(CollectionUtils.isNotEmpty(controllerList)) {
						for(int i = 0; i < controllerList.size(); i++) {
							JSONObject attributeObj = controllerList.getJSONObject(i);
							attributeLabelMap.put(attributeObj.getString("uuid"), attributeObj.getString("label"));
							attributeConfigMap.put(attributeObj.getString("uuid"), attributeObj.getJSONObject("config"));
						}
					}
				}catch(Exception ex) {
					logger.error("hash为" + processTaskForm.getFormContentHash() + "的processtask_form内容不是合法的JSON格式", ex);
				}
			}

			Map<String, String> oldContentMap = new HashMap<>();
			for(ProcessTaskFormAttributeDataVo attributeDataVo : oldProcessTaskFormAttributeDataList) {
				IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(attributeDataVo.getType());
				if(handler != null) {
					oldContentMap.put(attributeDataVo.getAttributeUuid(), handler.getValue(attributeDataVo, attributeConfigMap.get(attributeDataVo.getAttributeUuid())));
				}else {
					oldContentMap.put(attributeDataVo.getAttributeUuid(), attributeDataVo.getData());
				}
			}
			JSONArray contentList = new JSONArray();
			for(ProcessTaskFormAttributeDataVo attributeDataVo : processTaskFormAttributeDataList) {
				JSONObject content  = new JSONObject();
				content.put("label", attributeLabelMap.get(attributeDataVo.getAttributeUuid()));
				if(ProcessFormHandler.FORMCASCADELIST.getHandler().equalsIgnoreCase(attributeDataVo.getType()) 
						|| ProcessFormHandler.FORMDYNAMICLIST.getHandler().equalsIgnoreCase(attributeDataVo.getType()) 
						|| ProcessFormHandler.FORMSTATICLIST.getHandler().equalsIgnoreCase(attributeDataVo.getType()) 
						) {
					content.put("newContent", "已更新");
				}else {
					String oldContent = oldContentMap.get(attributeDataVo.getAttributeUuid());
					if(oldContent != null) {
						content.put("oldContent", oldContent);
					}
					IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(attributeDataVo.getType());
					if(handler != null) {
						content.put("newContent", handler.getValue(attributeDataVo, attributeConfigMap.get(attributeDataVo.getAttributeUuid())));
					}else {
						content.put("newContent", attributeDataVo.getData());
					}
				}
				contentList.add(content);
			}
			processTaskStepAuditDetailVo.setNewContent(JSON.toJSONString(contentList));
		}
	}

}
