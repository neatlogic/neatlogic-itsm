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

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.exception.form.FormImportException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
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
	
	@Output({
		@Param(name = "Return", type = ApiParamType.JSONARRAY, desc = "导入结果")
	})
	@Description(desc = "表单导入接口")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			//判断对象是否是表单对象, 不是就抛异常	
			if(obj instanceof FormVo) {
				List<String> resultList = new ArrayList<>();
				FormVo formVo = (FormVo) obj;
				int index = 0;
				String oldName = formVo.getName();
				//如果导入的表单名称已存在就重命名
				while(formMapper.checkFormNameIsRepeat(formVo) > 0) {
					index ++;
					formVo.setName(oldName + "_" + index);
				}
				List<FormVersionVo> formVersionList = formVo.getVersionList();
				//判断表单是否存在，不存在就新增，存在就更新
				if(formMapper.checkFormIsExists(formVo.getUuid()) == 0) {				
					formMapper.insertForm(formVo);
					resultList.add("新增表单：" + formVo.getName());
					for(FormVersionVo formVersion : formVersionList) {
						formVersion.setFormUuid(formVo.getUuid());
						formMapper.insertFormVersion(formVersion);
						resultList.add("新增版本" + formVersion.getVersion());
					}
				}else {
					resultList.add("更新表单：" + formVo.getName());
					for(FormVersionVo formVersion : formVersionList) {
						FormVersionVo existsFormVersionVo = formMapper.getFormVersionByUuid(formVersion.getUuid());
						//如果导入的表单版本已存在, 且表单uuid相同, 则覆盖，反之，新增一个版本
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
