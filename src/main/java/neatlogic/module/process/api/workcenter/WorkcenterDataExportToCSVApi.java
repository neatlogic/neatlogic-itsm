/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.api.workcenter;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnFactory;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.WorkcenterTheadVo;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.process.workcenter.table.constvalue.ProcessSqlTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.$;
import neatlogic.framework.util.FileUtil;
import neatlogic.module.process.service.NewWorkcenterService;
import neatlogic.module.process.sql.decorator.SqlBuilder;
import neatlogic.module.process.workcenter.column.handler.ProcessTaskCurrentStepColumn;
import neatlogic.module.process.workcenter.column.handler.ProcessTaskCurrentStepNameColumn;
import neatlogic.module.process.workcenter.column.handler.ProcessTaskCurrentStepWorkerColumn;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
@Deprecated
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkcenterDataExportToCSVApi extends PrivateBinaryStreamApiComponentBase {
    @Resource
    WorkcenterMapper workcenterMapper;

    @Resource
    NewWorkcenterService newWorkcenterService;

    @Resource
    ProcessTaskMapper processTaskMapper;

    @Override
    public String getToken() {
        return "workcenter/export/tocsv";
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
        try (OutputStream os = response.getOutputStream()) {
            String fileNameEncode = FileUtil.getEncodedFileName("工单数据" + ".csv");
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
                        if (Objects.equals(taskVo.getStatus(), ProcessTaskStatus.RUNNING.getValue())) {
                            taskVo.setStepList(processTaskMapper.getProcessTaskCurrentStepByProcessTaskId(taskVo.getId()));
                        }
                        Map<String, Object> map = new LinkedHashMap<>();
                        //重新渲染工单字段
                        for (Map.Entry<String, IProcessTaskColumn> entry : columnComponentMap.entrySet()) {
                            IProcessTaskColumn column = entry.getValue();
                            if (column.getIsShow() && column.getIsExport() && !column.getDisabled()) {
                                map.put($.t(column.getDisplayName()), column.getSimpleValue(taskVo));
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
