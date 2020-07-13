package codedriver.module.process.condition.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;

@Component
public class ProcessTaskStepUserCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{
	@Autowired
	UserMapper userMapper;

	@Override
	public String getName() {
		return "stepuser";
	}

	@Override
	public String getDisplayName() {
		return "处理人";
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return FormHandlerType.USERSELECT.toString();
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
		JSONObject returnObj = new JSONObject();
		returnObj.put("isMultiple", true);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 10;
	}

	@Override
	public ParamType getParamType() {
		return ParamType.ARRAY;
	}
	
	@Override
	protected String getMyEsWhere(Integer index,List<ConditionVo> conditionList) {
		ConditionVo condition = conditionList.get(index);
		List<ConditionVo> stepStatusConditionList = conditionList.stream().filter(con->con.getName().equals(ProcessWorkcenterField.STEP_STATUS.getValue())).collect(Collectors.toList());
		if(CollectionUtils.isNotEmpty(stepStatusConditionList)) {
			ConditionVo stepStatusCondition = stepStatusConditionList.get(0);
			List<String> stepStatusValueList = new ArrayList<>();
			if(stepStatusCondition.getValueList() instanceof String) {
				stepStatusValueList.add((String)stepStatusCondition.getValueList());
			}else if(stepStatusCondition.getValueList() instanceof List){
				List<String> valueList = JSON.parseArray(JSON.toJSONString(stepStatusCondition.getValueList()), String.class);
				stepStatusValueList.addAll(valueList);
			}
			List<String> stepUserValueList = new ArrayList<>();
			if(condition.getValueList() instanceof String) {
				stepUserValueList.add((String)condition.getValueList());
			}else if(condition.getValueList() instanceof List){
				List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
				stepUserValueList.addAll(valueList);
			}
			List<String> userList = new ArrayList<String>();
			for(String user : stepUserValueList) {
				for(String stepStatus : stepStatusValueList) {
					userList.add(user);
					//如果是待处理状态，则需额外匹配角色和组
					if(stepStatus.equals(ProcessTaskStatus.PENDING.getValue())) {
						UserVo userVo = userMapper.getUserByUuid(user.replace(GroupSearch.USER.getValuePlugin(),""));
						if(userVo != null) {
							List<String> teamList = userVo.getTeamNameList();
							if(CollectionUtils.isNotEmpty(teamList)) {
								for(String team : teamList) {
									userList.add(GroupSearch.TEAM.getValuePlugin()+team);
								}
							}
							List<String> roleUuidList = userVo.getRoleUuidList();
							if(CollectionUtils.isNotEmpty(roleUuidList)) {
								for(String roleUuid : roleUuidList) {
									userList.add(GroupSearch.ROLE.getValuePlugin() + roleUuid);
								}
							}
						}
					}
					
				}
			}
			String value = String.join("','",userList);
			return String.format(Expression.INCLUDE.getExpressionEs(),ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.STEP_USER.getValue()),String.format("'%s'",  value));
		}
		return null;
	}

	@Override
	public Object valueConversionText(Object value, JSONObject config) {
		// TODO Auto-generated method stub
		return null;
	}
}
