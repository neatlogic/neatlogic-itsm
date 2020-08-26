package codedriver.module.process.api.event;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.event.EventSolutionMapper;
import codedriver.framework.process.dto.event.EventSolutionVo;
import codedriver.framework.process.exception.event.EventSolutionNotFoundException;
import codedriver.framework.process.exception.event.EventSolutionRepeatException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


@AuthAction(name = "EVENT_SOLUTION_MODIFY")
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class EventSolutionSaveApi extends ApiComponentBase{

	@Autowired
	private EventSolutionMapper eventSolutionMapper;

	@Override
	public String getToken() {
		return "event/solution/save";
	}

	@Override
	public String getName() {
		return "保存解决方案";
	}

	@Override
	public String getConfig() {
		return null;
	}


	@Input({
			@Param(name = "id", type = ApiParamType.LONG, desc = "解决方案ID"),
			@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", desc = "解决方案名称",isRequired=true, xss=true),
			@Param(name = "isActive", type = ApiParamType.INTEGER,desc = "是否激活"),
			@Param(name = "content", type = ApiParamType.STRING,desc = "内容"),
			@Param(name = "eventTypeList", type = ApiParamType.JSONARRAY, desc = "关联的事件类型ID集合",isRequired = true)
	})
	@Output({@Param(name = "solutionId", type = ApiParamType.LONG, desc = "保存的解决方案ID")})
	@Description(desc = "保存解决方案")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject returnObj = new JSONObject();
		Long id = jsonObj.getLong("id");
		String name = jsonObj.getString("name");
		String content = jsonObj.getString("content");
		JSONArray eventTypeList = jsonObj.getJSONArray("eventTypeList");
		List<Long> eventTypeIds = eventTypeList.toJavaList(Long.class);
		EventSolutionVo eventSolutionVo = new EventSolutionVo();

		eventSolutionVo.setName(name);
		if(id == null){
			if(eventSolutionMapper.checkSolutionExistsByName(name) != null){
				throw new EventSolutionRepeatException(name);
			}
			eventSolutionVo.setContent(content);
			eventSolutionVo.setIsActive(1);
			eventSolutionVo.setFcu(UserContext.get().getUserUuid());
			eventSolutionVo.setFcd(new Date());
			eventSolutionVo.setLcu(UserContext.get().getUserUuid());
			eventSolutionVo.setLcd(new Date());
			eventSolutionMapper.insertSolution(eventSolutionVo);
			for(Long eventTypeId : eventTypeIds){
				eventSolutionMapper.insertEventTypeSolution(eventTypeId, eventSolutionVo.getId());
			}
		}else{
			if(eventSolutionMapper.checkSolutionExistsById(id) == null){
				throw new EventSolutionNotFoundException(id);
			}
			EventSolutionVo vo = eventSolutionMapper.checkSolutionExistsByName(name);
			if(vo != null && !vo.getId().equals(id)){
				throw new EventSolutionRepeatException(name);
			}
			Integer isActive = jsonObj.getInteger("isActive");
			eventSolutionVo.setId(id);
			eventSolutionVo.setIsActive(isActive);
			eventSolutionVo.setContent(content);
			eventSolutionVo.setLcu(UserContext.get().getUserUuid());
			eventSolutionVo.setLcd(new Date());
			eventSolutionMapper.updateSolutionById(eventSolutionVo);
			/** 关联事件类型 */
			eventSolutionMapper.deleteEventTypeSolution(id);
			for(Long eventTypeId : eventTypeIds){
				eventSolutionMapper.insertEventTypeSolution(eventTypeId,id);
			}

		}
		returnObj.put("solutionId", eventSolutionVo.getId());
		return returnObj;
	}
}
