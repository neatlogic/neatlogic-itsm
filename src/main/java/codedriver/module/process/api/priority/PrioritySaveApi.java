package codedriver.module.process.api.priority;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.exception.priority.PriorityNameRepeatException;
import codedriver.framework.process.exception.priority.PriorityNotFoundException;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class PrioritySaveApi extends PrivateApiComponentBase {

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
		
		String uuid = jsonObj.getString("uuid");
		if(uuid != null) {
			PriorityVo priority = priorityMapper.getPriorityByUuid(uuid);
			if(priority == null) {
				throw new PriorityNotFoundException(uuid);
			}
			priorityVo.setSort(priority.getSort());
			priorityMapper.updatePriority(priorityVo);
		}else {
			Integer sort = priorityMapper.getMaxSort();
			if(sort == null) {
				sort = 0;
			}
			sort++;
			priorityVo.setSort(sort);
			priorityMapper.insertPriority(priorityVo);
		}
		return priorityVo.getUuid();
	}

}
