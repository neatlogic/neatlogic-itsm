package neatlogic.module.process.api.priority;

import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.dao.mapper.PriorityMapper;
import neatlogic.framework.process.dto.PriorityVo;
import neatlogic.framework.process.exception.priority.PriorityNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.process.auth.PRIORITY_MODIFY;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PRIORITY_MODIFY.class)
public class PriorityMoveApi extends PrivateApiComponentBase {
	
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
