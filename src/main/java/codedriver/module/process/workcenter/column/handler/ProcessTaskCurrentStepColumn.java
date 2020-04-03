package codedriver.module.process.workcenter.column.handler;

import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.workcenter.column.core.IWorkcenterColumn;
import codedriver.framework.process.workcenter.column.core.WorkcenterColumnBase;

@Component
public class ProcessTaskCurrentStepColumn extends WorkcenterColumnBase implements IWorkcenterColumn{
	@Autowired
	UserMapper userMapper;
	@Override
	public String getName() {
		return "currentstep";
	}

	@Override
	public String getDisplayName() {
		return "当前步骤";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
		JSONArray stepArray = (JSONArray) json.getJSONArray(ProcessWorkcenterField.STEP.getValue());
		if(CollectionUtils.isEmpty(stepArray)) {
			return CollectionUtils.EMPTY_COLLECTION;
		}
		JSONArray stepResultArray = JSONArray.parseArray(stepArray.toJSONString());
		ListIterator<Object> stepIterator = stepResultArray.listIterator();
		while(stepIterator.hasNext()) {
			JSONObject currentStepJson = (JSONObject)stepIterator.next();
			String stepStatus =currentStepJson.getString("status");
			if(ProcessTaskStatus.PENDING.getValue().equals(stepStatus)||ProcessTaskStatus.RUNNING.getValue().equals(stepStatus)) {
				JSONObject stepStatusJson = new JSONObject();
				stepStatusJson.put("name", stepStatus);
				stepStatusJson.put("text", ProcessTaskStatus.getText(stepStatus));
				stepStatusJson.put("color", ProcessTaskStatus.getColor(stepStatus));
				currentStepJson.put("status", stepStatusJson);
				//去掉待处理,但未开始的user/role/team
				JSONArray userTypeArray = currentStepJson.getJSONArray("usertypelist"); 
				if(CollectionUtils.isNotEmpty(userTypeArray)) {
					ListIterator<Object> userTypeIterator = userTypeArray.listIterator();
					while(userTypeIterator.hasNext()) {
						JSONObject userTypeJson = (JSONObject) userTypeIterator.next();
						if(userTypeJson.getString("usertype").equals(ProcessTaskStatus.PENDING.getValue())) {
							userTypeIterator.remove();
						}else {
							JSONArray userArray = userTypeJson.getJSONArray("userlist");
							JSONArray userArrayTmp = new JSONArray();
							if(CollectionUtils.isNotEmpty(userArray)) {
								List<String> userList = userArray.stream().map(object -> object.toString()).collect(Collectors.toList());
								for(String user :userList) {
									if(StringUtils.isNotBlank(user.toString())) {
										UserVo userVo =userMapper.getUserByUserId(user.toString().replaceFirst(GroupSearch.USER.getValuePlugin(), StringUtils.EMPTY));
										if(userVo != null) {
											JSONObject userJson = new JSONObject();
											userJson.put("userid", user);
											userJson.put("username", userVo.getUserName());
											userArrayTmp.add(userJson);
										}
									}
									
								}
								userTypeJson.put("userlist", userArrayTmp);
							}
						}
					}
				}
			}else {
				stepIterator.remove();
			}
		}
		return stepResultArray;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getSort() {
		return 5;
	}
}
