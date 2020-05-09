package codedriver.module.process.api.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.process.constvalue.ProcessExpression;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixExternalMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixAttributeNotFoundException;
import codedriver.framework.process.exception.matrix.MatrixExternalNotFoundException;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixAttributeService;
import codedriver.module.process.service.MatrixDataService;

@Service
public class MatrixColumnDataSearchForSelectNewApi extends ApiComponentBase {

    @Autowired
    private MatrixAttributeService attributeService;
	
	@Autowired
	private MatrixDataService matrixDataService;

    @Autowired
    private MatrixMapper matrixMapper;
    
    @Autowired
    private MatrixAttributeMapper matrixAttributeMapper;

    @Autowired
    private MatrixDataMapper matrixDataMapper;

    @Autowired
    private MatrixExternalMapper matrixExternalMapper;
    
	@Autowired
	private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "matrix/column/data/search/forselect/new";
    }

    @Override
    public String getName() {
        return "矩阵属性数据查询-下拉级联接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
    	@Param(name = "keyword", desc = "关键字", type = ApiParamType.STRING, xss = true),
    	@Param(name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true), 
    	@Param(name = "keywordColumn", desc = "关键字属性uuid", type = ApiParamType.STRING),           
    	@Param(name = "columnList", desc = "属性uuid列表", type = ApiParamType.JSONARRAY, isRequired = true),
        @Param(name = "sourceColumnList", desc = "源属性集合", type = ApiParamType.JSONARRAY),
        @Param(name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER)
    	})
    @Description(desc = "矩阵属性数据查询-下拉级联接口")
    @Output({ 
    	@Param(name = "columnDataList", type = ApiParamType.JSONARRAY, desc = "属性数据集合")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	ProcessMatrixDataVo dataVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessMatrixDataVo>() {});
        ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
        if(matrixVo == null) {
        	throw new MatrixNotFoundException(dataVo.getMatrixUuid());
        }      

    	List<String> columnList = dataVo.getColumnList();
    	if(CollectionUtils.isEmpty(columnList)) {
    		throw new ParamIrregularException("参数“columnList”不符合格式要求");
    	}
    	String keywordColumn = jsonObj.getString("keywordColumn");
    	List<Map<String, Object>> resultList = new ArrayList<>();
        JSONObject returnObj = new JSONObject();
        if (ProcessMatrixType.CUSTOM.getValue().equals(matrixVo.getType())){
            List<ProcessMatrixAttributeVo> attributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
            if (CollectionUtils.isNotEmpty(attributeList)){
            	Map<String, ProcessMatrixAttributeVo> processMatrixAttributeMap = new HashMap<>();
            	for(ProcessMatrixAttributeVo processMatrixAttributeVo : attributeList) {
            		processMatrixAttributeMap.put(processMatrixAttributeVo.getUuid(), processMatrixAttributeVo);
            	}
            	for(String column : columnList) {
            		if(!processMatrixAttributeMap.containsKey(column)) {
            			throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
            		}
            	}
            	if(StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
            		ProcessMatrixAttributeVo processMatrixAttribute = processMatrixAttributeMap.get(keywordColumn);
                	if(processMatrixAttribute == null) {
                		throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), keywordColumn);
                	}
                	List<String> uuidList = matrixDataService.matrixAttributeValueKeyWordSearch(processMatrixAttribute, dataVo.getKeyword(), dataVo.getPageSize());
                	if(CollectionUtils.isNotEmpty(uuidList)) {
                		dataVo.setUuidList(uuidList);                	
                	}
            	}          	
            	List<Map<String, String>> dataMapList = matrixDataMapper.getDynamicTableDataByColumnList2(dataVo);
            	for(Map<String, String> dataMap : dataMapList) {
            		Map<String, Object> resultMap = new HashMap<>(dataMap.size());
            		for(Entry<String, String> entry : dataMap.entrySet()) {
            			String attributeUuid = entry.getKey();
            			resultMap.put(attributeUuid, matrixDataService.matrixAttributeValueHandle(processMatrixAttributeMap.get(attributeUuid), entry.getValue()));
            		}
            		resultList.add(resultMap);
            	}
            }
            
        }else {
        	ProcessMatrixExternalVo externalVo = matrixExternalMapper.getMatrixExternalByMatrixUuid(dataVo.getMatrixUuid());
            if(externalVo == null) {
            	throw new MatrixExternalNotFoundException(dataVo.getMatrixUuid());
            }
            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
    		if (handler == null) {
    			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
    		}
    		List<String> attributeList = new ArrayList<>();
    		List<ProcessMatrixAttributeVo> processMatrixAttributeList = attributeService.getExternalMatrixAttributeList(dataVo.getMatrixUuid(), integrationVo);
    		for(ProcessMatrixAttributeVo processMatrixAttributeVo : processMatrixAttributeList) {
    			attributeList.add(processMatrixAttributeVo.getUuid());
    		}
    		
        	for(String column : columnList) {
        		if(!attributeList.contains(column)) {
        			throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
        		}
        	}
        	if(StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
            	if(!attributeList.contains(keywordColumn)) {
            		throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), keywordColumn);
            	}
        	}
        	if(StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
        		ProcessMatrixColumnVo processMatrixColumnVo = new ProcessMatrixColumnVo();
        		processMatrixColumnVo.setColumn(keywordColumn);
        		processMatrixColumnVo.setExpression(ProcessExpression.LIKE.getExpression());
        		processMatrixColumnVo.setValue(dataVo.getKeyword());
        		List<ProcessMatrixColumnVo> sourceColumnList = dataVo.getSourceColumnList();
				if(CollectionUtils.isEmpty(sourceColumnList)) {
					sourceColumnList = new ArrayList<>();
				}
				sourceColumnList.add(processMatrixColumnVo);
				jsonObj.put("sourceColumnList", sourceColumnList);
			}
        	integrationVo.getParamObj().putAll(jsonObj);
        	IntegrationResultVo resultVo = handler.sendRequest(integrationVo);
    		if(resultVo != null && StringUtils.isNotBlank(resultVo.getTransformedResult())) {
    			JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
    			if(MapUtils.isNotEmpty(transformedResult)) {
    				JSONArray tbodyList = transformedResult.getJSONArray("tbodyList");
    				if(CollectionUtils.isNotEmpty(tbodyList)) {
    					for(int i = 0; i < tbodyList.size(); i++) {
    						JSONObject rowData = tbodyList.getJSONObject(i);
							Map<String, Object> resultMap = new HashMap<>(columnList.size());
    						for(String column : columnList) {
    							String columnValue = rowData.getString(column);
    							resultMap.put(column, matrixDataService.matrixAttributeValueHandle(columnValue)); 							
    						}
    						resultList.add(resultMap);
    						if(resultList.size() >= dataVo.getPageSize()) {
    							break;
    						}
    					}
    				}
    			}
    		}
        }
        returnObj.put("columnDataList", resultList);
        return returnObj;
    }
}
