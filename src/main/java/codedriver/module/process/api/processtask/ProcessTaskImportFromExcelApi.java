package codedriver.module.process.api.processtask;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.file.FileUploadException;
import codedriver.framework.process.constvalue.ProcessFormHandler;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
@Service
@Transactional
@OperationType(type = OperationTypeEnum.OPERATE)
public class ProcessTaskImportFromExcelApi extends PrivateBinaryStreamApiComponentBase {
    static Logger logger = LoggerFactory.getLogger(ProcessTaskImportFromExcelApi.class);

    private static final Pattern dataRegex = Pattern.compile("^[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])\\s([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$");
    private static final Pattern timeRegex = Pattern.compile("^([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$");

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
         * 整体思路：
         * 1、根据channelUuid查询服务、流程和表单
         * 2、获取表单属性列表，首先校验EXCEL中是否包含请求人、标题、优先级以及必填的表单字段
         * 3、读取Excel内容，校验每行数据，以importStatus区分是否通过校验
         * 4、记录未通过校验的工单
         * 5、批量上报通过校验的工单
         * 6、记录通过校验的工单
         */
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
        FormVersionVo formVersionVo = formMapper.getActionFormVersionByFormUuid(processForm.getFormUuid());
        List<FormAttributeVo> formAttributeList = null;
        if (formVersionVo != null && StringUtils.isNotBlank(formVersionVo.getFormConfig())) {
            formAttributeList = formVersionVo.getFormAttributeList();
        }
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
                    if(!headerList.contains(att.getLabel()) && att.isRequired()){
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
                        auditVo.setProcessTaskId(saveResultObj.getLong("processTaskId"));
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
                result.put("successCount",CollectionUtils.isNotEmpty(successAuditVoList) ? successAuditVoList.size() : 0);
                result.put("errorCount",CollectionUtils.isNotEmpty(errorAuditVoList) ? errorAuditVoList.size() : 0);
                return result;
            }
        }
        return null;
    }

    /**
     * 组装暂存上报工单列表
     * importStatus区分可上报与不可上报的工单
     * 通过校验的工单：importStatus=success，反之：importStatus=error
     * @param channelUuid
     * @param formAttributeList
     * @param contentList
     * @return
     */
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
                            /** 先校验必填 */
                            if(att.isRequired() && StringUtils.isBlank(entry.getValue())){
                                importStatus = "error";
                                importFailReason = "表单属性：" + entry.getKey() + "不能为空";
                                break;
                            }
                            String config = att.getConfig();
                            JSONObject configObj = JSONObject.parseObject(config);
                            JSONArray dataList = configObj.getJSONArray("dataList");
                            List<JSONObject> dataJsonList = null;
                            if(CollectionUtils.isNotEmpty(dataList)){
                                dataJsonList = dataList.toJavaList(JSONObject.class);
                            }
                            List<String> textList = null;
                            if(CollectionUtils.isNotEmpty(dataJsonList)){
                                textList = dataJsonList.stream().map(obj -> obj.getString("text")).collect(Collectors.toList());
                            }
                            Object data = entry.getValue();
                            /** 如果是文本框或者文本域，那么要校验字符长度 */
                            if(StringUtils.isNotBlank(entry.getValue()) && (ProcessFormHandler.FORMINPUT.getHandler().equals(att.getHandler()))){
                                Integer inputMaxlength = configObj.getInteger("inputMaxlength");
                                if(inputMaxlength != null && entry.getValue().length() > inputMaxlength.intValue()){
                                    importStatus = "error";
                                    importFailReason = entry.getKey() + "过长，不可超过" + inputMaxlength + "个字符";
                                    break;
                                }
                            }
                            if(StringUtils.isNotBlank(entry.getValue()) && (ProcessFormHandler.FORMTEXTAREA.getHandler().equals(att.getHandler()))){
                                Integer textareaMaxlength = configObj.getInteger("textareaMaxlength");
                                if(textareaMaxlength != null && entry.getValue().length() > textareaMaxlength.intValue()){
                                    importStatus = "error";
                                    importFailReason = entry.getKey() + "过长，不可超过" + textareaMaxlength + "个字符";
                                    break;
                                }
                            }
                            /** 如果是下拉框、单选钮，则校验是否在表单配置的可选值范围内 */
                            if(StringUtils.isNotBlank(entry.getValue()) && (ProcessFormHandler.FORMSELECT.getHandler().equals(att.getHandler()) || ProcessFormHandler.FORMRADIO.getHandler().equals(att.getHandler()))){
                                if(CollectionUtils.isNotEmpty(textList) && textList.contains(entry.getValue())){
                                    for(JSONObject json : dataJsonList){
                                        if(json.getString("text").equals(entry.getValue())){
                                            data = json.getString("value");
                                            break;
                                        }
                                    }
                                }else if(CollectionUtils.isNotEmpty(textList) && !textList.contains(entry.getValue())){
                                    importStatus = "error";
                                    importFailReason = entry.getKey() + "：" + entry.getValue() + "不在合法的值范围内";
                                    break;
                                }
                            }
                            /** 如果是复选框，那么就校验每一个值是否在候选值范围内 */
                            if(StringUtils.isNotBlank(entry.getValue()) && ProcessFormHandler.FORMCHECKBOX.getHandler().equals(att.getHandler())){
                                data = new JSONArray();
                                if(!entry.getValue().contains(",") && CollectionUtils.isNotEmpty(textList) && textList.contains(entry.getValue())){
                                    for(JSONObject json : dataJsonList){
                                        if(json.getString("text").equals(entry.getValue())){
                                            ((JSONArray) data).add(json.getString("value"));
                                            break;
                                        }
                                    }
                                }else if(entry.getValue().contains(",") && CollectionUtils.isNotEmpty(textList)){
                                    String[] dataArray = entry.getValue().split(",");
                                    for(String str : dataArray){
                                        for(JSONObject json : dataJsonList){
                                            if(json.getString("text").equals(str)){
                                                ((JSONArray) data).add(json.getString("value"));
                                                break;
                                            }
                                        }
                                    }
                                }
                                if(CollectionUtils.isEmpty((JSONArray) data)){
                                    importStatus = "error";
                                    importFailReason = entry.getKey() + "：" + entry.getValue() + "不在合法的值范围内，多个值需要以英文\",\"隔开";
                                }
                            }
                            /** 如果是日期或时间，则校验是否符合日期|时间格式 */
                            if(StringUtils.isNotBlank(entry.getValue()) && ProcessFormHandler.FORMDATE.getHandler().equals(att.getHandler()) && !dataRegex.matcher(entry.getValue()).matches()){
                                importStatus = "error";
                                importFailReason = entry.getKey() + "：" + entry.getValue() + "不符合日期格式，正确的日期格式为：2020-09-24 06:06:06";
                                break;
                            }
                            if(StringUtils.isNotBlank(entry.getValue()) && ProcessFormHandler.FORMTIME.getHandler().equals(att.getHandler()) && !timeRegex.matcher(entry.getValue()).matches()){
                                importStatus = "error";
                                importFailReason = entry.getKey() + "：" + entry.getValue() + "不符合时间格式，正确的时间格式为：10:06:03";
                                break;
                            }

                            formdata.put("dataList",data);
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
