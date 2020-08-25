package codedriver.module.process.api.processtask.datamigration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;

import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.constvalue.ProcessFlowDirection;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.PriorityVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessTaskConfigVo;
import codedriver.framework.process.dto.ProcessTaskContentVo;
import codedriver.framework.process.dto.ProcessTaskFileVo;
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepConfigVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepRelVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.process.dto.WorktimeVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.core.JsonStreamApiComponentBase;
import codedriver.framework.util.TimeUtil;

@SuppressWarnings("deprecation")
@Service
@Transactional
@OperationType(type = OperationTypeEnum.OPERATE)
public class ProcessTaskImportFromJsonApi extends JsonStreamApiComponentBase {
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

    })
    @Output({

    })
    @Description(desc = "目前用于同步老工单数据到本系统")
    @Override
    public Object myDoService(JSONObject paramObj, JSONReader jsonReader) throws Exception {
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
                        if (StringUtils.isNotBlank(process.getConfig())) {
                            String hash = DigestUtils.md5DigestAsHex(process.getConfig().getBytes());
                            processTask.setConfigHash(hash);
                            processTaskMapper.replaceProcessTaskConfig(new ProcessTaskConfigVo(hash, process.getConfig()));
                        }
                        break;
                    case "channelName":
                        ChannelVo channel = channelMapper.getChannelByName(taskValue);
                        processTask.setChannelUuid(channel.getUuid());
                        break;
                    case "priorityName":
                        PriorityVo priority = priorityMapper.getPriorityByName(taskValue);
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
                    case "workTimeName":
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
                                        processTaskMapper.replaceProcessTaskStepConfig(new ProcessTaskStepConfigVo(configHash,taskStepValue));
                                        break;
                                    case "processTaskStepContentList":
                                        jsonReader.startArray();
                                        while (jsonReader.hasNext()) {
                                            jsonReader.startObject();
                                            ProcessTaskStepCommentVo processTaskStepCommentVo = new ProcessTaskStepCommentVo();
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
                                                        processTaskStepCommentVo.setContentHash(hash);
                                                        processTaskMapper.replaceProcessTaskContent(new ProcessTaskContentVo(hash,content));
                                                        break;
                                                    case "fcu":
                                                        processTaskStepCommentVo.setFcu(taskStepContentValue);
                                                        break;
                                                    case "fcd":
                                                        processTaskStepCommentVo.setFcd(TimeUtil.convertStringToDate(taskStepContentValue, TimeUtil.YYYY_MM_DD_HH_MM_SS));
                                                        break;
                                                    case "fileList":
                                                        jsonReader.startArray();
                                                        while (jsonReader.hasNext()) {
                                                            jsonReader.startObject();
                                                            FileVo file = new FileVo();
                                                            ProcessTaskFileVo processTaskFileVo = new ProcessTaskFileVo();
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
                                                                        file.setUploadTime(taskStepFileValue);
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
                                                            processTaskMapper.insertProcessTaskFile(processTaskFileVo);
                                                            jsonReader.endObject();
                                                        }
                                                        jsonReader.endArray();
                                                        break;
                                                }
                                            }
                                            processTaskStepCommentVo.setProcessTaskId(processTask.getId());
                                            processTaskStepCommentVo.setProcessTaskStepId(processTaskStep.getId());
                                            processTaskMapper.insertProcessTaskStepComment(processTaskStepCommentVo);
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
                            processTaskMapper.replaceProcessTaskContent(contentVo);
                            processTaskMapper.replaceProcessTaskStepContent(new ProcessTaskStepContentVo(processTask.getId(), processTask.getStartProcessTaskStep().getId(), contentVo.getHash()));
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

    @Override
    public boolean isPrivate() {
        return true;
    }

}
