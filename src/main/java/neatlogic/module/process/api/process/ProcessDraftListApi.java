package neatlogic.module.process.api.process;

import com.alibaba.fastjson.JSONArray;
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
		return "流程草稿列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "processUuid", type = ApiParamType.STRING, desc = "流程uuid，获取已正式保存的当前流程的草稿列表，如不传，获取未正式保存的所有草稿列表")
	})
	@Output({
		@Param(name="Return",explode=ProcessDraftVo[].class,desc="流程草稿列表")
	})
	@Description(desc = "流程草稿列表接口，最后更新时间2020-02-18 14:55，修改参数说明及输出参数列表")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String processUuid = jsonObj.getString("processUuid");
		if (StringUtils.isBlank(processUuid)) {
			return new JSONArray();
		}
		ProcessDraftVo processDraftVo = new ProcessDraftVo();
		processDraftVo.setFcu(UserContext.get().getUserUuid(true));
		processDraftVo.setProcessUuid(processUuid);
		return processMapper.getProcessDraftList(processDraftVo);
	}

}
