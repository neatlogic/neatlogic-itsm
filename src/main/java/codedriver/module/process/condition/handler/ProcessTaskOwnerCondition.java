package codedriver.module.process.condition.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ParamType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.FormHandlerType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.condition.ConditionVo;

@Component
public class ProcessTaskOwnerCondition extends ProcessTaskConditionBase implements IProcessTaskCondition{

	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private UserMapper userMapper;
	
	@Override
	public String getName() {
		return "owner";
	}

	@Override
	public String getDisplayName() {
		return "上报人";
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
		return 4;
	}

	@Override
	public ParamType getParamType() {
		return ParamType.ARRAY;
	}
	
	@Override
	public boolean predicate(ProcessTaskStepVo currentProcessTaskStepVo, ConditionVo conditionVo) {
		if(Expression.INCLUDE.getExpression().equals(conditionVo.getExpression())) {
			List<String> valueList = conditionVo.getValueList();
			if(!CollectionUtils.isEmpty(valueList)) {
				//解析valueList
				//["user#userUuid", "team#teamUuid","role#roleUuid"]
				List<String> userUuidList = new ArrayList<>();
				List<String> teamUuidList = new ArrayList<>();
				List<String> roleUuidList = new ArrayList<>();
				for(String value : valueList) {
					String[] split = value.split("#");
					if(GroupSearch.USER.getValue().equals(split[0])) {
						userUuidList.add(split[1]);
					}else if(GroupSearch.TEAM.getValue().equals(split[0])) {
						teamUuidList.add(split[1]);
					}else if(GroupSearch.ROLE.getValue().equals(split[0])) {
						roleUuidList.add(split[1]);
					}
				}	
				ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(currentProcessTaskStepVo.getProcessTaskId());
				if(userUuidList.contains(processTaskVo.getOwner())) {
					return true;
				}
				UserVo user = userMapper.getUserByUuid(processTaskVo.getOwner());
				if(roleUuidList.removeAll(user.getRoleUuidList())) {
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
