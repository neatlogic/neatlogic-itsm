package codedriver.module.process.api.workcenter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.process.exception.workcenter.WorkcenterNameRepeatException;
import codedriver.framework.process.workcenter.dao.mapper.WorkcenterMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
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
		@Param(name="name", type = ApiParamType.STRING, desc="分类名",isRequired = false),
		@Param(name="isPrivate", type = ApiParamType.INTEGER, desc="类型，1：自定义分类，0：系统分类",isRequired = false),
		@Param(name="roleList", type = ApiParamType.JSONARRAY, desc="授权列表", isRequired = false),
		@Param(name="roleList[0].name", type = ApiParamType.STRING, desc="授权角色名")
	})
	@Output({
		
	})
	@Description(desc = "工单中心分类新增接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		WorkcenterVo workcenterVo = JSON.toJavaObject(jsonObj, WorkcenterVo.class);
		//重复name判断
		List<WorkcenterVo> workcenterList = workcenterMapper.getWorkcenter(workcenterVo);
		if(workcenterList.size()>0) {
			new WorkcenterNameRepeatException(workcenterVo.getName());
		}
		workcenterMapper.insertWorkcenter(workcenterVo);
		for(RoleVo roleVo: workcenterVo.getRoleList()) {
			workcenterMapper.insertWorkcenterRole(workcenterVo.getUuid(),roleVo.getName());
		}
		return null;
	}

}
