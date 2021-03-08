package codedriver.module.process.dashboard.handler;

import codedriver.framework.common.constvalue.dashboard.ChartType;
import codedriver.framework.common.constvalue.dashboard.DashboardShowConfig;
import codedriver.framework.condition.core.ConditionHandlerFactory;
import codedriver.framework.dashboard.core.DashboardChartBase;
import codedriver.framework.dashboard.core.DashboardChartFactory;
import codedriver.framework.dashboard.core.DashboardHandlerBase;
import codedriver.framework.dashboard.dto.DashboardShowConfigVo;
import codedriver.framework.dashboard.dto.DashboardWidgetVo;
import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnFactory;
import codedriver.framework.process.constvalue.ProcessConditionModel;
import codedriver.framework.process.constvalue.ProcessWorkcenterField;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.constvalue.FieldTypeEnum;
import codedriver.module.process.workcenter.core.SqlBuilder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class ProcessTaskDashboardHandler extends DashboardHandlerBase {

    @Autowired
    WorkcenterMapper workcenterMapper;

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
            JSONObject data = new JSONObject();
            JSONObject configChart = widgetVo.getChartConfigObj();
            String groupField = configChart.getString(DashboardShowConfig.GROUPFIELD.getValue());
            String subGroupField = configChart.getString(DashboardShowConfig.SUBGROUPFIELD.getValue());
            /* start: 从mysql 获取源数据 */
            WorkcenterVo workcenterVo = new WorkcenterVo(jsonObj);
            //1、查出group权重，用于排序截取最大组数量
            workcenterVo.setGroup(groupField);
            if (!ChartType.NUMBERCHART.getValue().equals(widgetVo.getChartType()) && !ChartType.TABLECHART.getValue().equals(widgetVo.getChartType()) && configChart.containsKey(DashboardShowConfig.MAXGROUP.getValue())) {
                //仅展示分组数
                workcenterVo.setCurrentPage(1);
                workcenterVo.setPageSize(configChart.getInteger(DashboardShowConfig.MAXGROUP.getValue()));
            }
            SqlBuilder sb = new SqlBuilder(workcenterVo, FieldTypeEnum.GROUP_COUNT);
            List<Map<String, String>> groupMapList = workcenterMapper.getWorkcenterProcessTaskMapBySql(sb.build());
            IProcessTaskColumn groupColumn = ProcessTaskColumnFactory.columnComponentMap.get(groupField);
            IProcessTaskColumn subGroupColumn = null;
            //2、如果存在subGroup,则根据步骤1查出的权重，排序截取最大组数量，查出二维数据
            List<Map<String, String>> subGroupMapList = null;
            if (StringUtils.isNotBlank(subGroupField)) {
                subGroupColumn = ProcessTaskColumnFactory.columnComponentMap.get(subGroupField);
                if(subGroupColumn != null) {
                    workcenterVo.setSubGroup(subGroupField);
                }
                //先排序分页获取前分组数的group
                groupColumn.getExchangeToDashboardGroupDataMap(groupMapList, workcenterVo);
                //根据分组groupDataList、子分组 再次搜索
                sb = new SqlBuilder(workcenterVo, FieldTypeEnum.GROUP_COUNT);
                groupMapList = workcenterMapper.getWorkcenterProcessTaskMapBySql(sb.build());
            }
            //3、替换mapList的key（group -》 column，subGroup -》 type）
            workcenterVo.setGroupType("group");
            groupColumn.getExchangeToDashboardResultData(groupMapList, workcenterVo);
            if (subGroupColumn != null) {
                workcenterVo.setGroupType("subGroup");
                subGroupColumn.getExchangeToDashboardResultData(groupMapList, workcenterVo);
            }
            /* end: 从mysql 获取源数据 */
            /* start: 将mysql源数据 按不同dashboard插件处理返回结果数据*/


            /* end: 将mysql源数据 按不同dashboard插件处理返回结果数据*/
            data.put("configObj", configChart);
            data.put("dataList",groupMapList);
            if (configChart.containsKey(DashboardShowConfig.GROUPFIELD.getValue())) {
                configChart.put("groupfieldtext", ProcessWorkcenterField.getName(groupField));
            }
            if (configChart.containsKey(DashboardShowConfig.SUBGROUPFIELD.getValue())) {
                configChart.put("subgroupfieldtext", ProcessWorkcenterField.getName(subGroupField));
            }
            return data;
        }
        return null;
    }

    private void getGroupFieldDataArray(JSONArray groupFieldDataArray, DashboardWidgetVo widgetVo, List<ProcessWorkcenterField> fieldList, Boolean isSub) {
        JSONObject groupFieldJson = new JSONObject();
        for (ProcessWorkcenterField groupField : fieldList) {
            if (!isSub && ChartType.NUMBERCHART.getValue().equals(widgetVo.getChartType())) {
                groupFieldJson = ConditionHandlerFactory.getHandler(groupField.getValue()).getConfig();
                groupFieldJson.remove("isMultiple");
                groupFieldJson.put("handler", ConditionHandlerFactory.getHandler(groupField.getValue()).getHandler(ProcessConditionModel.CUSTOM.getValue()));

            }
            groupFieldDataArray.add(JSONObject.parse(String.format("{'value':'%s','text':'%s',config:%s}", groupField.getValue(), groupField.getName(), groupFieldJson)));
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
                if (showConfigJson.containsKey(DashboardShowConfig.TYPE.getValue())) {
                    DashboardShowConfigVo aggregateShowConfig = (DashboardShowConfigVo) showConfigJson.get(DashboardShowConfig.TYPE.getValue());
                    processTaskShowChartConfigArray.add(aggregateShowConfig);
                }
                if (showConfigJson.containsKey(DashboardShowConfig.AGGREGATE.getValue())) {
                    DashboardShowConfigVo aggregateShowConfig = (DashboardShowConfigVo) showConfigJson.get(DashboardShowConfig.AGGREGATE.getValue());
                    processTaskShowChartConfigArray.add(aggregateShowConfig);
                }
                if (showConfigJson.containsKey(DashboardShowConfig.GROUPFIELD.getValue())) {
                    DashboardShowConfigVo groupShowConfig = (DashboardShowConfigVo) showConfigJson.get(DashboardShowConfig.GROUPFIELD.getValue());
                    getGroupFieldDataArray(groupShowConfig.getDataList(), widgetVo, Arrays.asList(
                            ProcessWorkcenterField.PRIORITY,
                            ProcessWorkcenterField.STATUS,
                            ProcessWorkcenterField.CHANNELTYPE,
                            ProcessWorkcenterField.CHANNEL,
                            ProcessWorkcenterField.STEP_USER,
                            ProcessWorkcenterField.OWNER
                    ), false);
                    processTaskShowChartConfigArray.add(groupShowConfig);
                }
                if (showConfigJson.containsKey(DashboardShowConfig.SUBGROUPFIELD.getValue())) {
                    DashboardShowConfigVo subGroupShowConfig = (DashboardShowConfigVo) showConfigJson.get(DashboardShowConfig.SUBGROUPFIELD.getValue());
                    getGroupFieldDataArray(subGroupShowConfig.getDataList(), widgetVo, Arrays.asList(
                            ProcessWorkcenterField.PRIORITY,
                            ProcessWorkcenterField.STATUS,
                            ProcessWorkcenterField.CHANNELTYPE,
                            ProcessWorkcenterField.CHANNEL,
                            ProcessWorkcenterField.STEP_USER,
                            ProcessWorkcenterField.OWNER
                    ), true);
                    processTaskShowChartConfigArray.add(subGroupShowConfig);
                }
                if (showConfigJson.containsKey(DashboardShowConfig.MAXGROUP.getValue())) {
                    DashboardShowConfigVo maxGroupShowConfig = (DashboardShowConfigVo) showConfigJson.get(DashboardShowConfig.MAXGROUP.getValue());
                    processTaskShowChartConfigArray.add(maxGroupShowConfig);
                }
                if (showConfigJson.containsKey(DashboardShowConfig.REFRESHTIME.getValue())) {
                    DashboardShowConfigVo refreshTimeShowConfig = (DashboardShowConfigVo) showConfigJson.get(DashboardShowConfig.REFRESHTIME.getValue());
                    processTaskShowChartConfigArray.add(refreshTimeShowConfig);
                }
                if (showConfigJson.containsKey(DashboardShowConfig.COLOR.getValue())) {
                    DashboardShowConfigVo refreshTimeShowConfig = (DashboardShowConfigVo) showConfigJson.get(DashboardShowConfig.COLOR.getValue());
                    processTaskShowChartConfigArray.add(refreshTimeShowConfig);
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
