package codedriver.module.process.service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.dto.FormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import codedriver.module.process.auth.label.PROCESSTASK_MODIFY;
import codedriver.module.process.workcenter.core.SqlBuilder;
import codedriver.module.process.workcenter.operate.WorkcenterOperateBuilder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title: NewWorkcenterServiceImpl
 * @Package: codedriver.module.process.service
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/1/19 20:09
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
public class NewWorkcenterServiceImpl implements NewWorkcenterService {

    Logger logger = LoggerFactory.getLogger(WorkcenterServiceImpl.class);

    @Autowired
    WorkcenterMapper workcenterMapper;

    @Autowired
    FormMapper formMapper;

    @Override
    public JSONObject doSearch(WorkcenterVo workcenterVo) {
        JSONObject returnObj = new JSONObject();
        List<JSONObject> dataList = new ArrayList<JSONObject>();
        JSONArray sortColumnList = new JSONArray();
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        //thead
        List<WorkcenterTheadVo> theadList = getWorkcenterTheadList(workcenterVo, columnComponentMap, sortColumnList);
        theadList = theadList.stream().sorted(Comparator.comparing(WorkcenterTheadVo::getSort)).collect(Collectors.toList());
        //找出符合条件分页后的工单ID List
        SqlBuilder sb = new SqlBuilder(workcenterVo, FieldTypeEnum.DISTINCT_ID);
        //System.out.println("idSql:-------------------------------------------------------------------------------");
        //System.out.println(sb.build());
        List<ProcessTaskVo> processTaskList = workcenterMapper.getWorkcenterProcessTaskIdBySql(sb.build());
        workcenterVo.setProcessTaskIdList(processTaskList.stream().map(ProcessTaskVo::getId).collect(Collectors.toList()));
        //补充工单字段信息
        workcenterVo.setTheadVoList(theadList);
        sb = new SqlBuilder(workcenterVo, FieldTypeEnum.FIELD);
        //System.out.println("fieldSql:-------------------------------------------------------------------------------");
        //System.out.println(sb.build());
        List<ProcessTaskVo> processTaskVoList = workcenterMapper.getWorkcenterProcessTaskInfoBySql(sb.build());
        ProcessAuthManager.Builder builder = new ProcessAuthManager.Builder();
        for (ProcessTaskVo processTaskVo : processTaskVoList) {
            builder.addProcessTaskId(processTaskVo.getId());
            for(ProcessTaskStepVo processStep : processTaskVo.getStepList()) {
                builder.addProcessTaskStepId(processStep.getId());
            }
        }
        Map<Long, Set<ProcessTaskOperationType>> operateTypeSetMap =
                builder.addOperationType(ProcessTaskOperationType.TASK_ABORT)
                        .addOperationType(ProcessTaskOperationType.TASK_RECOVER)
                        .addOperationType(ProcessTaskOperationType.TASK_URGE)
                        .addOperationType(ProcessTaskOperationType.STEP_WORK).build().getOperateMap();
        Boolean isHasProcessTaskAuth = AuthActionChecker.check(PROCESSTASK_MODIFY.class.getSimpleName());
        for (ProcessTaskVo processTaskVo : processTaskVoList) {
            processTaskVo.getParamObj().put("isHasProcessTaskAuth", isHasProcessTaskAuth);
            JSONObject taskJson = new JSONObject();
            //重新渲染工单字段
            for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
                IProcessTaskColumn column = entry.getValue();
                taskJson.put(column.getName(), column.getValue(processTaskVo));
            }
            // route 供前端跳转路由信息
            JSONObject routeJson = new JSONObject();
            routeJson.put("taskid", processTaskVo.getId());
            taskJson.put("route", routeJson);
            taskJson.put("taskid", processTaskVo.getId());
            // operate 获取对应工单的操作
            taskJson.put("action", getTaskOperate(processTaskVo, operateTypeSetMap));
            dataList.add(taskJson);

        }
        // 字段排序
        JSONArray sortList = workcenterVo.getSortList();
        if (CollectionUtils.isEmpty(sortList)) {
            sortList = sortColumnList;
        }
        //统计符合条件工单数量
        sb = new SqlBuilder(workcenterVo, FieldTypeEnum.COUNT);
        //System.out.println("countSql:-------------------------------------------------------------------------------");
        //System.out.println(sb.build());
        int total = workcenterMapper.getWorkcenterProcessTaskCountBySql(sb.build());

