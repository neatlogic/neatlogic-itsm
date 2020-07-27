package codedriver.module.process.api.priority;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.exception.priority.PriorityNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class PriorityDeleteApi extends ApiComponentBase {

	@Autowired
	private PriorityMapper priorityMapper;
	
	@Override
	public String getToken() {
		return "process/priority/delete";
	}

	@Override
	public String getName() {
		return "优先级信息删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "优先级uuid")
	})
	@Description(desc = "优先级信息删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		PriorityVo priorityVo = priorityMapper.getPriorityByUuid(uuid);
		if(priorityVo == null) {
			throw new PriorityNotFoundException(uuid);
		}
		priorityMapper.deletePriorityByUuid(uuid);
		priorityMapper.updateSortDecrement(priorityVo.getSort(), null);
		return null;
	}

}
