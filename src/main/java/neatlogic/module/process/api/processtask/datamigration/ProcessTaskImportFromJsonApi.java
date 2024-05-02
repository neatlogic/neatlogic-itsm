package neatlogic.module.process.api.processtask.datamigration;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessFlowDirection;
import neatlogic.framework.process.constvalue.ProcessStepType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberPolicyHandlerNotFoundException;
import neatlogic.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberPolicyNotFoundException;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.ProcessTaskSerialNumberPolicyHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateJsonStreamApiComponentBase;
import neatlogic.framework.util.TimeUtil;
import neatlogic.framework.worktime.dao.mapper.WorktimeMapper;
import neatlogic.framework.worktime.dto.WorktimeVo;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.catalog.PriorityMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSerialNumberMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
@Service
@Transactional
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class ProcessTaskImportFromJsonApi extends PrivateJsonStreamApiComponentBase {
    static Logger logger = LoggerFactory.getLogger(ProcessTaskImportFromJsonApi.class);
    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private ProcessMapper processMapper;

    @Autowired
    private ChannelMapper channelMapper;
    
    @Autowired
    private PriorityMapper priorityMapper;
    
    @Autowired
    private WorktimeMapper worktimeMapper;
    
    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private ProcessTaskSerialNumberMapper processTaskSerialNumberMapper;

    @Override
    public String getToken() {
        return "processtask/import/fromjson";
    }

    @Override
    public String getName() {
        return "导入工单数据(通过固定格式json文件)";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源"),
    })
    @Output({

    })
    @Description(desc = "目前用于同步老工单数据到本系统")
    @Override
    public Object myDoService(JSONObject paramObj, JSONReader jsonReader) throws Exception {
        String source = paramObj.getString("source");
        List<ProcessTaskVo> processTaskList = new ArrayList<ProcessTaskVo>();
        List<String> errorTaskList = new ArrayList<String>();
        jsonReader.startArray();
        while (jsonReader.hasNext()) {
            ProcessTaskVo processTask = new ProcessTaskVo();
            Boolean isContinute = false;
            Map<Long,String> stepIdUuidMap = new HashMap<Long,String>();
            jsonReader.startObject();
            while (jsonReader.hasNext()) {
                String taskKey = jsonReader.readString();
                String taskValue = StringUtils.EMPTY;
                if(isContinute) {
                    taskValue =jsonReader.readObject().toString();
                    continue;
                }
                if(!taskKey.equals("processTaskStepList")&&!taskKey.equals("processTaskStepRelList")&&!taskKey.equals("formAndPropList")) {
                    taskValue =jsonReader.readObject().toString();
                    if(taskValue.equals(StringUtils.EMPTY)) {
                        continue;
                    }
                }
                switch (taskKey) {
                    case "id":
                        processTask.setId(Long.valueOf(taskValue));
                        break;
                    case "title":
                        processTask.setTitle(taskValue);
                        break;
                    case "processName":
                        ProcessVo process = processMapper.getProcessByName(taskValue);
                        if(process == null) {
                            isContinute = true;
                            String errorTask = processTask.getId()+" 工单的 '"+taskValue+"' 流程不存在";
                            logger.error(errorTask);
                            errorTaskList.add(errorTask);
                            break;
                        }
                        processTask.setProcessUuid(process.getUuid());
                        String configStr = process.getConfigStr();
                        if (StringUtils.isNotBlank(configStr)) {
                            String hash = DigestUtils.md5DigestAsHex(configStr.getBytes());
                            processTask.setConfigHash(hash);
                            processTaskMapper.insertIgnoreProcessTaskConfig(new ProcessTaskConfigVo(hash, configStr));
                        }
                        break;
                    case "channelName":
                        ChannelVo channel = channelMapper.getChannelByName(taskValue);
                        if(channel == null) {
                            isContinute = true;
                            String errorTask = processTask.getId()+" 工单的 '"+taskValue+"' 服务不存在";
                            logger.error(errorTask);
                            errorTaskList.add(errorTask);
                            break;
                        }
                        processTask.setChannelUuid(channel.getUuid());
                        /* 生成工单号 **/
                        ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo = processTaskSerialNumberMapper.getProcessTaskSerialNumberPolicyByChannelTypeUuid(channel.getChannelTypeUuid());
                        if (processTaskSerialNumberPolicyVo == null) {
                            throw new ProcessTaskSerialNumberPolicyNotFoundException(channel.getChannelTypeUuid());
                        }
                        IProcessTaskSerialNumberPolicyHandler policyHandler = ProcessTaskSerialNumberPolicyHandlerFactory.getHandler(processTaskSerialNumberPolicyVo.getHandler());
                        if (policyHandler == null) {
                            throw new ProcessTaskSerialNumberPolicyHandlerNotFoundException(processTaskSerialNumberPolicyVo.getHandler());
                        }
                        String serialNumber = policyHandler.genarate(channel.getChannelTypeUuid());
                        processTask.setSerialNumber(serialNumber);
                        break;
                    case "priorityName":
                        PriorityVo priority = priorityMapper.getPriorityByName(taskValue);
                        if(priority == null) {
                            isContinute = true;
                            String errorTask = processTask.getId()+" 工单的 '"+taskValue+"' 优先级不存在";
                            logger.error(errorTask);
                            errorTaskList.add(errorTask);
                            break;
                        }
                        processTask.setPriorityUuid(priority.getUuid());
                        break;
                    case "status":
                        processTask.setStatus(taskValue);
                        break;
                    case "owner":
                        processTask.setOwner(taskValue);
                        break;
                    case "reporter":
                        processTask.setReporter(taskValue);
                        break;
                    case "startTime":
                        processTask.setStartTime(TimeUtil.convertStringToDate(taskValue, TimeUtil.YYYY_MM_DD_HH_MM_SS));
                        break;
                    case "endTime":
                        processTask.setEndTime(TimeUtil.convertStringToDate(taskValue, TimeUtil.YYYY_MM_DD_HH_MM_SS));
                        break;
                    case "worktimeName":
                        WorktimeVo worktime = worktimeMapper.getWorktimeByName(taskValue);
                        processTask.setWorktimeUuid(worktime.getUuid());
                        break;
                    case "processTaskStepList":
                        jsonReader.startArray();
                        List<ProcessStepVo> processStepList = processMapper.getProcessStepDetailByProcessUuid(processTask.getProcessUuid());
                        while (jsonReader.hasNext()) {
                            ProcessTaskStepVo processTaskStep = new ProcessTaskStepVo(); 
                           
                            Boolean isSaveProcessStep = true;
                            jsonReader.startObject();
                            while (jsonReader.hasNext()) {
                                String taskStepKey = jsonReader.readString();
                                String taskStepValue = (!taskStepKey.equals("processTaskStepContentList"))? jsonReader.readObject().toString():StringUtils.EMPTY;
                                if(!taskStepKey.equals("processTaskStepContentList")&&taskStepValue.equals(StringUtils.EMPTY)) {
                                    continue;
                                }
                                processTaskStep.setProcessTaskId(processTask.getId());
                                switch (taskStepKey) {
                                    case "id":
                                        processTaskStep.setId(Long.valueOf(taskStepValue));
                                        break;
                                    case "name":
                                        processTaskStep.setName(taskStepValue);
                                        List<ProcessStepVo> processStep = null;
                                        if("开始".equals(taskStepValue)) {
                                            processStep = processStepList.stream().filter(o ->ProcessStepType.START.getValue().equals(o.getType())).collect(Collectors.toList()); 
                                            processTaskStep.setProcessStepUuid(processStep.get(0).getUuid());
                                            stepIdUuidMap.put(processTaskStep.getId(), processTaskStep.getProcessStepUuid());
                                        }else {
                                            processStep = processStepList.stream().filter(o ->o.getName().equals(taskStepValue)).collect(Collectors.toList()); 
                                            if(CollectionUtils.isNotEmpty(processStep)) {
                                                processTaskStep.setProcessStepUuid(processStep.get(0).getUuid());
                                                stepIdUuidMap.put(processTaskStep.getId(), processTaskStep.getProcessStepUuid());
                                            }else {
                                                isSaveProcessStep = false;
                                                break;
                                            }
                                        }
                                        break;
                                    case "status":
                                        processTaskStep.setStatus(taskStepValue);
                                        break;
                                    case "type":
                                        processTaskStep.setType(taskStepValue);
                                        if(taskStepValue.equals(ProcessStepType.START.getValue())) {
                                            processTask.setStartProcessTaskStep(processTaskStep);
                                        }
                                        break;
                                    case "handler":
                                        processTaskStep.setHandler(taskStepValue);
                                        break;
                                    case "startTime":
                                        processTaskStep.setStartTime(TimeUtil.convertStringToDate(taskStepValue, TimeUtil.YYYY_MM_DD_HH_MM_SS));
                                        break;
                                    case "endTime":
                                        processTaskStep.setEndTime(TimeUtil.convertStringToDate(taskStepValue, TimeUtil.YYYY_MM_DD_HH_MM_SS));
                                        break;
                                    case "configHash":
                                        String configHash = DigestUtils.md5DigestAsHex(taskStepValue.getBytes());
                                        processTaskStep.setConfigHash(configHash);
                                        processTaskMapper.insertIgnoreProcessTaskStepConfig(new ProcessTaskStepConfigVo(configHash,taskStepValue));
                                        break;
                                    case "processTaskStepContentList":
                                        jsonReader.startArray();
                                        while (jsonReader.hasNext()) {
                                            jsonReader.startObject();
                                            ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo();
                                            while (jsonReader.hasNext()) {
                                                String taskStepContentKey = jsonReader.readString();
                                                String taskStepContentValue = StringUtils.EMPTY;
                                                if(!taskStepContentKey.equals("fileList")) {
                                                    taskStepContentValue = jsonReader.readObject().toString();
                                                    if(taskStepContentValue.equals(StringUtils.EMPTY)) {
                                                        continue;
                                                    }
                                                }
                                                switch (taskStepContentKey) {
                                                    case "content":
                                                        String content = StringEscapeUtils.unescapeHtml4(taskStepContentValue);
                                                        String hash = DigestUtils.md5DigestAsHex(content.getBytes());
                                                        processTaskStepContentVo.setContentHash(hash);
                                                        processTaskMapper.insertIgnoreProcessTaskContent(new ProcessTaskContentVo(hash,content));
                                                        break;
                                                    case "fcu":
                                                        processTaskStepContentVo.setFcu(taskStepContentValue);
                                                        break;
                                                    case "fcd":
                                                        processTaskStepContentVo.setFcd(TimeUtil.convertStringToDate(taskStepContentValue, TimeUtil.YYYY_MM_DD_HH_MM_SS));
                                                        break;
                                                    case "fileList":
                                                        jsonReader.startArray();
                                                        while (jsonReader.hasNext()) {
                                                            jsonReader.startObject();
                                                            FileVo file = new FileVo();
                                                            ProcessTaskStepFileVo processTaskFileVo = new ProcessTaskStepFileVo();
                                                            while (jsonReader.hasNext()) {
                                                                String taskStepFileKey = jsonReader.readString();
                                                                String taskStepFileValue = jsonReader.readObject().toString();
                                                                switch(taskStepFileKey) {
                                                                    case "id":
                                                                        file.setId(Long.valueOf(taskStepFileValue));
                                                                        break;
                                                                    case "name":
                                                                        file.setName(taskStepFileValue);
                                                                        break;
                                                                    case "size":
                                                                        file.setSize(Long.valueOf(taskStepFileValue));
                                                                        break;
                                                                    case "userId":
                                                                        file.setUserUuid(taskStepFileValue);
                                                                        break;
                                                                    case "uploadTime":
                                                                        //file.setUploadTime(taskStepFileValue);
                                                                        break;
                                                                    case "path":
                                                                        file.setPath("file:"+taskStepFileValue);
                                                                        break;
                                                                    case "contentType":
                                                                        file.setContentType(taskStepFileValue);
                                                                        break;
                                                                    case "type":
                                                                        file.setType(taskStepFileValue);
                                                                        break;
                                                                }
                                                            }
                                                            fileMapper.insertFile(file);
                                                            processTaskFileVo.setProcessTaskId(processTask.getId());
                                                            processTaskFileVo.setProcessTaskStepId(processTaskStep.getId());
                                                            processTaskFileVo.setFileId(file.getId());
                                                            processTaskFileVo.setContentId(processTaskStepContentVo.getId());
                                                            processTaskMapper.insertProcessTaskStepFile(processTaskFileVo);
                                                            jsonReader.endObject();
                                                        }
                                                        jsonReader.endArray();
                                                        break;
                                                }
                                            }
                                            processTaskStepContentVo.setProcessTaskId(processTask.getId());
                                            processTaskStepContentVo.setProcessTaskStepId(processTaskStep.getId());
                                            processTaskStepContentVo.setType(ProcessTaskOperationType.STEP_COMMENT.getValue());
                                            if (StringUtils.isNotBlank(source)) {
                                                processTaskStepContentVo.setSource(source);
                                            }
                                            processTaskMapper.insertProcessTaskStepContent(processTaskStepContentVo);
                                            jsonReader.endObject();
                                        }
                                        jsonReader.endArray();
                                        break;
                                }
                            }
                            jsonReader.endObject();
                            if(isSaveProcessStep) {
                                processTaskMapper.replaceProcessTaskStep(processTaskStep);
                            }
                        }
                        jsonReader.endArray();
                        break;
                    case "content":
                        if (StringUtils.isNotBlank(taskValue)) {
                            ProcessTaskContentVo contentVo = new ProcessTaskContentVo(taskValue);
                            processTaskMapper.insertIgnoreProcessTaskContent(contentVo);
                            ProcessTaskStepContentVo processTaskStepContentVo = new ProcessTaskStepContentVo(processTask.getId(), processTask.getStartProcessTaskStep().getId(), contentVo.getHash(), null);
                            if (StringUtils.isNotBlank(source)) {
                                processTaskStepContentVo.setSource(source);
                            }
                            processTaskMapper.insertProcessTaskStepContent(processTaskStepContentVo);
                        }
                        break;
                    case "processTaskStepRelList":
                        jsonReader.startArray();
                        while (jsonReader.hasNext()) {
                            jsonReader.startObject();
                            ProcessTaskStepRelVo processTaskStepRel = new ProcessTaskStepRelVo();
                            while (jsonReader.hasNext()) {
                                String taskStepRelKey = jsonReader.readString();
                                String taskStepRelValue = jsonReader.readObject().toString();
                                processTaskStepRel.setProcessTaskId(processTask.getId());
                                processTaskStepRel.setIsHit(1);
                                switch (taskStepRelKey) {
                                    case "fromStepId":
                                        processTaskStepRel.setFromProcessTaskStepId(Long.valueOf(taskStepRelValue));
                                        processTaskStepRel.setFromProcessStepUuid(stepIdUuidMap.get(processTaskStepRel.getFromProcessTaskStepId()));
                                        break;
                                    case "toStepId":
                                        processTaskStepRel.setToProcessTaskStepId(Long.valueOf(taskStepRelValue));
                                        processTaskStepRel.setToProcessStepUuid(stepIdUuidMap.get(processTaskStepRel.getToProcessTaskStepId()));
                                        break;
                                }
                            }
                            processTaskStepRel.setType(ProcessFlowDirection.FORWARD.getValue());
                            processTaskMapper.insertProcessTaskStepRel(processTaskStepRel);
                            jsonReader.endObject();
                        }
                        jsonReader.endArray();
                        break;
                    case "formAndPropList":
                        jsonReader.startObject();
                        String form = StringUtils.EMPTY;
                        String prop = StringUtils.EMPTY;
                        while (jsonReader.hasNext()) {
                            String formAndPropKey = jsonReader.readString();
                            String formAndPropValue = jsonReader.readObject().toString();
                            switch (formAndPropKey) {
                                case "formHtml":
                                    form = formAndPropValue;
                                    break;
                                case "propList":
                                    prop = formAndPropValue;
                                    break;
                            }
                        }
                        processTaskMapper.replaceProcessTaskOldFormProp(processTask.getId(), form, prop);
                        jsonReader.endObject();
                        break;
                }
            }
            jsonReader.endObject();
            if(!isContinute) {
                processTaskList.add(processTask);
                processTaskMapper.replaceProcessTask(processTask);
            }
            processTaskList.clear();

        }
        jsonReader.endArray();
        jsonReader.close();
        return errorTaskList;
    }
}
