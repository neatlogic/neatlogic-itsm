package codedriver.module.process.api.workcenter;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.process.workcenter.dao.mapper.WorkcenterMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.workcenter.dto.WorkcenterVo;

@Transactional
@Service
public class WorkcenterMultiSaveApi extends ApiComponentBase {

	@Autowired
	WorkcenterMapper workcenterMapper;
	
	@Override
	public String getToken() {
		return "workcenter/multi/save";
	}

	@Override
	public String getName() {
		return "工单中心分类管理保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="workcenterList", type = ApiParamType.JSONARRAY, desc="分类列表",isRequired = true),
		@Param(name="workcenterList[0].uuid", type = ApiParamType.STRING, desc="分类uuid"),
		@Param(name="workcenterList[0].name", type = ApiParamType.STRING, desc="分类名"),
		@Param(name="workcenterList[0].isPrivate", type = ApiParamType.INTEGER, desc="类型，1：自定义分类，0：系统分类"),
		@Param(name="workcenterList[0].roleList[0].name", type = ApiParamType.STRING, desc="授权列表"),
		@Param(name="workcenterList[0].sort", type = ApiParamType.INTEGER, desc="排序")
	})
	@Output({
		
	})
	@Description(desc = "工单中心分类管理保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Map<String,String> conditionConfigMap =  workcenterMapper.getWorkcenterConditionConfig();
		//删除分类
		workcenterMapper.deleteWorkcenter();
		workcenterMapper.deleteWorkcenterRole();
		//循环insert
		JSONArray workcenterArray = jsonObj.getJSONArray("workcenterList");
		for(Object workcenter : workcenterArray) {
			WorkcenterVo workcenterVo = JSON.toJavaObject( (JSONObject)JSON.toJSON(workcenter), WorkcenterVo.class);
			workcenterVo.setConditionConfig(conditionConfigMap.get(workcenterVo.getUuid()));
			workcenterMapper.insertWorkcenter(workcenterVo);
			for(RoleVo roleVo: workcenterVo.getRoleList()) {
				workcenterMapper.insertWorkcenterRole(workcenterVo.getUuid(),roleVo.getName());
			}
		}
		return null;
	}

}
