package codedriver.module.process.api.form;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
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

import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.exception.form.FormImportException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
import codedriver.module.process.dto.FormVersionVo;
import codedriver.module.process.dto.FormVo;

@Service
@Transactional
public class FormImportApi extends BinaryStreamApiComponentBase {

	@Autowired
	private FormMapper formMapper;
	
	@Override
	public String getToken() {
		return "process/form/import";
	}

	@Override
	public String getName() {
		return "表单导入接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Description(desc = "表单导入接口")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
				
			if(obj instanceof FormVo) {
				List<String> resultList = new ArrayList<>();
				FormVo formVo = (FormVo) obj;
				int index = 0;
				String oldName = formVo.getName();
				while(formMapper.checkFormNameIsRepeat(formVo) > 0) {
					index ++;
					formVo.setName(oldName + "_" + index);
				}
				List<FormVersionVo> formVersionList = formVo.getVersionList(); 
				if(formMapper.checkFormIsExists(formVo.getUuid()) == 0) {
					formVo.setUuid(null);					
					formMapper.replaceForm(formVo);
					resultList.add("新增表单：" + formVo.getName());
					int version = 1;
					for(FormVersionVo formVersion : formVersionList) {
						formVersion.setFormUuid(formVo.getUuid());
						formVersion.setUuid(null);
						formVersion.setVersion(version);
						formMapper.insertFormVersion(formVersion);
						resultList.add("新增版本" + version);
						version ++;
					}
				}else {
					resultList.add("更新表单：" + formVo.getName());
					for(FormVersionVo formVersion : formVersionList) {
						FormVersionVo existsFormVersionVo = formMapper.getFormVersionByUuid(formVersion.getUuid());
						if(existsFormVersionVo != null && existsFormVersionVo.getFormUuid().equals(formVo.getUuid())) {
							formMapper.updateFormVersion(formVersion);
							resultList.add("版本" + existsFormVersionVo.getVersion() + "被覆盖");
						}else {				
							Integer version = formMapper.getMaxVersionByFormUuid(formVo.getUuid());
							if(version == null) {
								version = 1;
							}else {
								version += 1;
							}
							formVersion.setVersion(version);
							formVersion.setUuid(null);
							formMapper.insertFormVersion(formVersion);
							resultList.add("新增版本" + version);
						}
					}
					
				}
				return resultList;
			}else {
				throw new FormImportException(multipartFile.getOriginalFilename() + "文件格式不正确");
			}			
		}	
		return null;
	}

}
