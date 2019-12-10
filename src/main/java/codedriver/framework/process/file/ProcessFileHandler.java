package codedriver.framework.process.file;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.file.core.FileTypeHandlerBase;
import codedriver.framework.file.dto.FileVo;

public class ProcessFileHandler extends FileTypeHandlerBase {

	@Override
	public boolean valid(String userId, JSONObject jsonObj) {
		return true;
	}

	@Override
	public String getDisplayName() {
		return "ITSM附件";
	}

	@Override
	public void afterUpload(FileVo fileVo, JSONObject jsonObj) {
	}

	@Override
	public String getBelong() {
		return "ITSM";
	}

}
