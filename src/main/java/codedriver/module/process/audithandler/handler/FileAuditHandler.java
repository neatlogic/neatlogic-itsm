package codedriver.module.process.audithandler.handler;

import java.util.ArrayList;
import java.util.List;

import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
@Service
public class FileAuditHandler implements IProcessTaskStepAuditDetailHandler {
	
	@Autowired
	private FileMapper fileMapper;
	
	@Override
	public String getType() {
		return ProcessTaskAuditDetailType.FILE.getValue();
	}

	@Override
	public int handle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		String oldContent = processTaskStepAuditDetailVo.getOldContent();
		if(StringUtils.isNotBlank(oldContent)) {
			processTaskStepAuditDetailVo.setOldContent(parse(oldContent));
		}
		String newContent = processTaskStepAuditDetailVo.getNewContent();
		if(StringUtils.isNotBlank(newContent)) {
			processTaskStepAuditDetailVo.setNewContent(parse(newContent));
		}
		if(processTaskStepAuditDetailVo.getOldContent() == null && processTaskStepAuditDetailVo.getNewContent() == null){
			return 0;
		}
		return 1;
	}

	private String parse(String content) {
		List<Long> fileIdList = JSON.parseArray(content, Long.class);
		if(CollectionUtils.isNotEmpty(fileIdList)) {
			List<FileVo> fileList = new ArrayList<>();
			for(Long fileId : fileIdList) {
				FileVo fileVo = fileMapper.getFileById(fileId);
				if(fileVo != null) {
					fileList.add(fileVo);
				}
			}
			return JSON.toJSONString(fileList);
		}
		return null;
	}
}
