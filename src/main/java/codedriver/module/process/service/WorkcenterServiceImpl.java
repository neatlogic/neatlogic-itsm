package codedriver.module.process.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;
import com.techsure.multiattrsearch.QueryResultSet;
import com.techsure.multiattrsearch.query.QueryResult;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthActionChecker;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.elasticsearch.core.ElasticSearchHandlerFactory;
import codedriver.framework.elasticsearch.core.IElasticSearchHandler;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.FormAttributeVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskSlaVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.elasticsearch.constvalue.ESHandler;
import codedriver.framework.process.formattribute.core.FormAttributeHandlerFactory;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;
import codedriver.framework.process.operationauth.core.ProcessOperateManager;
import codedriver.framework.process.workcenter.dto.WorkcenterFieldBuilder;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.util.TimeUtil;
import codedriver.module.process.auth.label.PROCESSTASK_MODIFY;
import codedriver.module.process.workcenter.action.WorkcenterActionBuilder;

@Service
public class WorkcenterServiceImpl implements WorkcenterService {
    Logger logger = LoggerFactory.getLogger(WorkcenterServiceImpl.class);
    @Autowired
    WorkcenterMapper workcenterMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    ProcessTaskService processTaskService;
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
    private SelectContentByHashMapper selectContentByHashMapper;

    @Autowired
    RoleMapper roleMapper;

    /**
     * 工单中心根据条件获取工单列表数据
     * 
     * @param workcenterVo
     * @return
     * @throws ParseException
     */
    @Override
    @Transactional
    public JSONObject doSearch(WorkcenterVo workcenterVo) throws ParseException {
        JSONObject returnObj = new JSONObject();
        JSONArray sortColumnList = new JSONArray();
        Boolean isHasProcessTaskAuth = AuthActionChecker.check(PROCESSTASK_MODIFY.class.getSimpleName());
        // 搜索es
        // Date time1 = new Date();
        @SuppressWarnings("unchecked")
        IElasticSearchHandler<WorkcenterVo, QueryResult> esHandler =
            ElasticSearchHandlerFactory.getHandler(ESHandler.PROCESSTASK.getValue());
        // Date time11 = new Date();
        // System.out.println("searchCostTime:"+(time11.getTime()-time1.getTime()));
        QueryResult result = esHandler.search(workcenterVo);
        List<MultiAttrsObject> resultData = result.getData();
        // 返回的数据重新加工
        List<JSONObject> dataList = new ArrayList<JSONObject>();
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        // 获取用户历史自定义theadList
        // Date time2 = new Date();
        List<WorkcenterTheadVo> theadList = getWorkcenterTheadList(workcenterVo, columnComponentMap, sortColumnList);
        theadList =
            theadList.stream().sorted(Comparator.comparing(WorkcenterTheadVo::getSort)).collect(Collectors.toList());
        // Date time22 = new Date();
        // System.out.println("矫正headerCostTime:"+(time22.getTime()-time2.getTime()));
        if (!resultData.isEmpty()) {
            // Date time3 = new Date();
            for (MultiAttrsObject el : resultData) {
                JSONObject taskJson = new JSONObject();
                taskJson.put("taskid", el.getId());
                for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
                    IProcessTaskColumn column = entry.getValue();
                    taskJson.put(column.getName(), column.getValue(el));
                }
                // route 供前端跳转路由信息
                JSONObject routeJson = new JSONObject();
                routeJson.put("taskid", el.getId());
                taskJson.put("route", routeJson);
                // action 操作
                taskJson.put("action", getStepAction(el, isHasProcessTaskAuth));
                dataList.add(taskJson);
            }
            // Date time33 = new Date();
            // System.out.println("拼装CostTime:" + (time33.getTime() - time3.getTime()));
        }

