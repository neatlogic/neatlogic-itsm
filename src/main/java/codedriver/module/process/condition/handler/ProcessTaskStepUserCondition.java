package codedriver.module.process.condition.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessExpression;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessFormHandlerType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.condition.ConditionVo;

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
		return ProcessFormHandlerType.USERSELECT.toString();
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
	public List<ProcessExpression> getExpressionList() {
		return Arrays.asList(ProcessExpression.INCLUDE,ProcessExpression.EXCLUDE);
	}

	@Override
	public ProcessExpression getDefaultExpression() {
		return ProcessExpression.INCLUDE;
	}

	@Override
	public boolean predicate(ProcessTaskStepVo currentProcessTaskStepVo, ConditionVo workcenterConditionVo) {
		// 条件步骤没有处理人
		return false;
	}
	
	@Override
	protected String getMyEsWhere(ConditionVo condition,List<ConditionVo> conditionList) {
		List<ConditionVo> stepStatusConditionList = conditionList.stream().filter(con->con.getName().equals(ProcessWorkcenterField.STEP_STATUS.getValue())).collect(Collectors.toList());
		if(CollectionUtils.isNotEmpty(stepStatusConditionList)) {
			ConditionVo stepStatusCondition = stepStatusConditionList.get(0);
			List<String> stepStatusValueList = stepStatusCondition.getValueList();
			List<String> stepUserValueList = condition.getValueList();
			List<String> userStepStatusList = new ArrayList<String>();
			for(String user : stepUserValueList) {
				for(String stepStatus : stepStatusValueList) {
					userStepStatusList.add(String.format("%s#%s", user,stepStatus));
					//如果是待处理状态，则需额外匹配角色和组
					if(stepStatus.equals(ProcessTaskStatus.PENDING.getValue())) {
						UserVo userVo = userMapper.getUserByUserId(user.replace(GroupSearch.USER.getValuePlugin(),""));
						if(userVo != null) {
							List<String> teamList = userVo.getTeamNameList();
							if(CollectionUtils.isNotEmpty(teamList)) {
								for(String team : teamList) {
									userStepStatusList.add(String.format("%s#%s", GroupSearch.TEAM.getValuePlugin()+team,stepStatus));
								}
							}
							List<String> roleList = userVo.getRoleNameList();
							if(CollectionUtils.isNotEmpty(roleList)) {
								for(String role : roleList) {
									userStepStatusList.add(String.format("%s#%s", GroupSearch.ROLE.getValuePlugin()+role,stepStatus));
								}
							}
						}
					}
					
				}
			}
			String value = String.join("','",userStepStatusList);
			return String.format(ProcessExpression.INCLUDE.getExpressionEs(),ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.USER_STEPSTATUS.getValue()),String.format("'%s'",  value));
		}
		return null;
	}
}
