/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.workcenter;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.form.attribute.core.FormAttributeDataConversionHandlerFactory;
import codedriver.framework.form.attribute.core.IFormAttributeDataConversionHandler;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.AttributeDataVo;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessFormVo;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.FileUtil;
import codedriver.module.framework.form.attribute.handler.DivideHandler;
import codedriver.module.process.dao.mapper.ProcessMapper;
import codedriver.module.process.service.NewWorkcenterService;
import codedriver.module.process.sql.decorator.SqlBuilder;
import codedriver.module.process.workcenter.column.handler.ProcessTaskCurrentStepColumn;
import codedriver.module.process.workcenter.column.handler.ProcessTaskCurrentStepNameColumn;
import codedriver.module.process.workcenter.column.handler.ProcessTaskCurrentStepWorkerColumn;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 导出工单中心工单数据接口
 * 同时支持按分类导出与实时查询结果导出
 */
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkcenterDataExportApi extends PrivateBinaryStreamApiComponentBase {

    static Logger logger = LoggerFactory.getLogger(WorkcenterDataExportApi.class);

    @Resource
    NewWorkcenterService newWorkcenterService;

    @Resource
    ChannelMapper channelMapper;

    @Resource
    ProcessMapper processMapper;

    @Resource
    FormMapper formMapper;

    @Resource
    ProcessTaskMapper processTaskMapper;

    @Override
    public String getToken() {
        return "workcenter/export";
    }

    @Override
    public String getName() {
        return "导出工单中心数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "分类uuid", isRequired = true),
            @Param(name = "conditionConfig", type = ApiParamType.JSONOBJECT, desc = "条件设置，为空则使用数据库中保存的条件")
    })
    @Output({})
    @Description(desc = "导出工单中心数据")
    @Override
    public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String uuid = jsonObj.getString("uuid");
        WorkcenterVo workcenterVo = JSONObject.toJavaObject(jsonObj, WorkcenterVo.class);
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        /* 获取表头 */
        List<WorkcenterTheadVo> theadList = newWorkcenterService.getWorkcenterTheadList(workcenterVo, columnComponentMap);
        if (CollectionUtils.isNotEmpty(theadList)) {
            /* 如果勾选了当前步骤，却没有勾选当前步骤名与当前步骤处理人，自动加上 */
            if (theadList.stream().anyMatch(o -> o.getName().equals(new ProcessTaskCurrentStepColumn().getName()) && o.getIsShow() == 1)) {
                IProcessTaskColumn stepNameColumn = new ProcessTaskCurrentStepNameColumn();
                IProcessTaskColumn stepWorkerColumn = new ProcessTaskCurrentStepWorkerColumn();
                if (theadList.stream().noneMatch(o -> o.getName().equals(stepNameColumn.getName()) && o.getIsShow() == 1)) {
                    theadList.add(new WorkcenterTheadVo(stepNameColumn));
                }
                if (theadList.stream().noneMatch(o -> o.getName().equals(stepWorkerColumn.getName()) && o.getIsShow() == 1)) {
                    theadList.add(new WorkcenterTheadVo(stepWorkerColumn));
                }
            }
            theadList = theadList.stream().filter(o -> o.getDisabled() == 0 && o.getIsExport() == 1 && o.getIsShow() == 1)
                    .sorted(Comparator.comparing(WorkcenterTheadVo::getSort)).collect(Collectors.toList());
            workcenterVo.setTheadVoList(theadList);
        }
        // 以服务为单位创建不同的sheet；
        // 不同的服务有着不同的表单，故每个sheet的表头也不同；
        // 循环每一批工单，判断是否存在该服务的sheet，不存在则创建，存在则追加数据
        List<String> publicHeadList = theadList.stream().map(WorkcenterTheadVo::getDisplayName).collect(Collectors.toList());
        Workbook workbook = new SXSSFWorkbook();
        Map<String, Sheet> sheetMap = new HashMap<>(); // channelUuid与sheet的map
        Map<String, List<FormAttributeVo>> channelFormAttributeListMap = new HashMap<>(); // channelUuid与其表单属性VO的map
        Map<String, Integer> sheetLastRowNumMap = new HashMap<>(); // 由于可能存在的单元格合并，sheet.getLastRowNum()不能获取实际的最后一行行号，需要手动记录每个sheet的最后一行行号

        SqlBuilder sb = new SqlBuilder(workcenterVo, ProcessSqlTypeEnum.TOTAL_COUNT);
        int total = processTaskMapper.getProcessTaskCountBySql(sb.build());
        if (total > 0) {
            workcenterVo.setRowNum(total);
            workcenterVo.setPageSize(100);
            Integer pageCount = workcenterVo.getPageCount();
            Map<Long, String> processTaskErrorMap = new HashMap<>(); // 记录工单抛出的异常
            for (int i = 1; i <= pageCount; i++) {
                workcenterVo.setCurrentPage(i);
                sb = new SqlBuilder(workcenterVo, ProcessSqlTypeEnum.DISTINCT_ID);
                List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskBySql(sb.build());
                workcenterVo.setProcessTaskIdList(processTaskList.stream().map(ProcessTaskVo::getId).collect(Collectors.toList()));
                sb = new SqlBuilder(workcenterVo, ProcessSqlTypeEnum.FIELD);
                List<ProcessTaskVo> processTaskVoList = processTaskMapper.getProcessTaskBySql(sb.build());
                for (ProcessTaskVo taskVo : processTaskVoList) {
                    try {
                        if (Objects.equals(taskVo.getStatus(), ProcessTaskStatus.RUNNING.getValue())) {
                            taskVo.setStepList(processTaskMapper.getProcessTaskCurrentStepByProcessTaskId(taskVo.getId()));
                        }
                        String channelUuid = taskVo.getChannelVo().getUuid();
                        Sheet sheet = sheetMap.get(channelUuid);
                        if (sheet == null) {
                            // 创建sheet并填充表头
                            sheet = workbook.createSheet(taskVo.getChannelVo().getName());
                            List<String> formLabelList = null;
                            Map<String, Integer> formLabelCellRangeMap = null; // 记录每个表单属性需要占据的单元格长度
                            String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
                            if (StringUtils.isNotBlank(processUuid)) {
                                ProcessFormVo processForm = processMapper.getProcessFormByProcessUuid(processUuid);
                                if (processForm != null) {
                                    FormVersionVo formVersionVo = formMapper.getActionFormVersionByFormUuid(processForm.getFormUuid());
                                    if (formVersionVo != null) {
                                        List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
                                        if (CollectionUtils.isNotEmpty(formAttributeList)) {
                                            List<FormAttributeVo> formAttributeVoList = new ArrayList<>();
                                            channelFormAttributeListMap.put(channelUuid, formAttributeVoList);
                                            // 记录每一种channel的表单组件label列表到channelFormLabelListMap
                                            // 不同的表单组件需要占据的单元格长度不同，使用组件handler的getExcelHeadLength方法计算出组件需要占据的长度，并记录到formLabelCellRangeMap
                                            formLabelList = new ArrayList<>();
                                            formLabelCellRangeMap = new HashMap<>();
                                            for (FormAttributeVo formAttributeVo : formAttributeList) {
                                                IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(formAttributeVo.getHandler());
                                                if (handler == null || handler instanceof DivideHandler) {
                                                    continue;
                                                }
                                                formLabelCellRangeMap.put(formAttributeVo.getLabel(), handler.getExcelHeadLength(formAttributeVo.getConfigObj()));
                                                formLabelList.add(formAttributeVo.getLabel());
                                                formAttributeVoList.add(formAttributeVo);
                                            }
                                        }
                                    }
                                }
                            }
                            List<String> formHeadCellValueList = new ArrayList<>();
                            List<CellRangeAddress> cellRangeAddressList = new ArrayList<>();
                            /**
                             * 跨列的表单组件，需要在表头占据多少cell，就要创建多少个cell，并且合并这些cell
                             * 根据formLabelCellRangeMap计算出实际需要创建多少个表单属性的cell，每个cell的值记录到formHeadCellValueList
                             * 同时计算需要合并的单元格并记录到cellRangeAddressList
                             */
                            if (CollectionUtils.isNotEmpty(formLabelList)) {
                                int start = publicHeadList.size();
                                int end = publicHeadList.size();
                                for (int k = 0; k < formLabelList.size(); k++) {
                                    Integer cellRange = formLabelCellRangeMap.get(formLabelList.get(k));
                                    if (cellRange != null && cellRange > 1) {
                                        for (int m = 0; m < cellRange; m++) {
                                            formHeadCellValueList.add(formLabelList.get(k));
                                        }
                                        end = start + cellRange - 1;
                                        cellRangeAddressList.add(new CellRangeAddress(0, 0, start, end));
                                        start = end + 1;
                                    } else {
                                        formHeadCellValueList.add(formLabelList.get(k));
                                        start++;
                                        end++;
                                    }
                                }
                            }
                            // 先填充工单属性的表头
                            Row headerRow = sheet.createRow(0);
                            for (int j = 0; j < publicHeadList.size(); j++) {
                                Cell cell = headerRow.createCell(j);
                                cell.setCellValue(publicHeadList.get(j));
                            }
                            // 在工单属性后填充表单属性的表头
                            if (CollectionUtils.isNotEmpty(formHeadCellValueList)) {
                                for (int j = 0; j < formHeadCellValueList.size(); j++) {
                                    Cell cell = headerRow.createCell(publicHeadList.size() + j);
                                    cell.setCellValue(formHeadCellValueList.get(j));
                                }
                            }
                            if (cellRangeAddressList.size() > 0) {
                                for (CellRangeAddress cellAddresses : cellRangeAddressList) {
                                    sheet.addMergedRegion(cellAddresses);
                                }
                            }
                            sheetMap.put(channelUuid, sheet);
                            sheetLastRowNumMap.put(channelUuid, 0);
                        }
                        Integer lastRowNum = sheetLastRowNumMap.get(channelUuid);
                        int beginRowNum = lastRowNum + 1;
                        Row row = sheet.createRow(beginRowNum);
                        int maxRowCount = 1; // 大于1说明需要合并行
                        List<FormAttributeVo> formAttributeList = channelFormAttributeListMap.get(channelUuid);
                        Map<String, ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataMap = null;
                        // 以channel当前关联的流程为准，如果流程没有关联表单或关联的表单没有属性，则无论当前工单是否有表单，都不查询
                        if (CollectionUtils.isNotEmpty(formAttributeList)) {
                            // 遍历当前工单的所有表单属性，找出数据行数最多的属性，该属性的数据行数作为当前工单需要合并的行数
                            List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(taskVo.getId());
                            if (processTaskFormAttributeDataList.size() > 0) {
                                processTaskFormAttributeDataMap = processTaskFormAttributeDataList.stream().collect(Collectors.toMap(AttributeDataVo::getAttributeLabel, e -> e));
                                for (FormAttributeVo formAttributeVo : formAttributeList) {
                                    ProcessTaskFormAttributeDataVo formAttributeDataVo = processTaskFormAttributeDataMap.get(formAttributeVo.getLabel());
                                    if (formAttributeDataVo == null || formAttributeDataVo.getData() == null) {
                                        continue;
                                    }
                                    IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(formAttributeVo.getHandler());
                                    if (handler == null || handler instanceof DivideHandler) {
                                        continue;
                                    }
                                    int excelRowCount = handler.getExcelRowCount(formAttributeDataVo, JSONObject.parseObject(formAttributeVo.getConfig()));
                                    if (excelRowCount > maxRowCount) {
                                        maxRowCount = excelRowCount;
                                    }
                                }
                            }
                        }
                        // 获取每个工单属性的值
                        Map<String, Object> map = new LinkedHashMap<>();
                        for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
                            IProcessTaskColumn column = entry.getValue();
                            if (column.getIsShow() && column.getIsExport() && !column.getDisabled()) {
                                map.put(column.getDisplayName(), column.getSimpleValue(taskVo));
                            }
                        }
                        // 填充工单属性&记录每个工单属性的单元格合并
                        List<CellRangeAddress> cellRangeAddressList = new ArrayList<>();
                        for (int j = 0; j < publicHeadList.size(); j++) {
                            Cell cell = row.createCell(j);
                            Object value = map.get(publicHeadList.get(j));
                            cell.setCellValue(value != null ? value.toString() : "");
                            if (maxRowCount > 1) {
                                cellRangeAddressList.add(new CellRangeAddress(beginRowNum, beginRowNum + maxRowCount - 1, j, j));
                            }
                        }
                        // 填充表单属性
                        if (MapUtils.isNotEmpty(processTaskFormAttributeDataMap)) {
                            Row headRow = sheet.getRow(beginRowNum);
                            int formCellIndex = publicHeadList.size() - 1; // 填充表单属性开始的列数
                            // POI不允许重复创建Row，遍历到表格类属性时，需要创建多个Row，把这些Row记录下来，待到下一个表格类属性时，重复使用这些Row
                            Map<Integer, Row> rowMap = new HashMap<>();
                            for (FormAttributeVo formAttributeVo : formAttributeList) {
                                IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(formAttributeVo.getHandler());
                                if (handler == null || handler instanceof DivideHandler) {
                                    continue;
                                }
                                Object detailedData = "";
                                ProcessTaskFormAttributeDataVo formAttributeDataVo = processTaskFormAttributeDataMap.get(formAttributeVo.getLabel());
                                // 因为表头中已经有了当前组件的label，所以如果当前工单表单中没有当前组件或组件值为null，那么使用空串来占位，防止后续组件前移
                                if (formAttributeDataVo != null && formAttributeDataVo.getData() != null) {
                                    detailedData = handler.dataTransformationForExcel(formAttributeDataVo, formAttributeVo.getConfigObj());
                                }
                                if (detailedData == null) {
                                    detailedData = "";
                                }
                                int excelHeadLength = handler.getExcelHeadLength(formAttributeVo.getConfigObj());
                                // excelHeadLength > 1表示该表单属性为表格类属性，需要生成嵌套表格
                                if (excelHeadLength > 1 && StringUtils.isNotBlank(detailedData.toString())) {
                                    JSONObject jsonObject = (JSONObject) detailedData;
                                    JSONArray _theadList = jsonObject.getJSONArray("theadList");
                                    JSONArray _tbodyList = jsonObject.getJSONArray("tbodyList");
                                    if (CollectionUtils.isNotEmpty(_theadList) && CollectionUtils.isNotEmpty(_tbodyList)) {
                                        Map<String, String> headMap = new LinkedHashMap<>();
                                        List<String> headKeyList = new ArrayList<>();
                                        // 填充表头
                                        for (int j = 0; j < _theadList.size(); j++) {
                                            JSONObject head = _theadList.getJSONObject(j);
                                            String key = head.getString("key");
                                            String title = head.getString("title");
                                            Cell cell = headRow.createCell(formCellIndex + j + 1);
                                            cell.setCellValue(title);
                                            headMap.put(key, title);
                                            headKeyList.add(key);
                                        }
                                        // 填充表格行
                                        for (int j = 0; j < _tbodyList.size(); j++) {
                                            // 先尝试从rowMap获取Row，获取不到说明之前没有创建过
                                            Row contentRow = rowMap.get(beginRowNum + j + 1);
                                            if (contentRow == null) {
                                                int formRowNum = beginRowNum + j + 1;
                                                contentRow = sheet.createRow(formRowNum);
                                                rowMap.put(formRowNum, contentRow);
                                            }
                                            JSONObject value = _tbodyList.getJSONObject(j);
                                            // 检查每一行数据的字段是否完整，完整是指keySet与表头字段数量一致，不一致则补全
                                            Set<String> keySet = value.keySet();
                                            for (String head : headKeyList) {
                                                if (!keySet.contains(head)) {
                                                    value.put(head, new JSONObject());
                                                }
                                            }
                                            Set<Map.Entry<String, Object>> entrySet = value.entrySet();
                                            int k = 0;
                                            for (Map.Entry<String, String> valueMap : headMap.entrySet()) {
                                                for (Map.Entry<String, Object> entry : entrySet) {
                                                    if (valueMap.getKey().equals(entry.getKey())) {
                                                        Cell cell = contentRow.createCell(formCellIndex + k + 1);
                                                        JSONObject entryValue = (JSONObject) entry.getValue();
                                                        String _value = "";
                                                        if (entryValue != null) {
                                                            Object text = entryValue.get("text");
                                                            if (text != null) {
                                                                if (text instanceof List) {
                                                                    _value = String.join("", ((List) text));
                                                                } else {
                                                                    _value = text.toString();
                                                                }
                                                            }
                                                        }
                                                        cell.setCellValue(_value);
                                                        k++;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    int beginColumnNum = formCellIndex + excelHeadLength;
                                    Cell cell = row.createCell(beginColumnNum);
                                    cell.setCellValue(detailedData.toString());
                                    // 对于只占一行的普通属性，如果存在占多行的表格类属性，那么普通属性需要占据同样的行数，并且合并这些行
                                    if (maxRowCount > 1) {
                                        cellRangeAddressList.add(new CellRangeAddress(beginRowNum, beginRowNum + maxRowCount - 1, beginColumnNum, beginColumnNum));
                                    }
                                }
                                formCellIndex += excelHeadLength; // 每次循环完毕一个表单属性，就把指针移到该属性之后
                            }
                        }
                        if (cellRangeAddressList.size() > 0) {
                            for (CellRangeAddress cellAddresses : cellRangeAddressList) {
                                sheet.addMergedRegion(cellAddresses);
                            }
                        }
                        sheetLastRowNumMap.put(channelUuid, beginRowNum + maxRowCount - 1);
                    } catch (Exception ex) {
                        processTaskErrorMap.put(taskVo.getId(), ExceptionUtils.getStackTrace(ex));
                    }
                }
                if (!processTaskErrorMap.isEmpty()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("导出以下工单时发生错误：\n");
                    for (Map.Entry<Long, String> entry : processTaskErrorMap.entrySet()) {
                        stringBuilder.append("工单ID：").append(entry.getKey()).append("；错误：").append(entry.getValue()).append("\n");
                    }
                    logger.error(stringBuilder.toString());
                    processTaskErrorMap.clear();
                }
            }
        }
        String fileNameEncode = FileUtil.getEncodedFileName("工单数据" + ".xlsx");
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");
        try (OutputStream os = response.getOutputStream()) {
            workbook.write(os);
            ((SXSSFWorkbook) workbook).dispose();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

}
