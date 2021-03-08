package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.process.workcenter.table.ProcessTaskSqlTable;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessTaskStatusColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getDisplayName() {
        return "工单状态";
    }

    @Override
    public Object getMyValue(JSONObject json) throws RuntimeException {
        JSONObject statusJson = new JSONObject();
        String status = json.getString(this.getName());
        statusJson.put("value", status);
        statusJson.put("text", ProcessTaskStatus.getText(status));
        statusJson.put("color", ProcessTaskStatus.getColor(status));
        return statusJson;
    }

    @Override
    public JSONObject getMyValueText(JSONObject json) {
        return (JSONObject) getMyValue(json);
    }

    @Override
    public Boolean allowSort() {
        return false;
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public String getClassName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getSort() {
        return 5;
    }

    @Override
    public Object getSimpleValue(Object json) {
        String status = null;
        if (json != null) {
            status = JSONObject.parseObject(json.toString()).getString("text");
        }
        return status;
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<TableSelectColumnVo>() {
            {
                add(new TableSelectColumnVo(new ProcessTaskSqlTable(), Collections.singletonList(
                        new SelectColumnVo(ProcessTaskSqlTable.FieldEnum.STATUS.getValue(), ProcessTaskSqlTable.FieldEnum.STATUS.getProValue(),true)
                )));
            }
        };
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        JSONObject statusJson = new JSONObject();
        String status = processTaskVo.getStatus();
        statusJson.put("value", status);
        statusJson.put("text", ProcessTaskStatus.getText(status));
        statusJson.put("color", ProcessTaskStatus.getColor(status));
        return statusJson;
    }

    @Override
    public List<Map<String, String>> getMyExchangeToDashboardResultData(List<Map<String, String>> mapList, WorkcenterVo workcenterVo) {
        for (int i = 0; i< mapList.size() ; i++) {
            Map<String, String> dataMap = mapList.get(i);
            Iterator<Map.Entry<String, String>> iterator = dataMap.entrySet().iterator();
            Map<String,String> tmpMap = new HashMap<>();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String key = entry.getKey();
                String value = String.valueOf(entry.getValue());
                //如果是分组
                if (ProcessTaskSqlTable.FieldEnum.STATUS.getProValue().equals(key)) {
                    if ("group".equals(workcenterVo.getGroupType())) {
                        if(StringUtils.isNotBlank(workcenterVo.getSubGroup())) {
                            tmpMap.put("total", workcenterVo.getGroupDataCountMap().get(value));
                        }else{
                            tmpMap.put("total", dataMap.get("count"));
                        }
                        tmpMap.put("column", ProcessTaskStatus.getText(value));
                    } else if ("subGroup".equals(workcenterVo.getGroupType())) {
                        tmpMap.put("type", ProcessTaskStatus.getText(value));
                    }
                }else if("count".equals(key)){
                    tmpMap.put("value", dataMap.get("count"));
                }else{
                    tmpMap.put(key,value);
                }
            }
            mapList.set(i,tmpMap);
        }
        return mapList;
    }

    @Override
    public LinkedHashMap<String, String> getMyExchangeToDashboardGroupDataMap(List<Map<String, String>> mapList) {
        LinkedHashMap<String, String> groupDataMap = new LinkedHashMap<>();
        for (Map<String, String> dataMap : mapList) {
            groupDataMap.put(dataMap.get(ProcessTaskSqlTable.FieldEnum.STATUS.getProValue()), dataMap.get("count"));
        }
        return groupDataMap;
    }
}
