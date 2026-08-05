package neatlogic.module.process.api.priority;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.PriorityVo;
import neatlogic.framework.process.exception.priority.PriorityNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.PriorityMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class PriorityGetApi extends PrivateApiComponentBase {
	
	@Resource
	private PriorityMapper priorityMapper;
	
	@Override
	public String getToken() {
		return "process/priority/get";
	}

	@Override
	public String getName() {
		return "nmpap.prioritygetapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "nmpap.prioritygetapi.input.param.desc.uuid")
	})
	@Output({
		@Param(name="Return", explode = PriorityVo.class, desc="nmpap.prioritygetapi.output.param.desc.return.name")
	})
	@Description(desc = "nmpap.prioritygetapi.getname")
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
