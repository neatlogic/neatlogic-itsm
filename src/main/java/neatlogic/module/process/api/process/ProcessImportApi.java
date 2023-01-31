package neatlogic.module.process.api.process;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import neatlogic.module.process.dao.mapper.ProcessMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.dto.ProcessVo;
import neatlogic.framework.process.exception.process.ProcessImportException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.process.auth.PROCESS_MODIFY;
import neatlogic.module.process.service.ProcessService;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_MODIFY.class)
public class ProcessImportApi extends PrivateBinaryStreamApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;
	
	@Autowired
	private ProcessService processService;
	
	@Override
	public String getToken() {
		return "process/import";
	}

	@Override
	public String getName() {
		return "流程导入接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Description(desc = "流程导入接口")
	@Override
	public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		//获取所有导入文件
		Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
		//如果没有导入文件, 抛异常
		if(multipartFileMap == null || multipartFileMap.isEmpty()) {
			throw new ProcessImportException("没有导入文件");
		}
		ObjectInputStream ois = null;
		Object obj = null;
		MultipartFile multipartFile = null;
		String result = null;
		//遍历导入文件, 目前只获取第一个文件内容, 其余的放弃
		for(Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
			multipartFile = entry.getValue();
			//反序列化获取对象
			try {
				ois = new ObjectInputStream(multipartFile.getInputStream());
				obj = ois.readObject();
			}catch(IOException e) {
				throw new ProcessImportException(multipartFile.getOriginalFilename() + "文件格式不正确");
			}finally {
				if(ois != null) {
					ois.close();
				}
			}
			
			if(obj instanceof ProcessVo) {
				ProcessVo processVo = (ProcessVo) obj;
				int index = 0;
				String oldName = processVo.getName();
				//如果导入的流程名称已存在就重命名
				while(processMapper.checkProcessNameIsRepeat(processVo) > 0) {
					index ++;
					processVo.setName(oldName + "_" + index);
				}
				if(processMapper.checkProcessIsExists(processVo.getUuid()) == 0) {
					result = "新建流程：'" + processVo.getName() +"'";
				}else {
					result = "更新流程：'" + processVo.getName() +"'";
				}
				processVo.setFcu(UserContext.get().getUserUuid(true));
				processVo.makeupConfigObj();
				processService.saveProcess(processVo);
				return result;
			}else {
				throw new ProcessImportException(multipartFile.getOriginalFilename() + "文件格式不正确");
			}
		}
		return null;
	}

}
