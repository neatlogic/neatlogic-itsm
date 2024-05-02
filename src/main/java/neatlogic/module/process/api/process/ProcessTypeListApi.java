package neatlogic.module.process.api.process;

import java.util.List;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.process.dto.ProcessTypeVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTypeListApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;
	
	@Override
	public String getToken() {
		return "process/type/list";
	}

	@Override
	public String getName() {
		return "流程类型列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({@Param(name="Return", explode = ProcessTypeVo[].class, desc = "流程类型列表")})
	@Description(desc = "流程类型列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		
		//TODO  确定是否有用，删除？
		List<ProcessTypeVo> processTyepList = processMapper.getAllProcessType();
		return processTyepList;
	}

}
