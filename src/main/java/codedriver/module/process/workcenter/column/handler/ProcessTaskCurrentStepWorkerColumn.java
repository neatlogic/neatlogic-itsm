package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.*;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskStepUserSqlTable;
import codedriver.framework.process.workcenter.table.ProcessTaskStepWorkerSqlTable;
import codedriver.framework.process.workcenter.table.UserTable;
import codedriver.framework.process.workcenter.table.util.SqlTableUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

@Component
public class ProcessTaskCurrentStepWorkerColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{
	@Autowired
	UserMapper userMapper;
	@Autowired
	RoleMapper roleMapper;
	@Autowired
	TeamMapper teamMapper;
	@Override
	public String getName() {
		return "currentstepworker";
	}

	@Override
	public String getDisplayName() {
		return "当前步骤处理人";
	}

	@Override
	public Object getMyValue(JSONObject json) throws RuntimeException {
	    JSONArray stepArray = null;
        try {
         stepArray = (JSONArray) json.getJSONArray(ProcessWorkcenterField.STEP.getValue());
        }catch(Exception ex){
            return "";
        }
        String processTaskStatus = json.getString("status");
        if(CollectionUtils.isEmpty(stepArray)) {
            return CollectionUtils.EMPTY_COLLECTION;
        }
        JSONArray stepResultArray = JSONArray.parseArray(stepArray.toJSONString());
        ListIterator<Object> stepIterator = stepResultArray.listIterator();
        JSONArray workerArray = new JSONArray();
        List<String> workerList = new ArrayList<String>();
        while(stepIterator.hasNext()) {
            JSONObject currentStepJson = (JSONObject)stepIterator.next();
            String stepStatus =currentStepJson.getString("status");
            Integer isActive =currentStepJson.getInteger("isactive");
            if(ProcessTaskStatus.RUNNING.getValue().equals(processTaskStatus)&&(ProcessTaskStatus.DRAFT.getValue().equals(stepStatus)||(ProcessTaskStatus.PENDING.getValue().equals(stepStatus)&& isActive == 1)||ProcessTaskStatus.RUNNING.getValue().equals(stepStatus))) {
                JSONObject stepStatusJson = new JSONObject();
                stepStatusJson.put("name", stepStatus);
                stepStatusJson.put("text", ProcessTaskStatus.getText(stepStatus));
                stepStatusJson.put("color", ProcessTaskStatus.getColor(stepStatus));
                currentStepJson.put("status", stepStatusJson);
                JSONArray userTypeArray = currentStepJson.getJSONArray("usertypelist"); 
                if(CollectionUtils.isNotEmpty(userTypeArray)) {
                    ListIterator<Object> userTypeIterator = userTypeArray.listIterator();
                    while(userTypeIterator.hasNext()) {
                        JSONObject userTypeJson = (JSONObject) userTypeIterator.next();
                        //判断子任务|变更步骤
                        if(ProcessStepHandlerType.CHANGEHANDLE.getHandler().equals(currentStepJson.getString("handler"))
                            && ProcessUserType.MINOR.getValue().equals(userTypeJson.getString("usertype"))) {
                            userTypeJson.put("usertypename", "变更步骤");
                        }else if(ProcessUserType.MINOR.getValue().equals(userTypeJson.getString("usertype"))){
                            userTypeJson.put("usertypename", "子任务");
                        }
                        
                        JSONArray userArray = userTypeJson.getJSONArray("userlist");
                        JSONArray userArrayTmp = new JSONArray();
                        if(CollectionUtils.isNotEmpty(userArray)) {
                            List<String> userList = userArray.stream().map(object -> object.toString()).collect(Collectors.toList());
                            for(String user :userList) {
                                if(StringUtils.isNotBlank(user.toString())) {
                                    if(!workerList.contains(user+userTypeJson.getString("usertypename"))) {
                                        workerList.add(user+userTypeJson.getString("usertypename"));
                                        if(user.toString().startsWith(GroupSearch.USER.getValuePlugin())) {
                                            UserVo userVo =userMapper.getUserBaseInfoByUuid(user.toString().replaceFirst(GroupSearch.USER.getValuePlugin(), StringUtils.EMPTY));
                                            if(userVo != null) {
                                                JSONObject userJson = new JSONObject();
                                                userJson.put("workerVo",userVo != null ? JSON.parseObject(JSONObject.toJSONString(userVo)) : null);
//                                                userJson.put("type", GroupSearch.USER.getValue());
//                                                userJson.put("worker", user);
//                                                userJson.put("workername", userVo.getUserName());
//                                                userJson.put("workerAvatar", userVo.getAvatar());
//                                                userJson.put("workerVipLevel",userVo.getVipLevel());
                                                userJson.put("workTypename",userTypeJson.getString("usertypename"));
                                                userArrayTmp.add(userJson);
                                            }
                                        }else if(user.toString().startsWith(GroupSearch.ROLE.getValuePlugin())) {
                                            RoleVo roleVo = roleMapper.getRoleByUuid(user.toString().replaceFirst(GroupSearch.ROLE.getValuePlugin(), StringUtils.EMPTY));
                                            if(roleVo != null) {
                                                JSONObject roleJson = new JSONObject();
                                                JSONObject vo = new JSONObject();
                                                vo.put("initType",GroupSearch.ROLE.getValue());
                                                vo.put("uuid",roleVo.getUuid());
                                                vo.put("name",roleVo.getName());
                                                roleJson.put("workerVo",vo);
//                                                roleJson.put("type", GroupSearch.ROLE.getValue());
//                                                roleJson.put("worker", roleVo.getUuid());
//                                                roleJson.put("workername", roleVo.getName());
                                                userArrayTmp.add(roleJson);
                                            }
                                        }else if(user.toString().startsWith(GroupSearch.TEAM.getValuePlugin())) {
                                            TeamVo teamVo = teamMapper.getTeamByUuid(user.toString().replaceFirst(GroupSearch.TEAM.getValuePlugin(), StringUtils.EMPTY));
                                            if(teamVo != null) {
                                                JSONObject teamJson = new JSONObject();
                                                JSONObject vo = new JSONObject();
                                                vo.put("initType",GroupSearch.TEAM.getValue());
                                                vo.put("uuid",teamVo.getUuid());
                                                vo.put("name",teamVo.getName());
                                                teamJson.put("workerVo",vo);
//                                                teamJson.put("type", GroupSearch.TEAM.getValue());
//                                                teamJson.put("worker", teamVo.getUuid());
//                                                teamJson.put("workername", teamVo.getName());
                                                userArrayTmp.add(teamJson);
                                            }
                                        }
                                    }
                                }

                            }
                            workerArray.addAll(userArrayTmp);
                        }
                        
                    }
                }
            }else {
                stepIterator.remove();
            }
        }
        return workerArray;
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

	@Override
	public Object getSimpleValue(Object json) {
		StringBuilder sb = new StringBuilder();
		if(json != null){
			JSONArray array = JSONArray.parseArray(json.toString());
			if(CollectionUtils.isNotEmpty(array)){
				for(int i = 0;i < array.size();i++){
					sb.append(array.getJSONObject(i).getJSONObject("workerVo").getString("name") + ";");
				}
			}
		}
		return sb.toString();
	}

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        JSONArray workerArray = new JSONArray();
        List<ProcessTaskStepVo> stepVoList =  processTaskVo.getStepList();
        if(ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
            for (ProcessTaskStepVo stepVo : stepVoList) {
                if(ProcessTaskStatus.DRAFT.getValue().equals(stepVo.getStatus()) ||
                        ProcessTaskStatus.RUNNING.getValue().equals(stepVo.getStatus()) ||
                        ProcessTaskStatus.PENDING.getValue().equals(stepVo.getStatus())
                ) {
                    for (ProcessTaskStepWorkerVo workerVo : stepVo.getWorkerList()) {
                        JSONObject workerJson = new JSONObject();
                        if (ProcessStepHandlerType.CHANGEHANDLE.getHandler().equals(stepVo.getHandler())
                                && ProcessUserType.MINOR.getValue().equals(workerVo.getUserType())) {
                            workerJson.put("workTypename", "变更步骤");
                        } else if (ProcessUserType.MINOR.getValue().equals(workerVo.getUserType())) {
                            workerJson.put("workTypename", "子任务");
                        }
                        if (GroupSearch.USER.getValue().equals(workerVo.getUserType())) {
                            UserVo userVo = userMapper.getUserBaseInfoByUuid(workerVo.getUuid());
                            if (userVo != null) {
                                workerJson.put("workerVo", JSON.parseObject(JSONObject.toJSONString(userVo)));
                                workerArray.add(workerJson);
                            }
                        } else if (GroupSearch.TEAM.getValue().equals(workerVo.getUserType())) {
                            TeamVo teamVo = teamMapper.getTeamByUuid(workerVo.getUuid());
                            if (teamVo != null) {
                                JSONObject teamTmp = new JSONObject();
                                teamTmp.put("initType", GroupSearch.TEAM.getValue());
                                teamTmp.put("uuid", teamVo.getUuid());
                                teamTmp.put("name", teamVo.getName());
                                workerJson.put("workerVo", teamTmp);
                                workerArray.add(workerJson);
                            }
                        } else {
                            RoleVo roleVo = roleMapper.getRoleByUuid(workerVo.getUuid());
                            if (roleVo != null) {
                                JSONObject roleTmp = new JSONObject();
                                roleTmp.put("initType", GroupSearch.ROLE.getValue());
                                roleTmp.put("uuid", roleVo.getUuid());
                                roleTmp.put("name", roleVo.getName());
                                workerJson.put("workerVo", roleTmp);
                                workerArray.add(workerJson);
                            }
                        }
                    }

                    for (ProcessTaskStepUserVo userVo : stepVo.getUserList()) {
                        JSONObject userJson = new JSONObject();
                        if (ProcessStepHandlerType.CHANGEHANDLE.getHandler().equals(stepVo.getHandler())
                                && ProcessUserType.MINOR.getValue().equals(userVo.getUserType())) {
                            userJson.put("workTypename", "变更步骤");
                        } else if (ProcessUserType.MINOR.getValue().equals(userVo.getUserType())) {
                            userJson.put("workTypename", "子任务");
                        }
                        userJson.put("workerVo", userVo);
                        workerArray.add(userJson);
                    }
                }
            }
        }
        return workerArray;
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<TableSelectColumnVo>(){
            {
                add(new TableSelectColumnVo(new ProcessTaskStepSqlTable(), Arrays.asList(
                        new SelectColumnVo(ProcessTaskStepSqlTable.FieldEnum.ID.getValue(),"processTaskStepId"),
                        new SelectColumnVo(ProcessTaskStepSqlTable.FieldEnum.HANDLER.getValue(),"processTaskStepHandler")
                )));
                add(new TableSelectColumnVo(new ProcessTaskStepWorkerSqlTable(), Arrays.asList(
                        new SelectColumnVo(ProcessTaskStepWorkerSqlTable.FieldEnum.UUID.getValue(),"stepWorkerUuid"),
                        new SelectColumnVo(ProcessTaskStepWorkerSqlTable.FieldEnum.TYPE.getValue(),"stepWorkerType"),
                        new SelectColumnVo(ProcessTaskStepWorkerSqlTable.FieldEnum.USER_TYPE.getValue(),"stepWorkerUserType")
                )));
                add(new TableSelectColumnVo(new ProcessTaskStepUserSqlTable(), Arrays.asList(
                        new SelectColumnVo(ProcessTaskStepUserSqlTable.FieldEnum.STATUS.getValue(),"stepUserUserStatus"),
                        new SelectColumnVo(ProcessTaskStepUserSqlTable.FieldEnum.USER_TYPE.getValue(),"stepUserUserType")
                )));
                add(new TableSelectColumnVo(new UserTable(),"ptsuser", Arrays.asList(
                        new SelectColumnVo(UserTable.FieldEnum.USER_NAME.getValue(),"stepUserUserName"),
                        new SelectColumnVo(UserTable.FieldEnum.UUID.getValue(),"stepUserUserUuid"),
                        new SelectColumnVo(UserTable.FieldEnum.USER_INFO.getValue(),"stepUserUserInfo"),
                        new SelectColumnVo(UserTable.FieldEnum.VIP_LEVEL.getValue(),"stepUserUserVipLevel"),
                        new SelectColumnVo(UserTable.FieldEnum.PINYIN.getValue(),"stepUserUserPinyin")
                )));
            }
        };
    }


    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList() {
        return SqlTableUtil.getStepUserJoinTableSql();
    }
}
