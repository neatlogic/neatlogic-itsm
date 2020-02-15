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
import codedriver.framework.restful.annotation.Output;
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
	@Output({
		@Param(name = "version", type = ApiParamType.INTEGER, desc = "导入版本号"),
		@Param(name = "result", type = ApiParamType.STRING, desc = "导入结果")
	})
	@Description(desc = "表单版本导入接口")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String uuid = paramObj.getString("uuid");
		//判断表单是否存在
		if(formMapper.checkFormIsExists(uuid) == 0) {
			throw new FormNotFoundException(uuid);
		}
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		//获取所有导入文件
		Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();		
		//如果没有导入文件, 抛异常
		if(multipartFileMap == null || multipartFileMap.isEmpty()) {
			throw new FormImportException("没有导入文件");
		}
		ObjectInputStream ois = null;
		Object obj = null;
		MultipartFile multipartFile = null;
		JSONObject resultObj = new JSONObject();
		//遍历导入文件, 目前只获取第一个文件内容, 其余的放弃
		for(Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
			multipartFile = entry.getValue();
			//反序列化获取对象
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
			//判断对象是否是表单版本对象, 不是就抛异常	
			if(obj instanceof FormVersionVo) {
				FormVersionVo formVersionVo = (FormVersionVo) obj;
				formVersionVo.setFormUuid(uuid);
				//将导入版本设置为激活版本
				formVersionVo.setIsActive(0);
				FormVersionVo existsFormVersionVo = formMapper.getFormVersionByUuid(formVersionVo.getUuid());
				//如果导入的表单版本已存在, 且表单uuid相同, 则覆盖，反之，新增一个版本
				if(existsFormVersionVo != null && existsFormVersionVo.getFormUuid().equals(uuid)) {
					formMapper.updateFormVersion(formVersionVo);
					resultObj.put("version", existsFormVersionVo.getVersion());
					resultObj.put("result", "版本" + existsFormVersionVo.getVersion() + "被覆盖");
					return resultObj;
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
					resultObj.put("version", version);
					resultObj.put("result", "新增版本" + version);
					return resultObj;
				}
				
			}else {
				throw new FormImportException(multipartFile.getOriginalFilename() + "文件格式不正确");
			}
			
		}
		
		return null;
	}

}