        returnObj.put("sortList", sortList);
        returnObj.put("theadList", theadList);
        returnObj.put("tbodyList", dataList);
        returnObj.put("rowNum", total);
        returnObj.put("pageSize", workcenterVo.getPageSize());
        returnObj.put("currentPage", workcenterVo.getCurrentPage());
        returnObj.put("pageCount", PageUtil.getPageCount(total, workcenterVo.getPageSize()));
        //补充待办数
        workcenterVo.setIsProcessingOfMine(1);
        workcenterVo.setPageSize(100);
        sb = new SqlBuilder(workcenterVo, FieldTypeEnum.COUNT);
        Integer count  = workcenterMapper.getWorkcenterProcessTaskCountBySql(sb.build());
        //System.out.println("countProcessingOfMineSql:-------------------------------------------------------------------------------");
        //System.out.println(sb.build());
        returnObj.put("processingOfMineCount", count>99?"99+":count.toString());
        return returnObj;
    }

    @Override
    public List<ProcessTaskVo> doSearchKeyword(WorkcenterVo workcenterVo,String columnName) {
        List<JSONObject> dataList = new ArrayList<JSONObject>();
        JSONArray sortColumnList = new JSONArray();
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        //thead
        List<WorkcenterTheadVo> theadList = getWorkcenterTheadList(workcenterVo, columnComponentMap, sortColumnList);
        theadList = theadList.stream().filter(t->t.getName().equals(columnName)).collect(Collectors.toList());
        //找出符合条件分页后的工单ID List
        SqlBuilder sb = new SqlBuilder(workcenterVo, FieldTypeEnum.DISTINCT_ID);
        //System.out.println("idSql:-------------------------------------------------------------------------------");
        System.out.println(sb.build());
        List<ProcessTaskVo> processTaskList = workcenterMapper.getWorkcenterProcessTaskIdBySql(sb.build());
        workcenterVo.setProcessTaskIdList(processTaskList.stream().map(ProcessTaskVo::getId).collect(Collectors.toList()));
        //补充工单字段信息
        workcenterVo.setTheadVoList(theadList);
        sb = new SqlBuilder(workcenterVo, FieldTypeEnum.FIELD);
        //System.out.println("fieldSql:-------------------------------------------------------------------------------");
        //System.out.println(sb.build());
        return workcenterMapper.getWorkcenterProcessTaskInfoBySql(sb.build());
    }

    @Override
    public Integer doSearchCount(WorkcenterVo workcenterVo) {
        SqlBuilder sb = new SqlBuilder(workcenterVo, FieldTypeEnum.COUNT);
        //System.out.println("countSql:-------------------------------------------------------------------------------");
        //System.out.println(sb.build());
        return workcenterMapper.getWorkcenterProcessTaskCountBySql(sb.build());
    }

    /**
     * @Description: 获取用户工单中心table column theadList
     * @Author: 89770
     * @Date: 2021/1/19 20:38
     * @Params: [workcenterVo, columnComponentMap, sortColumnList]
     * @Returns: java.util.List<codedriver.framework.process.workcenter.dto.WorkcenterTheadVo>
     **/
    public List<WorkcenterTheadVo> getWorkcenterTheadList(WorkcenterVo workcenterVo, Map<String, IProcessTaskColumn> columnComponentMap, JSONArray sortColumnList) {
        List<WorkcenterTheadVo> theadList = workcenterMapper.getWorkcenterThead(new WorkcenterTheadVo(workcenterVo.getUuid(), UserContext.get().getUserUuid()));
        // 矫正theadList 或存在表单属性或固定字段增删
        // 多删
        ListIterator<WorkcenterTheadVo> it = theadList.listIterator();
        while (it.hasNext()) {
            WorkcenterTheadVo thead = it.next();
            if (thead.getType().equals(ProcessFieldType.COMMON.getValue())) {
                if (!columnComponentMap.containsKey(thead.getName())) {
                    it.remove();
                } else {
                    thead.setDisabled(columnComponentMap.get(thead.getName()).getDisabled() ? 1 : 0);
                    thead.setDisplayName(columnComponentMap.get(thead.getName()).getDisplayName());
                    thead.setClassName(columnComponentMap.get(thead.getName()).getClassName());
                    thead.setIsExport(columnComponentMap.get(thead.getName()).getIsExport() ? 1 : 0);
                    //thead.setIsShow(columnComponentMap.get(thead.getName()).getIsShow() ? 1 : 0);
                }
            } else {
                List<String> channelUuidList = workcenterVo.getChannelUuidList();
                if (CollectionUtils.isNotEmpty(channelUuidList)) {
                    List<FormAttributeVo> formAttrList =
                            formMapper.getFormAttributeListByChannelUuidList(channelUuidList);
                    List<FormAttributeVo> theadFormList = formAttrList.stream()
                            .filter(attr -> attr.getUuid().equals(thead.getName())).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(theadFormList)) {
                        it.remove();
                    } else {
                        thead.setDisplayName(theadFormList.get(0).getLabel());
                    }
                }
            }
        }
        // 少补
        for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
            IProcessTaskColumn column = entry.getValue();
            if (column.getIsShow() && CollectionUtils.isEmpty(theadList.stream()
                    .filter(data -> column.getName().endsWith(data.getName())).collect(Collectors.toList()))) {
                theadList.add(new WorkcenterTheadVo(column));
            }
            // 如果需要排序
            if (sortColumnList != null && column.getIsSort()) {
                sortColumnList.add(column.getName());
            }
        }
        return theadList;
    }


    /**
     * @Description:
     * @Author: 89770
     * @Date: 2021/1/29 11:44
     * @Params: [processTaskVo, operateTypeSetMap]
     * @Returns: com.alibaba.fastjson.JSONObject
     **/
    private JSONObject getTaskOperate(ProcessTaskVo processTaskVo,Map<Long, Set<ProcessTaskOperationType>> operateTypeSetMap) {
        JSONObject action = new JSONObject();
        String processTaskStatus = processTaskVo.getStatus();
        boolean isHasAbort = false;
        boolean isHasRecover = false;
        boolean isHasUrge = false;
        JSONArray handleArray = new JSONArray();
        if ((ProcessTaskStatus.RUNNING.getValue().equals(processTaskStatus)
                || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStatus)
                || ProcessTaskStatus.ABORTED.getValue().equals(processTaskStatus))) {
            Set<ProcessTaskOperationType> operationTypeSet = operateTypeSetMap.get(processTaskVo.getId());

            if (CollectionUtils.isNotEmpty(operationTypeSet)) {
                if (operationTypeSet.contains(ProcessTaskOperationType.TASK_ABORT)) {
                    isHasAbort = true;
                }
                if (operationTypeSet.contains(ProcessTaskOperationType.TASK_RECOVER)) {
                    isHasRecover = true;
                }
                if (operationTypeSet.contains(ProcessTaskOperationType.TASK_URGE)) {
                    isHasUrge = true;
                }
            }
            for (ProcessTaskStepVo step : processTaskVo.getStepList()) {
                Set<ProcessTaskOperationType> set = operateTypeSetMap.get(step.getId());
                if (set != null && set.contains(ProcessTaskOperationType.STEP_WORK)) {
                    JSONObject configJson = new JSONObject();
                    configJson.put("taskid", processTaskVo.getId());
                    configJson.put("stepid", step.getId());
                    configJson.put("stepName", step.getName());
                    JSONObject actionJson = new JSONObject();
                    actionJson.put("name", "handle");
                    actionJson.put("text", step.getName());
                    actionJson.put("config", configJson);
                    handleArray.add(actionJson);
                }
            }
        }
        // 返回实际操作按钮
        /**
         * 实质性操作按钮：如“处理”、“取消”、“催办”，根据用户权限展示 ;次要的操作按钮：如“隐藏”、“删除”，只有管理员可见 移动端按钮展示规则：
         * 1、工单显示时，优先展示实质性的按钮，次要的操作按钮收起到“更多”中；如果没有任何实质性的操作按钮，则将次要按钮放出来（管理员可见）；
         * 2、工单隐藏时，仅“显示”、“删除”按钮放出来，其他实质性按钮需要等工单显示后才会展示；
         */

        WorkcenterOperateBuilder workcenterFirstOperateBuilder = new WorkcenterOperateBuilder();
        JSONArray workcenterFirstOperateArray = workcenterFirstOperateBuilder.setHandleOperate(handleArray)
                .setAbortRecoverOperate(isHasAbort, isHasRecover, processTaskVo).setUrgeOperate(isHasUrge, processTaskVo)
                .build();
        Boolean isNeedFirstOperate = false;
        for (Object firstOperate : workcenterFirstOperateArray) {
            JSONObject firstOperateJson = JSONObject.parseObject(firstOperate.toString());
            if (firstOperateJson.getInteger("isEnable") == 1) {
                isNeedFirstOperate = true;
            }
        }
        WorkcenterOperateBuilder workcenterSecondOperateBuilder = new WorkcenterOperateBuilder();
        JSONArray workcenterSecondOperateJsonArray =
                workcenterSecondOperateBuilder.setShowHideOperate(processTaskVo).setDeleteOperate(processTaskVo).build();
        for (Object workcenterSecondOperateObj : workcenterSecondOperateJsonArray) {
            JSONObject workcenterSecondOperateJson = JSONObject.parseObject(workcenterSecondOperateObj.toString());
            if (ProcessTaskOperationType.TASK_SHOW.getValue().equals(workcenterSecondOperateJson.getString("name"))) {
                isNeedFirstOperate = false;
            }
        }
        if (isNeedFirstOperate) {
            action.put("firstActionList", workcenterFirstOperateArray);
            action.put("secondActionList", workcenterSecondOperateJsonArray);
        } else {
            action.put("firstActionList", workcenterSecondOperateJsonArray);
            action.put("secondActionList", new JSONArray());
        }
        return action;
    }
}
