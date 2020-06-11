package codedriver.module.process.api.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.process.dao.mapper.MatrixExternalMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixDispatcherVo;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.dto.ProcessMatrixFormComponentVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixExternalException;
import codedriver.framework.process.exception.matrix.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixService;
@Service
@Deprecated
public class MatrixExternalDataSearchApi extends ApiComponentBase {

	private final static Logger logger = LoggerFactory.getLogger(MatrixExternalDataSearchApi.class);
	
    @Autowired
    private MatrixService matrixService;
	@Autowired
	private IntegrationMapper integrationMapper;
	
    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixExternalMapper externalMapper;

	@Override
	public String getToken() {
		return "matrix/external/data/search";
	}

	@Override
	public String getName() {
		return "外部数据源数据检索接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({ 
        @Param(name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
        @Param(name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
        @Param(name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER)
	})
	@Output({ 
		@Param(name = "tbodyList", desc = "矩阵数据集合"),
	    @Param(name = "theadList", desc = "矩阵属性集合"),
	    @Param(explode = BasePageVo.class)
	})
	@Description( desc = "矩阵数据检索接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String matrixUuid = jsonObj.getString("matrixUuid");
		ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if(matrixVo == null) {
        	throw new MatrixNotFoundException(matrixUuid);
        }
        ProcessMatrixExternalVo externalVo = externalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
        JSONObject returnObj = new JSONObject();
        if(externalVo != null) {
        	IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
    		if (handler == null) {
    			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
    		}
    		
        	integrationVo.getParamObj().putAll(jsonObj);
    		IntegrationResultVo resultVo = handler.sendRequest(integrationVo);
    		if(StringUtils.isNotBlank(resultVo.getError())) {
    			logger.error(resultVo.getError());
        		throw new MatrixExternalException("外部接口访问异常");
        	}else if(StringUtils.isNotBlank(resultVo.getTransformedResult())) {
    			JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
    			if(MapUtils.isNotEmpty(transformedResult)) {
    				returnObj.putAll(transformedResult);
    				JSONArray tbodyArray = transformedResult.getJSONArray("tbodyList");
    				if(CollectionUtils.isNotEmpty(tbodyArray)) {
    					List<Map<String, Object>> tbodyList = new ArrayList<>();
    					for(int i = 0; i < tbodyArray.size(); i++) {
    						JSONObject rowData = tbodyArray.getJSONObject(i);
    						Integer pageSize = jsonObj.getInteger("pageSize");
    						pageSize = pageSize == null ? 10 : pageSize;
    						if(MapUtils.isNotEmpty(rowData)) {
    							Map<String, Object> rowDataMap = new HashMap<>();
    							for(Entry<String, Object> entry : rowData.entrySet()) {
    								rowDataMap.put(entry.getKey(), matrixService.matrixAttributeValueHandle(entry.getValue()));
    							}
    							tbodyList.add(rowDataMap);
    							if(tbodyList.size() >= pageSize) {
        							break;
        						}
    						}
    					}
    					returnObj.put("tbodyList", tbodyList);
    				}
    			}
    		}
        }
        
        List<ProcessMatrixDispatcherVo> dispatcherVoList = matrixMapper.getMatrixDispatcherByMatrixUuid(matrixUuid);
        returnObj.put("dispatcherVoList", dispatcherVoList);
        List<ProcessMatrixFormComponentVo> componentVoList = matrixMapper.getMatrixFormComponentByMatrixUuid(matrixUuid);
        returnObj.put("componentVoList", componentVoList);
        returnObj.put("usedCount", dispatcherVoList.size() + componentVoList.size());
		return returnObj;
	}

}
