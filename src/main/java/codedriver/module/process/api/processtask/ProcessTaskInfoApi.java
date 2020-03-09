package codedriver.module.process.api.processtask;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ProcessTaskVo;
@Service
public class ProcessTaskInfoApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "processtask/info";
	}

	@Override
	public String getName() {
		return "工单基本信息获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")
	})
	@Output({
		@Param(explode = ProcessTaskVo.class, desc = "工单信息")
	})
	@Description(desc = "工单基本信息获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return null;
	}

}
