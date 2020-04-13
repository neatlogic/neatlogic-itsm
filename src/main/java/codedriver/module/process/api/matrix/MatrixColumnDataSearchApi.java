package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixDataService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
        return "矩阵属性数据查询接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true),
            @Param( name = "targetColumnList", desc = "目标属性ID", type = ApiParamType.JSONARRAY, isRequired = true),
            @Param( name = "sourceColumnList", desc = "源属性集合", type = ApiParamType.JSONARRAY)})
    @Description(desc = "矩阵属性数据查询接口")
    @Output({ @Param( name = "columnDataList", type = ApiParamType.JSONARRAY, desc = "属性数据集合")})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        JSONArray columnArray = jsonObj.getJSONArray("targetColumnList");
        List<String> targetColumnList = new ArrayList<>();
        for (int i = 0; i < columnArray.size(); i++){
            targetColumnList.add(columnArray.getString(i));
        }
        ProcessMatrixDataVo dataVo = new ProcessMatrixDataVo();
        dataVo.setColumnList(targetColumnList);
        dataVo.setMatrixUuid(jsonObj.getString("matrixUuid"));
        List<ProcessMatrixColumnVo> sourceColumnVoList = new ArrayList<>();
        if (jsonObj.containsKey("sourceColumnList")){
            JSONArray sourceArray = jsonObj.getJSONArray("sourceColumnList");
            for (int i = 0; i < sourceArray.size(); i++){
                JSONObject sourceObj = sourceArray.getJSONObject(i);
                ProcessMatrixColumnVo sourceColumn = JSON.toJavaObject(sourceObj, ProcessMatrixColumnVo.class);
                sourceColumnVoList.add(sourceColumn);
            }
        }
        dataVo.setSourceColumnList(sourceColumnVoList);
        ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(jsonObj.getString("matrixUuid"));
        if (matrixVo.getType().equals(ProcessMatrixType.CUSTOM.getValue())){
            List<Map<String, String>> dataMapList = matrixDataService.getDynamicTableDataByColumnList(dataVo);
            returnObj.put("columnDataList", dataMapList);
        }else {
            //外部数据源矩阵  暂未实现
            return null;
        }
        return returnObj;
    }
}
