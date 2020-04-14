package codedriver.module.process.api.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixColumnDataRepeatException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixDataService;

@Service
public class MatrixColumnDataSearchApi extends ApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixDataService matrixDataService;

    @Override
    public String getToken() {
        return "matrix/column/data/search";
    }

    @Override
    public String getName() {
        return "矩阵属性数据查询-下拉级联接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true),
            @Param( name = "value", desc = "作为值的目标属性", type = ApiParamType.STRING, isRequired = true),
            @Param( name = "text", desc = "作为显示文字的目标属性", type = ApiParamType.STRING, isRequired = true),
            @Param( name = "sourceColumnList", desc = "源属性集合", type = ApiParamType.JSONARRAY)})
    @Description(desc = "矩阵属性数据查询-下拉级联接口")
    @Output({ @Param( name = "columnDataList", type = ApiParamType.JSONARRAY, desc = "属性数据集合")})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String matrixUuid = jsonObj.getString("matrixUuid");
        String value = jsonObj.getString("value");
        String text = jsonObj.getString("value");
        List<String> valueList = new ArrayList<>();
        ProcessMatrixDataVo dataVo = new ProcessMatrixDataVo();
        if (jsonObj.containsKey("sourceColumnList")){
        	List<ProcessMatrixColumnVo> sourceColumnVoList = new ArrayList<>();
        	JSONArray sourceArray = jsonObj.getJSONArray("sourceColumnList");
            for (int i = 0; i < sourceArray.size(); i++){
                JSONObject sourceObj = sourceArray.getJSONObject(i);
                ProcessMatrixColumnVo sourceColumn = JSON.toJavaObject(sourceObj, ProcessMatrixColumnVo.class);
                sourceColumnVoList.add(sourceColumn);
            }
            dataVo.setSourceColumnList(sourceColumnVoList);
        }
        List<String> targetColumnList = new ArrayList<>();
        targetColumnList.add(value);
        targetColumnList.add(text);
        dataVo.setColumnList(targetColumnList);
        dataVo.setMatrixUuid(matrixUuid);
        ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo.getType().equals(ProcessMatrixType.CUSTOM.getValue())){
            List<Map<String, String>> dataMapList = matrixDataService.getDynamicTableDataByColumnList(dataVo);
            for(Map<String,String> dataMap : dataMapList){
            	String valueTmp = dataMap.get(value);
            	if(valueList.contains(valueTmp)) {
            		throw new MatrixColumnDataRepeatException();
            	}else {
            		valueList.add(valueTmp);
            	}
            }
            returnObj.put("columnDataList", dataMapList);
        }else {
            //外部数据源矩阵  暂未实现
            return null;
        }
        return returnObj;
    }
}
