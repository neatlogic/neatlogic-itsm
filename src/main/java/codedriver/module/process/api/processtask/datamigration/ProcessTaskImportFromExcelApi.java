package codedriver.module.process.api.processtask.datamigration;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.file.FileUploadException;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
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
            List<String> headerList = (List<String>)data.get("header");
            List<Map<String, Object>> contentList = (List<Map<String, Object>>) data.get("content");
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
                if(CollectionUtils.isNotEmpty(taskList)){
                    canSaveTaskList = taskList.stream().filter(json -> "success".equals(json.getJSONObject("status").getString("reportStatus"))).collect(Collectors.toList());
                    cannotSaveTaskList = taskList.stream().filter(json -> "error".equals(json.getJSONObject("status").getString("reportStatus"))).collect(Collectors.toList());
                }
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
                    }
                }
                JSONObject result = new JSONObject();
                result.put("cannotSaveTaskList",cannotSaveTaskList);
                return result;
            }
        }
        return null;
    }

    private List<JSONObject> parseTaskList(String channelUuid, List<FormAttributeVo> formAttributeList, List<Map<String, Object>> contentList) {
        List<JSONObject> taskList = new ArrayList<>();
        for(Map<String, Object> map : contentList){
            JSONObject task = new JSONObject();
            JSONArray formAttributeDataList = new JSONArray();
            JSONObject status = new JSONObject();
            String reportStatus = "success";
            String reportFailReason = null;

            task.put("channelUuid",channelUuid);
            for(Map.Entry<String,Object> entry : map.entrySet()){
                if("标题".equals(entry.getKey())){
                    if(entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().toString())){
                        task.put("title",entry.getValue().toString());
                    }else{
                        reportStatus = "error";
                        reportFailReason = "工单标题为空";
                    }
                }else if("请求人".equals(entry.getKey())){
                    if(entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().toString())){
                        UserVo user = userMapper.getUserByUserId(entry.getValue().toString());
                        if(user != null){
                            task.put("owner",user.getUuid());
                        }else{
                            reportStatus = "error";
                            reportFailReason = "请求人：" + entry.getValue().toString() + "不存在";
                        }
                    }else{
                        reportStatus = "error";
                        reportFailReason = "请求人为空";
                    }
                }else if("优先级".equals(entry.getKey())){
                    if(entry.getValue() != null && StringUtils.isNotBlank(entry.getValue().toString())){
                        PriorityVo priority = priorityMapper.getPriorityByName(entry.getValue().toString());
                        if(priority != null){
                            task.put("priorityUuid",priority.getUuid());
                        }else{
                            reportStatus = "error";
                            reportFailReason = "优先级：" + entry.getValue().toString() + "不存在";
                        }
                    }else{
                        reportStatus = "error";
                        reportFailReason = "优先级为空";
                    }
                }else if("描述".equals(entry.getKey())){
                    task.put("content",entry.getValue());
                }

                for(FormAttributeVo att: formAttributeList){
                    if(att.getLabel().equals(entry.getKey())){
                        JSONObject formdata = new JSONObject();
                        formdata.put("attributeUuid",att.getUuid());
                        formdata.put("handler",att.getHandler());
                        // TODO 多个值时待处理，如果是日期等特殊类型待校验和转换
                        formdata.put("dataList",entry.getValue().toString());
                        formAttributeDataList.add(formdata);
                        break;
                    }
                }
                status.put("reportStatus",reportStatus);
                status.put("reportFailReason",reportFailReason);
                task.put("status",status);
                task.put("formAttributeDataList",formAttributeDataList);
                task.put("hidecomponentList",new JSONArray());
            }
            taskList.add(task);
        }
        return taskList;
    }
}
