package codedriver.module.process.api.process;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessExportApi extends PrivateBinaryStreamApiComponentBase {
	
	@Autowired
	private ProcessMapper processMapper;
	
	@Override
	public String getToken() {
		return "process/export";
	}

	@Override
	public String getName() {
		return "流程导出接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "流程uuid")
	})
	@Description(desc = "流程导出接口")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String uuid = paramObj.getString("uuid");
		if(processMapper.checkProcessIsExists(uuid) == 0) {
			throw new ProcessNotFoundException(uuid);
		}	
		ProcessVo processVo = processMapper.getProcessByUuid(uuid);
		//设置导出文件名
		String fileNameEncode = processVo.getName() + ".process";
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
		oos.writeObject(processVo);
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
