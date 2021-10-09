package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import codedriver.framework.fulltextindex.core.IFullTextIndexHandler;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskStepDataType;
import codedriver.framework.process.dao.mapper.ProcessTaskStepDataMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskStepTaskMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.exception.processtask.task.ProcessTaskStepTaskNotCompleteException;
import codedriver.framework.process.fulltextindex.ProcessFullTextIndexType;
import codedriver.framework.process.service.ProcessTaskService;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.process.task.TaskConfigManager;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskCompleteApi extends PrivateApiComponentBase {

//    @Resource
//    private ProcessTaskStepSubtaskMapper processTaskStepSubtaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Resource
    ProcessTaskStepTaskMapper processTaskStepTaskMapper;

    @Resource
    TaskConfigManager taskConfigManager;

    @Override
    public String getToken() {
        return "processtask/complete";
    }

    @Override
    public String getName() {
        return "工单完成接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单Id"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "当前步骤Id"),
            @Param(name = "nextStepId", type = ApiParamType.LONG, isRequired = true, desc = "激活下一步骤Id"),
            @Param(name = "action", type = ApiParamType.ENUM, rule = "complete,back", isRequired = true, desc = "操作类型"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "原因"),
            @Param(name = "assignWorkerList", type = ApiParamType.JSONARRAY, desc = "分配步骤处理人信息列表，格式[{\"processTaskStepId\":1, \"workerList\":[\"user#xxx\",\"team#xxx\",\"role#xxx\"]}]")
    })
    @Description(desc = "工单完成接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        Long nextStepId = jsonObj.getLong("nextStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId, nextStepId);
        ProcessTaskStepVo processTaskStepVo = processTaskVo.getCurrentProcessTaskStep();
        IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
        }
        //任务
        List<ProcessTaskStepTaskVo> stepTaskVoList = processTaskStepTaskMapper.getStepTaskWithUserByProcessTaskStepId(processTaskStepId);
        if (CollectionUtils.isNotEmpty(stepTaskVoList)) {
            for (ProcessTaskStepTaskVo stepTaskVo : stepTaskVoList) {
                TaskConfigManager.Action<ProcessTaskStepTaskVo> action = taskConfigManager.getConfigMap().get(stepTaskVo.getTaskConfigPolicy());
                if (action != null && !action.execute(stepTaskVo)) {
                    throw new ProcessTaskStepTaskNotCompleteException(stepTaskVo);
                }
            }
        }


//        List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskListByProcessTaskStepId(processTaskStepId);
//        for (ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
//            if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepSubtask.getStatus())) {
//                String subtaskText = "子任务";
//                JSONArray replaceableTextList = processTaskService.getReplaceableTextList(processTaskStepVo);
//                for (int i = 0; i < replaceableTextList.size(); i++) {
//                    JSONObject replaceableText = replaceableTextList.getJSONObject(i);
//                    String name = replaceableText.getString("name");
//                    if (Objects.equals(ReplaceableText.SUBTASK.getValue(), name)) {
//                        String value = replaceableText.getString("value");
//                        if (StringUtils.isNotBlank(value)) {
//                            subtaskText = value;
//                        } else {
//                            String text = replaceableText.getString("text");
//                            if (StringUtils.isNotBlank(text)) {
//                                subtaskText = text;
//                            }
//                        }
//                    }
//                }
//                //如果还有子任务未完成，该步骤不能流转
//                throw new ProcessTaskRuntimeException("请完成所有" + subtaskText + "后再流转");
//            }
//        }
        ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
        processTaskStepDataVo.setProcessTaskId(processTaskId);
        processTaskStepDataVo.setProcessTaskStepId(processTaskStepId);
        processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
        processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
        ProcessTaskStepDataVo stepDraftSaveData = processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
        if (stepDraftSaveData != null) {
            JSONObject dataObj = stepDraftSaveData.getData();
            if (MapUtils.isNotEmpty(dataObj)) {
                JSONArray formAttributeDataList = dataObj.getJSONArray("formAttributeDataList");
                if (CollectionUtils.isNotEmpty(formAttributeDataList)) {
                    jsonObj.put("formAttributeDataList", formAttributeDataList);
                }
                JSONArray hidecomponentList = dataObj.getJSONArray("hidecomponentList");
                if (CollectionUtils.isNotEmpty(hidecomponentList)) {
                    jsonObj.put("hidecomponentList", hidecomponentList);
                }
                JSONArray readcomponentList = dataObj.getJSONArray("readcomponentList");
                if (CollectionUtils.isNotEmpty(readcomponentList)) {
                    jsonObj.put("readcomponentList", readcomponentList);
                }
                JSONObject handlerStepInfo = dataObj.getJSONObject("handlerStepInfo");
                if (MapUtils.isNotEmpty(handlerStepInfo)) {
                    jsonObj.put("handlerStepInfo", handlerStepInfo);
                }
                String priorityUuid = dataObj.getString("priorityUuid");
                if (StringUtils.isNotBlank(priorityUuid)) {
                    jsonObj.put("priorityUuid", priorityUuid);
                }
                JSONArray fileIdList = dataObj.getJSONArray("fileIdList");
                if (CollectionUtils.isNotEmpty(fileIdList)) {
                    jsonObj.put("fileIdList", fileIdList);
                }
                if (!jsonObj.containsKey("content")) {
                    String content = dataObj.getString("content");
                    if (StringUtils.isNotBlank(content)) {
                        jsonObj.put("content", content);
                    }
                }
            }
        }
        processTaskStepVo.setParamObj(jsonObj);
        try {
            handler.complete(processTaskStepVo);
        } catch (ProcessTaskNoPermissionException e) {
            throw new PermissionDeniedException();
        }
        processTaskStepDataMapper.deleteProcessTaskStepData(processTaskStepDataVo);

        //创建全文检索索引
        IFullTextIndexHandler indexFormHandler = FullTextIndexHandlerFactory.getComponent(ProcessFullTextIndexType.PROCESSTASK);//合并工单合并form索引
        if (indexFormHandler != null) {
            indexFormHandler.createIndex(processTaskStepVo.getProcessTaskId());
        }
        return null;
    }

}
