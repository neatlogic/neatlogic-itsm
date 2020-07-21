package codedriver.module.process.api.form;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dto.FormVersionVo;
import codedriver.framework.process.dto.FormVo;
import codedriver.framework.process.exception.form.FormNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class FormExportApi extends BinaryStreamApiComponentBase {

	@Autowired
	private FormMapper formMapper;
	
	@Override
	public String getToken() {
		return "process/form/export";
	}

	@Override
	public String getName() {
		return "表单导出接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "表单uuid", isRequired = true)
		})
	@Description(desc = "表单导出接口")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String uuid = paramObj.getString("uuid");
		FormVo formVo = formMapper.getFormByUuid(uuid);
		//判断表单是否存在
		if(formVo == null) {
			throw new FormNotFoundException(uuid);
		}
		//获取表单的所有版本
		List<FormVersionVo> formVersionLsit = formMapper.getFormVersionByFormUuid(uuid);
		formVo.setVersionList(formVersionLsit);		
		
		//设置导出文件名, 表单名称_版本号
		String fileNameEncode = formVo.getName() + ".form";
		Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
		if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
			fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
		} else {
			fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
		}
		response.setContentType("aplication/x-msdownload");
		response.setHeader("Content-Disposition", "attachment;fileName=\"" + fileNameEncode + "\"");
		
		//获取序列化字节数组
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(formVo);
		
		ServletOutputStream os = null;
		os = response.getOutputStream();
		IOUtils.write(baos.toByteArray(), os);
		if (os != null) {
			os.flush();
			os.close();
		}
		if(oos != null) {
			oos.close();
		}
		if(baos != null) {
			baos.close();
		}
		return null;
	}

}
