package codedriver.module.process.api.form;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.exception.form.FormNotFoundException;
import codedriver.framework.process.exception.form.FormImportException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
import codedriver.module.process.dto.FormVersionVo;

@Service
@Transactional
public class FormVersionImportApi extends BinaryStreamApiComponentBase{

	@Autowired
	private FormMapper formMapper;
	
	@Override
	public String getToken() {
		return "process/form/version/import";
	}

	@Override
	public String getName() {
		return "表单版本导入接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid", isRequired = true)
		})
	@Description(desc = "表单版本导入接口")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String uuid = paramObj.getString("uuid");
		if(formMapper.checkFormIsExists(uuid) == 0) {
			throw new FormNotFoundException(uuid);
		}
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
		ObjectInputStream ois = null;
		Object obj = null;
		MultipartFile multipartFile = null;
		if(multipartFileMap == null || multipartFileMap.isEmpty()) {
			throw new FormImportException("没有导入文件");
		}
		
		for(Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
			multipartFile = entry.getValue();
			
			try {
				ois = new ObjectInputStream(multipartFile.getInputStream());
				obj = ois.readObject();
			}catch(IOException e) {
				throw new FormImportException(multipartFile.getOriginalFilename() + "文件格式不正确");
			}finally {
				if(ois != null) {
					ois.close();
				}
			}
				
			if(obj instanceof FormVersionVo) {
				FormVersionVo formVersionVo = (FormVersionVo) obj;
				formVersionVo.setFormUuid(uuid);
				formVersionVo.setIsActive(1);
				FormVersionVo existsFormVersionVo = formMapper.getFormVersionByUuid(formVersionVo.getUuid());
				if(existsFormVersionVo != null && existsFormVersionVo.getFormUuid().equals(uuid)) {
					formMapper.updateFormVersion(formVersionVo);
					return "版本" + existsFormVersionVo.getVersion() + "被覆盖";
				}else {				
					Integer version = formMapper.getMaxVersionByFormUuid(uuid);
					if(version == null) {
						version = 1;
					}else {
						version += 1;
					}
					formVersionVo.setVersion(version);
					formVersionVo.setUuid(null);
					formMapper.insertFormVersion(formVersionVo);
					return "新增版本" + version;
				}
				
			}else {
				throw new FormImportException(multipartFile.getOriginalFilename() + "文件格式不正确");
			}
			
		}
		
		return null;
	}

}
