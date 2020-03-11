package codedriver.module.process.api.process;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.IsActived;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ProcessDraftVo;
@Service
@IsActived
public class ProcessDraftListApi extends ApiComponentBase {

	@Autowired
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
		ProcessDraftVo processDraftVo = new ProcessDraftVo();
		processDraftVo.setFcu(UserContext.get().getUserId());
		if(jsonObj.containsKey("processUuid")) {
			processDraftVo.setProcessUuid(jsonObj.getString("processUuid"));
		}
		List<ProcessDraftVo> processDraftList = processMapper.getProcessDraftList(processDraftVo);
		return processDraftList;
	}

}
