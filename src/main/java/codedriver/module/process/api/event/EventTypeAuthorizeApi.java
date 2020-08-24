package codedriver.module.process.api.event;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.AuthorityVo;
import codedriver.framework.process.dao.mapper.event.EventTypeMapper;
import codedriver.framework.process.dto.event.EventTypeVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@AuthAction(name = "EVENT_TYPE_MODIFY")
@Service
@Transactional
@OperationType(type = OperationTypeEnum.OPERATE)
public class EventTypeAuthorizeApi extends ApiComponentBase{

	@Autowired
	private EventTypeMapper eventTypeMapper;

	@Override
	public String getToken() {
		return "eventtype/auth/save";
	}

	@Override
	public String getName() {
		return "保存事件类型授权";
	}

	@Override
	public String getConfig() {
		return null;
	}


	@Input({
			@Param(name = "id", type = ApiParamType.LONG, desc = "事件类型ID",isRequired = true),
			@Param(name = "authorityList", type = ApiParamType.JSONARRAY, desc = "授权对象，可多选，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]")
	})
	@Description(desc = "保存事件类型授权")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {

		Long id = jsonObj.getLong("id");
		EventTypeVo eventType = new EventTypeVo();
		eventType.setId(id);
		//首先删除所有权限
		eventTypeMapper.deleteAuthorityByEventTypeId(id);
		JSONArray authorityArray = jsonObj.getJSONArray("authorityList");
		if(CollectionUtils.isNotEmpty(authorityArray)){
			List<String> authorityList = authorityArray.toJavaList(String.class);
			eventType.setAuthorityList(authorityList);
			List<AuthorityVo> authorityVoList = eventType.getAuthorityVoList();
			if(CollectionUtils.isNotEmpty(authorityVoList)){
				for(AuthorityVo authorityVo : authorityVoList) {
					eventTypeMapper.insertEventTypeAuthority(authorityVo,eventType.getId());
				}
			}
		}

		return null;
	}
}
