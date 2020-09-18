package codedriver.module.process.api.processtask.datamigration;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.file.FileUploadException;
import codedriver.framework.process.dao.mapper.*;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.form.FormHasNoAttributeException;
import codedriver.framework.process.exception.form.FormNotFoundException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskExcelMissColumnException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentFactory;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.ExcelUtil;
import codedriver.module.process.api.processtask.ProcessTaskDraftSaveApi;
import codedriver.module.process.api.processtask.ProcessTaskProcessableStepList;
import codedriver.module.process.api.processtask.ProcessTaskStartProcessApi;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
@Service
@Transactional
@OperationType(type = OperationTypeEnum.OPERATE)
public class ProcessTaskImportFromExcelApi extends PrivateBinaryStreamApiComponentBase {
    static Logger logger = LoggerFactory.getLogger(ProcessTaskImportFromExcelApi.class);

    @Autowired
    private ChannelMapper channelMapper;
    @Autowired
    private ProcessMapper processMapper;
    @Autowired
    private FormMapper formMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PriorityMapper priorityMapper;
    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getToken() {
        return "processtask/import/fromexcel";
    }

    @Override
    public String getName() {
        return "导入工单数据(通过固定格式excel文件)";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name="channelUuid", type= ApiParamType.STRING, isRequired=true, desc="服务uuid")})
    @Output({})
    @Description(desc = "导入工单数据(通过固定格式excel文件)")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        /**
         * 根据服务ID寻找对应的流程和表单，以此判断导入的excel格式是否合法
         */
        // TODO 记录导入记录
        // TODO 校验表单数据
        String channelUuid = paramObj.getString("channelUuid");
        ChannelVo channel = channelMapper.getChannelByUuid(channelUuid);
        if(channel == null){
            throw new ChannelNotFoundException(channelUuid);
        }
        String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
        if(processMapper.checkProcessIsExists(processUuid) == 0) {
            throw new ProcessNotFoundException(processUuid);
        }
        ProcessFormVo processForm = processMapper.getProcessFormByProcessUuid(processUuid);
        if(processForm == null || formMapper.checkFormIsExists(processForm.getFormUuid()) == 0){
            throw new FormNotFoundException(processForm.getFormUuid());
        }
        List<String> channelUuidList = new ArrayList<>();
        channelUuidList.add(channelUuid);
        List<FormAttributeVo> formAttributeList = formMapper.getFormAttributeListByChannelUuidList(channelUuidList);
        if(CollectionUtils.isEmpty(formAttributeList)){
            throw new FormHasNoAttributeException(processForm.getFormUuid());
        }

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        //获取所有导入文件
        Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
        //如果没有导入文件, 抛异常
        if(multipartFileMap == null || multipartFileMap.isEmpty()) {
            throw new FileUploadException("没有导入文件");
        }
        MultipartFile multipartFile = null;
        for(Map.Entry<String, MultipartFile> file : multipartFileMap.entrySet()) {
            multipartFile = file.getValue();
            Map<String, Object> data = ExcelUtil.getExcelData(multipartFile);
            if(MapUtils.isEmpty(data)){
                throw  new FileUploadException("Excel内容为空");
            }
            List<String> headerList = (List<String>)data.get("header");
            List<Map<String, String>> contentList = (List<Map<String, String>>) data.get("content");
            if(CollectionUtils.isNotEmpty(headerList) && CollectionUtils.isNotEmpty(contentList)){
                if (!headerList.contains("标题") || !headerList.contains("请求人") || !headerList.contains("优先级")) {
                    throw new ProcessTaskExcelMissColumnException("Excel中缺少标题、请求人或者优先级");
                }
                for(FormAttributeVo att: formAttributeList){
                    if(!headerList.contains(att.getLabel()) && att.getIsRequiredFromConfig() == true){
                        throw new ProcessTaskExcelMissColumnException("Excel中缺少" + att.getLabel());
                    }
                }
                List<JSONObject> taskList = parseTaskList(channelUuid, formAttributeList, contentList);
                List<JSONObject> canSaveTaskList = null;
                List<JSONObject> cannotSaveTaskList = null;
                List<ProcessTaskImportAuditVo> successAuditVoList = new ArrayList<>();
                List<ProcessTaskImportAuditVo> errorAuditVoList = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(taskList)){
                    canSaveTaskList = taskList.stream().filter(json -> "success".equals(json.getString("importStatus"))).collect(Collectors.toList());
                    cannotSaveTaskList = taskList.stream().filter(json -> "error".equals(json.getString("importStatus"))).collect(Collectors.toList());
                }
                /** 先把没有通过检验的待上报工单记录起来 */
                if(CollectionUtils.isNotEmpty(cannotSaveTaskList)){
                    for(JSONObject jsonObj : cannotSaveTaskList){
                        ProcessTaskImportAuditVo auditVo = new ProcessTaskImportAuditVo();
                        auditVo.setChannelUuid(channelUuid);
                        auditVo.setTitle(jsonObj.getString("title"));
                        auditVo.setStatus(0);
                        auditVo.setErrorReason(jsonObj.getString("importFailReason"));
                        auditVo.setOwner(jsonObj.getString("owner"));
                        errorAuditVoList.add(auditVo);
                    }
                }
                if(CollectionUtils.isNotEmpty(errorAuditVoList)){
                    processTaskMapper.batchInsertProcessTaskImportAudit(errorAuditVoList);
                }
                /** 提交通过校验的待上报工单 */
                if(CollectionUtils.isNotEmpty(canSaveTaskList)){
                    ProcessTaskDraftSaveApi drafSaveApi = (ProcessTaskDraftSaveApi) PrivateApiComponentFactory.getInstance(ProcessTaskDraftSaveApi.class.getName());
                    for(JSONObject jsonObj : canSaveTaskList){
                        JSONObject saveResultObj = JSONObject.parseObject(drafSaveApi.doService(PrivateApiComponentFactory.getApiByToken(drafSaveApi.getToken()), jsonObj).toString());
                        saveResultObj.put("action", "start");

                        //查询可执行下一步骤
                        ProcessTaskProcessableStepList stepListApi  = (ProcessTaskProcessableStepList)PrivateApiComponentFactory.getInstance(ProcessTaskProcessableStepList.class.getName());
                        Object nextStepListObj = stepListApi.doService(PrivateApiComponentFactory.getApiByToken(stepListApi.getToken()),saveResultObj);
                        List<ProcessTaskStepVo> nextStepList  =  (List<ProcessTaskStepVo>)nextStepListObj;
                        if(CollectionUtils.isEmpty(nextStepList) && nextStepList.size() != 1) {
                            throw new RuntimeException("抱歉！暂不支持开始节点连接多个后续节点。");
                        }
                        saveResultObj.put("nextStepId", nextStepList.get(0).getId());

                        //流转
                        ProcessTaskStartProcessApi startProcessApi  = (ProcessTaskStartProcessApi)PrivateApiComponentFactory.getInstance(ProcessTaskStartProcessApi.class.getName());
                        startProcessApi.doService(PrivateApiComponentFactory.getApiByToken(startProcessApi.getToken()),saveResultObj);
                        ProcessTaskImportAuditVo auditVo = new ProcessTaskImportAuditVo();
                        auditVo.setProcesstaskId(saveResultObj.getLong("processTaskId"));
                        auditVo.setChannelUuid(channelUuid);
                        auditVo.setTitle(jsonObj.getString("title"));
                        auditVo.setStatus(1);
                        auditVo.setOwner(jsonObj.getString("owner"));
                        successAuditVoList.add(auditVo);
                    }
                }
                /** 记录上报成功的工单 */
                if(CollectionUtils.isNotEmpty(successAuditVoList)){
                    processTaskMapper.batchInsertProcessTaskImportAudit(successAuditVoList);
                }
                JSONObject result = new JSONObject();
                result.put("cannotSaveTaskList",cannotSaveTaskList);
                return result;
            }
        }
        return null;
    }

    private List<JSONObject> parseTaskList(String channelUuid, List<FormAttributeVo> formAttributeList, List<Map<String, String>> contentList) {
        List<JSONObject> taskList = new ArrayList<>();
        for(Map<String, String> map : contentList){
            JSONObject task = new JSONObject();
            JSONArray formAttributeDataList = new JSONArray();
            String importStatus = "success";
            String importFailReason = null;

            task.put("channelUuid",channelUuid);
            for(Map.Entry<String,String> entry : map.entrySet()){
                if("标题".equals(entry.getKey())){
                    if(StringUtils.isNotBlank(entry.getValue())){
                        task.put("title",entry.getValue());
                    }else{
                        importStatus = "error";
                        importFailReason = "工单标题为空";
                    }
                }else if("请求人".equals(entry.getKey())){
                    if(StringUtils.isNotBlank(entry.getValue())){
                        UserVo user = userMapper.getUserByUserId(entry.getValue());
                        if(user != null){
                            task.put("owner",user.getUuid());
                        }else{
                            importStatus = "error";
                            importFailReason = "请求人：" + entry.getValue() + "不存在";
                        }
                    }else{
                        importStatus = "error";
                        importFailReason = "请求人为空";
                    }
                }else if("优先级".equals(entry.getKey())){
                    if(StringUtils.isNotBlank(entry.getValue())){
                        PriorityVo priority = priorityMapper.getPriorityByName(entry.getValue());
                        List<ChannelPriorityVo> priorityList = channelMapper.getChannelPriorityListByChannelUuid(channelUuid);
                        List<String> priorityUuidList = null;
                        if(CollectionUtils.isNotEmpty(priorityList)){
                            priorityUuidList = priorityList.stream().map(ChannelPriorityVo::getPriorityUuid).collect(Collectors.toList());
                        }
                        if(priority == null){
                            importStatus = "error";
                            importFailReason = "优先级：" + entry.getValue() + "不存在";
                        }else if(CollectionUtils.isNotEmpty(priorityUuidList) && !priorityUuidList.contains(priority.getUuid())){
                            importStatus = "error";
                            importFailReason = "优先级：" + entry.getValue() + "与服务优先级不匹配";
                        }else{
                            task.put("priorityUuid",priority.getUuid());
                        }
                    }else{
                        importStatus = "error";
                        importFailReason = "优先级为空";
                    }
                }else if("描述".equals(entry.getKey())){
                    task.put("content",entry.getValue());
                }else{
                    for(FormAttributeVo att: formAttributeList){
                        if(att.getLabel().equals(entry.getKey())){
                            JSONObject formdata = new JSONObject();
                            formdata.put("attributeUuid",att.getUuid());
                            formdata.put("handler",att.getHandler());
                            // TODO 多个值时待处理，如果是日期等特殊类型待校验和转换，要根据不同的handler校验
                            formdata.put("dataList",entry.getValue());
                            formAttributeDataList.add(formdata);
                            break;
                        }
                    }
                }

                task.put("importStatus",importStatus);
                task.put("importFailReason",importFailReason);
                task.put("formAttributeDataList",formAttributeDataList);
                task.put("hidecomponentList",new JSONArray());
            }
            taskList.add(task);
        }
        return taskList;
    }
}