        // 字段排序
        JSONArray sortList = workcenterVo.getSortList();
        if (CollectionUtils.isEmpty(sortList)) {
            sortList = sortColumnList;
        }
        returnObj.put("sortList", sortList);
        returnObj.put("theadList", theadList);
        returnObj.put("tbodyList", dataList);
        returnObj.put("rowNum", result.getTotal());
        returnObj.put("pageSize", workcenterVo.getPageSize());
        returnObj.put("currentPage", workcenterVo.getCurrentPage());
        returnObj.put("pageCount", PageUtil.getPageCount(result.getTotal(), workcenterVo.getPageSize()));
        // 补充待办数
        workcenterVo.setIsMeWillDo(1);
        workcenterVo.setPageSize(100);
        Integer meWillDoCount = doSearchCount(workcenterVo);
        returnObj.put("meWillDoRowNum", meWillDoCount > 99 ? "99+" : meWillDoCount.toString());
        return returnObj;
    }

    /**
     * 工单中心 获取操作按钮
     * 
     * @param MultiAttrsObject
     *            el
     * @return
     * @throws ParseException
     */
    @Override
    public Object getStepAction(MultiAttrsObject el, Boolean isHasProcessTaskAuth) throws ParseException {
        JSONObject commonJson = (JSONObject)el.getJSON(ProcessFieldType.COMMON.getValue());
        if (commonJson == null) {
            return CollectionUtils.EMPTY_COLLECTION;
        }
        // task
        ProcessTaskVo processTaskVo = new ProcessTaskVo();
        processTaskVo.setId(Long.valueOf(el.getId()));
        processTaskVo.setTitle(commonJson.getString(ProcessWorkcenterField.TITLE.getValue()));
        processTaskVo.setProcessUuid(commonJson.getString(ProcessWorkcenterField.PROCESS.getValue()));
        processTaskVo.setChannelUuid(commonJson.getString(ProcessWorkcenterField.CHANNEL.getValue()));
        processTaskVo.setConfigHash(commonJson.getString(ProcessWorkcenterField.CONFIGHASH.getValue()));
        processTaskVo.setPriorityUuid(commonJson.getString(ProcessWorkcenterField.PRIORITY.getValue()));
        processTaskVo.setStatus(commonJson.getString(ProcessWorkcenterField.STATUS.getValue()));
        processTaskVo.setOwner(commonJson.getString(ProcessWorkcenterField.OWNER.getValue()));
        processTaskVo.setReporter(commonJson.getString(ProcessWorkcenterField.REPORTER.getValue()));
        processTaskVo.setWorktimeUuid(commonJson.getString(ProcessWorkcenterField.WOKRTIME.getValue()));
        processTaskVo.setStartTime(
            TimeUtil.convertStringToDate(commonJson.getString("starttime"), TimeUtil.YYYY_MM_DD_HH_MM_SS));
        processTaskVo
            .setEndTime(TimeUtil.convertStringToDate(commonJson.getString("endtime"), TimeUtil.YYYY_MM_DD_HH_MM_SS));
        processTaskVo.setIsShow(commonJson.getInteger(ProcessWorkcenterField.IS_SHOW.getValue()) == null ? 1
            : commonJson.getInteger(ProcessWorkcenterField.IS_SHOW.getValue()));
        processTaskVo.getParamObj().put("isHasProcessTaskAuth", isHasProcessTaskAuth);
        // step
        JSONArray stepArray = null;
        try {
            stepArray = (JSONArray)commonJson.getJSONArray(ProcessWorkcenterField.STEP.getValue());
        } catch (Exception ex) {
            return "";
        }
        if (CollectionUtils.isEmpty(stepArray)) {
            return CollectionUtils.EMPTY_COLLECTION;
        }
        List<ProcessTaskStepVo> stepList = new ArrayList<>();
        for (Object stepObj : stepArray) {
            JSONObject stepJson = (JSONObject)stepObj;
            if (ProcessTaskStatus.PENDING.getValue().equals(stepJson.getString("status"))
                || ProcessTaskStatus.DRAFT.getValue().equals(stepJson.getString("status"))
                || ProcessTaskStatus.RUNNING.getValue().equals(stepJson.getString("status"))) {
                ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
                processTaskStepVo.setId(stepJson.getLong("id"));
                processTaskStepVo.setProcessTaskId(processTaskVo.getId());
                processTaskStepVo.setName(stepJson.getString("name"));
                processTaskStepVo.setStatus(stepJson.getString("status"));
                processTaskStepVo.setType(stepJson.getString("type"));
                processTaskStepVo.setHandler(stepJson.getString("handler"));
                processTaskStepVo.setConfigHash(stepJson.getString("confighash"));
                processTaskStepVo.setStartTime(
                    TimeUtil.convertStringToDate(stepJson.getString("starttime"), TimeUtil.YYYY_MM_DD_HH_MM_SS));
                processTaskStepVo.setEndTime(
                    TimeUtil.convertStringToDate(stepJson.getString("endtime"), TimeUtil.YYYY_MM_DD_HH_MM_SS));
                processTaskStepVo.setIsActive(stepJson.getInteger("isactive"));
                stepList.add(processTaskStepVo);
            }
        }
        JSONObject actionJson = getStepActionArray(processTaskVo, stepList);
        return actionJson;
    }

    /**
     * 工单中心根据条件获取工单列表数据
     * 
     * @param workcenterVo
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public Integer doSearchCount(WorkcenterVo workcenterVo) {
        // 搜索es
        IElasticSearchHandler<WorkcenterVo, ?> esHandler =
            ElasticSearchHandlerFactory.getHandler(ESHandler.PROCESSTASK.getValue());
        return esHandler.searchCount(workcenterVo);
    }

    /**
     * 流式分批获取并处理数据
     */
    @Override
    public JSONObject getSearchIterate(QueryResultSet resultSet, WorkcenterVo workcenterVo) {
        JSONObject returnObj = new JSONObject();
        List<JSONObject> dataList = new ArrayList<JSONObject>();
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        if (resultSet.hasMoreResults()) {
            QueryResult result = resultSet.fetchResult();
            if (!result.getData().isEmpty()) {
                for (MultiAttrsObject el : result.getData()) {
                    JSONObject taskJson = new JSONObject();
                    taskJson.put("taskid", el.getId());
                    for (Object columnObj : workcenterVo.getResultColumnList()) {
                        IProcessTaskColumn column = columnComponentMap.get(columnObj);
                        Object valueText = column.getValueText(el);
                        if (valueText != null) {
                            taskJson.put(column.getName(), column.getValueText(el));
                        }
                    }
                    dataList.add(taskJson);
                }
            }
        }
        returnObj.put("tbodyList", dataList);
        return returnObj;
    }

    @Override
    public JSONObject doSearch(Long processtaskId) throws ParseException {
        Boolean isHasProcessTaskAuth = AuthActionChecker.check(PROCESSTASK_MODIFY.class.getSimpleName());
        ProcessTaskVo processTask = processTaskMapper.getProcessTaskAndStepById(processtaskId);
        JSONObject taskJson = null;
        if (processTask != null) {
            processTask.getParamObj().put("isHasProcessTaskAuth", isHasProcessTaskAuth);
            JSONObject task = assembleSingleProcessTask(processTask);
            taskJson = new JSONObject();
            Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
            for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
                IProcessTaskColumn column = entry.getValue();
                taskJson.put(column.getName(), column.getMyValue(task));
            }

            taskJson.put("taskid", processTask.getId());
            // route 供前端跳转路由信息
            JSONObject routeJson = new JSONObject();
            routeJson.put("taskid", processTask.getId());
            taskJson.put("route", routeJson);
            // action 操作
            taskJson.put("action", getStepAction(processTask));
            // 显示/隐藏
            taskJson.put("isShow", task.getInteger(ProcessWorkcenterField.IS_SHOW.getValue()));
        }
        return taskJson;
    }

    @SuppressWarnings("unchecked")
    @Override
    public QueryResultSet searchTaskIterate(WorkcenterVo workcenterVo) {
        return (QueryResultSet)ElasticSearchHandlerFactory.getHandler(ESHandler.PROCESSTASK.getValue())
            .iterateSearch(workcenterVo);
    }

    public Object getStepAction(ProcessTaskVo processTaskVo) {
        List<ProcessTaskStepVo> stepList = processTaskVo.getStepList();
        if (CollectionUtils.isEmpty(stepList)) {
            return CollectionUtils.EMPTY_COLLECTION;
        }
        JSONObject actionJson = getStepActionArray(processTaskVo, stepList);
        return actionJson;
    }

    private JSONObject getStepActionArray(ProcessTaskVo processTaskVo, List<ProcessTaskStepVo> stepList) {
        JSONObject action = new JSONObject();
        String processTaskStatus = processTaskVo.getStatus();
        Boolean isHasAbort = false;
        Boolean isHasRecover = false;
        Boolean isHasUrge = false;
        JSONArray handleArray = new JSONArray();
//        List<ProcessTaskOperationType> operationList = new ArrayList<>();
        if ((ProcessTaskStatus.RUNNING.getValue().equals(processTaskStatus)
            || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStatus)
            || ProcessTaskStatus.ABORTED.getValue().equals(processTaskStatus))) {
            // 工单权限
//            IProcessStepUtilHandler taskHandler =
//                ProcessStepUtilHandlerFactory.getHandler(ProcessStepHandlerType.OMNIPOTENT.getHandler());
//            operationList = taskHandler.getOperateList(processTaskVo, null, new ArrayList<ProcessTaskOperationType>() {
//                private static final long serialVersionUID = 1L;
//                {
//                    add(ProcessTaskOperationType.ABORTPROCESSTASK);
//                    add(ProcessTaskOperationType.RECOVERPROCESSTASK);
//                    add(ProcessTaskOperationType.URGE);
//                }
//            });
//            if (operationList.contains(ProcessTaskOperationType.ABORTPROCESSTASK)) {
//                isHasAbort = true;
//            }
//            if (operationList.contains(ProcessTaskOperationType.RECOVERPROCESSTASK)) {
//                isHasRecover = true;
//            }
//            if (operationList.contains(ProcessTaskOperationType.URGE)) {
//                isHasUrge = true;
//            }
//            // 步骤权限
//            for (ProcessTaskStepVo step : stepList) {
//                if (step.getIsActive() == 1) {
//                    step.setProcessTaskId(processTaskVo.getId());
//                    try {
//                        if (step.getHandler() != null) {
//                            IProcessStepUtilHandler handler =
//                                ProcessStepUtilHandlerFactory.getHandler(step.getHandler());
//                            if (handler != null) {
//                                operationList.addAll(handler.getOperateList(processTaskVo, step,
//                                    new ArrayList<ProcessTaskOperationType>() {
//                                        private static final long serialVersionUID = 1L;
//                                        {
//                                            add(ProcessTaskOperationType.WORK);
//                                        }
//                                    }));
//                            }
//                        }
//                    } catch (Exception ex) {
//                        logger.error(ex.getMessage(), ex);
//                    }
//
//                    if (operationList.contains(ProcessTaskOperationType.WORK)) {
//                        JSONObject configJson = new JSONObject();
//                        configJson.put("taskid", processTaskVo.getId());
//                        configJson.put("stepid", step.getId());
//                        configJson.put("stepName", step.getName());
//                        JSONObject actionJson = new JSONObject();
//                        actionJson.put("name", "handle");
//                        actionJson.put("text", step.getName());
//                        actionJson.put("config", configJson);
//                        handleArray.add(actionJson);
//                    }
//                }
//            }
            ProcessOperateManager.Builder builder = new ProcessOperateManager.Builder(processTaskMapper, userMapper);
            for (ProcessTaskStepVo step : stepList) {
                builder.addProcessTaskStepId(step.getProcessTaskId(), step.getId());
            }
            Map<Long, Set<ProcessTaskOperationType>> operateTypeSetMap =
                builder.addOperationType(ProcessTaskOperationType.ABORTPROCESSTASK)
                    .addOperationType(ProcessTaskOperationType.RECOVERPROCESSTASK)
                    .addOperationType(ProcessTaskOperationType.URGE)
                    .addOperationType(ProcessTaskOperationType.WORKCURRENTSTEP)
                    .build().getOperateMap();

            Set<ProcessTaskOperationType> operationTypeSet = operateTypeSetMap.get(processTaskVo.getId());
            if (CollectionUtils.isNotEmpty(operationTypeSet)) {
                if (operationTypeSet.contains(ProcessTaskOperationType.ABORTPROCESSTASK)) {
                    isHasAbort = true;
                }
                if (operationTypeSet.contains(ProcessTaskOperationType.RECOVERPROCESSTASK)) {
                    isHasRecover = true;
                }
                if (operationTypeSet.contains(ProcessTaskOperationType.URGE)) {
                    isHasUrge = true;
                }
            }
            for (ProcessTaskStepVo step : stepList) {
                Set<ProcessTaskOperationType> set = operateTypeSetMap.get(step.getId());
                if (set.contains(ProcessTaskOperationType.WORKCURRENTSTEP)) {
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

        WorkcenterActionBuilder workcenterFirstActionBuilder = new WorkcenterActionBuilder();
        JSONArray workcenterFirstActionArray = workcenterFirstActionBuilder.setHandleAction(handleArray)
            .setAbortRecoverAction(isHasAbort, isHasRecover, processTaskVo).setUrgeAction(isHasUrge, processTaskVo)
            .build();
        Boolean isNeedFirstAction = false;
        for (Object firstAction : workcenterFirstActionArray) {
            JSONObject firstActionJson = JSONObject.parseObject(firstAction.toString());
            if (firstActionJson.getInteger("isEnable") == 1) {
                isNeedFirstAction = true;
            }
        }
        WorkcenterActionBuilder workcenterSecondActionBuilder = new WorkcenterActionBuilder();
        JSONArray workcenterSecondActionJsonArray =
            workcenterSecondActionBuilder.setShowHideAction(processTaskVo).setDeleteAction(processTaskVo).build();
        for (Object workcenterSecondActionObj : workcenterSecondActionJsonArray) {
            JSONObject workcenterSecondActionJson = JSONObject.parseObject(workcenterSecondActionObj.toString());
            if (ProcessTaskOperationType.SHOW.getValue().equals(workcenterSecondActionJson.getString("name"))) {
                isNeedFirstAction = false;
            }
        }
        if (isNeedFirstAction) {
            action.put("firstActionList", workcenterFirstActionArray);
            action.put("secondActionList", workcenterSecondActionJsonArray);
        } else {
            action.put("firstActionList", workcenterSecondActionJsonArray);
            action.put("secondActionList", new JSONArray());
        }
        return action;
    }

    private JSONObject assembleSingleProcessTask(ProcessTaskVo processTaskVo) {
        if (processTaskVo != null) {
            JSONObject esObject = getProcessTaskESObject(processTaskVo);
            if (MapUtils.isNotEmpty(esObject)) {
                return esObject.getJSONObject("common");
            }
            return null;
        }
        return null;
    }

    @Override
    public JSONObject getProcessTaskESObject(ProcessTaskVo processTaskVo) {
        /** 获取服务信息 **/
        ChannelVo channel = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
        if (channel == null) {
            channel = new ChannelVo();
        }
        /** 获取服务目录信息 **/
        CatalogVo catalog = null;
        if (StringUtils.isNotBlank(channel.getParentUuid())) {
            catalog = catalogMapper.getCatalogByUuid(channel.getParentUuid());
        }
        if (catalog == null) {
            catalog = new CatalogVo();
        }
        /** 获取开始节点内容信息 **/
        ProcessTaskContentVo startContentVo = null;
        List<ProcessTaskStepVo> stepList = processTaskMapper
            .getProcessTaskStepByProcessTaskIdAndType(processTaskVo.getId(), ProcessStepType.START.getValue());
        if (stepList.size() == 1) {
            ProcessTaskStepVo startStepVo = stepList.get(0);
            List<ProcessTaskStepContentVo> processTaskStepContentList =
                processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(startStepVo.getId());
            for (ProcessTaskStepContentVo processTaskStepContent : processTaskStepContentList) {
                if (ProcessTaskOperationType.STARTPROCESS.getValue().equals(processTaskStepContent.getType())) {
                    startContentVo =
                        selectContentByHashMapper.getProcessTaskContentByHash(processTaskStepContent.getContentHash());
                    break;
                }
            }
        }
        /** 获取转交记录 **/
        List<ProcessTaskStepAuditVo> transferAuditList = processTaskMapper.getProcessTaskAuditList(
            new ProcessTaskStepAuditVo(processTaskVo.getId(), ProcessTaskOperationType.TRANSFER.getValue()));

        /** 获取工单当前步骤 **/
        @SuppressWarnings("serial")
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper
            .getProcessTaskActiveStepByProcessTaskIdAndProcessStepType(processTaskVo.getId(), new ArrayList<String>() {
                {
                    add(ProcessStepType.PROCESS.getValue());
                    add(ProcessStepType.START.getValue());
                }
            }, null);
        WorkcenterFieldBuilder builder = new WorkcenterFieldBuilder();

        /** 时效列表 **/
        List<ProcessTaskSlaVo> processTaskSlaList =
            processTaskMapper.getProcessTaskSlaByProcessTaskId(processTaskVo.getId());

        /** 关注此工单的用户列表 */
        List<String> focusUsers = processTaskMapper.getFocusUsersOfProcessTask(processTaskVo.getId());

        // form
        JSONArray formArray = new JSONArray();
        List<ProcessTaskFormAttributeDataVo> formAttributeDataList =
            processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskVo.getId());
        for (ProcessTaskFormAttributeDataVo attributeData : formAttributeDataList) {
            IFormAttributeHandler formHandler = FormAttributeHandlerFactory.getHandler(attributeData.getType());
            if (formHandler != null && !formHandler.isConditionable()) {
                /*if (attributeData.getType().equals(ProcessFormHandlerType.FORMCASCADELIST.getHandler())
                || attributeData.getType().equals(ProcessFormHandlerType.FORMDIVIDER.getHandler())
                || attributeData.getType().equals(ProcessFormHandlerType.FORMDYNAMICLIST.getHandler())
                || attributeData.getType().equals(ProcessFormHandlerType.FORMSTATICLIST.getHandler())) {*/
                continue;
            }
            JSONObject formJson = new JSONObject();
            formJson.put("key", attributeData.getAttributeUuid());
            Object dataObj = attributeData.getDataObj();
            if (dataObj == null) {
                continue;
            }
            formJson.put("value_" + formHandler.getDataType(), dataObj);
            formArray.add(formJson);
        }

        // common
        JSONObject WorkcenterFieldJson = builder.setId(processTaskVo.getId().toString())
            .setSerialNumber(processTaskVo.getSerialNumber()).setTitle(processTaskVo.getTitle())
            .setStatus(processTaskVo.getStatus()).setPriority(processTaskVo.getPriorityUuid())
            .setCatalog(catalog.getUuid()).setChannelType(channel.getChannelTypeUuid()).setChannel(channel.getUuid())
            .setProcessUuid(processTaskVo.getProcessUuid()).setConfigHash(processTaskVo.getConfigHash())
            .setContent(startContentVo).setStartTime(processTaskVo.getStartTime())
            .setEndTime(processTaskVo.getEndTime()).setOwner(processTaskVo.getOwner())
            .setReporter(processTaskVo.getReporter(), processTaskVo.getOwner()).setStepList(processTaskStepList)
            .setTransferFromUserList(transferAuditList).setWorktime(channel.getWorktimeUuid())
            .setExpiredTime(processTaskSlaList).setFocusUsers(focusUsers).setIsShow(processTaskVo.getIsShow()).build();
        JSONObject esObject = new JSONObject();
        esObject.put("form", formArray);
        esObject.put("common", WorkcenterFieldJson);
        return esObject;
    }

    @Override
    public List<WorkcenterTheadVo> getWorkcenterTheadList(WorkcenterVo workcenterVo,
        Map<String, IProcessTaskColumn> columnComponentMap, JSONArray sortColumnList) {
        List<WorkcenterTheadVo> theadList = workcenterMapper
            .getWorkcenterThead(new WorkcenterTheadVo(workcenterVo.getUuid(), UserContext.get().getUserUuid()));
        // 矫正theadList 或存在表单属性或固定字段增删
        // 多删
        ListIterator<WorkcenterTheadVo> it = theadList.listIterator();
        while (it.hasNext()) {
            WorkcenterTheadVo thead = it.next();
            if (thead.getType().equals(ProcessFieldType.COMMON.getValue())) {
                if (!columnComponentMap.containsKey(thead.getName())) {
                    it.remove();
                } else {
                    thead.setDisabled(columnComponentMap.get(thead.getName()).getDisabled()? 1 : 0);
                    thead.setDisplayName(columnComponentMap.get(thead.getName()).getDisplayName());
                    thead.setClassName(columnComponentMap.get(thead.getName()).getClassName());
                    thead.setIsExport(columnComponentMap.get(thead.getName()).getIsExport() ? 1 : 0);
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

}
