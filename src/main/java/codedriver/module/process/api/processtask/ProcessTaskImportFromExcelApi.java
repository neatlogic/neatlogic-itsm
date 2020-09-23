package codedriver.module.process.api.processtask;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.exception.file.EmptyExcelException;
import codedriver.framework.exception.file.ExcelFormatIllegalException;
import codedriver.framework.exception.file.FileNotUploadException;
import codedriver.framework.process.dao.mapper.*;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.form.FormHasNoAttributeException;
import codedriver.framework.process.exception.form.FormNotFoundException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.exception.file.ExcelMissColumnException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.restful.core.publicapi.PublicApiComponentFactory;
import codedriver.framework.restful.dto.ApiVo;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
@Service
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
         * 整体思路：
         * 1、根据channelUuid查询服务、流程和表单
         * 2、获取表单属性列表，首先校验EXCEL中是否包含请求人、标题、优先级以及必填的表单字段
         * 3、读取Excel内容，组装上报工单
         * 4、批量上报
         * 6、保存导入记录
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
            throw new FileNotUploadException();
        }
        MultipartFile multipartFile = null;
        for(Map.Entry<String, MultipartFile> file : multipartFileMap.entrySet()) {
            multipartFile = file.getValue();
            if(!multipartFile.getOriginalFilename().endsWith(".xlsx")){
                throw new ExcelFormatIllegalException(".xlsx");
            }
            Map<String, Object> data = ExcelUtil.getExcelData(multipartFile);
            if(MapUtils.isEmpty(data)){
                throw new EmptyExcelException();
            }
            List<String> headerList = (List<String>)data.get("header");
            List<Map<String, String>> contentList = (List<Map<String, String>>) data.get("content");
            if(CollectionUtils.isNotEmpty(headerList) && CollectionUtils.isNotEmpty(contentList)){
                if (!headerList.contains("标题") || !headerList.contains("请求人") || !headerList.contains("优先级")) {
                    throw new ExcelMissColumnException("标题、请求人或者优先级");
                }
                for(FormAttributeVo att : formAttributeList){
                    if(!headerList.contains(att.getLabel()) && att.isRequired()){
                        throw new ExcelMissColumnException(att.getLabel());
                    }
                }
                List<ProcessTaskImportAuditVo> auditVoList = new ArrayList<>();
                ProcessTaskCreatePublicApi taskCreatePublicApi = (ProcessTaskCreatePublicApi)PublicApiComponentFactory.getInstance(ProcessTaskCreatePublicApi.class.getName());
                int successCount = 0;
                /** 上报工单 */
                for(Map<String, String> map : contentList){
                    JSONObject task = parseTask(channelUuid, formAttributeList, map);
                    ProcessTaskImportAuditVo auditVo = new ProcessTaskImportAuditVo();
                    auditVo.setChannelUuid(channelUuid);
                    auditVo.setTitle(task.getString("title"));
                    auditVo.setOwner(task.getString("owner"));
                    try{
                        ApiVo apiVo = new ApiVo();
                        apiVo.setIsActive(1);
                        JSONObject resultObj = JSONObject.parseObject(taskCreatePublicApi.doService(apiVo,task).toString());
                        auditVo.setProcessTaskId(resultObj.getLong("processTaskId"));
                        auditVo.setStatus(1);
                        successCount++;
                    }catch (Exception e){
                        auditVo.setStatus(0);
                        auditVo.setErrorReason(e.getMessage());
                    }
                    auditVoList.add(auditVo);
                }

                /** 保存导入日志 */
                if(CollectionUtils.isNotEmpty(auditVoList)){
                    if(auditVoList.size() <= 100){
                        processTaskMapper.batchInsertProcessTaskImportAudit(auditVoList);
                    }else{
                        processTaskMapper.batchInsertProcessTaskImportAudit(auditVoList.subList(0,100));
                        processTaskMapper.batchInsertProcessTaskImportAudit(auditVoList.subList(100,auditVoList.size()));
                    }
                }
                JSONObject result = new JSONObject();
                result.put("successCount",successCount);
                result.put("totalCount",contentList.size());
                return result;
            }
        }
        return null;
    }

    /**
     * 组装暂存上报工单
     * @param channelUuid
     * @param formAttributeList
     * @param map
     * @return
     */
    private JSONObject parseTask(String channelUuid, List<FormAttributeVo> formAttributeList, Map<String, String> map) {
        JSONObject task = new JSONObject();
        JSONArray formAttributeDataList = new JSONArray();
        task.put("channelUuid",channelUuid);
        for(Map.Entry<String,String> entry : map.entrySet()){
            if("标题".equals(entry.getKey())){
                task.put("title",entry.getValue());
            }else if("请求人".equals(entry.getKey())){
                UserVo user = null;
                if(StringUtils.isNotBlank(entry.getValue()) && (user = userMapper.getUserByUserId(entry.getValue())) != null){
                    task.put("owner",user.getUuid());
                }else{
                    task.put("owner" ,null);
                }
            }else if("优先级".equals(entry.getKey())){
                PriorityVo priority = null;
                if(StringUtils.isNotBlank(entry.getValue()) && (priority = priorityMapper.getPriorityByName(entry.getValue())) != null){
                    task.put("priorityUuid",priority.getUuid());
                }else{
                    task.put("priorityUuid",null);
                }
            }else if("描述".equals(entry.getKey())){
                task.put("content",entry.getValue());
            }else{
                for(FormAttributeVo att: formAttributeList){
                    if(att.getLabel().equals(entry.getKey())){
                        JSONObject formdata = new JSONObject();
                        formdata.put("attributeUuid",att.getUuid());
                        formdata.put("handler",att.getHandler());
                        if(StringUtils.isNotBlank(entry.getValue()) && entry.getValue().startsWith("[") && entry.getValue().endsWith("]")){
                            JSONArray dataList = JSONArray.parseArray(entry.getValue());
                            formdata.put("dataList",dataList);
                            formAttributeDataList.add(formdata);
                            break;
                        }else{
                            formdata.put("dataList",entry.getValue());
                            formAttributeDataList.add(formdata);
                            break;
                        }
                    }
                }
            }
        }
        task.put("formAttributeDataList",formAttributeDataList);
        task.put("hidecomponentList",new JSONArray());
        return task;
    }
}
