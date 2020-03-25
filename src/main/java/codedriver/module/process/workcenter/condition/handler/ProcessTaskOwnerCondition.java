package codedriver.module.process.workcenter.condition.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessExpression;
import codedriver.framework.process.constvalue.ProcessFormHandlerType;
import codedriver.framework.process.constvalue.ProcessWorkcenterColumn;
import codedriver.framework.process.constvalue.ProcessWorkcenterColumnType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.condition.ConditionVo;
import codedriver.framework.process.workcenter.condition.core.IWorkcenterCondition;

@Component
public class ProcessTaskOwnerCondition implements IWorkcenterCondition{

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private UserMapper userMapper;
	
	@Override
	public String getName() {
		return ProcessWorkcenterColumn.OWNER.getValue();
	}

	@Override
	public String getDisplayName() {
		return ProcessWorkcenterColumn.OWNER.getName();
	}

	@Override
	public String getHandler(String processWorkcenterConditionType) {
		return ProcessFormHandlerType.USERSELECT.toString();
	}
	
	@Override
	public String getType() {
		return ProcessWorkcenterColumnType.COMMON.getValue();
	}

	@Override
	public JSONObject getConfig() {
		JSONObject returnObj = new JSONObject();
		returnObj.put("isMultiple", true);
		return returnObj;
	}

	@Override
	public Integer getSort() {
		return 3;
	}

	@Override
	public List<ProcessExpression> getExpressionList() {
		return Arrays.asList(ProcessExpression.INCLUDE);
	}
	
	@Override
	public ProcessExpression getDefaultExpression() {
		return ProcessExpression.INCLUDE;
	}

	@Override
	public boolean predicate(ProcessTaskStepVo currentProcessTaskStepVo, ConditionVo conditionVo) {
		if(ProcessExpression.INCLUDE.getExpression().equals(conditionVo.getExpression())) {
			List<String> valueList = conditionVo.getValueList();
			if(!CollectionUtils.isEmpty(valueList)) {
				//解析valueList
				//["user#userId", "team#teamUuid","role#roleName"]
				List<String> userIdList = new ArrayList<>();
				List<String> teamUuidList = new ArrayList<>();
				List<String> roleNameList = new ArrayList<>();
				for(String value : valueList) {
					String[] split = value.split("#");
					if(GroupSearch.USER.getValue().equals(split[0])) {
						userIdList.add(split[1]);
					}else if(GroupSearch.TEAM.getValue().equals(split[0])) {
						teamUuidList.add(split[1]);
					}else if(GroupSearch.ROLE.getValue().equals(split[0])) {
						roleNameList.add(split[1]);
					}
				}	
				ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
				if(userIdList.contains(processTaskVo.getOwner())) {
					return true;
				}
				UserVo user = userMapper.getUserByUserId(processTaskVo.getOwner());
				if(roleNameList.removeAll(user.getRoleNameList())) {
					return true;
				}
				if(!CollectionUtils.isEmpty(user.getTeamList())) {
					for(TeamVo team : user.getTeamList()) {
						if(teamUuidList.contains(team.getUuid())) {
							return true;
						}
					}
				}
			}		
		}
		return false;
	}

}
