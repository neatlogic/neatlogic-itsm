package neatlogic.module.process.matrix.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.matrix.constvalue.SearchExpression;
import neatlogic.framework.matrix.core.IMatrixPrivateDataSourceHandler;
import neatlogic.framework.matrix.dto.MatrixAttributeVo;
import neatlogic.framework.matrix.dto.MatrixDataVo;
import neatlogic.framework.matrix.dto.MatrixFilterVo;
import neatlogic.framework.process.constvalue.ProcessTaskStepStatus;
import neatlogic.framework.util.UuidUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessTaskStepStatusMatrixPrivateDataSourceHandler implements IMatrixPrivateDataSourceHandler {

    private final List<MatrixAttributeVo> matrixAttributeList = new ArrayList<>();
    private final Map<String , String> columnsMap = new HashMap<>();
    {
        JSONArray attributeDefinedList = new JSONArray();
        {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("name", "状态码");
            jsonObj.put("label", "status");
            jsonObj.put("isPrimaryKey", 1);
            jsonObj.put("isSearchable", 1);
            attributeDefinedList.add(jsonObj);
        }
        {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("name", "状态名称");
            jsonObj.put("label", "statusText");
            jsonObj.put("isPrimaryKey", 0);
            jsonObj.put("isSearchable", 1);
            attributeDefinedList.add(jsonObj);
        }
        this.setAttribute(matrixAttributeList , attributeDefinedList);
        for(MatrixAttributeVo matrixAttributeVo : matrixAttributeList){
            columnsMap.put(matrixAttributeVo.getLabel() , matrixAttributeVo.getUuid());
        }
    }
    @Override
    public String getUuid() {
        return UuidUtil.getCustomUUID(getLabel());
    }

    @Override
    public String getName() {
        return "流程步骤状态";
    }

    @Override
    public String getLabel() {
        return "ProcessTaskStepStatus";
    }

    @Override
    public List<MatrixAttributeVo> getAttributeList() {
        return matrixAttributeList;
    }

    @Override
    public List<Map<String, String>> searchTableData(MatrixDataVo dataVo) {
        List<MatrixFilterVo> filterList = dataVo.getFilterList();
        List<Map<String, String>> dataList = new ArrayList<>();

        if (CollectionUtils.isEmpty(filterList)) {
            for (ProcessTaskStepStatus type : ProcessTaskStepStatus.values()) {
                Map<String, String> data = new HashMap<>();
                data.put("uuid", UuidUtil.getCustomUUID(getLabel() + type.getValue()));
                data.put(columnsMap.get("status"), type.getValue());
                data.put(columnsMap.get("statusText"), type.getText());
                dataList.add(data);
            }
        }else{
            for (ProcessTaskStepStatus type : ProcessTaskStepStatus.values()) {
                if(checkFilter(type , filterList)){
                    Map<String, String> data = new HashMap<>();
                    data.put("uuid", UuidUtil.getCustomUUID(getLabel() + type.getValue()));
                    data.put(columnsMap.get("status"), type.getValue());
                    data.put(columnsMap.get("statusText"), type.getText());
                    dataList.add(data);
                }
            }
        }
        return dataList;
    }

    private Boolean checkFilter(ProcessTaskStepStatus status , List<MatrixFilterVo> filterList) {
        for (MatrixFilterVo filter : filterList) {
            String label = null;
            for (MatrixAttributeVo matrixAttributeVo : matrixAttributeList) {
                if (Objects.equals(matrixAttributeVo.getUuid(), filter.getUuid())) {
                    label = matrixAttributeVo.getLabel();
                }
            }
            String statusValue = null;
            if (Objects.equals(label, "status")) {
                statusValue = status.getValue();
            } else if (Objects.equals(label, "statusText")) {
                statusValue = status.getText();
            } else {
                continue;
            }
            String expression = filter.getExpression();
            if (StringUtils.isBlank(expression)) {
                expression = SearchExpression.EQ.getExpression();
            }
            if (Objects.equals(expression, SearchExpression.EQ.getExpression())) {
                if (!Objects.equals(filter.getValueList().get(0), statusValue)) {
                    return false;
                }
            } else if (Objects.equals(expression, SearchExpression.NE.getExpression())) {
                if (Objects.equals(filter.getValueList().get(0), statusValue)) {
                    return false;
                }
            } else if (Objects.equals(expression, SearchExpression.LI.getExpression())) {
                if (!statusValue.contains(filter.getValueList().get(0))) {
                    return false;
                }
            } else if (Objects.equals(expression, SearchExpression.NL.getExpression())) {
                if (statusValue.contains(filter.getValueList().get(0))) {
                    return false;
                }
            }else if (Objects.equals(expression, SearchExpression.NULL.getExpression())) {
                if (StringUtils.isNotBlank(statusValue)) {
                    return false;
                }
            } else if (Objects.equals(expression, SearchExpression.NOTNULL.getExpression())) {
                if (StringUtils.isBlank(statusValue)) {
                    return false;
                }
            }
        }
        return true;
    }
}
