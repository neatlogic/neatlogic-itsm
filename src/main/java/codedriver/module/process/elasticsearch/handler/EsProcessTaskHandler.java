package codedriver.module.process.elasticsearch.handler;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.constvalue.DeviceType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.constvalue.UserType;
import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.dto.condition.ConditionGroupRelVo;
import codedriver.framework.dto.condition.ConditionGroupVo;
import codedriver.framework.dto.condition.ConditionRelVo;
import codedriver.framework.dto.condition.ConditionVo;
import codedriver.framework.elasticsearch.core.ElasticSearchHandlerBase;
import codedriver.framework.process.condition.core.IProcessTaskCondition;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dao.mapper.*;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.elasticsearch.constvalue.ESHandler;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.module.process.auth.label.PROCESSTASK_MODIFY;
import codedriver.module.process.service.WorkcenterService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.QueryResultSet;
import com.techsure.multiattrsearch.query.QueryResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class EsProcessTaskHandler extends ElasticSearchHandlerBase<WorkcenterVo, Object> {
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
        return ESHandler.PROCESSTASK.getValue();
    }

    @Override
    public JSONObject mySave(Long taskId) {
        /** 获取工单信息 **/
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(taskId);
        JSONObject esObject = new JSONObject();
        if (processTaskVo != null) {
            esObject = workcenterService.getProcessTaskESObject(processTaskVo);
        } else {
            throw new ProcessTaskNotFoundException(taskId.toString());
        }
        return esObject;
    }

    @Override
    public String buildSql(WorkcenterVo workcenterVo) {
        // WorkcenterVo workcenterVo = (WorkcenterVo)t;

        JSONArray resultColumnArray = workcenterVo.getResultColumnList();
        String selectColumn = "*";
        //选择展示字段
        if (!CollectionUtils.isEmpty(resultColumnArray)) {
            List<String> columnResultList = new ArrayList<String>();
            for (Object column : resultColumnArray) {
                columnResultList.add(((IProcessTaskCondition)ConditionHandlerFactory.getHandler(column.toString())).getEsName());
                selectColumn = String.join(",", columnResultList);
            }
        }
        String where = assembleWhere(workcenterVo);
        // 待办条件
        where = getMeWillDoCondition(workcenterVo,where);
        
        // 设备服务过滤
        where = getChannelDeviceCondition(workcenterVo,where);
        
        //隐藏工单过滤
        if (!AuthActionChecker.check(PROCESSTASK_MODIFY.class.getSimpleName())) {
            where = getIsShowCondition(workcenterVo,where);
        }
        
        String orderBy = assembleOrderBy(workcenterVo);
        String sql =
            String.format("select %s from %s %s %s limit %d,%d", selectColumn, TenantContext.get().getTenantUuid(),
                where, orderBy, workcenterVo.getStartNum(), workcenterVo.getPageSize());
        return sql;
    }
    
    /*
     * 隐藏工单过滤
     */
    private String getIsShowCondition(WorkcenterVo workcenterVo,String where) {
        String isShowCondition = String.format(Expression.UNEQUAL.getExpressionEs(), ((IProcessTaskCondition)ConditionHandlerFactory.getHandler(ProcessWorkcenterField.IS_SHOW.getValue())).getEsName(), 0);
        if (StringUtils.isBlank(where)) {
            where = " where " + isShowCondition;
        } else {
            where = where + " and " + isShowCondition;
        }
        return where;
    }

    /**
     * 
     * 获取设备（移动端|pc端）服务过滤条件
     */
    private String getChannelDeviceCondition(WorkcenterVo workcenterVo,String where) {
        //如果是pc端，则可以查看所有工单，包括移动端的
        if(DeviceType.PC.getValue().equals(workcenterVo.getDevice())) {
            return where;
        }
        String deviceCondition = StringUtils.EMPTY;
        ChannelVo channelVo = new ChannelVo();
        channelVo.setSupport(workcenterVo.getDevice());
        channelVo.setUserUuid(UserContext.get().getUserUuid(true));
        channelVo.setNeedPage(false);
        List<ChannelVo> channelList = channelMapper.searchChannelList(channelVo);
        List<String> channelUuidList = new ArrayList<String>();
        for (ChannelVo channel : channelList) {
            channelUuidList.add(channel.getUuid());
        }
        if (CollectionUtils.isNotEmpty(channelUuidList)) {
            String channelUuids = String.format("'%s'", StringUtil.join(channelUuidList.toArray(new String[0]), "','"));
            deviceCondition = String.format(Expression.INCLUDE.getExpressionEs(),
                ((IProcessTaskCondition)ConditionHandlerFactory.getHandler(ProcessWorkcenterField.CHANNEL.getValue())).getEsName(), channelUuids);

        }
        
        if (!DeviceType.ALL.getValue().equals(workcenterVo.getDevice())) {
            if (StringUtils.isNotBlank(deviceCondition)) {
                if (StringUtils.isBlank(where)) {
                    where = " where " + deviceCondition;
                } else {
                    where = where + " and " + deviceCondition;
                }
            }
        }
        return where;
    }

    /**
     * TODO 需要改成过滤条件联动 附加我的待办条件
     * 
     * @return
     */
    @Deprecated
    private String getMeWillDoCondition(WorkcenterVo workcenterVo,String where) {
        String meWillDoSql = StringUtils.EMPTY;
        // status
        List<String> statusList = Arrays.asList(ProcessTaskStatus.DRAFT.getValue(),ProcessTaskStatus.RUNNING.getValue()).stream()
            .map(object -> object.toString()).collect(Collectors.toList());
        String statusSql = String.format(Expression.INCLUDE.getExpressionEs(),
            ((IProcessTaskCondition)ConditionHandlerFactory.getHandler(ProcessWorkcenterField.STATUS.getValue())).getEsName(),
            String.format(" '%s' ", String.join("','", statusList)));
        // common.step.filtstatus
        List<String> stepStatusList =
            Arrays.asList(ProcessTaskStatus.DRAFT.getValue(),ProcessTaskStatus.PENDING.getValue(), ProcessTaskStatus.RUNNING.getValue()).stream()
                .map(object -> object.toString()).collect(Collectors.toList());
        String stepStatusSql = String.format(Expression.INCLUDE.getExpressionEs(),
            ProcessWorkcenterField.getConditionValue(ProcessWorkcenterField.STEP.getValue())+ ".status",
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
        
        
        if (workcenterVo.getIsProcessingOfMine() == 1) {
            if (StringUtils.isBlank(where)) {
                where = " where " + meWillDoSql;
            } else {
                where = where + " and " + meWillDoSql;
            }
        }
        return where;
    }

    /**
    * @Author 89770
    * @Time 2020年10月19日  
    * @Description:  拼接order条件 
    * @Param 
    * @return
     */
    private String assembleOrderBy(WorkcenterVo workcenterVo) {
        StringBuilder orderSb = new StringBuilder(" order by ");
        List<String> orderList = new ArrayList<String>();
        
        JSONArray sortJsonArray = workcenterVo.getSortList();
        if(CollectionUtils.isNotEmpty(sortJsonArray)) {
            for(Object sortObj : sortJsonArray) {
                JSONObject sortJson = JSONObject.parseObject(sortObj.toString());
                for(Entry<String, Object> entry : sortJson.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue().toString();
                    IProcessTaskCondition condition = (IProcessTaskCondition)ConditionHandlerFactory.getHandler(key);
                    if(condition != null) {
                        orderList.add(condition.getEsOrderBy(value));
                    }else {
                        //Condition 不存在
                    }
                }
            }
            orderSb.append(String.join(",", orderList));
        }else {
            orderSb.append(String.format(" %s DESC ",((IProcessTaskCondition)ConditionHandlerFactory.getHandler(ProcessWorkcenterField.STARTTIME.getValue())).getEsName()));
        }
        return orderSb.toString();
    }
    
    
    /**
     * 拼接where条件
     * TODO 需重构
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
                // 关于我的 或 当前组/部 必定会 nested
                if (condition.getName().endsWith(ProcessWorkcenterField.ABOUTME.getValue())
                    ||condition.getValueList().toString().contains(GroupSearch.COMMON.getValuePlugin()+UserType.LOGIN_DEPARTMENT.getValue())
                    ||condition.getValueList().toString().contains(GroupSearch.COMMON.getValuePlugin()+UserType.LOGIN_TEAM.getValue())) {
                    nestedBasisCount = nestedBasisCount + 2;
                }
                String[] valueList = null;
                if(condition.getValueList()!= null && condition.getValueList() instanceof JSONArray) {
                    List<String> tmpList = JSONObject.parseArray(condition.getValueList().toString(), String.class);
                    valueList =  tmpList.toArray(new String[tmpList.size()]);
                }else {
                    valueList = new String[] {condition.getValueList().toString()};
                }
                if (!condition.getType().equals("form") && ((IProcessTaskCondition)ConditionHandlerFactory.getHandler(condition.getName())).getEsName(valueList).trim()
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
                //当相近and连接的condition装载完后，按name 排序
                if (i == conditionList.size() - 1) {
                    andConditionList.add(condition);
                    Collections.sort(andConditionList, new Comparator<Object>() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            try {
                                ConditionVo obj1 = (ConditionVo)o1;
                                ConditionVo obj2 = (ConditionVo)o2;
                                return ((IProcessTaskCondition)ConditionHandlerFactory.getHandler(obj1.getName())).getEsName()
                                    .compareTo(((IProcessTaskCondition)ConditionHandlerFactory.getHandler(obj2.getName())).getEsName());
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
                    String[] valueList = null;
                    //nested 不同path 前缀则需 分开 中括号 括起来
                    if(isNested&&andIndex != 0) {
                       int tempIndex = andIndex - 1;
                       ConditionVo preCondition =andConditionTmpList.get(tempIndex);
                       
                       if(condition.getValueList()!= null && condition.getValueList() instanceof JSONArray) {
                           List<String> tmpList = JSONObject.parseArray(condition.getValueList().toString(), String.class);
                           valueList =  tmpList.toArray(new String[tmpList.size()]);
                       }else {
                           valueList = new String[] {condition.getValueList().toString()};
                       }
                       String prePrefix = ((IProcessTaskCondition)ConditionHandlerFactory.getHandler(preCondition.getName())).getEsPath(valueList);
                       String prefix = ((IProcessTaskCondition)ConditionHandlerFactory.getHandler(condition.getName())).getEsPath(valueList);
                       if(!prefix.startsWith(prePrefix)) {
                           whereSb.append(" ] and [ ");
                       }
                    }
                    
                    IProcessTaskCondition workcenterCondition =
                        (IProcessTaskCondition)ConditionHandlerFactory.getHandler(condition.getName());
                    if (condition.getType().equals("form")) {
                        formConditionList.add(condition);
                    } else {
                        String conditionWhere = workcenterCondition.getEsWhere(andConditionTmpList, andIndex);
                        whereSb.append(conditionWhere);
                        boolean comparePrefix = true;
                        if(isNested && andIndex+1 < andConditionTmpList.size()) {
                            ConditionVo nextCondition = andConditionTmpList.get(andIndex + 1);
                            if(nextCondition.getValueList()!= null && nextCondition.getValueList() instanceof JSONArray) {
                                List<String> tmpList = JSONObject.parseArray(nextCondition.getValueList().toString(), String.class);
                                valueList =  tmpList.toArray(new String[tmpList.size()]);
                            }else {
                                valueList = new String[] {nextCondition.getValueList().toString()};
                            }
                            String prefix = ((IProcessTaskCondition)ConditionHandlerFactory.getHandler(condition.getName())).getEsPath(valueList);
                            String nextprefix = ((IProcessTaskCondition)ConditionHandlerFactory.getHandler(nextCondition.getName())).getEsPath(valueList);
                            if(!nextprefix.startsWith(prefix)) {
                                comparePrefix = false;
                            }
                        }
                        if (andIndex != andConditionTmpList.size() - 1 && !andConditionTmpList.get(andIndex + 1).getType().equals("form") && comparePrefix
                            ) {
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

    @Override
    protected QueryResult makeupQueryResult(WorkcenterVo workcenterVo, QueryResult result) {
        return result;
    }

    @Override
    protected QueryResultSet makeupQueryIterateResult(WorkcenterVo t, QueryResultSet result) {
        // TODO Auto-generated method stub
        return result;
    }

}
