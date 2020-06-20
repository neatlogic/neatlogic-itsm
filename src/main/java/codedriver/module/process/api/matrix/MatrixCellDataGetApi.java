package codedriver.module.process.api.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.Expression;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixExternalMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixAttributeNotFoundException;
import codedriver.framework.process.exception.matrix.MatrixExternalException;
import codedriver.framework.process.exception.matrix.MatrixExternalNotFoundException;
import codedriver.framework.process.exception.matrix.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixService;
@Service
public class MatrixCellDataGetApi extends ApiComponentBase {

	private final static Logger logger = LoggerFactory.getLogger(MatrixCellDataGetApi.class);
	
    @Autowired
    private MatrixService matrixService;

	@Autowired
	private MatrixMapper matrixMapper;
	
	@Autowired
	private MatrixDataMapper matrixDataMapper;
	
	@Autowired
	private MatrixAttributeMapper matrixAttributeMapper;

    @Autowired
    private MatrixExternalMapper matrixExternalMapper;
    
	@Autowired
	private IntegrationMapper integrationMapper;
	
	@Override
	public String getToken() {
		return "matrix/celldata/get";
	}

	@Override
	public String getName() {
		return "矩阵单元格数据获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
        @Param( name = "matrixUuid", type = ApiParamType.STRING, isRequired = true, desc = "矩阵uuid"),
        @Param( name = "sourceColumn", type = ApiParamType.STRING, isRequired = true, desc = "源列属性uuid"),
        @Param( name = "sourceColumnValueList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "源列属性值列表"),
        @Param( name = "targetColumn", type = ApiParamType.STRING, isRequired = true, desc = "目标列属性uuid")
    })
	@Output({ 
		@Param( name = "Return", type = ApiParamType.JSONARRAY, desc = "目标列属性值列表")
	})
	@Description( desc = "矩阵单元格数据获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String matrixUuid = jsonObj.getString("matrixUuid");
		ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if(matrixVo == null) {
        	throw new MatrixNotFoundException(matrixUuid);
        }
		
		List<String> resultObj = new ArrayList<>();
		List<String> sourceColumnValueList = JSON.parseArray(jsonObj.getString("sourceColumnValueList"), String.class);
		if(CollectionUtils.isNotEmpty(sourceColumnValueList)) {
			String sourceColumn = jsonObj.getString("sourceColumn");
			String targetColumn = jsonObj.getString("targetColumn");
	        if (ProcessMatrixType.CUSTOM.getValue().equals(matrixVo.getType())){
	        	List<ProcessMatrixAttributeVo> attributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
	    		List<String> attributeUuidList = attributeList.stream().map(ProcessMatrixAttributeVo::getUuid).collect(Collectors.toList());
	    		if(!attributeUuidList.contains(sourceColumn)) {
	    			throw new MatrixAttributeNotFoundException(matrixUuid, sourceColumn);
	    		}

	    		if(!attributeUuidList.contains(targetColumn)) {
	    			throw new MatrixAttributeNotFoundException(matrixUuid, targetColumn);
	    		}
	    		ProcessMatrixColumnVo sourceColumnVo = new ProcessMatrixColumnVo();
	    		sourceColumnVo.setColumn(sourceColumn);
	    		for(String sourceColumnValue : sourceColumnValueList) {
	    			sourceColumnVo.setValue(sourceColumnValue);
	    			String targetColumnValue = null;
	    			List<String> targetColumnValueList = matrixDataMapper.getDynamicTableCellData(matrixUuid, sourceColumnVo, targetColumn);
	    			if(CollectionUtils.isNotEmpty(targetColumnValueList)) {
	    				targetColumnValue = targetColumnValueList.get(0);
	    			}
	    			resultObj.add(targetColumnValue);
	    		}
	        }else {
	        	ProcessMatrixExternalVo externalVo = matrixExternalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
	            if(externalVo == null) {
	            	throw new MatrixExternalNotFoundException(matrixUuid);
	            }
	            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
	            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
	    		if (handler == null) {
	    			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
	    		}
	    		List<String> attributeUuidList = new ArrayList<>();
	    		List<ProcessMatrixAttributeVo> processMatrixAttributeList = matrixService.getExternalMatrixAttributeList(matrixUuid, integrationVo);
	    		for(ProcessMatrixAttributeVo processMatrixAttributeVo : processMatrixAttributeList) {
	    			attributeUuidList.add(processMatrixAttributeVo.getUuid());
	    		}
	    		if(!attributeUuidList.contains(sourceColumn)) {
	    			throw new MatrixAttributeNotFoundException(matrixUuid, sourceColumn);
	    		}

	    		if(!attributeUuidList.contains(targetColumn)) {
	    			throw new MatrixAttributeNotFoundException(matrixUuid, targetColumn);
	    		}

    			List<ProcessMatrixColumnVo> sourceColumnList = new ArrayList<>();
	    		ProcessMatrixColumnVo sourceColumnVo = new ProcessMatrixColumnVo();
	    		sourceColumnVo.setColumn(sourceColumn);
	    		List<String> columnList = new ArrayList<>();
	    		columnList.add(targetColumn);
	    		for(String sourceColumnValue : sourceColumnValueList) {
	    			sourceColumnVo.setValue(sourceColumnValue);
	    			sourceColumnVo.setExpression(Expression.EQUAL.getExpression());
	    			String targetColumnValue = null;
	    			sourceColumnList.clear();
	    			sourceColumnList.add(sourceColumnVo);
	    			integrationVo.getParamObj().put("sourceColumnList", sourceColumnList);
	            	IntegrationResultVo resultVo = handler.sendRequest(integrationVo);
	            	if(StringUtils.isNotBlank(resultVo.getError())) {
	        			logger.error(resultVo.getError());
	            		throw new MatrixExternalException("外部接口访问异常");
	            	}else {
		            	List<Map<String, JSONObject>> tbodyList = matrixService.getExternalDataTbodyList(resultVo, columnList, 1, null);
		            	if(CollectionUtils.isNotEmpty(tbodyList)) {
		            		targetColumnValue = tbodyList.get(0).get(targetColumn).getString("value");
		            	}
	            	}
	    			resultObj.add(targetColumnValue);
	    		}
	        }
		}
		
		return resultObj;
	}

}