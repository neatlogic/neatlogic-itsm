package codedriver.module.process.api.workcenter;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.FileUtil;
import codedriver.module.process.service.NewWorkcenterService;
import codedriver.module.process.workcenter.column.handler.ProcessTaskCurrentStepColumn;
import codedriver.module.process.workcenter.column.handler.ProcessTaskCurrentStepNameColumn;
import codedriver.module.process.workcenter.column.handler.ProcessTaskCurrentStepWorkerColumn;
import codedriver.module.process.sql.decorator.SqlBuilder;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            @Param(name = "conditionGroupList", type = ApiParamType.JSONARRAY, desc = "条件组条件"),
            @Param(name = "conditionGroupRelList", type = ApiParamType.JSONARRAY, desc = "条件组连接类型")
    })
    @Output({})
    @Description(desc = "导出工单中心数据")
    @Override
    public Object myDoService(JSONObject jsonObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String title = "";
        String uuid = jsonObj.getString("uuid");
        if (StringUtils.isNotBlank(uuid)) {
            WorkcenterVo workcenter = workcenterMapper.getWorkcenterByUuid(uuid);
            if (workcenter != null) {
                title = workcenter.getName();
                jsonObj = JSONObject.parseObject(workcenter.getConditionConfig());
                jsonObj.put("uuid", uuid);
            }
        }

        WorkcenterVo workcenterVo = new WorkcenterVo(jsonObj);
        Map<String, IProcessTaskColumn> columnComponentMap = ProcessTaskColumnFactory.columnComponentMap;
        /* 获取表头 */
        List<WorkcenterTheadVo> theadList = newWorkcenterService.getWorkcenterTheadList(workcenterVo, columnComponentMap, null);
        if (CollectionUtils.isNotEmpty(theadList)) {
            /** 如果勾选了当前步骤，却没有勾选当前步骤名与当前步骤处理人，自动加上 */
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
        try (OutputStream os = response.getOutputStream()) {
            String fileNameEncode = FileUtil.getEncodedFileName(request.getHeader("User-Agent"), (StringUtils.isNotBlank(title) ? title : "工单数据") + ".csv");
            response.setContentType("application/text;charset=GBK");
            response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");

            List<String> headList = theadList.stream().map(WorkcenterTheadVo::getDisplayName).collect(Collectors.toList());
            StringBuilder header = new StringBuilder();
            theadList.forEach(o -> header.append(o.getDisplayName()).append(","));
            header.append("\n");
            os.write(header.toString().getBytes("GBK"));
            os.flush();

            SqlBuilder sb = new SqlBuilder(workcenterVo, ProcessSqlTypeEnum.TOTAL_COUNT);
            int total = processTaskMapper.getProcessTaskCountBySql(sb.build());
            if (total > 0) {
                workcenterVo.setRowNum(total);
                workcenterVo.setPageSize(100);
                Integer pageCount = workcenterVo.getPageCount();
                for (int i = 1; i <= pageCount; i++) {
                    StringBuilder content = new StringBuilder();
                    workcenterVo.setCurrentPage(i);
                    sb = new SqlBuilder(workcenterVo, ProcessSqlTypeEnum.DISTINCT_ID);
                    List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskBySql(sb.build());
                    workcenterVo.setProcessTaskIdList(processTaskList.stream().map(ProcessTaskVo::getId).collect(Collectors.toList()));
                    sb = new SqlBuilder(workcenterVo, ProcessSqlTypeEnum.FIELD);
                    List<ProcessTaskVo> processTaskVoList = processTaskMapper.getProcessTaskBySql(sb.build());
                    for (ProcessTaskVo taskVo : processTaskVoList) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        //重新渲染工单字段
                        for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
                            IProcessTaskColumn column = entry.getValue();
                            if (column.getIsShow() && column.getIsExport() && !column.getDisabled()) {
                                map.put(column.getDisplayName(), column.getSimpleValue(taskVo));
                            }
                        }
                        for (String head : headList) {
                            content.append(map.get(head) != null ? map.get(head).toString().replaceAll("\n", "").replaceAll(",", "，") : StringUtils.EMPTY).append(",");
                        }
                        content.append("\n");
                    }
                    os.write(content.toString().getBytes("GBK"));
                    os.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
