package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskFocusUsersColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{

	@Override
	public String getName() {
		return "focususers";
	}

	/** 此列在工单中心不需要中文名，也不需要可拖拽，所以displayName为空且disable为true */
	@Override
	public String getDisplayName() {
		return "";
	}

	@Override
	public Boolean getDisabled() {
		return true;
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		JSONObject focusUserObj = new JSONObject();
		JSONArray focusUsers = json.getJSONArray(this.getName());
		focusUserObj.put("focusUserList",focusUsers);
		boolean isCurrentUserFocus = false;
		if(CollectionUtils.isNotEmpty(focusUsers)){
			String userUuid = "user#" + UserContext.get().getUserUuid();
			isCurrentUserFocus = focusUsers.contains(userUuid);
		}
		focusUserObj.put("isCurrentUserFocus",isCurrentUserFocus ? 1 : 0);
		return focusUserObj;
	}

	@Override
	public Boolean allowSort() {
		return false;
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public String getClassName() {
		return null;
	}

	@Override
	public Integer getSort() {
		return -1;
	}

	@Override
	public Object getSimpleValue(Object json) {
		return null;
	}
}
