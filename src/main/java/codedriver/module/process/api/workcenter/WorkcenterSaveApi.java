package codedriver.module.process.api.workcenter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.exception.workcenter.WorkcenterNameRepeatException;
import codedriver.framework.process.workcenter.dao.mapper.WorkcenterMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.workcenter.dto.WorkcenterRoleVo;
import codedriver.module.process.workcenter.dto.WorkcenterVo;

@Transactional
@Service
public class WorkcenterSaveApi extends ApiComponentBase {

	@Autowired
	WorkcenterMapper workcenterMapper;
	
	@Override
	public String getToken() {
		return "workcenter/save";
	}

	@Override
	public String getName() {
		return "工单中心分类新增接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="uuid", type = ApiParamType.STRING, desc="分类uuid"),
		@Param(name="name", type = ApiParamType.STRING, desc="分类名",isRequired = true,xss = true),
		@Param(name="conditionConfig", type = ApiParamType.JSONOBJECT, desc="分类过滤配置，json格式",isRequired = true),
		@Param(name="valueList", type = ApiParamType.JSONARRAY, desc="授权列表", isRequired = true)
	})
	@Output({
		
	})
	@Description(desc = "工单中心分类新增接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		String name = jsonObj.getString("name");
		WorkcenterVo workcenterVo = new WorkcenterVo(name);
		//重复name判断
		workcenterVo.setUuid(uuid);
		List<WorkcenterVo> workcenterList = workcenterMapper.getWorkcenter(workcenterVo);
		if(workcenterList.size()>0) {
			throw new WorkcenterNameRepeatException(name);
		}
		//保存、更新分类
		JSONArray valueList = jsonObj.getJSONArray("valueList");
		if(valueList != null && valueList.size()>0) {
			workcenterVo.setIsPrivate(0);
		}else {
			workcenterVo.setIsPrivate(1);
		}
		if(uuid == null) {
			workcenterVo.setConditionConfig(jsonObj.getString("conditionConfig"));
			workcenterMapper.insertWorkcenter(workcenterVo);
		}else { 
			workcenterMapper.deleteWorkcenterRoleByUuid(workcenterVo.getUuid());
			workcenterMapper.updateWorkcenter(workcenterVo);
		}
		//更新角色
		for(Object value:valueList) {
			String[] roles = value.toString().split("#");
			WorkcenterRoleVo workcenterRoleVo = new WorkcenterRoleVo();
			workcenterRoleVo.setWorkcenterUuid(workcenterVo.getUuid());
			if(roles[0].equals("role")) {
				workcenterRoleVo.setRoleName(roles[1]);
			}else if(roles[0].equals("user")) {
				workcenterRoleVo.setUserId(roles[1]);
			}
			workcenterMapper.insertWorkcenterRole(workcenterRoleVo);
			
		}
		return null;
	}

}