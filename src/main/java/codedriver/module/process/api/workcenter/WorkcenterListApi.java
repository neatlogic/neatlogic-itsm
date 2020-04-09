package codedriver.module.process.api.workcenter;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserAuthVo;
import codedriver.framework.process.constvalue.ProcessWorkcenterType;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.auth.label.WORKCENTER_MODIFY;
import codedriver.module.process.service.WorkcenterService;

@AuthAction(name = "WORKCENTER_VIEW")
@Service
public class WorkcenterListApi extends ApiComponentBase {

	@Autowired
	WorkcenterMapper workcenterMapper;
	@Autowired
	UserMapper userMapper;
	
	@Autowired
	WorkcenterService workcenterService;
	
	@Override
	public String getToken() {
		return "workcenter/list";
	}

	@Override
	public String getName() {
		return "获取工单中心分类列表接口";
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
	@Description(desc = "获取工单中心分类列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<WorkcenterVo>  workcenterList = workcenterMapper.getWorkcenter(new WorkcenterVo(UserContext.get().getUserId(),UserContext.get().getRoleNameList(),UserContext.get().getUserId()));
		List<UserAuthVo> userAuthList = userMapper.searchUserAllAuthByUserAuth(new UserAuthVo(UserContext.get().getUserId(),WORKCENTER_MODIFY.class.getSimpleName()));
		Iterator<WorkcenterVo> it =workcenterList.iterator();
	    while (it.hasNext()) {
	        WorkcenterVo workcenter = it.next();
			if(workcenter.getType().equals(ProcessWorkcenterType.FACTORY.getValue())) {
				workcenter.setIsCanEdit(0);
			}else if(CollectionUtils.isNotEmpty(userAuthList)){
				workcenter.setIsCanEdit(1);
				workcenter.setIsCanRole(1);
			}else {
				if(UserContext.get().getUserId().equalsIgnoreCase(workcenter.getOwner())) {
					workcenter.setIsCanEdit(1);
				}else {
					workcenter.setIsCanEdit(0);
				}
				workcenter.setIsCanRole(0);
			}
			//查询数量
			workcenter.setCount(workcenterService.doSearchCount(new WorkcenterVo(JSONObject.parseObject(workcenter.getConditionConfig()))));
			workcenter.setConditionConfig(null);
		}
	    
	    
		return workcenterList.stream().sorted(Comparator.comparing(WorkcenterVo::getSort)).collect(Collectors.toList());
	}
}
