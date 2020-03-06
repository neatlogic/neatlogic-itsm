package codedriver.module.process.groupsearch;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.restful.groupsearch.core.IGroupSearchHandler;
import codedriver.module.process.constvalue.UserType;
@Service
public class ProcessUserTypeGroupHandler implements IGroupSearchHandler {
	@Override
	public String getName() {
		return "processUserType";
	}
	
	@Override
	public String getHeader() {
		return getName()+"#";
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> search(JSONObject jsonObj) {
		List<String> userTypeList = new ArrayList<String>();
		for (UserType s : UserType.values()) {
			userTypeList.add(s.getValue());
		}
		return (List<T>) userTypeList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> reload(JSONObject jsonObj) {
		List<String> userTypeList = new ArrayList<String>();
		for(Object value :jsonObj.getJSONArray("valueList")) {
			if(value.toString().startsWith(getHeader())){
				userTypeList.add(value.toString().replace(getHeader(), ""));
			}
		}
		return (List<T>) userTypeList;
	}

	@Override
	public <T> JSONObject repack(List<T> userTypeList) {
		JSONObject userTypeObj = new JSONObject();
		userTypeObj.put("value", "processUserType");
		userTypeObj.put("text", "工单干系人");
		JSONArray userTypeArray = new JSONArray();
		for(T userType : userTypeList) {
			JSONObject userTypeTmp = new JSONObject();
			userTypeTmp.put("value", getHeader()+userType);
			userTypeTmp.put("text", UserType.getText(userType.toString()));
			userTypeArray.add(userTypeTmp);
		}
		userTypeObj.put("sort", getSort());
		userTypeObj.put("dataList", userTypeArray);
		return userTypeObj;
	}

	@Override
	public int getSort() {
		return 0;
	}

	@Override
	public Boolean isLimit() {
		return false;
	}
}
