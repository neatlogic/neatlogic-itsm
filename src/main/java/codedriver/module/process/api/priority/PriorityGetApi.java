package codedriver.module.process.api.priority;

import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.exception.priority.PriorityNotFoundException;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class PriorityGetApi extends PrivateApiComponentBase {
	
	@Autowired
	private PriorityMapper priorityMapper;
	
	@Override
	public String getToken() {
		return "process/priority/get";
	}

	@Override
	public String getName() {
		return "优先级信息获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "优先级uuid")
	})
	@Output({
		@Param(name="Return", explode = PriorityVo.class, desc="优先级信息")
	})
	@Description(desc = "优先级信息获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		PriorityVo priority = priorityMapper.getPriorityByUuid(uuid);
		if(priority == null) {
			throw new PriorityNotFoundException(uuid);
		}
		return priority;
	}

}
