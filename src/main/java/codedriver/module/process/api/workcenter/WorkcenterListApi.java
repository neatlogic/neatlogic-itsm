package codedriver.module.process.api.workcenter;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.process.workcenter.WorkcenterHandler;
import codedriver.framework.process.workcenter.dao.mapper.WorkcenterMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.auth.label.WORKCENTER_MODIFY;
import codedriver.module.process.constvalue.ProcessWorkcenterType;
import codedriver.module.process.workcenter.dto.WorkcenterVo;

@AuthAction(name = "WORKCENTER_VIEW")
@Service
public class WorkcenterListApi extends ApiComponentBase {

	@Autowired
	WorkcenterMapper workcenterMapper;
	@Autowired
	UserMapper userMapper;
	
	@Override
	public String getToken() {
		return "workcenter/list";
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
		List<WorkcenterVo>  workcenterList = workcenterMapper.getWorkcenter(new WorkcenterVo(UserContext.get().getUserId(),UserContext.get().getRoleNameList(),UserContext.get().getUserId()));
		List<UserAuthVo> userAuthList = userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(UserContext.get().getUserId(),WORKCENTER_MODIFY.class.getSimpleName()));
		Iterator<WorkcenterVo> it =workcenterList.iterator();
	    while (it.hasNext()) {
	        WorkcenterVo workcenter = it.next();
			if(workcenter.getType().equals(ProcessWorkcenterType.FACTORY.getValue())) {
				workcenter.setIsCanEdit(0);
			}else if(CollectionUtils.isNotEmpty(userAuthList)&&workcenter.getType().equals(ProcessWorkcenterType.SYSTEM.getValue())){
				workcenter.setIsCanEdit(1);
				workcenter.setIsCanRole(1);
			}else {
				workcenter.setIsCanEdit(1);
				workcenter.setIsCanRole(0);
			}
			//查询数量
			workcenter.setCount(WorkcenterHandler.doSearchCount(new WorkcenterVo(JSONObject.parseObject(workcenter.getConditionConfig()))));
		}
		return workcenterList;
	}
}
