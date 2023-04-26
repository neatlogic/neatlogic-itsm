package neatlogic.module.process.matrix.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.matrix.core.IMatrixPrivateDataSourceHandler;
import neatlogic.framework.matrix.dto.MatrixAttributeVo;
import neatlogic.framework.matrix.dto.MatrixDataVo;
import neatlogic.framework.matrix.dto.MatrixFilterVo;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.util.UuidUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProcessTaskStepStatusMatrixPrivateDataSourceHandler implements IMatrixPrivateDataSourceHandler {

    private final List<MatrixAttributeVo> matrixAttributeList = new ArrayList<>();
    private final Map<String , String> columnsMap = new HashMap<>();

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
        if (matrixAttributeList.size() == 0) {
            this.setAttribute(matrixAttributeList , attributeDefinedList);

            for(MatrixAttributeVo matrixAttributeVo : matrixAttributeList){
                columnsMap.put(matrixAttributeVo.getLabel() , matrixAttributeVo.getUuid());
            }
        }
        return matrixAttributeList;
    }

    @Override
    public List<Map<String, String>> searchTableData(MatrixDataVo dataVo) {
        List<MatrixFilterVo> filterList = dataVo.getFilterList();
        List<Map<String, String>> dataList = new ArrayList<>();

        if (CollectionUtils.isEmpty(filterList)) {
            for (ProcessTaskStatus type : ProcessTaskStatus.values()) {
                Map<String, String> data = new HashMap<>();
                data.put("uuid", UuidUtil.getCustomUUID(getLabel() + type.getValue()));
                data.put(columnsMap.get("status"), type.getValue());
                data.put(columnsMap.get("statusText"), type.getText());
                dataList.add(data);
            }
        }else{
            for (ProcessTaskStatus type : ProcessTaskStatus.values()) {
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

    private Boolean checkFilter(ProcessTaskStatus type , List<MatrixFilterVo> filterList){
        boolean isContain = false ;
        flag:for (MatrixFilterVo filter : filterList) {
            for(String value:filter.getValueList()){
                if(type.getText().contains(value) || type.getValue().contains(value)){
                    isContain = true ;
                    break flag;
                }
            }
        }
        return isContain;
    }
}
