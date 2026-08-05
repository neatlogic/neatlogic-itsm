package neatlogic.module.process.api.priority;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PRIORITY_MODIFY;
import neatlogic.framework.process.dto.PriorityVo;
import neatlogic.framework.process.exception.priority.PriorityNotFoundException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.PriorityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PRIORITY_MODIFY.class)
public class PriorityMoveApi extends PrivateApiComponentBase {
	
	@Resource
	private PriorityMapper priorityMapper;

	@Override
	public String getToken() {
		return "process/priority/move";
	}

	@Override
	public String getName() {
		return "nmpap.prioritymoveapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "nmpap.prioritymoveapi.input.param.desc.uuid"),
		@Param(name = "sort", type = ApiParamType.INTEGER, isRequired = true, desc = "nmpap.prioritymoveapi.input.param.desc.sort")
	})
	@Description(desc = "nmpap.prioritymoveapi.getname")
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
