package codedriver.module.process.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
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
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.elasticsearch.core.ElasticSearchFactory;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessFormHandler;
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
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.process.workcenter.dto.WorkcenterFieldBuilder;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.util.TimeUtil;

@Service
public class WorkcenterServiceImpl implements WorkcenterService{
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
        // 搜索es
        // Date time1 = new Date();
        QueryResult result = ElasticSearchFactory.getHandler("processtask").search(workcenterVo);
        // Date time11 = new Date();
        // System.out.println("searchCostTime:"+(time11.getTime()-time1.getTime()));
        List<MultiAttrsObject> resultData = result.getData();
        // 返回的数据重新加工
        List<JSONObject> dataList = new ArrayList<JSONObject>();
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        // 获取用户历史自定义theadList
        // Date time2 = new Date();
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
                    thead.setDisplayName(columnComponentMap.get(thead.getName()).getDisplayName());
                    thead.setClassName(columnComponentMap.get(thead.getName()).getClassName());
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
        }
        // Date time22 = new Date();
        // System.out.println("矫正headerCostTime:"+(time22.getTime()-time2.getTime()));
        if (!resultData.isEmpty()) {
            //Date time3 = new Date();
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
                taskJson.put("action", getStepAction(el));
                dataList.add(taskJson);
            }
            //Date time33 = new Date();
            //System.out.println("拼装CostTime:" + (time33.getTime() - time3.getTime()));
        }
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
    public Object getStepAction(MultiAttrsObject el) throws ParseException {
        JSONObject commonJson = (JSONObject)el.getJSON(ProcessFieldType.COMMON.getValue());
        if (commonJson == null) {
            return CollectionUtils.EMPTY_COLLECTION;
        }
        //task
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
        processTaskVo.setStartTime(TimeUtil.convertStringToDate(commonJson.getString("starttime"), TimeUtil.YYYY_MM_DD_HH_MM_SS));
        processTaskVo.setEndTime(TimeUtil.convertStringToDate(commonJson.getString("endtime"), TimeUtil.YYYY_MM_DD_HH_MM_SS));
        //step
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
            JSONObject stepJson = (JSONObject) stepObj;
            ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
            processTaskStepVo.setId(stepJson.getLong("id"));
            processTaskStepVo.setProcessTaskId(processTaskVo.getId());
            processTaskStepVo.setName(stepJson.getString("name"));
            processTaskStepVo.setStatus(stepJson.getString("status"));
            processTaskStepVo.setType(stepJson.getString("type"));
            processTaskStepVo.setHandler(stepJson.getString("handler"));
            processTaskStepVo.setConfigHash(stepJson.getString("confighash"));
            processTaskStepVo.setStartTime(TimeUtil.convertStringToDate(stepJson.getString("starttime"), TimeUtil.YYYY_MM_DD_HH_MM_SS));
            processTaskStepVo.setEndTime(TimeUtil.convertStringToDate(stepJson.getString("endtime"), TimeUtil.YYYY_MM_DD_HH_MM_SS));
            processTaskStepVo.setIsActive(stepJson.getInteger("isactive"));
            stepList.add(processTaskStepVo);
        }
        JSONArray actionArray = getStepActionArray(processTaskVo, stepList);
        return actionArray;
    }

    /**
     * 工单中心根据条件获取工单列表数据
     * 
     * @param workcenterVo
     * @return
     */
    @Override
    public Integer doSearchCount(WorkcenterVo workcenterVo) {
        // 搜索es
        QueryResult result = ElasticSearchFactory.getHandler("processtask").search(workcenterVo);
        return result.getTotal();
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
    public JSONObject doSearch(Long processtaskId) throws ParseException{
        ProcessTaskVo processTask = processTaskMapper.getProcessTaskAndStepById(processtaskId);
        JSONObject taskJson = null;
        if (processTask != null) {
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
        }
        return taskJson;
    }
    
    @Override
    public QueryResultSet searchTaskIterate(WorkcenterVo workcenterVo) {
        return ElasticSearchFactory.getHandler("processtask").iterateSearch(workcenterVo);
    }

    public Object getStepAction(ProcessTaskVo processTaskVo){
        List<ProcessTaskStepVo> stepList = processTaskVo.getStepList();
        if (CollectionUtils.isEmpty(stepList)) {
            return CollectionUtils.EMPTY_COLLECTION;
        }
        JSONArray actionArray = getStepActionArray(processTaskVo, stepList);
        return actionArray;
    }

    private JSONArray getStepActionArray(ProcessTaskVo processTaskVo, List<ProcessTaskStepVo> stepList) {
        JSONArray actionArray = new JSONArray();
        String processTaskStatus = processTaskVo.getStatus();
        Boolean isHasAbort = false;
        Boolean isHasRecover = false;
        Boolean isHasUrge = false;
        JSONObject handleActionJson = new JSONObject();
        JSONArray handleArray = new JSONArray();
        for (ProcessTaskStepVo step : stepList) {
            Integer isActive = step.getIsActive();
            step.setProcessTaskId(processTaskVo.getId());

            if ((ProcessTaskStatus.RUNNING.getValue().equals(processTaskStatus)
                    || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStatus)
                    || ProcessTaskStatus.ABORTED.getValue().equals(processTaskStatus)
                    || (ProcessTaskStatus.PENDING.getValue().equals(processTaskStatus) && isActive == 1))) {
                List<ProcessTaskOperationType> operationList = new ArrayList<>();
                try {
                    if (step.getHandler() != null) {
                        IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler(step.getHandler());
                        if (handler != null) {
                            operationList = handler.getOperateList(processTaskVo, step, new ArrayList<ProcessTaskOperationType>(){
                                private static final long serialVersionUID = 1L;
                                {
                                    add(ProcessTaskOperationType.WORK);
                                    add(ProcessTaskOperationType.ABORTPROCESSTASK);
                                    add(ProcessTaskOperationType.RECOVERPROCESSTASK);
                                    add(ProcessTaskOperationType.URGE);
                                }
                            });
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }

                if (operationList.contains(ProcessTaskOperationType.WORK)) {
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
                if (operationList.contains(ProcessTaskOperationType.ABORTPROCESSTASK)) {
                    isHasAbort = true;
                }
                if (operationList.contains(ProcessTaskOperationType.RECOVERPROCESSTASK)) {
                    isHasRecover = true;
                }
                if (operationList.contains(ProcessTaskOperationType.URGE)) {
                    isHasUrge = true;
                }
            }
        }

        handleActionJson.put("name", "handle");
        handleActionJson.put("text", "处理");
        handleActionJson.put("sort", 2);
        if (CollectionUtils.isNotEmpty(handleArray)) {
            handleActionJson.put("handleList", handleArray);
            handleActionJson.put("isEnable", 1);
        } else {
            handleActionJson.put("isEnable", 0);
        }

        actionArray.add(handleActionJson);
        // abort|recover
        if (isHasAbort || isHasRecover) {
            if (isHasAbort) {
                JSONObject abortActionJson = new JSONObject();
                abortActionJson.put("name", ProcessTaskOperationType.ABORTPROCESSTASK.getValue());
                abortActionJson.put("text", ProcessTaskOperationType.ABORTPROCESSTASK.getText());
                abortActionJson.put("sort", 2);
                JSONObject configJson = new JSONObject();
                configJson.put("taskid", processTaskVo.getId());
                configJson.put("interfaceurl", "api/rest/processtask/abort?processTaskId=" + processTaskVo.getId());
                abortActionJson.put("config", configJson);
                abortActionJson.put("isEnable", 1);
                actionArray.add(abortActionJson);
            } else {
                JSONObject recoverActionJson = new JSONObject();
                recoverActionJson.put("name", ProcessTaskOperationType.RECOVERPROCESSTASK.getValue());
                recoverActionJson.put("text", ProcessTaskOperationType.RECOVERPROCESSTASK.getText());
                recoverActionJson.put("sort", 2);
                JSONObject configJson = new JSONObject();
                configJson.put("taskid", processTaskVo.getId());
                configJson.put("interfaceurl", "api/rest/processtask/recover?processTaskId=" + processTaskVo.getId());
                recoverActionJson.put("config", configJson);
                recoverActionJson.put("isEnable", 1);
                actionArray.add(recoverActionJson);
            }
        } else {
            JSONObject abortActionJson = new JSONObject();
            abortActionJson.put("name", ProcessTaskOperationType.ABORTPROCESSTASK.getValue());
            abortActionJson.put("text", ProcessTaskOperationType.ABORTPROCESSTASK.getText());
            abortActionJson.put("sort", 2);
            abortActionJson.put("isEnable", 0);
            actionArray.add(abortActionJson);
        }

        // 催办
        JSONObject urgeActionJson = new JSONObject();
        urgeActionJson.put("name", ProcessTaskOperationType.URGE.getValue());
        urgeActionJson.put("text", ProcessTaskOperationType.URGE.getText());
        urgeActionJson.put("sort", 3);
        if (isHasUrge) {
            JSONObject configJson = new JSONObject();
            configJson.put("taskid", processTaskVo.getId());
            configJson.put("interfaceurl", "api/rest/processtask/urge?processTaskId=" + processTaskVo.getId());
            urgeActionJson.put("config", configJson);
            urgeActionJson.put("isEnable", 1);
        } else {
            urgeActionJson.put("isEnable", 0);
        }

        actionArray.add(urgeActionJson);

        actionArray.sort(Comparator.comparing(obj -> ((JSONObject)obj).getInteger("sort")));
        return actionArray;
    }

    private JSONObject assembleSingleProcessTask(ProcessTaskVo processTaskVo) {
        if(processTaskVo != null) {
            JSONObject esObject = getProcessTaskESObject(processTaskVo);
            if(MapUtils.isNotEmpty(esObject)){
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
        if(channel == null){
            channel = new ChannelVo();
        }
        /** 获取服务目录信息 **/
        CatalogVo catalog = null;
        if(StringUtils.isNotBlank(channel.getParentUuid())){
            catalog = catalogMapper.getCatalogByUuid(channel.getParentUuid());
        }
        if(catalog == null){
            catalog = new CatalogVo();
        }
        /** 获取开始节点内容信息 **/
        ProcessTaskContentVo startContentVo = null;
        List<ProcessTaskStepVo> stepList = processTaskMapper.getProcessTaskStepByProcessTaskIdAndType(processTaskVo.getId(), ProcessStepType.START.getValue());
        if (stepList.size() == 1) {
            ProcessTaskStepVo startStepVo = stepList.get(0);
            List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(startStepVo.getId());
            for(ProcessTaskStepContentVo processTaskStepContent : processTaskStepContentList) {
                if (ProcessTaskOperationType.STARTPROCESS.getValue().equals(processTaskStepContent.getType())) {
                    startContentVo = selectContentByHashMapper.getProcessTaskContentByHash(processTaskStepContent.getContentHash());
                    break;
                }
            }
        }
        /** 获取转交记录 **/
        List<ProcessTaskStepAuditVo> transferAuditList = processTaskMapper.getProcessTaskAuditList(new ProcessTaskStepAuditVo(processTaskVo.getId(),ProcessTaskOperationType.TRANSFER.getValue()));

        /** 获取工单当前步骤 **/
        @SuppressWarnings("serial")
        List<ProcessTaskStepVo>  processTaskStepList = processTaskMapper.getProcessTaskActiveStepByProcessTaskIdAndProcessStepType(processTaskVo.getId(),new ArrayList<String>() {{add(ProcessStepType.PROCESS.getValue());add(ProcessStepType.START.getValue());}},null);
        WorkcenterFieldBuilder builder = new WorkcenterFieldBuilder();

        /** 时效列表 **/
        List<ProcessTaskSlaVo> processTaskSlaList = processTaskMapper.getProcessTaskSlaByProcessTaskId(processTaskVo.getId());

        //form
        JSONArray formArray = new JSONArray();
        List<ProcessTaskFormAttributeDataVo> formAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskVo.getId());
        for (ProcessTaskFormAttributeDataVo attributeData : formAttributeDataList) {
            if(attributeData.getType().equals(ProcessFormHandler.FORMCASCADELIST.getHandler())
                    ||attributeData.getType().equals(ProcessFormHandler.FORMDIVIDER.getHandler())
                    ||attributeData.getType().equals(ProcessFormHandler.FORMDYNAMICLIST.getHandler())
                    ||attributeData.getType().equals(ProcessFormHandler.FORMSTATICLIST.getHandler())){
                continue;
            }
            JSONObject formJson = new JSONObject();
            formJson.put("key", attributeData.getAttributeUuid());
            Object dataObj = attributeData.getDataObj();
            if(dataObj == null) {
                continue;
            }
            formJson.put("value_"+ProcessFormHandler.getDataType(attributeData.getType()),dataObj);
            formArray.add(formJson);
        }

        //common
        JSONObject WorkcenterFieldJson = builder
                .setId(processTaskVo.getId().toString())
                .setTitle(processTaskVo.getTitle())
                .setStatus(processTaskVo.getStatus())
                .setPriority(processTaskVo.getPriorityUuid())
                .setCatalog(catalog.getUuid())
                .setChannelType(channel.getChannelTypeUuid())
                .setChannel(channel.getUuid())
                .setProcessUuid(processTaskVo.getProcessUuid())
                .setConfigHash(processTaskVo.getConfigHash())
                .setContent(startContentVo)
                .setStartTime(processTaskVo.getStartTime())
                .setEndTime(processTaskVo.getEndTime())
                .setOwner(processTaskVo.getOwner())
                .setReporter(processTaskVo.getReporter(),processTaskVo.getOwner())
                .setStepList(processTaskStepList)
                .setTransferFromUserList(transferAuditList)
                .setWorktime(channel.getWorktimeUuid())
                .setExpiredTime(processTaskSlaList)
                .build();
        JSONObject esObject = new JSONObject();
        esObject.put("form",formArray);
        esObject.put("common",WorkcenterFieldJson);
        return esObject;
    }

   
}
