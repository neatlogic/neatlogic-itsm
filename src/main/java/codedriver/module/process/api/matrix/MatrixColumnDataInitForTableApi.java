package codedriver.module.process.api.matrix;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class MatrixColumnDataInitForTableApi extends ApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;
    
    @Autowired
    private MatrixDataMapper matrixDataMapper;
    
    @Autowired
    private MatrixAttributeMapper matrixAttributeMapper;

    @Override
    public String getToken() {
        return "matrix/column/data/init/fortable";
    }

    @Override
    public String getName() {
        return "矩阵属性数据回显-table接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
    	@Param( name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true),
    	@Param( name = "targetColumnList", desc = "目标属性集合，数据按这个字段顺序返回", type = ApiParamType.JSONARRAY, isRequired = true),
        @Param( name = "dataUuidList", desc = "需要回显的数据uuid集合", type = ApiParamType.JSONARRAY)
    })
    @Description(desc = "矩阵属性数据回显-table接口")
    @Output({ @Param( name = "columnDataList", type = ApiParamType.JSONARRAY, desc = "属性数据集合")})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String matrixUuid = jsonObj.getString("matrixUuid");
        //tbodyList
        List<String> targetColumnList =  JSONObject.parseArray(jsonObj.getJSONArray("targetColumnList").toJSONString(), String.class);
        List<String> dataUuidList =  JSONObject.parseArray(jsonObj.getJSONArray("dataUuidList").toJSONString(), String.class);
        ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo.getType().equals(ProcessMatrixType.CUSTOM.getValue())){
            List<Map<String, String>> dataMapList = matrixDataMapper.getDynamicTableDataByDataUuidList(dataUuidList,targetColumnList,matrixUuid);
            returnObj.put("tbodyList", dataMapList);
        }else {
            //外部数据源矩阵  暂未实现
            return null;
        }
        //theadList
    	JSONArray theadList = new JSONArray();
    	List<ProcessMatrixAttributeVo> matrixAttributeTheadList =  matrixAttributeMapper.getMatrixAttributeByMatrixUuidList(targetColumnList,matrixUuid);
    	for(String column :targetColumnList) {
    		for(ProcessMatrixAttributeVo matrixAttributeSearch:matrixAttributeTheadList) {
        		if(matrixAttributeSearch.getUuid().equals(column)) {
        			theadList.add(matrixAttributeSearch);
        		}
        	}
    	}
    	returnObj.put("theadList", theadList);
        return returnObj;
    }
}
