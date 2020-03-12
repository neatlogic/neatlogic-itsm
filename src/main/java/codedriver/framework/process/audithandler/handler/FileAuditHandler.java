package codedriver.framework.process.audithandler.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.module.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.module.process.dto.ProcessTaskStepAuditDetailVo;
@Service
public class FileAuditHandler implements IProcessTaskStepAuditDetailHandler{
	
	@Autowired
	private FileMapper fileMapper;
	
	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.FILE.getValue();
	}

	@Override
	public void handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		String oldContent = processTaskStepAuditDetailVo.getOldContent();
		if(StringUtils.isNotBlank(oldContent)) {
			List<String> fileUuidList = JSON.parseArray(oldContent, String.class);
			if(CollectionUtils.isNotEmpty(fileUuidList)) {
				List<FileVo> fileList = new ArrayList<>();
				for(String fileUuid : fileUuidList) {
					FileVo fileVo = fileMapper.getFileByUuid(fileUuid);
					if(fileVo != null) {
						fileList.add(fileVo);
					}
				}
				processTaskStepAuditDetailVo.setOldContent(JSON.toJSONString(fileList));
			}
		}
		String newContent = processTaskStepAuditDetailVo.getNewContent();
		if(StringUtils.isNotBlank(newContent)) {
			List<String> fileUuidList = JSON.parseArray(newContent, String.class);
			if(CollectionUtils.isNotEmpty(fileUuidList)) {
				List<FileVo> fileList = new ArrayList<>();
				for(String fileUuid : fileUuidList) {
					FileVo fileVo = fileMapper.getFileByUuid(fileUuid);
					if(fileVo != null) {
						fileList.add(fileVo);
					}
				}
				processTaskStepAuditDetailVo.setNewContent(JSON.toJSONString(fileList));
			}
		}
	}

}
