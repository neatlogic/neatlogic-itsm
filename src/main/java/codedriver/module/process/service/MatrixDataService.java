package codedriver.module.process.service;

import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 15:36
 **/
public interface MatrixDataService {

    public List<Map<String, String>> searchDynamicTableData(ProcessMatrixDataVo dataVo);

    public List<String> getExternalMatrixColumnData();
    
    public List<Map<String, Object>> matrixTableDataValueHandle(List<ProcessMatrixAttributeVo> attributeVoList, List<Map<String, String>> valueList);
    
    public JSONObject matrixAttributeValueHandle(ProcessMatrixAttributeVo processMatrixAttributeVo, Object value);
    
    public JSONObject matrixAttributeValueHandle(Object value);
    
    public List<String> matrixAttributeValueKeyWordSearch(ProcessMatrixAttributeVo processMatrixAttributeVo, String keyword, int pageSize);
    
    public List<Map<String, JSONObject>> getExternalDataTbodyList(IntegrationResultVo resultVo, List<String> columnList, int pageSize, JSONObject resultObj);
}
