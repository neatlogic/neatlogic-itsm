package codedriver.module.process.dashboard.handler;

import codedriver.framework.common.constvalue.dashboard.ChartType;
import codedriver.framework.common.constvalue.dashboard.DashboardShowConfig;
import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.dashboard.constvalue.DashboardGroupField;
import codedriver.framework.dashboard.constvalue.IDashboardGroupField;
import codedriver.framework.dashboard.core.DashboardChartBase;
import codedriver.framework.dashboard.core.DashboardChartFactory;
import codedriver.framework.dashboard.core.DashboardHandlerBase;
import codedriver.framework.dashboard.dto.DashboardDataVo;
import codedriver.framework.dashboard.dto.DashboardShowConfigVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import codedriver.module.process.workcenter.core.SqlBuilder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ProcessTaskDashboardHandler extends DashboardHandlerBase {

    @Resource
    WorkcenterMapper workcenterMapper;

    @Resource
    ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "processtask";
    }

    @Override
    protected JSONObject myGetData(DashboardWidgetVo widgetVo) {
        DashboardChartBase chart = DashboardChartFactory.getChart(widgetVo.getChartType());
        JSONObject jsonObj = new JSONObject();
        if (chart != null) {
            JSONObject conditionConfig = JSONObject.parseObject(widgetVo.getConditionConfig());
            jsonObj.put("conditionConfig", conditionConfig);
            JSONObject configChart = widgetVo.getChartConfigObj();
            String groupField = configChart.getString(DashboardShowConfig.GROUPFIELD.getValue());
            String subGroupField = configChart.getString(DashboardShowConfig.SUBGROUPFIELD.getValue());
            if (configChart.containsKey(DashboardShowConfig.GROUPFIELD.getValue())) {
                configChart.put("groupfieldtext", ProcessWorkcenterField.getText(groupField));
            }
            if (configChart.containsKey(DashboardShowConfig.SUBGROUPFIELD.getValue())) {
                configChart.put("subgroupfieldtext", ProcessWorkcenterField.getText(subGroupField));
            }
            DashboardDataVo dashboardDataVo = new DashboardDataVo();
            dashboardDataVo.setChartConfig(configChart);
            /* start: 从mysql 获取源数据 */
            WorkcenterVo workcenterVo = new WorkcenterVo(jsonObj);
            //1、查出group权重，用于排序截取最大组数量
            workcenterVo.getDashboardConfigVo().setGroup(groupField);
            if (!ChartType.NUMBERCHART.getValue().equals(widgetVo.getChartType()) && !ChartType.TABLECHART.getValue().equals(widgetVo.getChartType()) && configChart.containsKey(DashboardShowConfig.MAXGROUP.getValue())) {
                //仅展示分组数
                workcenterVo.setCurrentPage(1);
                workcenterVo.setPageSize(configChart.getInteger(DashboardShowConfig.MAXGROUP.getValue()));
            }
            //设置chartConfig 以备后续特殊情况，如：数值图需要二次过滤选项
            workcenterVo.getDashboardConfigVo().setChartConfig(configChart);
            SqlBuilder sb = new SqlBuilder(workcenterVo, FieldTypeEnum.GROUP_COUNT);
            List<Map<String, Object>> groupMapList = processTaskMapper.getWorkcenterProcessTaskMapBySql(sb.build());
            IProcessTaskColumn groupColumn = ProcessTaskColumnFactory.columnComponentMap.get(groupField);

            IProcessTaskColumn subGroupColumn = null;
            //2、如果存在subGroup,则根据步骤1查出的权重，排序截取最大组数量，查出二维数据
            if (StringUtils.isNotBlank(subGroupField)) {
                subGroupColumn = ProcessTaskColumnFactory.columnComponentMap.get(subGroupField);
                if (subGroupColumn != null) {
                    workcenterVo.getDashboardConfigVo().setSubGroup(subGroupField);
                    //先排序分页获取前分组数的group
                    groupColumn.getExchangeToDashboardGroupDataMap(groupMapList, workcenterVo);
                    //根据分组groupDataList、子分组 再次搜索
                    sb = new SqlBuilder(workcenterVo, FieldTypeEnum.GROUP_COUNT);
                    groupMapList = processTaskMapper.getWorkcenterProcessTaskMapBySql(sb.build());
                    subGroupColumn.getDashboardDataVo(dashboardDataVo, workcenterVo, groupMapList);
                }
            }
            groupColumn.getDashboardDataVo(dashboardDataVo, workcenterVo, groupMapList);
            dashboardDataVo.getDataGroupVo().setDataCountMap(workcenterVo.getDashboardConfigVo().getGroupDataCountMap());
            dashboardDataVo.getDataGroupVo().setDataList(groupMapList);
            /* end: 从mysql 获取源数据 */
            /* start: 将mysql源数据 按不同dashboard插件处理返回结果数据*/
            JSONObject data = new JSONObject();
            data.put("dataList", chart.getData(dashboardDataVo).get("dataList"));
            data.put("columnList",chart.getData(dashboardDataVo).get("columnList"));
            data.put("theadList",chart.getData(dashboardDataVo).get("theadList"));
            /* end: 将mysql源数据 按不同dashboard插件处理返回结果数据*/
            data.put("configObj", configChart);
            return data;
        }
        return null;
    }

    private void getGroupFieldDataArray(JSONArray groupFieldDataArray, DashboardWidgetVo widgetVo, List<IDashboardGroupField> fieldList) {
        JSONObject groupFieldJson = new JSONObject();
        for (IDashboardGroupField groupField : fieldList) {
            if (ChartType.NUMBERCHART.getValue().equals(widgetVo.getChartType())) {
                groupFieldJson = ConditionHandlerFactory.getHandler(groupField.getValue()).getConfig();
                groupFieldJson.remove("isMultiple");
                groupFieldJson.put("handler", ConditionHandlerFactory.getHandler(groupField.getValue()).getHandler(FormConditionModel.CUSTOM));

            }
            groupFieldDataArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s',config:%s}", groupField.getValue(), groupField.getText(), groupFieldJson)));
        }
    }

    @Override
    public JSONObject myGetConfig(DashboardWidgetVo widgetVo) {
        DashboardChartBase chart = DashboardChartFactory.getChart(widgetVo.getChartType());
        JSONObject processTaskChartConfig = new JSONObject();
        JSONArray processTaskShowChartConfigArray = new JSONArray();
        JSONObject chartConfig = null;
        if (chart != null) {
            chartConfig = chart.getChartConfig();
            if (chartConfig.containsKey("showConfig")) {
                JSONObject showConfigJson = chartConfig.getJSONObject("showConfig");
                for(DashboardShowConfig gs : DashboardShowConfig.values()) {
                    DashboardShowConfigVo groupShowConfig = (DashboardShowConfigVo) showConfigJson.get(gs.getValue());
                    if(groupShowConfig == null){
                        continue;
                    }
                    if(Objects.equals(gs.getValue(),DashboardShowConfig.GROUPFIELD.getValue()) || Objects.equals(gs.getValue(),DashboardShowConfig.SUBGROUPFIELD.getValue())){
                        List<IDashboardGroupField> groupFields = Arrays.asList(
                                ProcessWorkcenterField.PRIORITY,
                                ProcessWorkcenterField.STATUS,
                                ProcessWorkcenterField.CHANNELTYPE,
                                ProcessWorkcenterField.CHANNEL,
                                ProcessWorkcenterField.STEP_USER,
                                ProcessWorkcenterField.OWNER
                        );
                        if(Arrays.stream(chart.getSupportChart()).noneMatch(o->Objects.equals(o,ChartType.NUMBERCHART.getValue()))){
                            groupFields = Lists.newArrayList(groupFields);
                            groupFields.add(DashboardGroupField.EVERY_DAY);
                        }
                        getGroupFieldDataArray(groupShowConfig.getDataList(), widgetVo, groupFields);
                    }
                    processTaskShowChartConfigArray.add(groupShowConfig);
                }
            }
        }
        processTaskChartConfig.put("showConfig", processTaskShowChartConfigArray);
        return processTaskChartConfig;
    }

    @Override
    public String getType() {
        return "ITSM";
    }

    @Override
    public String getDisplayName() {
        return "ITSM任务数据";
    }

    @Override
    public String getIcon() {
        return "xx-icon";
    }

}
