package codedriver.module.process.api.priority;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.exception.priority.PriorityNameRepeatException;
import codedriver.framework.process.exception.priority.PriorityNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.PriorityVo;
@Service
@Transactional
public class PrioritySaveApi extends ApiComponentBase {

	@Autowired
	private PriorityMapper priorityMapper;
	
	@Override
	public String getToken() {
		return "process/priority/save";
	}

	@Override
	public String getName() {
		return "优先级信息保存接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "优先级uuid"),
		@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "名称"),
		@Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", isRequired=true, desc = "状态"),
		@Param(name = "icon", type = ApiParamType.STRING, isRequired = true, desc = "图标"),
		@Param(name = "color", type = ApiParamType.STRING, isRequired = true, desc = "颜色"),
		@Param(name = "desc", type = ApiParamType.STRING, xss = true, desc = "描述"),
	})
	@Output({
		@Param(name="Return", type = ApiParamType.STRING, desc="优先级uuid")
	})
	@Description(desc = "优先级信息保存接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		PriorityVo priorityVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<PriorityVo>() {});
		if(priorityMapper.checkPriorityNameIsRepeat(priorityVo) > 0) {
			throw new PriorityNameRepeatException(priorityVo.getName());
		}
		
		Integer sort = priorityMapper.getMaxSort();
		if(sort == null) {
			sort = 0;
		}else {
			sort++;
		}
		priorityVo.setSort(sort);
		String uuid = jsonObj.getString("uuid");
		if(uuid != null) {
			if(priorityMapper.checkPriorityIsExists(uuid) == 0) {
				throw new PriorityNotFoundException(uuid);
			}
			priorityMapper.updatePriority(priorityVo);
		}else {
			priorityMapper.insertPriority(priorityVo);
		}
		return priorityVo.getUuid();
	}

}
