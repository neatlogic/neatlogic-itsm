package codedriver.module.process.api.process;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.auth.label.PROCESS_MODIFY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.process.exception.process.ProcessNameRepeatException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_MODIFY.class)
public class ProcessUpdateApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;
	
	@Override
	public String getToken() {
		return "process/update";
	}

	@Override
	public String getName() {
		return "流程基本信息更新接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "流程uuid"),
		@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired= true, maxLength = 50, desc = "流程名称")
	})
	@Output({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "流程uuid")
	})
	@Description(desc = "流程基本信息更新接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessVo processVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessVo>() {});
		if(processMapper.checkProcessIsExists(processVo.getUuid()) == 0) {
			throw new ProcessNotFoundException(processVo.getUuid());
		}
		if(processMapper.checkProcessNameIsRepeat(processVo) > 0) {
			throw new ProcessNameRepeatException(processVo.getName());
		}
		processMapper.updateProcess(processVo);
		return processVo.getUuid();
	}

}
