package codedriver.module.process.audithandler.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
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
		String oldContent = processTaskStepAuditDetailVo.getOldContent();
		List<ProcessTaskFormAttributeDataVo> oldProcessTaskFormAttributeDataList = JSON.parseArray(oldContent, ProcessTaskFormAttributeDataVo.class);
		if(oldProcessTaskFormAttributeDataList == null) {
			oldProcessTaskFormAttributeDataList = new ArrayList<>();
		}
		String newContent = processTaskStepAuditDetailVo.getNewContent();
		List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = JSON.parseArray(newContent, ProcessTaskFormAttributeDataVo.class);
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
			List<String> oldDataList = oldProcessTaskFormAttributeDataList.stream().map(ProcessTaskFormAttributeDataVo::getData).collect(Collectors.toList());
			processTaskStepAuditDetailVo.setOldContent(JSON.toJSONString(oldDataList));

			List<String> dataList = processTaskFormAttributeDataList.stream().map(ProcessTaskFormAttributeDataVo::getData).collect(Collectors.toList());
			processTaskStepAuditDetailVo.setNewContent(JSON.toJSONString(dataList));
//			ProcessTaskFormVo processTaskForm = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskFormAttributeDataList.get(0).getProcessTaskId());
//			if(processTaskForm != null && StringUtils.isNotBlank(processTaskForm.getFormContent())) {
//				try {
//					JSONObject formConfig = JSON.parseObject(processTaskForm.getFormContent());
//					//
//				}catch(Exception ex) {
//					logger.error("hash为" + processTaskForm.getFormContentHash() + "的processtask_form内容不是合法的JSON格式", ex);
//				}
//				
//			}
		}
	}

}
