package codedriver.module.process.api.workcenter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.process.exception.workcenter.WorkcenterNoAuthException;
import codedriver.framework.process.exception.workcenter.WorkcenterParamException;
import codedriver.framework.process.workcenter.dao.mapper.WorkcenterMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.auth.label.WORKCENTER_MODIFY;
import codedriver.module.process.constvalue.ProcessWorkcenterType;
import codedriver.module.process.workcenter.dto.WorkcenterRoleVo;
import codedriver.module.process.workcenter.dto.WorkcenterVo;

@Transactional
@Service
public class WorkcenterSaveApi extends ApiComponentBase {

	@Autowired
	WorkcenterMapper workcenterMapper;
	@Autowired
	UserMapper userMapper;	
	
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
		@Param(name="name", type = ApiParamType.STRING, desc="分类名",isRequired = true,xss = true),
		@Param(name="conditionConfig", type = ApiParamType.JSONOBJECT, desc="分类过滤配置，json格式",isRequired = true),
		@Param(name="valueList", type = ApiParamType.JSONARRAY, desc="授权列表", isRequired = false)
	})
	@Output({
		
	})
	@Description(desc = "工单中心分类新增接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		String name = jsonObj.getString("name");
		String userId = UserContext.get().getUserId();
		WorkcenterVo workcenterVo = new WorkcenterVo(name);
		//重复name判断
		/*workcenterVo.setUuid(uuid);
		if(workcenterMapper.checkWorkcenterNameIsRepeat(name,uuid)>0) {
			throw new WorkcenterNameRepeatException(name);
		}*/
		workcenterVo.setUuid(uuid);
		//保存、更新分类
		JSONArray valueList = jsonObj.getJSONArray("valueList");
		if(CollectionUtils.isNotEmpty(valueList)) {
			//判断是否有管理员权限
			if(CollectionUtils.isEmpty(userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(userId,WORKCENTER_MODIFY.class.getSimpleName())))) {
				throw new WorkcenterNoAuthException("授权");
			}
			workcenterVo.setType(ProcessWorkcenterType.SYSTEM.getValue());
			workcenterMapper.deleteWorkcenterRoleByUuid(workcenterVo.getUuid());
			workcenterMapper.deleteWorkcenterOwnerByUuid(workcenterVo.getUuid());
			//更新角色
			for(Object value:valueList) {
				String[] roles = value.toString().split("#");
				WorkcenterRoleVo workcenterRoleVo = new WorkcenterRoleVo();
				workcenterRoleVo.setWorkcenterUuid(workcenterVo.getUuid());
				if(roles[0].equals("role")) {
					workcenterRoleVo.setRoleName(roles[1]);
				}else if(roles[0].equals("user")) {
					workcenterRoleVo.setUserId(roles[1]);
				}else {
					throw new WorkcenterParamException("valueList");
				}
				workcenterMapper.insertWorkcenterRole(workcenterRoleVo);
			}
		}else {
			workcenterVo.setType(ProcessWorkcenterType.CUSTOM.getValue());
			workcenterMapper.insertWorkcenterOwner(userId, workcenterVo.getUuid());
		}
		if(StringUtils.isBlank(uuid)) {
			workcenterVo.setConditionConfig(jsonObj.getString("conditionConfig"));
			workcenterMapper.insertWorkcenter(workcenterVo);
		}else { 
			workcenterMapper.updateWorkcenter(workcenterVo);
		}
		
		return null;
	}

}