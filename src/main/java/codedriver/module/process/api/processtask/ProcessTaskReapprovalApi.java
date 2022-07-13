/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

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
import codedriver.framework.process.dto.ProcessTaskStepDataVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.fulltextindex.ProcessFullTextIndexType;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.process.stephandler.core.IProcessStepHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
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

/**
 * @author linbq
 * @since 2021/9/17 10:54
 **/
@Service
@OperationType(type = OperationTypeEnum.OPERATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskReapprovalApi extends PrivateApiComponentBase {

//    @Resource
//    private ProcessTaskStepSubtaskMapper processTaskStepSubtaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Override
    public String getToken() {
        return "processtask/reapproval";
    }

    @Override
    public String getName() {
        return "工单步骤重审接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "步骤id"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源")
    })
    @Description(desc = "工单步骤重审接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long processTaskId = paramObj.getLong("processTaskId");
        Long processTaskStepId = paramObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        ProcessTaskStepVo processTaskStepVo = processTaskVo.getCurrentProcessTaskStep();
        IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
        }
//        List<ProcessTaskStepSubtaskVo> processTaskStepSubtaskList = processTaskStepSubtaskMapper.getProcessTaskStepSubtaskListByProcessTaskStepId(processTaskStepId);
//        for(ProcessTaskStepSubtaskVo processTaskStepSubtask : processTaskStepSubtaskList) {
//            if(ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepSubtask.getStatus())) {
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
//                throw new ProcessTaskRuntimeException("请完成所有" +  subtaskText + "后再流转");
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
                    paramObj.put("formAttributeDataList", formAttributeDataList);
                }
                JSONArray hidecomponentList = dataObj.getJSONArray("hidecomponentList");
                if (CollectionUtils.isNotEmpty(hidecomponentList)) {
                    paramObj.put("hidecomponentList", hidecomponentList);
                }
                JSONArray readcomponentList = dataObj.getJSONArray("readcomponentList");
                if (CollectionUtils.isNotEmpty(readcomponentList)) {
                    paramObj.put("readcomponentList", readcomponentList);
                }
                JSONObject handlerStepInfo = dataObj.getJSONObject("handlerStepInfo");
                if (MapUtils.isNotEmpty(handlerStepInfo)) {
                    paramObj.put("handlerStepInfo", handlerStepInfo);
                }
                String priorityUuid = dataObj.getString("priorityUuid");
                if (StringUtils.isNotBlank(priorityUuid)) {
                    paramObj.put("priorityUuid", priorityUuid);
                }
                JSONArray fileIdList = dataObj.getJSONArray("fileIdList");
                if (CollectionUtils.isNotEmpty(fileIdList)) {
                    paramObj.put("fileIdList", fileIdList);
                }
                if (!paramObj.containsKey("content")) {
                    String content = dataObj.getString("content");
                    if (StringUtils.isNotBlank(content)) {
                        paramObj.put("content", content);
                    }
                }
            }
        }
        processTaskStepVo.getParamObj().putAll(paramObj);
        handler.reapproval(processTaskStepVo);
        processTaskStepDataMapper.deleteProcessTaskStepData(processTaskStepDataVo);

        //创建全文检索索引
        IFullTextIndexHandler indexFormHandler = FullTextIndexHandlerFactory.getHandler(ProcessFullTextIndexType.PROCESSTASK);
        if (indexFormHandler != null) {
            indexFormHandler.createIndex(processTaskStepVo.getProcessTaskId());
        }
        return null;
    }
}
