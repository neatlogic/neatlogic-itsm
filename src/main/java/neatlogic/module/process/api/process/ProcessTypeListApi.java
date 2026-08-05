package neatlogic.module.process.api.process;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ProcessTypeVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTypeListApi extends PrivateApiComponentBase {

	@Resource
	private ProcessMapper processMapper;
	
	@Override
	public String getToken() {
		return "process/type/list";
	}

	@Override
	public String getName() {
		return "nmpap.processtypelistapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Output({@Param(name="Return", explode = ProcessTypeVo[].class, desc = "nmpap.processtypelistapi.output.param.desc.return.name")})
	@Description(desc = "nmpap.processtypelistapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		
		//TODO  确定是否有用，删除？
		List<ProcessTypeVo> processTyepList = processMapper.getAllProcessType();
		return processTyepList;
	}

}
