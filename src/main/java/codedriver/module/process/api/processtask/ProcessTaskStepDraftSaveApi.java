package codedriver.module.process.api.processtask;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.process.actionauthorityverificationhandler.core.IProcessTaskStepUserActionAuthorityVerificationHandler;
import codedriver.framework.process.actionauthorityverificationhandler.core.ProcessTaskStepUserActionAuthorityVerificationHandlerFactory;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.module.process.constvalue.ProcessTaskStepAction;
import codedriver.module.process.dto.ProcessTaskContentVo;
import codedriver.module.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.module.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.module.process.dto.ProcessTaskStepAuditVo;
@Service
@Transactional
public class ProcessTaskStepDraftSaveApi extends ApiComponentBase {

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private FileMapper fileMapper;
	
	@Override
	public String getToken() {
		return "processtask/step/draft/save";
	}

	@Override
	public String getName() {
		return "工单步骤暂存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "步骤id"),
		@Param(name="formAttributeDataList", type = ApiParamType.JSONARRAY, desc = "表单属性数据列表"),
		@Param(name = "auditId", type = ApiParamType.LONG, desc = "活动id"),
		@Param(name = "content", type = ApiParamType.STRING, xss = true, desc = "描述"),
		@Param(name="fileUuidList", type=ApiParamType.JSONARRAY, desc = "附件uuid列表")
	})
	@Description(desc = "工单步骤暂存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		IProcessTaskStepUserActionAuthorityVerificationHandler handler = ProcessTaskStepUserActionAuthorityVerificationHandlerFactory.getHandler(ProcessTaskStepAction.SAVE.getValue());
		if(handler != null) {
			if(!handler.test(processTaskId, processTaskStepId)) {
				return null;
			}
		}
		//写入当前步骤的表单属性值
		JSONArray formAttributeDataList = jsonObj.getJSONArray("formAttributeDataList");
		if(CollectionUtils.isNotEmpty(formAttributeDataList)) {
			for(int i = 0; i < formAttributeDataList.size(); i++) {
				JSONObject formAttributeDataObj = formAttributeDataList.getJSONObject(i);
				ProcessTaskFormAttributeDataVo attributeData = new ProcessTaskFormAttributeDataVo();
				String dataList = formAttributeDataObj.getString("dataList");
				if(StringUtils.isBlank(dataList)) {
					continue;
				}
				attributeData.setData(dataList);
				attributeData.setProcessTaskId(processTaskId);
				attributeData.setAttributeUuid(formAttributeDataObj.getString("attributeUuid"));
				attributeData.setType(formAttributeDataObj.getString("handler"));
				processTaskMapper.replaceProcessTaskFormAttributeData(attributeData);
			}
		}
		
		//删除暂存活动
		Long auditId = jsonObj.getLong("auditId");
		if(auditId != null) {
			ProcessTaskStepAuditVo processTaskStepAuditVo = processTaskMapper.getProcessTaskStepAuditById(auditId);
			if(processTaskStepAuditVo == null) {
				throw new ProcessTaskRuntimeException("活动：'" + auditId + "'不存在");
			}
			if(!ProcessTaskStepAction.SAVE.getValue().equals(processTaskStepAuditVo.getAction())) {
				throw new ProcessTaskRuntimeException("活动：'" + auditId + "'不是暂存活动");
			}
			if(!UserContext.get().getUserId(true).equals(processTaskStepAuditVo.getUserId())) {
				throw new ProcessTaskRuntimeException("活动：'" + auditId + "'不是当前用户的暂存活动");
			}
			processTaskMapper.deleteProcessTaskStepAuditById(auditId);
		}
		String content = jsonObj.getString("content");
		String fileUuidListStr = jsonObj.getString("fileUuidList");
		if(StringUtils.isBlank(content) && (StringUtils.isBlank(fileUuidListStr) || "[]".equals(fileUuidListStr))) {
			return null;
		}
		//生成活动
		ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
		processTaskStepAuditVo.setAction(ProcessTaskStepAction.SAVE.getValue());
		processTaskStepAuditVo.setProcessTaskId(processTaskId);
		processTaskStepAuditVo.setProcessTaskStepId(processTaskStepId);
		processTaskStepAuditVo.setUserId(UserContext.get().getUserId(true));
		processTaskMapper.insertProcessTaskStepAudit(processTaskStepAuditVo);
		
		if (StringUtils.isNotBlank(content)) {
			ProcessTaskContentVo contentVo = new ProcessTaskContentVo(content);
			processTaskMapper.replaceProcessTaskContent(contentVo);
			processTaskMapper.insertProcessTaskStepAuditDetail(new ProcessTaskStepAuditDetailVo(processTaskStepAuditVo.getId(), ProcessTaskAuditDetailType.CONTENT.getValue(), null, contentVo.getHash()));
		}
		
		if(StringUtils.isNotBlank(fileUuidListStr)) {
			List<String> fileUuidList = JSON.parseArray(fileUuidListStr, String.class);
			if(CollectionUtils.isNotEmpty(fileUuidList)) {
				for(String fileUuid : fileUuidList) {
					if(fileMapper.getFileByUuid(fileUuid) == null) {
						throw new ProcessTaskRuntimeException("上传附件uuid:'" + fileUuid + "'不存在");
					}
				}
				processTaskMapper.insertProcessTaskStepAuditDetail(new ProcessTaskStepAuditDetailVo(processTaskStepAuditVo.getId(), ProcessTaskAuditDetailType.FILE.getValue(), null, fileUuidListStr));
			}
		}
		return null;
	}

}
