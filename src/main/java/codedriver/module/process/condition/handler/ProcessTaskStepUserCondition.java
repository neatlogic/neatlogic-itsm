package codedriver.module.process.condition.handler;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.*;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.condition.core.ProcessTaskConditionBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import codedriver.framework.process.workcenter.table.util.SqlTableUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ProcessTaskStepUserCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {
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
        JSONObject config = new JSONObject();
        config.put("type", FormHandlerType.USERSELECT.toString());
        config.put("groupList", Collections.singletonList("user"));
        config.put("multiple", true);
        /** 以下代码是为了兼容旧数据结构，前端有些地方还在用 **/
        config.put("isMultiple", true);
        return config;
    }

    @Override
    public Integer getSort() {
        return 12;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
    }

    @Override
    public String getMyEsName() {
        return String.format(" %s.%s", getType(), "step.usertypelist.userlist");
    }

    @Override
    protected String getMyEsWhere(Integer index, List<ConditionVo> conditionList) {
        ConditionVo condition = conditionList.get(index);
        //List<ConditionVo> stepStatusConditionList = conditionList.stream().filter(con->con.getName().equals(ProcessWorkcenterField.STEP_STATUS.getValue())).collect(Collectors.toList());
        //if(CollectionUtils.isNotEmpty(stepStatusConditionList)) {
        //ConditionVo stepStatusCondition = stepStatusConditionList.get(0);
//			List<String> stepStatusValueList = new ArrayList<>();
//			if(stepStatusCondition.getValueList() instanceof String) {
//				stepStatusValueList.add((String)stepStatusCondition.getValueList());
//			}else if(stepStatusCondition.getValueList() instanceof List){
//				List<String> valueList = JSON.parseArray(JSON.toJSONString(stepStatusCondition.getValueList()), String.class);
//				stepStatusValueList.addAll(valueList);
//			}
        List<String> stepUserValueList = new ArrayList<>();
        if (condition.getValueList() instanceof String) {
            stepUserValueList.add((String) condition.getValueList());
        } else if (condition.getValueList() instanceof List) {
            List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
            stepUserValueList.addAll(valueList);
        }
        List<String> userList = new ArrayList<String>();
        for (String user : stepUserValueList) {
            if (user.equals(GroupSearch.COMMON.getValuePlugin() + UserType.LOGIN_USER.getValue())) {
                user = GroupSearch.USER.getValuePlugin() + UserContext.get().getUserUuid();
            }
            //for(String stepStatus : stepStatusValueList) {
            userList.add(user);
            //如果是待处理状态，则需额外匹配角色和组
            //if(stepStatus.equals(ProcessTaskStatus.PENDING.getValue())) {
            UserVo userVo = userMapper.getUserByUuid(user.replace(GroupSearch.USER.getValuePlugin(), ""));
            if (userVo != null) {
                List<String> teamList = userVo.getTeamUuidList();
                if (CollectionUtils.isNotEmpty(teamList)) {
                    for (String team : teamList) {
                        userList.add(GroupSearch.TEAM.getValuePlugin() + team);
                    }
                }
                List<String> roleUuidList = userVo.getRoleUuidList();
                if (CollectionUtils.isNotEmpty(roleUuidList)) {
                    for (String roleUuid : roleUuidList) {
                        userList.add(GroupSearch.ROLE.getValuePlugin() + roleUuid);
                    }
                }
            }
            //}

            //}
        }
        String value = String.join("','", userList);
        //排除开始节点的处理人
        return String.format(Expression.INCLUDE.getExpressionEs(), this.getEsName(), String.format("'%s'", value)) + " and not common.step.usertypelist.type = 'start' ";
//		}else {
//		    List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
//	        //替换“当前登录人标识”为当前登录用户 
//	        String loginUser = GroupSearch.COMMON.getValuePlugin() + UserType.LOGIN_USER.getValue();
//	        if(valueList.contains(loginUser)) {
//	            Iterator<String>  valueIterator = valueList.iterator();
//	            if(valueIterator.hasNext()) {
//	                String value = valueIterator.next();
//	                if(value.equals(loginUser)) {
//	                    valueIterator.remove();
//	                    valueList.add(GroupSearch.USER.getValuePlugin()+UserContext.get().getUserUuid());
//	                }
//	            }
//	        }
//	        String value = String.join("','", valueList);
//	        if(StringUtils.isNotBlank(value.toString())) {
//	            value = String.format("'%s'",  value);
//	        }
//	        return String.format(Expression.INCLUDE.getExpressionEs(),this.getEsName(),value);
//		}
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void getSqlConditionWhere(List<ConditionVo> conditionList, Integer index, StringBuilder sqlSb) {
        ConditionVo condition = conditionList.get(index);
        List<String> stepUserValueList = new ArrayList<>();
        if (condition.getValueList() instanceof String) {
            stepUserValueList.add((String) condition.getValueList());
        } else if (condition.getValueList() instanceof List) {
            List<String> valueList = JSON.parseArray(JSON.toJSONString(condition.getValueList()), String.class);
            stepUserValueList.addAll(valueList);
        }
        List<String> userList = new ArrayList<String>();
        List<String> teamList = new ArrayList<String>();
        List<String> roleList = new ArrayList<String>();
        for (String user : stepUserValueList) {
            user = user.replace(GroupSearch.USER.getValuePlugin(), "");
            if (user.equals(GroupSearch.COMMON.getValuePlugin() + UserType.LOGIN_USER.getValue())) {
                user = UserContext.get().getUserUuid();
            }
            //如果是待处理状态，则需额外匹配角色和组
            UserVo userVo = userMapper.getUserByUuid(user);
            if (userVo != null) {
                userList.add(user);
                teamList = userVo.getTeamUuidList();
                roleList = userVo.getRoleUuidList();
            }
        }
        sqlSb.append(" (");
        //非开始节点
        sqlSb.append(Expression.getExpressionSql(Expression.UNEQUAL.getExpression(), new ProcessTaskStepSqlTable().getShortName(), ProcessTaskStepSqlTable.FieldEnum.TYPE.getValue(), ProcessStepHandlerType.START.getHandler()));
        sqlSb.append(" and (");
        //补充待处理人sql 条件
        getProcessingTaskOfMineSqlWhere(sqlSb, userList, teamList, roleList);
        sqlSb.append(" )) ");

    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList(WorkcenterVo workcenterVo) {
        return SqlTableUtil.getStepUserJoinTableSql();
    }
}
