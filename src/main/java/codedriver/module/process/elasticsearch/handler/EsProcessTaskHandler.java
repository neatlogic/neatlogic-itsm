package codedriver.module.process.elasticsearch.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.DeviceType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.dto.condition.ConditionGroupRelVo;
import codedriver.framework.dto.condition.ConditionGroupVo;
import codedriver.framework.dto.condition.ConditionRelVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.elasticsearch.core.EsHandlerBase;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.module.process.service.WorkcenterService;

@Service
public class EsProcessTaskHandler extends EsHandlerBase {
	Logger logger = LoggerFactory.getLogger(EsProcessTaskHandler.class);
	
	@Autowired
	WorkcenterMapper workcenterMapper;
	@Autowired
	FormMapper formMapper;
	@Autowired
	ProcessTaskMapper processTaskMapper;
	@Autowired
	ChannelMapper channelMapper;
	@Autowired
	CatalogMapper catalogMapper;
	@Autowired
	WorktimeMapper worktimeMapper;
	@Autowired
	WorkcenterService workcenterService;
	@Autowired
    UserMapper userMapper;

	@Override
	public String getDocument() {
		return "processtask";
	}
	
	@Override
    public String getDocumentId() {
        return "taskId";
    }
		
	@Override
	public JSONObject mySave(JSONObject paramJson) {
		 Long taskId = paramJson.getLong("processTaskId");
		 /** 获取工单信息 **/
		 ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(taskId);
		 JSONObject esObject = null;
		 if(processTaskVo != null) {
			 esObject = workcenterService.getProcessTaskESObject(processTaskVo);
		 }
		 return esObject;
	}

