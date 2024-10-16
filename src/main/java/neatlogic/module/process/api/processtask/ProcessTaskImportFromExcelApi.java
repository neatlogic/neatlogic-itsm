package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.file.*;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormActiveVersionNotFoundExcepiton;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.process.auth.BATCH_REPORT_PROCESS_TASK;
import neatlogic.framework.process.constvalue.ProcessTaskSource;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.ExcelUtil;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.catalog.PriorityMapper;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskCreatePublicService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
@Service
@AuthAction(action = BATCH_REPORT_PROCESS_TASK.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class ProcessTaskImportFromExcelApi extends PrivateBinaryStreamApiComponentBase {
    static Logger logger = LoggerFactory.getLogger(ProcessTaskImportFromExcelApi.class);

    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private ProcessMapper processMapper;
    @Resource
    private FormMapper formMapper;
    @Resource
    private PriorityMapper priorityMapper;
    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private ProcessTaskCreatePublicService processTaskCreatePublicService;

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

    @Input({})
    @Output({
            @Param(name = "successCount", type = ApiParamType.INTEGER, desc = "导入成功的工单数"),
            @Param(name = "totalCount", type = ApiParamType.INTEGER, desc = "导入的总工单数")
    })
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
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        //获取所有导入文件
        Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
        //如果没有导入文件, 抛异常
        if (multipartFileMap == null || multipartFileMap.isEmpty()) {
            throw new FileNotUploadException();
        }
        MultipartFile multipartFile = null;
        for (Map.Entry<String, MultipartFile> file : multipartFileMap.entrySet()) {
            multipartFile = file.getValue();
            if (!multipartFile.getOriginalFilename().endsWith(".xlsx")) {
                throw new ExcelFormatIllegalException(".xlsx");
            }
            Map<String, Object> data = getTaskDataFromFirstSheet(multipartFile);
            if (MapUtils.isEmpty(data)) {
                throw new EmptyExcelException();
            }
            List<String> channelData = (List<String>) data.get("channelData");
            if (CollectionUtils.isEmpty(channelData)) {
                throw new ExcelLostChannelUuidException();
            }
            /** 从excel首行第四列取出服务UUID */
            String channelUuid = channelData.get(3);
            if (StringUtils.isBlank(channelUuid)) {
                throw new ExcelLostChannelUuidException();
            }
            ChannelVo channel = channelMapper.getChannelByUuid(channelUuid);
            if (channel == null) {
                throw new ChannelNotFoundException(channelUuid);
            }
            String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
            ProcessVo processVo = processMapper.getProcessBaseInfoByUuid(processUuid);
            if (processVo == null) {
                throw new ProcessNotFoundException(processUuid);
            }
            List<FormAttributeVo> formAttributeVoList = new ArrayList<>();
            JSONObject configObj = processVo.getConfig();
            JSONObject processObj = configObj.getJSONObject("process");
            JSONObject formConfigObj = processObj.getJSONObject("formConfig");
            if (MapUtils.isNotEmpty(formConfigObj)) {
                String formUuid = formConfigObj.getString("uuid");
                if (StringUtils.isNotBlank(formUuid)) {
                    FormVersionVo formVersionVo = formMapper.getActionFormVersionByFormUuid(formUuid);
                    if (formVersionVo == null) {
                        FormVo formVo = formMapper.getFormByUuid(formUuid);
                        if (formVo == null) {
                            throw new FormNotFoundException(formUuid);
                        } else {
                            throw new FormActiveVersionNotFoundExcepiton(formVo.getName());
                        }
                    }
                    ProcessStepVo processStepVo = processMapper.getStartProcessStepByProcessUuid(processUuid);
                    JSONArray stepList = processObj.getJSONArray("stepList");
                    for (int i = 0; i < stepList.size(); i++) {
                        JSONObject stepObj = stepList.getJSONObject(i);
                        if (Objects.equals(stepObj.getString("uuid"), processStepVo.getUuid())) {
                            JSONObject stepConfig = stepObj.getJSONObject("stepConfig");
                            String formSceneUuid = stepConfig.getString("formSceneUuid");
                            if (StringUtils.isNotBlank(formSceneUuid)) {
                                formVersionVo.setSceneUuid(formSceneUuid);
                            }
                        }
                    }
                    formAttributeVoList = formVersionVo.getFormAttributeList();
                }
            }

            List<String> headerList = (List<String>) data.get("header");
            List<Map<String, String>> contentList = (List<Map<String, String>>) data.get("content");
            if (CollectionUtils.isNotEmpty(headerList) && CollectionUtils.isNotEmpty(contentList)) {
                /** 去除表头中的必填提示文字 */
                headerList = headerList.stream().map(header -> header = header.replace("(必填)", "")).collect(Collectors.toList());
                if (!headerList.contains("标题") || !headerList.contains("请求人")) {
                    throw new ExcelMissColumnException("标题、请求人");
                }
                List<ChannelPriorityVo> priorityVos = channelMapper.getChannelPriorityListByChannelUuid(channelUuid);
                int isNeedPriority = 0;
                if (CollectionUtils.isNotEmpty(priorityVos)) {
                    isNeedPriority = 1;
                    if (!headerList.contains("优先级")) {
                        throw new ExcelMissColumnException("优先级");
                    }
                }
                if (CollectionUtils.isNotEmpty(formAttributeVoList)) {
                    for (FormAttributeVo att : formAttributeVoList) {
                        if (!headerList.contains(att.getLabel()) && att.isRequired()) {
                            throw new ExcelMissColumnException(att.getLabel());
                        }
                    }
                }

                List<ProcessTaskImportAuditVo> auditVoList = new ArrayList<>();
                int successCount = 0;
                /** 上报工单 */
                for (Map<String, String> map : contentList) {
                    ProcessTaskCreateVo task = parseTask(channelUuid, formAttributeVoList, map, isNeedPriority);
//                    task.put("source", ProcessTaskSource.IMPORT.getValue());
                    task.setSource(ProcessTaskSource.IMPORT.getValue());
                    ProcessTaskImportAuditVo auditVo = new ProcessTaskImportAuditVo();
                    auditVo.setChannelUuid(channelUuid);
                    auditVo.setTitle(task.getTitle());
                    auditVo.setOwner(task.getOwner());
                    try {
                        Long processTaskId = processTaskCreatePublicService.createProcessTask(task);
                        auditVo.setProcessTaskId(processTaskId);
                        auditVo.setStatus(1);
                        successCount++;
                    } catch (Exception e) {
                        String errMsg = e.getMessage();
                        if (errMsg.contains("title")) {
                            errMsg = errMsg.replace("title", "标题");
                        } else if (errMsg.contains("owner")) {
                            errMsg = errMsg.replace("owner", "请求人");
                        } else if (errMsg.contains("priorityUuid")) {
                            errMsg = errMsg.replace("priorityUuid", "优先级");
                        }
                        auditVo.setStatus(0);
                        auditVo.setErrorReason(errMsg);
                    }
                    auditVoList.add(auditVo);
                }

                /** 保存导入日志 */
                if (CollectionUtils.isNotEmpty(auditVoList)) {
                    if (auditVoList.size() <= 100) {
                        processTaskMapper.batchInsertProcessTaskImportAudit(auditVoList);
                    } else {
                        processTaskMapper.batchInsertProcessTaskImportAudit(auditVoList.subList(0, 100));
                        processTaskMapper.batchInsertProcessTaskImportAudit(auditVoList.subList(100, auditVoList.size()));
                    }
                }
                JSONObject result = new JSONObject();
                result.put("successCount", successCount);
                result.put("totalCount", contentList.size());
                return result;
            } else {
                throw new EmptyExcelException();
            }
        }
        return null;
    }

    /**
     * 组装暂存上报工单
     *
     * @param channelUuid
     * @param formAttributeList
     * @param map
     * @param isNeedPriority    是否需要优先级
     * @return
     */
    private ProcessTaskCreateVo parseTask(String channelUuid, List<FormAttributeVo> formAttributeList, Map<String, String> map, int isNeedPriority) {
//        JSONObject task = new JSONObject();
        ProcessTaskCreateVo processTaskCreateVo = new ProcessTaskCreateVo();
        JSONArray formAttributeDataList = new JSONArray();
//        task.put("channel", channelUuid);
        processTaskCreateVo.setChannel(channelUuid);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey().replace("(必填)", "");
            if ("标题".equals(key)) {
//                task.put("title", entry.getValue());
                processTaskCreateVo.setTitle(entry.getValue());
            } else if ("请求人".equals(key)) {
                if (StringUtils.isNotBlank(entry.getValue())) {
//                    task.put("owner", entry.getValue());
                    processTaskCreateVo.setOwner(entry.getValue());
                }
            } else if ("优先级".equals(key)) {
                PriorityVo priority = null;
                if (isNeedPriority == 1 && StringUtils.isNotBlank(entry.getValue()) && (priority = priorityMapper.getPriorityByName(entry.getValue())) != null) {
//                    task.put("priority", priority.getUuid());
                    processTaskCreateVo.setPriority(priority.getUuid());
                }
            } else if ("描述".equals(key)) {
//                task.put("content", entry.getValue());
                processTaskCreateVo.setContent(entry.getValue());
            } else {
                if (CollectionUtils.isNotEmpty(formAttributeList) && formAttributeList.stream().anyMatch(o -> Objects.equals(o.getLabel(), key))) {
                    JSONObject formdata = new JSONObject();
                    formdata.put("label", key);
                    String content = entry.getValue();
                    if (StringUtils.isNotBlank(content)) {
                        Object formData;
                        if (content.contains(",")) {
                            formData = Arrays.asList(content.split(","));
                        } else {
                            formData = content;
                        }
                        formdata.put("dataList", formData);
                        formAttributeDataList.add(formdata);
                    }
                }
            }
        }
//        task.put("formAttributeDataList", formAttributeDataList);
//        task.put("hidecomponentList", new JSONArray());
//        task.put("readcomponentList", new JSONArray());
        processTaskCreateVo.setFormAttributeDataList(formAttributeDataList);
        processTaskCreateVo.setHidecomponentList(new JSONArray());
        processTaskCreateVo.setReadcomponentList(new JSONArray());
        return processTaskCreateVo;
    }

    private Map<String, Object> getTaskDataFromFirstSheet(MultipartFile file) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            Workbook wb = new XSSFWorkbook(file.getInputStream());
            if (wb == null) {
                throw new EmptyExcelException();
            }

            List<String> headerList = new ArrayList<String>();
            List<Map<String, String>> contentList = new ArrayList<Map<String, String>>();
            List<String> channelData = new ArrayList<>();
            resultMap.put("header", headerList);
            resultMap.put("content", contentList);
            resultMap.put("channelData", channelData);

            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) {
                throw new EmptyExcelException();
            }
            Row channelRow = sheet.getRow(0);
            if (channelRow == null) {
                throw new ExcelLostChannelUuidException();
            }
            //读取服务信息
            for (int i = 0; i < channelRow.getPhysicalNumberOfCells(); i++) {
                Cell cell = channelRow.getCell(i);
                if (cell != null) {
                    String content = ExcelUtil.getCellContent(cell);
                    if (StringUtils.isNotBlank(content)) {
                        channelData.add(content);
                    }
                }
            }

            Row headRow = sheet.getRow(1);
            if (headRow == null) {
                throw new EmptyExcelException();
            }
            List<Integer> cellIndex = new ArrayList<>();
            Iterator<Cell> cellIterator = headRow.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if (cell != null) {
                    String content = ExcelUtil.getCellContent(cell);
                    if (StringUtils.isNotBlank(content)) {
                        headerList.add(content);
                        cellIndex.add(cell.getColumnIndex());
                    }
                }
            }
            if (CollectionUtils.isEmpty(headerList) && CollectionUtils.isEmpty(cellIndex)) {
                throw new EmptyExcelException();
            }
            for (int r = 2; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row != null) {
                    Map<String, String> contentMap = new HashMap<>(cellIndex.size() + 1);
                    for (int ci = 0; ci < cellIndex.size(); ci++) {
                        Cell cell = row.getCell(cellIndex.get(ci));
                        if (cell != null) {
                            String content = ExcelUtil.getCellContent(cell);
                            contentMap.put(headerList.get(ci), content);
                        } else {
                            contentMap.put(headerList.get(ci), null);
                        }
                    }
                    contentList.add(contentMap);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                file.getInputStream().close();
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        return resultMap;
    }
}
