package codedriver.module.process.api.workcenter;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.workcenter.dto.WorkcenterConditionGroupRelVo;
import codedriver.module.process.workcenter.dto.WorkcenterConditionGroupVo;
import codedriver.module.process.workcenter.dto.WorkcenterVo;

@Service
public class WorkcenterSaveApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "workcenter/save";
	}

	@Override
	public String getName() {
		return "工单中心分类保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="uuid", type = ApiParamType.STRING, desc="分类uuid"),
		@Param(name="name", type = ApiParamType.STRING, desc="分类名",isRequired = true, xss = true),
		@Param(name="is_private", type = ApiParamType.INTEGER, desc="是否属于私人分类，1：是，0：否",isRequired = true, xss = true),
		@Param(name="type", type = ApiParamType.STRING, desc="分类的所属分类:'status','user','custom'"),
		@Param(name="sort", type = ApiParamType.INTEGER, desc="排序id",isRequired = true, xss = true),
		@Param(name="conditionConfig", type = ApiParamType.STRING, desc="条件json")
	})
	@Output({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "分类uuid")
	})
	@Description(desc = "获取工单中心分类接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
