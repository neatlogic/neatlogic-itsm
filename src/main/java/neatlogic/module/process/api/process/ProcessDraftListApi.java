package neatlogic.module.process.api.process;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_MODIFY;
import neatlogic.framework.process.dto.ProcessDraftVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = PROCESS_MODIFY.class)
public class ProcessDraftListApi extends PrivateApiComponentBase {

	@Resource
	private ProcessMapper processMapper;
	
	@Override
	public String getToken() {
		return "process/draft/list";
	}

	@Override
	public String getName() {
		return "nmpap.processdraftlistapi.getname";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "processUuid", type = ApiParamType.STRING, desc = "nmpap.processdraftlistapi.input.param.desc.processuuid")
	})
	@Output({
		@Param(name="Return",explode=ProcessDraftVo[].class,desc="nmpap.processdraftlistapi.output.param.desc.return.name")
	})
	@Description(desc = "nmpap.processdraftlistapi.getname")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String processUuid = jsonObj.getString("processUuid");
		ProcessDraftVo processDraftVo = new ProcessDraftVo();
		processDraftVo.setFcu(UserContext.get().getUserUuid(true));
		if (StringUtils.isNotBlank(processUuid)) {
			processDraftVo.setProcessUuid(processUuid);
		}
		return processMapper.getProcessDraftList(processDraftVo);
	}

}
