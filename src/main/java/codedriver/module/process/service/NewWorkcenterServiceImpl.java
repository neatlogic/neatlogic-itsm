/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.batch.BatchRunner;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.process.auth.PROCESSTASK_MODIFY;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepTaskMapper;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import codedriver.module.process.workcenter.core.SqlBuilder;
import codedriver.module.process.workcenter.operate.WorkcenterOperateBuilder;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewWorkcenterServiceImpl implements NewWorkcenterService {

    Logger logger = LoggerFactory.getLogger(NewWorkcenterServiceImpl.class);

    @Resource
    WorkcenterMapper workcenterMapper;

    @Resource
    FormMapper formMapper;

    @Resource
    ChannelMapper channelMapper;

    @Resource
    ProcessTaskMapper processTaskMapper;

    @Resource
    UserMapper userMapper;
    @Resource
    RoleMapper roleMapper;
    @Resource
    TeamMapper teamMapper;
    @Resource
    ProcessTaskStepTaskMapper processTaskStepTaskMapper;

    @Override
    public JSONObject doSearch(WorkcenterVo workcenterVo) {
        JSONObject returnObj = new JSONObject();
        List<JSONObject> dataList = Collections.synchronizedList(new ArrayList<JSONObject>());//线程安全
        JSONArray sortColumnList = new JSONArray();
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        //补充工单字段信息
        long theadStartTime = System.currentTimeMillis();
         System.out.println("##start workcenter-thead:-------------------------------------------------------------------------------");
        List<WorkcenterTheadVo> theadList = getWorkcenterTheadList(workcenterVo, columnComponentMap, sortColumnList);
        theadList = theadList.stream().sorted(Comparator.comparing(WorkcenterTheadVo::getSort)).collect(Collectors.toList());
        workcenterVo.setTheadVoList(theadList);
         System.out.println((System.currentTimeMillis()-theadStartTime)+" ##end workcenter-thead:------------------------------------------------------------------------------- ");
        //统计符合条件工单数量
        SqlBuilder sb = new SqlBuilder(workcenterVo, FieldTypeEnum.TOTAL_COUNT);
        long countStartTime = System.currentTimeMillis();
         System.out.println("##start workcenter-count:-------------------------------------------------------------------------------");
        // System.out.println(sb.build());
        int total = processTaskMapper.getProcessTaskCountBySql(sb.build());
         System.out.println((System.currentTimeMillis()-countStartTime)+" ##end workcenter-count:------------------------------------------------------------------------------- ");
         System.out.println((System.currentTimeMillis()-theadStartTime)+" ##end workcenter-thead-count:------------------------------------------------------------------------------- ");
        if (total > 0) {
            //找出符合条件分页后的工单ID List
            long idStartTime = System.currentTimeMillis();
             System.out.println("##start workcenter-id:-------------------------------------------------------------------------------");
            sb = new SqlBuilder(workcenterVo, FieldTypeEnum.DISTINCT_ID);
            // System.out.println(sb.build());
            List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskBySql(sb.build());
            workcenterVo.setProcessTaskIdList(processTaskList.stream().map(ProcessTaskVo::getId).collect(Collectors.toList()));
             System.out.println((System.currentTimeMillis()-idStartTime)+" ##end workcenter-id:-------------------------------------------------------------------------------");

            long detailStartTime = System.currentTimeMillis();
             System.out.println("##start workcenter-detail:-------------------------------------------------------------------------------");
            sb = new SqlBuilder(workcenterVo, FieldTypeEnum.FIELD);
            // System.out.println(sb.build());
            List<ProcessTaskVo> processTaskVoList = processTaskMapper.getProcessTaskBySql(sb.build());
             System.out.println((System.currentTimeMillis()-detailStartTime)+" ##end workcenter-detail:-------------------------------------------------------------------------------");
            //纠正顺序
            //按钮权限
            long authBuildStartTime = System.currentTimeMillis();
             System.out.println("##start workcenter-authBuild:-------------------------------------------------------------------------------");
            ProcessAuthManager.Builder builder = new ProcessAuthManager.Builder();
            for (ProcessTaskVo processTaskVo : processTaskVoList) {
                builder.addProcessTaskId(processTaskVo.getId());
                for (ProcessTaskStepVo processStep : processTaskVo.getStepList()) {
                    builder.addProcessTaskStepId(processStep.getId());
                }
            }
            Map<Long, Set<ProcessTaskOperationType>> operateTypeSetMap =
                    builder.addOperationType(ProcessTaskOperationType.PROCESSTASK_ABORT)
                            .addOperationType(ProcessTaskOperationType.PROCESSTASK_RECOVER)
                            .addOperationType(ProcessTaskOperationType.PROCESSTASK_URGE)
                            .addOperationType(ProcessTaskOperationType.STEP_WORK).build().getOperateMap();
             System.out.println((System.currentTimeMillis()-authBuildStartTime)+" ##end workcenter-authBuild:-------------------------------------------------------------------------------");
            long tmpColumnStartTime = System.currentTimeMillis();
            Boolean isHasProcessTaskAuth = AuthActionChecker.check(PROCESSTASK_MODIFY.class.getSimpleName());
            BatchRunner<ProcessTaskVo> runner = new BatchRunner<>();
            runner.execute(processTaskVoList, processTaskVoList.size(), processTaskVo -> {
                processTaskVo.getParamObj().put("isHasProcessTaskAuth", isHasProcessTaskAuth);
                JSONObject taskJson = new JSONObject();
                //重新渲染工单字段
                for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
                    long tmp = System.currentTimeMillis();
                    IProcessTaskColumn column = entry.getValue();
                    taskJson.put(column.getName(), column.getValue(processTaskVo));
                    // System.out.println(System.currentTimeMillis()-tmp+" ##end workcenter-column "+column.getName()+":-------------------------------------------------------------------------------");
                }
                // route 供前端跳转路由信息
                JSONObject routeJson = new JSONObject();
                routeJson.put("taskid", processTaskVo.getId());
                taskJson.put("route", routeJson);
                taskJson.put("taskid", processTaskVo.getId());
                // operate 获取对应工单的操作
                taskJson.put("action", getTaskOperate(processTaskVo, operateTypeSetMap));
                dataList.add(taskJson);
            }, "WORKCENTER-COLUMN-SEARCHER");
             System.out.println(System.currentTimeMillis()-tmpColumnStartTime+" ##end workcenter-auth column:-------------------------------------------------------------------------------");

        }
        // 字段排序
        JSONArray sortList = workcenterVo.getSortList();
        if (CollectionUtils.isEmpty(sortList)) {
            sortList = sortColumnList;
        }
        returnObj.put("sortList", sortList);
        returnObj.put("theadList", theadList);
        returnObj.put("tbodyList", dataList);
        returnObj.put("rowNum", total);
        returnObj.put("pageSize", workcenterVo.getPageSize());
        returnObj.put("currentPage", workcenterVo.getCurrentPage());
        returnObj.put("pageCount", PageUtil.getPageCount(total, workcenterVo.getPageSize()));
        Integer count = 0;
        long ofMineStartTime = System.currentTimeMillis();
         System.out.println("##start workcenter-ofMine:-------------------------------------------------------------------------------");
        if (total > 0) {
            //补充待办数
            workcenterVo.setIsProcessingOfMine(1);
            workcenterVo.setCurrentPage(1);
            workcenterVo.setPageSize(100);
            sb = new SqlBuilder(workcenterVo, FieldTypeEnum.LIMIT_COUNT);
            // System.out.println(sb.build());
            count = processTaskMapper.getProcessTaskCountBySql(sb.build());
        }
        returnObj.put("processingOfMineCount", count > 99 ? "99+" : count.toString());
         System.out.println((System.currentTimeMillis()-ofMineStartTime)+" ##end workcenter-ofMine:-------------------------------------------------------------------------------");
         System.out.println((System.currentTimeMillis()-theadStartTime)+" ##end workcenter:-------------------------------------------------------------------------------");

        return returnObj;
    }

    @Override
    public JSONObject doSearch(Long processtaskId) throws ParseException {
        Boolean isHasProcessTaskAuth = AuthActionChecker.check(PROCESSTASK_MODIFY.class.getSimpleName());
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskAndStepById(processtaskId);
        JSONObject taskJson = null;
        if (processTaskVo != null) {
            Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
            //获取工单&&步骤操作
            ProcessAuthManager.Builder builder = new ProcessAuthManager.Builder();
            builder.addProcessTaskId(processTaskVo.getId());
            for (ProcessTaskStepVo processStep : processTaskVo.getStepList()) {
                builder.addProcessTaskStepId(processStep.getId());
            }
            Map<Long, Set<ProcessTaskOperationType>> operateTypeSetMap =
                    builder.addOperationType(ProcessTaskOperationType.PROCESSTASK_ABORT)
                            .addOperationType(ProcessTaskOperationType.PROCESSTASK_RECOVER)
                            .addOperationType(ProcessTaskOperationType.PROCESSTASK_URGE)
                            .addOperationType(ProcessTaskOperationType.STEP_WORK).build().getOperateMap();

            processTaskVo.getParamObj().put("isHasProcessTaskAuth", isHasProcessTaskAuth);
            taskJson = new JSONObject();
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
        }
        return taskJson;
    }

    @Override
    public List<ProcessTaskVo> doSearchKeyword(WorkcenterVo workcenterVo) {
        //找出符合条件分页后的工单ID List
        //SqlBuilder sb = new SqlBuilder(workcenterVo, FieldTypeEnum.FULL_TEXT);
        //System.out.println("fullTextSql:-------------------------------------------------------------------------------");
//        System.out.println(sb.build());
        //return processTaskMapper.getProcessTaskBySql(sb.build());
        List<String> keywordList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(workcenterVo.getKeywordList())) {
            keywordList = new ArrayList<>(workcenterVo.getKeywordList());
        }
        return processTaskMapper.getProcessTaskColumnByIndexKeyword(keywordList, workcenterVo.getPageSize(), workcenterVo.getKeywordColumn(), workcenterVo.getKeywordPro());
    }

    @Override
    public Integer doSearchLimitCount(WorkcenterVo workcenterVo) {
        SqlBuilder sb = new SqlBuilder(workcenterVo, FieldTypeEnum.LIMIT_COUNT);
        //System.out.println("countSql:-------------------------------------------------------------------------------");
        //System.out.println(sb.build());
        return processTaskMapper.getProcessTaskCountBySql(sb.build());
    }

    @Override
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
                    List<String> formUuidList = channelMapper.getFormUuidListByChannelUuidList(channelUuidList);
                    if (CollectionUtils.isNotEmpty(formUuidList)) {
                        List<FormAttributeVo> formAttrList =
                                formMapper.getFormAttributeListByFormUuidList(formUuidList);
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
    private JSONObject getTaskOperate(ProcessTaskVo processTaskVo, Map<Long, Set<ProcessTaskOperationType>> operateTypeSetMap) {
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
                if (operationTypeSet.contains(ProcessTaskOperationType.PROCESSTASK_ABORT)) {
                    isHasAbort = true;
                }
                if (operationTypeSet.contains(ProcessTaskOperationType.PROCESSTASK_RECOVER)) {
                    isHasRecover = true;
                }
                if (operationTypeSet.contains(ProcessTaskOperationType.PROCESSTASK_URGE)) {
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
        boolean isNeedFirstOperate = false;
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
            if (ProcessTaskOperationType.PROCESSTASK_SHOW.getValue().equals(workcenterSecondOperateJson.getString("name"))) {
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

    /**
     * @Description: 获取工单idList by  关键字搜索条件 keywordConditionList
     * @Author: 89770
     * @Date: 2021/2/9 17:08
     * @Params: [workcenterVo]
     * @Returns: java.util.List<java.lang.Long>
     **/
    @Deprecated
    private List<Long> getProcessTaskIdListByKeywordConditionList(WorkcenterVo workcenterVo) {
        if (CollectionUtils.isNotEmpty(workcenterVo.getKeywordConditionList())) {
            SqlBuilder sb = new SqlBuilder(workcenterVo, FieldTypeEnum.FULL_TEXT);
//        System.out.println("fullTextGetIdListSql:-------------------------------------------------------------------------------");
//        System.out.println(sb.build());
            List<ProcessTaskVo> processTaskVoList = processTaskMapper.getProcessTaskBySql(sb.build());
            return processTaskVoList.stream().map(ProcessTaskVo::getId).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public JSONArray getKeywordOptionsPCNew(WorkcenterVo workcenterVo) {
        JSONArray returnArray = new JSONArray();
//        workcenterVo.setSqlFieldType(FieldTypeEnum.FULL_TEXT.getValue());
        // 搜索标题
        workcenterVo.setKeywordHandler(ProcessTaskSqlTable.FieldEnum.TITLE.getHandlerName());
        workcenterVo.setKeywordText(ProcessTaskSqlTable.FieldEnum.TITLE.getText());
        workcenterVo.setKeywordPro(ProcessTaskSqlTable.FieldEnum.TITLE.getProValue());
        workcenterVo.setKeywordColumn(ProcessTaskSqlTable.FieldEnum.TITLE.getValue());
        returnArray.addAll(getKeywordOptionPCNew(workcenterVo));

        // 搜索ID
        workcenterVo.setKeywordHandler(ProcessTaskSqlTable.FieldEnum.SERIAL_NUMBER.getHandlerName());
        workcenterVo.setKeywordText(ProcessTaskSqlTable.FieldEnum.SERIAL_NUMBER.getText());
        workcenterVo.setKeywordPro(ProcessTaskSqlTable.FieldEnum.SERIAL_NUMBER.getProValue());
        workcenterVo.setKeywordColumn(ProcessTaskSqlTable.FieldEnum.SERIAL_NUMBER.getValue());
        returnArray.addAll(getKeywordOptionPCNew(workcenterVo));
        return returnArray;
    }

    /**
     * @Description: 根据关键字获取所有过滤选项 pc端
     * @Author: 89770
     * @Date: 2021/2/5 9:59
     * @Params: [condition, keyword, pageSize, columnName]
     * @Returns: com.alibaba.fastjson.JSONArray
     **/
    private JSONArray getKeywordOptionPCNew(WorkcenterVo workcenterVo) {
        JSONArray returnArray = new JSONArray();
        List<ProcessTaskVo> processTaskVoList = doSearchKeyword(workcenterVo);
        if (!processTaskVoList.isEmpty()) {
            JSONObject titleObj = new JSONObject();
            JSONArray dataList = new JSONArray();
            for (ProcessTaskVo processTaskVo : processTaskVoList) {
                dataList.add(JSONObject.parseObject(JSONObject.toJSONString(processTaskVo)).getString(workcenterVo.getKeywordPro()));
            }
            titleObj.put("dataList", dataList);
            titleObj.put("value", workcenterVo.getKeywordColumn());
            titleObj.put("text", workcenterVo.getKeywordText());
            returnArray.add(titleObj);
        }
        return returnArray;
    }

    @Override
    public void getStepTaskWorkerList(JSONArray workerArray, ProcessTaskStepVo stepVo) {
        if (ProcessTaskStatus.DRAFT.getValue().equals(stepVo.getStatus()) ||
                ProcessTaskStatus.RUNNING.getValue().equals(stepVo.getStatus()) ||
                ProcessTaskStatus.PENDING.getValue().equals(stepVo.getStatus()) && stepVo.getIsActive() == 1
        ) {
            List<ProcessTaskStepWorkerVo> majorWorkerList = stepVo.getWorkerList().stream().filter(o -> Objects.equals(o.getUserType(), ProcessUserType.MAJOR.getValue())).collect(Collectors.toList());
            for (ProcessTaskStepWorkerVo majorWorker : majorWorkerList) {
                JSONObject workerJson = new JSONObject();
                getWorkerInfo(majorWorker, workerJson, workerArray);
            }
            Map<Long, List<ProcessTaskStepWorkerVo>> stepMinorWorkerListMap = new HashMap<>();
            List<IProcessStepHandler> handlerList = ProcessStepHandlerFactory.getHandlerList();
            for (IProcessStepHandler stepHandler : handlerList) {
                List<ProcessTaskStepWorkerVo> stepWorkerVos = stepHandler.getMinorWorkerList(stepVo);
                if (CollectionUtils.isNotEmpty(stepWorkerVos)) {
                    stepMinorWorkerListMap.put(stepVo.getId(), stepWorkerVos);
                }
            }
            List<String> workerUuidTypeList = new ArrayList<>();
            for (ProcessTaskStepWorkerVo workerVo : stepVo.getWorkerList()) {
                if (Objects.equals(workerVo.getUserType(), ProcessUserType.MINOR.getValue())) {
                    stepTaskWorker(workerVo, stepVo, workerArray, workerUuidTypeList);
                    otherWorker(workerVo, stepVo, workerArray, stepMinorWorkerListMap, workerUuidTypeList);
                }
            }
        }
    }


    /**
     * 任务处理人
     *
     * @param workerVo           处理人
     * @param stepVo             工单步骤
     * @param workerArray        处理人数组
     * @param workerUuidTypeList 用于去重
     */
    @Override
    public void stepTaskWorker(ProcessTaskStepWorkerVo workerVo, ProcessTaskStepVo stepVo, JSONArray workerArray, List<String> workerUuidTypeList) {
        if (Objects.equals(workerVo.getType(), GroupSearch.USER.getValue())) {
            List<ProcessTaskStepTaskVo> stepTaskVoList = processTaskStepTaskMapper.getStepTaskWithUserByProcessTaskStepId(stepVo.getId());
            for (ProcessTaskStepTaskVo stepTaskVo : stepTaskVoList) {
                for (ProcessTaskStepTaskUserVo userVo : stepTaskVo.getStepTaskUserVoList()) {
                    if (Objects.equals(userVo.getUserUuid(), workerVo.getUuid()) && !Objects.equals(ProcessTaskStatus.SUCCEED.getValue(), userVo.getStatus())) {
                        String workerUuidType = workerVo.getUuid() + stepTaskVo.getTaskConfigName();
                        if (!workerUuidTypeList.contains(workerUuidType)) {
                            JSONObject workerJson = new JSONObject();
                            workerJson.put("workTypename", stepTaskVo.getTaskConfigName());
                            getWorkerInfo(workerVo, workerJson, workerArray);
                            workerUuidTypeList.add(workerUuidType);
                        }
                    }
                }
            }
        }
    }

    /**
     * 其它模块协助处理人
     *
     * @param workerVo           处理人
     * @param stepVo             工单步骤
     * @param workerArray        处理人数组
     * @param workerUuidTypeList 用于去重
     */
    @Override
    public void otherWorker(ProcessTaskStepWorkerVo workerVo, ProcessTaskStepVo stepVo, JSONArray workerArray, Map<Long, List<ProcessTaskStepWorkerVo>> stepMinorWorkerMap, List<String> workerUuidTypeList) {
        List<IProcessStepHandler> handlerList = ProcessStepHandlerFactory.getHandlerList();
        for (IProcessStepHandler stepHandler : handlerList) {
            if (Objects.equals(stepHandler.getHandler(), stepVo.getHandler())) {
                List<ProcessTaskStepWorkerVo> workerVoList = stepMinorWorkerMap.get(stepVo.getId());
                if (CollectionUtils.isNotEmpty(workerVoList)) {
                    if (workerVoList.stream().anyMatch(w -> Objects.equals(workerVo.getUuid(), w.getUuid()))) {
                        String workerUuidType = workerVo.getUuid() + stepHandler.getMinorName();
                        if (!workerUuidTypeList.contains(workerUuidType)) {
                            JSONObject workerJson = new JSONObject();
                            workerJson.put("workTypename", stepHandler.getMinorName());
                            getWorkerInfo(workerVo, workerJson, workerArray);
                            workerUuidTypeList.add(workerUuidType);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void getWorkerInfo(ProcessTaskStepWorkerVo workerVo, JSONObject workerJson, JSONArray workerArray) {
        if (GroupSearch.USER.getValue().equals(workerVo.getType())) {
            UserVo userVo = userMapper.getUserBaseInfoByUuid(workerVo.getUuid());
            if (userVo != null) {
                workerJson.put("workerVo", JSON.parseObject(JSONObject.toJSONString(userVo)));
                workerArray.add(workerJson);
            }
        } else if (GroupSearch.TEAM.getValue().equals(workerVo.getType())) {
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
}
