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

/**
 * @program: codedriver
 * @description:
 * @create: 2020-04-10 16:24
 **/
@Service
public class MatrixColumnDataGetApi extends ApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixDataService matrixDataService;

    @Override
    public String getToken() {
        return "matrix/column/data/get";
    }

    @Override
    public String getName() {
        return "矩阵属性数据获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true),
             @Param( name = "targetColumn", desc = "目标属性ID", type = ApiParamType.STRING, isRequired = true),
             @Param( name = "sourceColumnList", desc = "源属性集合", type = ApiParamType.JSONARRAY)})
    @Description(desc = "矩阵属性数据获取接口")
    @Output({ @Param( name = "columnDataList", type = ApiParamType.JSONARRAY, desc = "属性数据集合")})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        ProcessMatrixDataVo dataVo = new ProcessMatrixDataVo();
        dataVo.setMatrixUuid(jsonObj.getString("matrixUuid"));
        dataVo.setTargetColumn(jsonObj.getString("targetColumn"));
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
        Set<String> columnDataSet = new HashSet<>();
        if (matrixVo.getType().equals(ProcessMatrixType.CUSTOM.getValue())){
            List<Map<String, String>> dataMapList = matrixDataService.getDynamicTableColumnData(dataVo);
            if (CollectionUtils.isNotEmpty(dataMapList)){
                for (Map<String, String> map : dataMapList){
                    columnDataSet.add(map.get(jsonObj.getString(dataVo.getTargetColumn())));
                }
            }
        }else {
            //外部数据源矩阵  暂未实现
            return null;
        }
        returnObj.put("columnDataList", columnDataSet);
        return returnObj;
    }
}
