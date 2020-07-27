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
@OperationType(type = OperationTypeEnum.UPDATE)
public class PriorityMoveApi extends ApiComponentBase {
	
	@Autowired
	private PriorityMapper priorityMapper;

	@Override
	public String getToken() {
		return "process/priority/move";
	}

	@Override
	public String getName() {
		return "移动动优先级接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "被移动的优先级uuid"),
		@Param(name = "sort", type = ApiParamType.INTEGER, isRequired = true, desc = "移动后的序号")
	})
	@Description(desc = "移动动优先级接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		PriorityVo priorityVo = priorityMapper.getPriorityByUuid(uuid);
		if(priorityVo == null) {
			throw new PriorityNotFoundException(uuid);
		}
		int oldSort = priorityVo.getSort();
		int newSort = jsonObj.getIntValue("sort");
		if(oldSort < newSort) {//往后移动
			priorityMapper.updateSortDecrement(oldSort, newSort);
		}else if(oldSort > newSort) {//往前移动
			priorityMapper.updateSortIncrement(newSort, oldSort);
		}
		priorityVo.setSort(newSort);
		priorityMapper.updatePriority(priorityVo);
		return null;
	}

}
