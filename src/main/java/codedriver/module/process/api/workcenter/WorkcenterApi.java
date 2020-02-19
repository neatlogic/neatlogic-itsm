package codedriver.module.process.api.workcenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.workcenter.dao.mapper.WorkcenterMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.workcenter.dto.WorkcenterVo;

@Service
public class WorkcenterApi extends ApiComponentBase {

	@Autowired
	WorkcenterMapper workcenterMapper;
	
	@Override
	public String getToken() {
		return "workcenter";
	}

	@Override
	public String getName() {
		return "获取工单中心分类接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		
	})
	@Output({
		@Param(name="workcenter", explode = WorkcenterVo.class, desc="分类信息")
	})
	@Description(desc = "获取工单中心分类接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		return workcenterMapper.getWorkcenter(new WorkcenterVo());
	}

}
