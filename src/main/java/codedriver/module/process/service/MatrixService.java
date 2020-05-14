package codedriver.module.process.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.exception.util.FreemarkerTransformException;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;

public interface MatrixService {

	public List<ProcessMatrixAttributeVo> getExternalMatrixAttributeList(String matrixUuid, IntegrationVo integrationVo) throws FreemarkerTransformException;
    
    public List<Map<String, Object>> matrixTableDataValueHandle(List<ProcessMatrixAttributeVo> attributeVoList, List<Map<String, String>> valueList);
    
    public JSONObject matrixAttributeValueHandle(ProcessMatrixAttributeVo processMatrixAttributeVo, Object value);
    
    public JSONObject matrixAttributeValueHandle(Object value);
    
    public List<String> matrixAttributeValueKeyWordSearch(ProcessMatrixAttributeVo processMatrixAttributeVo, String keyword, int pageSize);
    
    public List<Map<String, JSONObject>> getExternalDataTbodyList(IntegrationResultVo resultVo, List<String> columnList, int pageSize, JSONObject resultObj);

}
