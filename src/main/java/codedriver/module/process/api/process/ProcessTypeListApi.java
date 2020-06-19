package codedriver.module.process.api.process;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ProcessTypeVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class ProcessTypeListApi extends ApiComponentBase {

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
