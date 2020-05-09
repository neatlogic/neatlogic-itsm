package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixExternalMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixExternalNotFoundException;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.util.FreemarkerUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:06
 **/
@Service
public class MatrixAttributeSearchApi extends ApiComponentBase {

    @Autowired
    private MatrixAttributeMapper attributeMapper;

    @Autowired
    private MatrixDataMapper matrixDataMapper;

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixExternalMapper matrixExternalMapper;
    
	@Autowired
	private IntegrationMapper integrationMapper;

    @Override
    public String getToken() {
        return "matrix/attribute/search";
    }

    @Override
    public String getName() {
        return "矩阵属性检索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true)})
    @Output( {@Param( name = "Return", desc = "矩阵属性集合", explode = ProcessMatrixAttributeVo[].class)})
    @Description( desc = "矩阵属性检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String matrixUuid = jsonObj.getString("matrixUuid");
        ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
    	if(matrixVo == null) {
    		throw new MatrixNotFoundException(matrixUuid);
    	}
    	if (matrixVo.getType().equals(ProcessMatrixType.CUSTOM.getValue())){
    		List<ProcessMatrixAttributeVo> processMatrixAttributeList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
        	if(CollectionUtils.isNotEmpty(processMatrixAttributeList)) {
        		List<String> columnList = processMatrixAttributeList.stream().map(ProcessMatrixAttributeVo :: getUuid).collect(Collectors.toList());
        		ProcessMatrixDataVo dataVo = new ProcessMatrixDataVo();
        		dataVo.setMatrixUuid(matrixUuid);
        		dataVo.setColumnList(columnList);
        		dataVo.setNeedPage(false);
        		
        		List<Map<String, String>> matrixRowDataList = matrixDataMapper.searchDynamicTableData(dataVo);
        		if(CollectionUtils.isNotEmpty(matrixRowDataList)) {
        			Map<String, Boolean> attributeDataIsExistMap = new HashMap<>();
        			for(Map<String, String> rowData : matrixRowDataList) {
            			for(Entry<String, String> columnData : rowData.entrySet()) {
            				if(Boolean.TRUE.equals(attributeDataIsExistMap.get(columnData.getKey()))) {
            					continue;
            				}
            				if(StringUtils.isNotBlank(columnData.getValue())) {
            					attributeDataIsExistMap.put(columnData.getKey(), true);
            				}
            			}
            		}
        			for(ProcessMatrixAttributeVo processMatrixAttributeVo : processMatrixAttributeList) {
        				if(attributeDataIsExistMap.containsKey(processMatrixAttributeVo.getUuid())) {
        					processMatrixAttributeVo.setIsDeletable(0);
        				}
        			}
        		}
        	}
            return processMatrixAttributeList;
    	}else {
    		ProcessMatrixExternalVo externalVo = matrixExternalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
            if(externalVo == null) {
            	throw new MatrixExternalNotFoundException(matrixUuid);
            }
            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
            if(integrationVo != null) {
            	IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
        		if (handler == null) {
        			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
        		}
        		JSONObject config = integrationVo.getConfig();
        		if(MapUtils.isNotEmpty(config)) {
        			JSONObject output = config.getJSONObject("output");
        			if(MapUtils.isNotEmpty(output)) {
        				String content = output.getString("content");
        				content = FreemarkerUtil.transform(null, content);
        				JSONObject contentObj = JSON.parseObject(content);
        				if(MapUtils.isNotEmpty(contentObj)) {
        					JSONArray theadList = contentObj.getJSONArray("theadList");
                    		if(CollectionUtils.isNotEmpty(theadList)) {
                    			List<ProcessMatrixAttributeVo> processMatrixAttributeList = new ArrayList<>();
                    			for(int i = 0; i < theadList.size(); i++) {
                    				JSONObject theadObj = theadList.getJSONObject(i);
                    				ProcessMatrixAttributeVo processMatrixAttributeVo = new ProcessMatrixAttributeVo();
                    				processMatrixAttributeVo.setMatrixUuid(matrixUuid);
                    				processMatrixAttributeVo.setUuid(theadObj.getString("key"));
                    				processMatrixAttributeVo.setName(theadObj.getString("title"));
                    				processMatrixAttributeVo.setType(ProcessMatrixAttributeType.INPUT.getValue());
                    				processMatrixAttributeVo.setIsDeletable(0);
                    				processMatrixAttributeVo.setSort(i);
                    				processMatrixAttributeVo.setIsRequired(0);
                    				Integer isSearchable = theadObj.getInteger("isSearchable");
                    				processMatrixAttributeVo.setIsSearchable((isSearchable == null || isSearchable.intValue() != 1) ? 0 : 1);
                    				processMatrixAttributeList.add(processMatrixAttributeVo);
                    			}
                    			return processMatrixAttributeList;
                    		}
        				}
        			}
        		}
            }
            return null;
    	}    	
    }
}
