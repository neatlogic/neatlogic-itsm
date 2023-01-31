package neatlogic.module.process.api.priority;

import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.process.exception.priority.PriorityIsInvokedException;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.process.auth.PRIORITY_MODIFY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.dao.mapper.PriorityMapper;
import neatlogic.framework.process.dto.PriorityVo;
import neatlogic.framework.process.exception.priority.PriorityNameRepeatException;
import neatlogic.framework.process.exception.priority.PriorityNotFoundException;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PRIORITY_MODIFY.class)
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
			/** 如果禁用优先级，先判断有没有被服务引用，有引用则不能禁用 **/
			if(priorityVo.getIsActive() == 0 && priority.getIsActive() == 1){
				if(priorityMapper.checkPriorityIsInvoked(uuid) > 0){
					throw new PriorityIsInvokedException(priority.getName());
				}
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

	public IValid name(){
		return value -> {
			PriorityVo priorityVo = JSON.toJavaObject(value, PriorityVo.class);
			if(priorityMapper.checkPriorityNameIsRepeat(priorityVo) > 0) {
				return new FieldValidResultVo(new PriorityNameRepeatException(priorityVo.getName()));
			}
			return new FieldValidResultVo();
		};
	}

}
