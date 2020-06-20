package codedriver.module.process.api.workcenter;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.process.constvalue.ProcessWorkcenterType;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.exception.workcenter.WorkcenterNoAuthException;
import codedriver.framework.process.exception.workcenter.WorkcenterNotFoundException;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.auth.label.WORKCENTER_MODIFY;

@Transactional
@Service
public class WorkcenterConditionSaveApi extends ApiComponentBase {

	@Autowired
	WorkcenterMapper workcenterMapper;
	@Autowired
	UserMapper userMapper;	
	@Autowired
	RoleMapper roleMapper;
	
	@Override
	public String getToken() {
		return "workcenter/condition/save";
	}

	@Override
	public String getName() {
		return "工单中心分类条件修改接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name="uuid", type = ApiParamType.STRING, desc="分类uuid",isRequired = true),
		@Param(name="conditionConfig", type = ApiParamType.JSONOBJECT, desc="分类过滤配置，json格式",isRequired = true)
	})
	@Output({
		
	})
	@Description(desc = "工单中心分类条件修改接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		String userUuid = UserContext.get().getUserUuid(true);
		List<WorkcenterVo> workcenterList = workcenterMapper.getWorkcenterByNameAndUuid(null, uuid);
		if(CollectionUtils.isEmpty(workcenterList)) {
			throw new WorkcenterNotFoundException(uuid);
		}
		WorkcenterVo workcenterVo = workcenterList.get(0);
		if(ProcessWorkcenterType.FACTORY.getValue().equals(workcenterVo.getType())) {
			throw new WorkcenterNoAuthException("修改出厂分类");
		}else if(ProcessWorkcenterType.SYSTEM.getValue().equals(workcenterVo.getType())&&CollectionUtils.isEmpty(userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(userUuid,WORKCENTER_MODIFY.class.getSimpleName())))&&CollectionUtils.isEmpty(roleMapper.getRoleByUuidList(UserContext.get().getRoleUuidList()))) {
			throw new WorkcenterNoAuthException("管理");
		}else if(ProcessWorkcenterType.CUSTOM.getValue().equals(workcenterVo.getType())&&!workcenterVo.getOwner().equalsIgnoreCase(userUuid)) {
			throw new WorkcenterNoAuthException("修改个人分类");
		}
		workcenterVo.setConditionConfig(jsonObj.getString("conditionConfig"));
		workcenterMapper.updateWorkcenterCondition(workcenterVo);
		return null;
	}

}