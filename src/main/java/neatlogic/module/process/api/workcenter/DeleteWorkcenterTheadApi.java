package neatlogic.module.process.api.workcenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.workcenter.dto.WorkcenterTheadVo;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.workcenter.WorkcenterMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class DeleteWorkcenterTheadApi extends PrivateApiComponentBase {

	@Resource
	WorkcenterMapper workcenterMapper;
	
	@Override
	public String getToken() {
		return "workcenter/thead/delete";
	}

	@Override
	public String getName() {
		return "nmpaw.deleteworkcentertheadapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="uuid", type = ApiParamType.STRING, desc="common.typeuuid",isRequired = true)
	})
	@Output({
		
	})
	@Description(desc = "nmpaw.deleteworkcentertheadapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		WorkcenterVo workcenterVo = JSON.toJavaObject(jsonObj,WorkcenterVo.class);
		WorkcenterVo oldWorkcenterVo = workcenterMapper.getWorkcenterByUuid(workcenterVo.getUuid());
		if (oldWorkcenterVo != null) {
			workcenterMapper.deleteWorkcenterThead(new WorkcenterTheadVo(workcenterVo.getUuid(), UserContext.get().getUserUuid(true)));
		}
		return null;
	}

}
