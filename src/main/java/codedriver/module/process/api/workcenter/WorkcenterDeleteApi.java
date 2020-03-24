package codedriver.module.process.api.workcenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.workcenter.dao.mapper.WorkcenterMapper;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Transactional
@Service
public class WorkcenterDeleteApi extends ApiComponentBase {

	@Autowired
	WorkcenterMapper workcenterMapper;
	
	@Override
	public String getToken() {
		return "workcenter/delete";
	}

	@Override
	public String getName() {
		return "工单中心分类删除接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="uuid", type = ApiParamType.STRING, desc="分类uuid",isRequired = true)
	})
	@Output({
		
	})
	@Description(desc = "工单中心分类删除接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		workcenterMapper.deleteWorkcenterRoleByUuid(uuid);
		workcenterMapper.deleteWorkcenterByUuid(uuid);
		workcenterMapper.deleteWorkcenterThead(new WorkcenterTheadVo(uuid,null));
		return null;
	}

}