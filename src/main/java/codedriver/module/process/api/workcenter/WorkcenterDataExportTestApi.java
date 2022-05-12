/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.workcenter;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.form.attribute.core.FormAttributeHandlerFactory;
import codedriver.framework.form.attribute.core.IFormAttributeHandler;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.AttributeDataVo;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
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
public class WorkcenterDataExportTestApi extends PrivateBinaryStreamApiComponentBase {
    @Resource
    WorkcenterMapper workcenterMapper;

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

    @Resource
    SelectContentByHashMapper selectContentByHashMapper;

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
        Map<String, Sheet> sheetMap = new HashMap<>();
        Map<String, List<String>> channelFormLabelListMap = new HashMap<>();
        Map<String, List<FormAttributeVo>> channelFormAttributeListMap = new HashMap<>();
        Map<String, Integer> sheetLastRowNumMap = new HashMap<>();// 由于可能存在的单元格合并，sheet.getLastRowNum()不能获取实际的最后一行行号，需要手动记录

        SqlBuilder sb = new SqlBuilder(workcenterVo, ProcessSqlTypeEnum.TOTAL_COUNT);
        int total = processTaskMapper.getProcessTaskCountBySql(sb.build());
        if (total > 0) {
            workcenterVo.setRowNum(total);
            workcenterVo.setPageSize(100);
            Integer pageCount = workcenterVo.getPageCount();
            for (int i = 1; i <= pageCount; i++) {
                workcenterVo.setCurrentPage(i);
                sb = new SqlBuilder(workcenterVo, ProcessSqlTypeEnum.DISTINCT_ID);
                List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskBySql(sb.build());
                workcenterVo.setProcessTaskIdList(processTaskList.stream().map(ProcessTaskVo::getId).collect(Collectors.toList()));
                sb = new SqlBuilder(workcenterVo, ProcessSqlTypeEnum.FIELD);
                List<ProcessTaskVo> processTaskVoList = processTaskMapper.getProcessTaskBySql(sb.build());
                for (ProcessTaskVo taskVo : processTaskVoList) {
                    String channelUuid = taskVo.getChannelVo().getUuid();
                    Sheet sheet = sheetMap.get(channelUuid);
                    if (sheet == null) {
                        sheet = workbook.createSheet(taskVo.getChannelVo().getName());
                        List<String> headList = new ArrayList<>(publicHeadList);
                        List<String> formLabelList = null;
                        Map<String, Integer> formLabelCellRangeMap = null;
                        String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
                        if (StringUtils.isNotBlank(processUuid)) {
                            ProcessFormVo processForm = processMapper.getProcessFormByProcessUuid(processUuid);
                            if (processForm != null) {
                                FormVersionVo formVersionVo = formMapper.getActionFormVersionByFormUuid(processForm.getFormUuid());
                                if (formVersionVo != null) {
                                    List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
                                    if (CollectionUtils.isNotEmpty(formAttributeList)) {
                                        channelFormAttributeListMap.put(channelUuid, formAttributeList);
                                        /**
                                         * todo 批量合并上报流程
                                         * 如果存在表格类字段，则需要根据表头字段数量计算出sheet表头需要合并多少列
                                         * 表头数量获取途径：
                                         * 账号选择组件-AccountsHandler：theadList
                                         * 表格选择组件-DynamicListHandler：dataConfig(扩展属性从attributeList拿)
                                         * 表格输入组件-StaticListHandler：attributeList
                                         * 配置项修改组件-CiEntitySyncHandler：dataConfig(注意isShow)
                                         */
                                        formLabelList = new ArrayList<>();
                                        formLabelCellRangeMap = new HashMap<>();
                                        for (FormAttributeVo formAttributeVo : formAttributeList) {
                                            IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(formAttributeVo.getHandler());
                                            if (handler == null || handler instanceof DivideHandler) {
                                                continue;
                                            }
                                            formLabelCellRangeMap.put(formAttributeVo.getLabel(), handler.getExcelHeadLength(formAttributeVo.getConfigObj()));
                                            formLabelList.add(formAttributeVo.getLabel());
                                        }
                                        channelFormLabelListMap.put(channelUuid, formLabelList);
                                    }
                                }
                            }
                        }
                        List<String> formCellValueList = new ArrayList<>();
                        List<CellRangeAddress> cellRangeAddressList = new ArrayList<>();
                        // 记录表单字段值&计算表单字段单元格合并
                        if (CollectionUtils.isNotEmpty(formLabelList)) {
                            int start = headList.size();
                            int end = headList.size();
                            for (int k = 0; k < formLabelList.size(); k++) {
                                Integer cellRange = formLabelCellRangeMap.get(formLabelList.get(k));
                                if (cellRange != null && cellRange > 1) {
                                    for (int m = 0; m < cellRange; m++) {
                                        formCellValueList.add(formLabelList.get(k));
                                    }
                                    end = start + cellRange - 1;
                                    cellRangeAddressList.add(new CellRangeAddress(0, 0, start, end));
                                    start = end + 1;
                                } else {
                                    formCellValueList.add(formLabelList.get(k));
                                    start++;
                                    end++;
                                }
                            }
                        }
                        // 填充工单字段表头
                        Row headerRow = sheet.createRow(0);
                        for (int j = 0; j < headList.size(); j++) {
                            Cell cell = headerRow.createCell(j);
                            cell.setCellValue(headList.get(j));
                        }
                        // 填充表单字段表头
                        if (CollectionUtils.isNotEmpty(formCellValueList)) {
                            for (int j = 0; j < formCellValueList.size(); j++) {
                                Cell cell = headerRow.createCell(headList.size() + j);
                                cell.setCellValue(formCellValueList.get(j));
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
                    if (CollectionUtils.isNotEmpty(formAttributeList)) {
                        // 先找出当前工单是否有表格类表单字段，有的话找出数据行数最多的那个字段，该字段的数据行数作为当前行需要合并的行数
                        String formContent = selectContentByHashMapper.getProcessTaskFromContentByProcessTaskId(taskVo.getId());
                        if (StringUtils.isNotBlank(formContent)) {
                            taskVo.setFormConfig(JSONObject.parseObject(formContent));
                            List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(taskVo.getId());
                            if (processTaskFormAttributeDataList.size() > 0) {
                                processTaskFormAttributeDataMap = processTaskFormAttributeDataList.stream().collect(Collectors.toMap(AttributeDataVo::getAttributeLabel, e -> e));
                                for (FormAttributeVo formAttributeVo : formAttributeList) {
                                    ProcessTaskFormAttributeDataVo formAttributeDataVo = processTaskFormAttributeDataMap.get(formAttributeVo.getLabel());
                                    if (formAttributeDataVo == null || formAttributeDataVo.getData() == null) {
                                        continue;
                                    }
                                    IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(formAttributeVo.getHandler());
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
                    }
                    // 填充工单字段并根据表单中的表格字段最大行数合并单元格
                    Map<String, Object> map = new LinkedHashMap<>();
                    for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
                        IProcessTaskColumn column = entry.getValue();
                        if (column.getIsShow() && column.getIsExport() && !column.getDisabled()) {
                            map.put(column.getDisplayName(), column.getSimpleValue(taskVo));
                        }
                    }
                    List<CellRangeAddress> cellRangeAddressList = new ArrayList<>();
                    for (int j = 0; j < publicHeadList.size(); j++) {
                        Cell cell = row.createCell(j);
                        Object value = map.get(publicHeadList.get(j));
                        cell.setCellValue(value != null ? value.toString() : "");
                        if (maxRowCount > 1) {
                            cellRangeAddressList.add(new CellRangeAddress(beginRowNum, beginRowNum + maxRowCount - 1, j, j));
                        }
                    }
                    // 填充表单字段
                    if (MapUtils.isNotEmpty(processTaskFormAttributeDataMap)) {
                        Row headRow = sheet.getRow(beginRowNum);
                        int formCellIndex = publicHeadList.size() - 1; // 表单cell开始的列数
                        Map<Integer, Row> rowMap = new HashMap<>();
                        for (FormAttributeVo formAttributeVo : formAttributeList) {
                            ProcessTaskFormAttributeDataVo formAttributeDataVo = processTaskFormAttributeDataMap.get(formAttributeVo.getLabel());
                            if (formAttributeDataVo == null || formAttributeDataVo.getData() == null) {
                                continue;
                            }
                            IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(formAttributeVo.getHandler());
                            if (handler == null || handler instanceof DivideHandler) {
                                continue;
                            }
                            Object detailedData = handler.dataTransformationForExcel(formAttributeDataVo, formAttributeVo.getConfigObj());
                            int excelHeadLength = handler.getExcelHeadLength(formAttributeVo.getConfigObj());
                            if (detailedData != null) {
                                // excelHeadLength > 1表示该表单字段为表格类字段，需要嵌套表格
                                if (excelHeadLength > 1) {
                                    JSONObject jsonObject = (JSONObject) detailedData;
                                    JSONArray _theadList = jsonObject.getJSONArray("theadList");
                                    JSONArray _tbodyList = jsonObject.getJSONArray("tbodyList");
                                    if (CollectionUtils.isNotEmpty(_theadList) && CollectionUtils.isNotEmpty(_tbodyList)) {
                                        Map<String, String> headMap = new LinkedHashMap<>();
                                        List<String> headKeyList = new ArrayList<>();
                                        // 填充表头
                                        for (int j = 0; j < _theadList.size(); j++) {
                                            JSONObject head = _theadList.getJSONObject(j);
                                            Cell cell = headRow.createCell(formCellIndex + j + 1);
                                            cell.setCellValue(head.getString("title"));
                                            headMap.put(head.getString("key"), head.getString("title"));
                                            headKeyList.add(head.getString("key"));
                                        }
                                        // 填充表格行数据
                                        for (int j = 0; j < _tbodyList.size(); j++) {
                                            // POI不允许重复创建行，创建过的行需要记录下来，循环到下一个表单属性时重复使用
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
                                                                _value = text.toString();
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
                                    if (maxRowCount > 1) {
                                        cellRangeAddressList.add(new CellRangeAddress(beginRowNum, beginRowNum + maxRowCount - 1, beginColumnNum, beginColumnNum));
                                    }
                                }
                            }
                            formCellIndex += excelHeadLength;
                        }
                    }
                    if (cellRangeAddressList.size() > 0) {
                        for (CellRangeAddress cellAddresses : cellRangeAddressList) {
                            sheet.addMergedRegion(cellAddresses);
                        }
                    }
                    sheetLastRowNumMap.put(channelUuid, beginRowNum + maxRowCount - 1);
                }
            }
        }
        String fileNameEncode = FileUtil.getEncodedFileName(request.getHeader("User-Agent"), "工单数据" + ".xlsx");
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");
        try (OutputStream os = response.getOutputStream()) {
            workbook.write(os);
            ((SXSSFWorkbook) workbook).dispose();
        } catch (IOException e) {
            throw e;
        }

        return null;
    }

}