    @Override
    public <T> String mySql(T t) {
        WorkcenterVo workcenterVo = (WorkcenterVo)t;
        JSONArray resultColumnArray = workcenterVo.getResultColumnList();
        String selectColumn = "*";
        //
        if (!CollectionUtils.isEmpty(resultColumnArray)) {
            List<String> columnResultList = new ArrayList<String>();
            for (Object column : resultColumnArray) {
                columnResultList.add(ProcessWorkcenterField.getConditionValue(column.toString()));
                selectColumn = String.join(",", columnResultList);
            }
        }
        String where = assembleWhere(workcenterVo);
        //待办条件
        if (workcenterVo.getIsMeWillDo() == 1) {
            String meWillDoCondition = getMeWillDoCondition(workcenterVo);
            if (StringUtils.isBlank(where)) {
                where = " where " + meWillDoCondition;
            } else {
                where = where + " and " + meWillDoCondition;
            }
        }
        //设备服务过滤
        if(!DeviceType.ALL.getValue().equals(workcenterVo.getDevice())) {
            String deviceCondition = getChannelDeviceCondition(workcenterVo);
            if (StringUtils.isNotBlank(deviceCondition)) {
                if (StringUtils.isBlank(where)) {
                    where = " where " + deviceCondition;
                } else {
                    where = where + " and " + deviceCondition;
                }
            }
        }
        String orderBy = "order by common.starttime desc";
        String sql =
            String.format("select %s from %s %s %s limit %d,%d", selectColumn, TenantContext.get().getTenantUuid(),
                where, orderBy, workcenterVo.getStartNum(), workcenterVo.getPageSize());
        return sql;
    }
    
    
    /**
     * 
     *  获取设备（移动端|pc端）服务过滤条件
     */
    private String getChannelDeviceCondition(WorkcenterVo workcenterVo) {
        String deviceCondition = StringUtils.EMPTY;
        ChannelVo channelVo = new ChannelVo();
        channelVo.setSupport(workcenterVo.getDevice());
        channelVo.setUserUuid(UserContext.get().getUserUuid(true));
        channelVo.setNeedPage(false);
        List<ChannelVo>  channelList = channelMapper.searchChannelList(channelVo);
        List<String> channelUuidList = new ArrayList<String>();
        for(ChannelVo channel : channelList) {
            channelUuidList.add(channel.getUuid());
        }
        if(CollectionUtils.isNotEmpty(channelUuidList)){
            String channelUuids = String.format("'%s'", StringUtil.join(channelUuidList.toArray(new String[0]), "','"));
            deviceCondition = String.format(Expression.INCLUDE.getExpressionEs(),ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.CHANNEL.getValue()),channelUuids);;
            
        }
        return deviceCondition;
    }

    /**
     * TODO 需要改成过滤条件联动 附加我的待办条件
     * 
     * @return
     */
    @Deprecated
    private String getMeWillDoCondition(WorkcenterVo workcenterVo) {
        String meWillDoSql = StringUtils.EMPTY;
        // status
        List<String> statusList = Arrays.asList(ProcessTaskStatus.RUNNING.getValue()).stream()
            .map(object -> object.toString()).collect(Collectors.toList());
        String statusSql = String.format(Expression.INCLUDE.getExpressionEs(),
            ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.STATUS.getValue()),
            String.format(" '%s' ", String.join("','", statusList)));
        // common.step.filtstatus
        List<String> stepStatusList =
            Arrays.asList(ProcessTaskStatus.PENDING.getValue(), ProcessTaskStatus.RUNNING.getValue()).stream()
                .map(object -> object.toString()).collect(Collectors.toList());
        String stepStatusSql = String.format(Expression.INCLUDE.getExpressionEs(),
            ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.STEP.getValue()) + ".filtstatus",
            String.format(" '%s' ", String.join("','", stepStatusList)));
        // common.step.usertypelist.userlist
        List<String> userList = new ArrayList<String>();
        userList.add(GroupSearch.USER.getValuePlugin() + UserContext.get().getUserUuid());
        // 如果是待处理状态，则需额外匹配角色和组
        UserVo userVo = userMapper.getUserByUuid(UserContext.get().getUserUuid());
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
        meWillDoSql = String.format(
            " ([%s and %s and common.step.usertypelist.list.value contains any ( %s ) and common.step.usertypelist.list.status contains any ('pending','doing') and not common.step.isactive contains any (0,-1)])",
            statusSql, stepStatusSql, String.format(" '%s' ", String.join("','", userList)));
        // meWillDoSql = String.format(" common.step.usertypelist.list.value contains any ( %s ) and
        // common.step.usertypelist.list.status contains any ('pending','doing')", String.format(" '%s' ",
        // String.join("','",userList))) ;
        return meWillDoSql;
    }
    
    
    /**
     * 拼接where条件
     * 
     * @param workcenterVo
     * @return
     */
    @SuppressWarnings("unchecked")
    private String assembleWhere(WorkcenterVo workcenterVo) {
        Map<String, String> groupRelMap = new HashMap<String, String>();
        StringBuilder whereSb = new StringBuilder();
        whereSb.append(" where (");
        List<ConditionGroupRelVo> groupRelList = workcenterVo.getConditionGroupRelList();
        if (CollectionUtils.isNotEmpty(groupRelList)) {
            // 将group 以连接表达式 存 Map<fromUuid_toUuid,joinType>
            for (ConditionGroupRelVo groupRel : groupRelList) {
                groupRelMap.put(groupRel.getFrom() + "_" + groupRel.getTo(), groupRel.getJoinType());
            }
        }
        List<ConditionGroupVo> groupList = workcenterVo.getConditionGroupList();
        if (CollectionUtils.isEmpty(groupList)) {
            return "";
        }
        String fromGroupUuid = null;
        String toGroupUuid = groupList.get(0).getUuid();
        for (ConditionGroupVo group : groupList) {
            Map<String, String> conditionRelMap = new HashMap<String, String>();
            if (fromGroupUuid != null) {
                toGroupUuid = group.getUuid();
                whereSb.append(groupRelMap.get(fromGroupUuid + "_" + toGroupUuid));
            }
            whereSb.append("(");
            List<ConditionRelVo> conditionRelList = group.getConditionRelList();
            if (CollectionUtils.isNotEmpty(conditionRelList)) {
                // 将condition 以连接表达式 存 Map<fromUuid_toUuid,joinType>
                for (ConditionRelVo conditionRel : conditionRelList) {
                    conditionRelMap.put(conditionRel.getFrom() + "_" + conditionRel.getTo(),
                        conditionRel.getJoinType());
                }
            }
            List<ConditionVo> conditionList = group.getConditionList();
            // 按es and 组合,数组元素之间用or
            JSONArray conditionRelationArray = new JSONArray();
            ConditionVo fromCondition = null;
            ArrayList<ConditionVo> andConditionList = new ArrayList<ConditionVo>();
            // 统计 common.step 开头的condition count
            int nestedBasisCount = 0;
            for (int i = 0; i < conditionList.size(); i++) {
                ConditionVo condition = conditionList.get(i);
                // 关于我的 必定会 nested
                if (condition.getName().endsWith(ProcessWorkcenterField.ABOUTME.getValue())) {
                    nestedBasisCount = nestedBasisCount + 2;
                }
                if (!condition.getType().equals("form") && ProcessWorkcenterField.getConditionValue(condition.getName())
                    .startsWith(ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.STEP.getValue()))) {
                    nestedBasisCount++;
                }
                if (fromCondition == null) {
                    fromCondition = condition;
                    if (conditionList.size() == 1) {
                        JSONObject conditionRelationJson = new JSONObject();
                        andConditionList.add(condition);
                        conditionRelationJson.put("list", andConditionList);
                        conditionRelationJson.put("isNested", nestedBasisCount > 0 ? true : false);
                        conditionRelationArray.add(conditionRelationJson);
                        andConditionList = new ArrayList<ConditionVo>();
                    }
                    andConditionList.add(condition);
                    continue;
                }
                String conditionUuid = condition.getUuid();
                String conditionType = conditionRelMap.get(fromCondition.getUuid() + "_" + conditionUuid);
                if (i != conditionList.size() - 1 && conditionType.equals("and")) {
                    andConditionList.add(condition);
                }
                if (conditionType.equals("or")) {// 如果是or 则另新建数组元素
                    JSONObject conditionRelationJson = new JSONObject();
                    conditionRelationJson.put("list", andConditionList);
                    conditionRelationJson.put("isNested", false);
                    conditionRelationArray.add(conditionRelationJson);
                    andConditionList = new ArrayList<ConditionVo>();
                    if (i != conditionList.size() - 1) {
                        andConditionList.add(condition);
                    }
                }
                if (i == conditionList.size() - 1) {
                    andConditionList.add(condition);
                    Collections.sort(andConditionList, new Comparator<Object>() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            try {
                                ConditionVo obj1 = (ConditionVo)o1;
                                ConditionVo obj2 = (ConditionVo)o2;
                                return ProcessWorkcenterField.getConditionValue(obj1.getName())
                                    .compareTo(ProcessWorkcenterField.getConditionValue(obj2.getName()));
                            } catch (Exception ex) {

                            }
                            return 0;
                        }
                    });
                    ArrayList<ConditionVo> tmpConditionList = new ArrayList<ConditionVo>();
                    tmpConditionList.addAll(andConditionList);
                    JSONObject conditionRelationJson = new JSONObject();
                    conditionRelationJson.put("list", tmpConditionList);
                    if (nestedBasisCount > 1) {
                        conditionRelationJson.put("isNested", true);
                    } else {
                        conditionRelationJson.put("isNested", false);
                    }
                    conditionRelationArray.add(conditionRelationJson);
                    andConditionList = new ArrayList<ConditionVo>();
                    nestedBasisCount = 0;
                }
                fromCondition = condition;
            }
            for (int orIndex = 0; orIndex < conditionRelationArray.size(); orIndex++) {
                Object conditionRelation = conditionRelationArray.get(orIndex);
                JSONObject conditionRelationJson = (JSONObject)JSONObject.toJSON(conditionRelation);
                ArrayList<ConditionVo> andConditionTmpList = (ArrayList<ConditionVo>)conditionRelationJson.get("list");
                ArrayList<ConditionVo> formConditionList = new ArrayList<ConditionVo>();
                Boolean isNested = (Boolean)conditionRelationJson.get("isNested");
                if (isNested) {
                    whereSb.append(" [");
                }

                for (int andIndex = 0; andIndex < andConditionTmpList.size(); andIndex++) {
                    ConditionVo condition = andConditionTmpList.get(andIndex);
                    IProcessTaskCondition workcenterCondition =
                        (IProcessTaskCondition)ConditionHandlerFactory.getHandler(condition.getName());
                    if (condition.getType().equals("form")) {
                        formConditionList.add(condition);
                    } else {
                        String conditionWhere = workcenterCondition.getEsWhere(andConditionTmpList, andIndex);
                        whereSb.append(conditionWhere);
                        if (andIndex != andConditionTmpList.size() - 1
                            && !andConditionTmpList.get(andIndex + 1).getType().equals("form")) {
                            whereSb.append(" and ");
                        }
                    }
                }
                if (isNested) {
                    whereSb.append(" ]");
                }
                // form
                if (formConditionList.size() > 0 && andConditionTmpList.size() != formConditionList.size()) {
                    whereSb.append(" and ");
                }
                for (int formIndex = 0; formIndex < formConditionList.size(); formIndex++) {
                    IProcessTaskCondition workcenterCondition =
                        (IProcessTaskCondition)ConditionHandlerFactory.getHandler("form");
                    String conditionWhere = workcenterCondition.getEsWhere(formConditionList, formIndex);
                    whereSb.append(conditionWhere);
                    if (formIndex != formConditionList.size() - 1) {
                        whereSb.append(" and ");
                    }
                }
                if (orIndex != conditionRelationArray.size() - 1) {
                    whereSb.append(" or ");
                }
            }

            whereSb.append(")");
            fromGroupUuid = toGroupUuid;
        }
        return whereSb.toString() + ")";
    }

    
}
