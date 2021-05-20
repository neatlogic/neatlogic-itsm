package codedriver.module.process.api.workcenter;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.ExcelUtil;
import codedriver.module.process.service.NewWorkcenterService;
import codedriver.module.process.workcenter.column.handler.ProcessTaskCurrentStepColumn;
import codedriver.module.process.workcenter.column.handler.ProcessTaskCurrentStepNameColumn;
import codedriver.module.process.workcenter.column.handler.ProcessTaskCurrentStepWorkerColumn;
import codedriver.module.process.workcenter.core.SqlBuilder;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    @Resource
    WorkcenterMapper workcenterMapper;

    @Resource
    NewWorkcenterService newWorkcenterService;

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
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "分类uuid,据此从数据库获取对应分类的条件"),
            @Param(name = "isMeWillDo", type = ApiParamType.INTEGER, desc = "是否带我处理的，1：是；0：否"),
            @Param(name = "conditionGroupList", type = ApiParamType.JSONARRAY, desc = "条件组条件", isRequired = false),
            @Param(name = "conditionGroupRelList", type = ApiParamType.JSONARRAY, desc = "条件组连接类型", isRequired = false)
    })
    @Output({})
    @Description(desc = "导出工单中心数据")
    @Override
    public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String title = "";
        String uuid = jsonObj.getString("uuid");
        if (StringUtils.isNotBlank(uuid)) {
            List<WorkcenterVo> workcenterList = workcenterMapper.getWorkcenterByNameAndUuid(null, uuid);
            if (CollectionUtils.isNotEmpty(workcenterList)) {
                title = workcenterList.get(0).getName();
                jsonObj = JSONObject.parseObject(workcenterList.get(0).getConditionConfig());
                jsonObj.put("uuid", uuid);
            }
        }

        WorkcenterVo workcenterVo = new WorkcenterVo(jsonObj);
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        /* 获取表头 */
        List<WorkcenterTheadVo> theadList = newWorkcenterService.getWorkcenterTheadList(workcenterVo, columnComponentMap, null);
        if (CollectionUtils.isNotEmpty(theadList)) {
            /** 如果勾选了当前步骤，却没有勾选当前步骤名与当前步骤处理人，自动加上 */
            if (theadList.stream()
                    .anyMatch(o -> o.getName()
                            .equals(new ProcessTaskCurrentStepColumn().getName()) && o.getIsShow() == 1)) {
                IProcessTaskColumn stepNameColumn = new ProcessTaskCurrentStepNameColumn();
                IProcessTaskColumn stepWorkerColumn = new ProcessTaskCurrentStepWorkerColumn();
                if (theadList.stream()
                        .noneMatch(o -> o.getName().equals(stepNameColumn.getName()) && o.getIsShow() == 1)) {
                    theadList.add(new WorkcenterTheadVo(stepNameColumn));
                }
                if (theadList.stream()
                        .noneMatch(o -> o.getName().equals(stepWorkerColumn.getName()) && o.getIsShow() == 1)) {
                    theadList.add(new WorkcenterTheadVo(stepWorkerColumn));
                }
            }
            theadList = theadList.stream()
                    .filter(o -> o.getDisabled() == 0 && o.getIsExport() == 1 && o.getIsShow() == 1)
                    .sorted(Comparator.comparing(WorkcenterTheadVo::getSort)).collect(Collectors.toList());
            workcenterVo.setTheadVoList(theadList);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        Set<String> headList = new LinkedHashSet<>();
        Set<String> columnList = new LinkedHashSet<>();


        SqlBuilder sb = new SqlBuilder(workcenterVo, FieldTypeEnum.TOTAL_COUNT);
        int total = processTaskMapper.getProcessTaskCountBySql(sb.build());
        if (total > 0) {
            int pageCount = PageUtil.getPageCount(total, workcenterVo.getPageSize());
            workcenterVo.setRowNum(total);
            for (int i = 1; i <= pageCount; i++) {
                workcenterVo.setCurrentPage(i);
                sb = new SqlBuilder(workcenterVo, FieldTypeEnum.DISTINCT_ID);
                List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskBySql(sb.build());
                workcenterVo.setProcessTaskIdList(processTaskList.stream().map(ProcessTaskVo::getId).collect(Collectors.toList()));
                sb = new SqlBuilder(workcenterVo, FieldTypeEnum.FIELD);
                List<ProcessTaskVo> processTaskVoList = processTaskMapper.getProcessTaskBySql(sb.build());
                for (ProcessTaskVo taskVo : processTaskVoList) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    //重新渲染工单字段
                    for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
                        IProcessTaskColumn column = entry.getValue();
                        if(column.getIsShow() && column.getIsExport() && !column.getDisabled()) {
                            map.put(column.getDisplayName(), column.getSimpleValue(taskVo));
                            headList.add(column.getDisplayName());
                            columnList.add(column.getDisplayName());
                        }
                    }
                    list.add(map);
                }
            }
        }
		/*QueryResultSet resultSet = workcenterService.searchTaskIterate(workcenterVo);
		if(CollectionUtils.isNotEmpty(theadList) && resultSet.hasMoreResults()){
			while(resultSet.hasMoreResults()){
				QueryResult result = resultSet.fetchResult();
				if(!result.getData().isEmpty()){
					for(MultiAttrsObject el : result.getData()){
						Map<String,Object> map = new LinkedHashMap<>();
						for(WorkcenterTheadVo vo : theadList){
							IProcessTaskColumn column = columnComponentMap.get(vo.getName());
							Object value = column.getSimpleValue(column.getValue(el));
							map.put(column.getDisplayName(),value);
							headList.add(column.getDisplayName());
							columnList.add(column.getDisplayName());
						}
						list.add(map);
					}
				}
			}
		}*/

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        ExcelUtil.exportData(workbook, new ArrayList<>(headList), new ArrayList<>(columnList), list, 35, 0);
        String fileNameEncode = (StringUtils.isNotBlank(title) ? title : "工单数据") + ".xlsx";
        boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
        if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
            fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
        } else {
            fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
        }
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");
        try (OutputStream os = response.getOutputStream()) {
            workbook.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
