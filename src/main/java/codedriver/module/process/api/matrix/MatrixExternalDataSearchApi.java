package codedriver.module.process.api.matrix;

import java.util.List;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
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
import codedriver.framework.process.exception.matrix.MatrixExternalNotFoundException;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class MatrixExternalDataSearchApi extends ApiComponentBase {

//    @Autowired
//    private MatrixService matrixService;
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
        @Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
        @Param( name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
        @Param( name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER)
	})
	@Output({ @Param( name = "tbodyList", desc = "矩阵数据集合"),
	         @Param( name = "theadList", desc = "矩阵属性集合"),
	         @Param( explode = BasePageVo.class)})
	@Description( desc = "矩阵数据检索接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String matrixUuid = jsonObj.getString("matrixUuid");
		ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if(matrixVo == null) {
        	throw new MatrixNotFoundException(matrixUuid);
        }
        ProcessMatrixExternalVo externalVo = externalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
        if(externalVo == null) {
        	throw new MatrixExternalNotFoundException(matrixUuid);
        }
        IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
        IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
		if (handler == null) {
			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
		}
		JSONObject returnObj = new JSONObject();
		IntegrationResultVo resultVo = handler.sendRequest(integrationVo);
		if(resultVo != null && StringUtils.isNotBlank(resultVo.getTransformedResult())) {
			JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
			if(MapUtils.isNotEmpty(transformedResult)) {
				returnObj.putAll(transformedResult);
			}
		}
//        String plugin = externalVo.getPlugin();
//        IMatrixExternalRequestHandler requestHandler = MatrixExternalRequestFactory.getHandler(plugin);
//        if(requestHandler == null) {
//        	throw new MatrixExternalRequestHandlerNotFoundException(plugin);
//        }
//        JSONObject returnObj = new JSONObject();
//        JSONObject externalObj = JSONObject.parseObject(externalVo.getConfig());
//        if(MapUtils.isNotEmpty(externalObj)) {
//        	JSONArray columnList = externalObj.getJSONArray("columnList");
//        	if(CollectionUtils.isNotEmpty(columnList)) {
//        		String url = externalObj.getString("url");
//                if(StringUtils.isNotBlank(url)) {
//                	String rootName = externalObj.getString("rootName");
//                	if(externalObj.getBooleanValue("needPage")) {
//                    	String currentPageKey = externalObj.getString("currentPage");
//                    	String pageSizeKey = externalObj.getString("pageSize");
//                    	Integer currentPage = jsonObj.getInteger("currentPage");
//                    	currentPage = currentPage == null ? 1 : currentPage;
//                    	Integer pageSize = jsonObj.getInteger("pageSize");
//                    	pageSize = pageSize == null ? 10 : pageSize;
//                    	if(url.contains("?")) {
//                    		url = url + "&" + currentPageKey + "=" + currentPage + "&" + pageSizeKey + "=" + pageSize;
//                    	}else {
//                    		url = url + "?" + currentPageKey + "=" + currentPage + "&" + pageSizeKey + "=" + pageSize;
//                    	}
//                	}
//                    JSONObject result = requestHandler.dataHandler(url, rootName, externalObj);
//                    JSONArray dataArray = result.getJSONArray("tbodyList");
//                    if (CollectionUtils.isNotEmpty(dataArray)){
//                        JSONArray theadList = new JSONArray();
//                        Map<String, String> targetAndSourceColumnMap = new HashMap<>();
//                        for (int i = 0; i < columnList.size(); i++){
//                            JSONObject obj = columnList.getJSONObject(i);
//                            JSONObject theadObj = new JSONObject();
//                            String sourceColumn = obj.getString("sourceColumn");
//                            String targetColumn = obj.getString("targetColumn");
//                            theadObj.put("title", obj.getString("title"));
//                            theadObj.put("key", targetColumn);
//                            theadList.add(theadObj);
//                            if(!Objects.equals(sourceColumn, targetColumn)) {
//                            	targetAndSourceColumnMap.put(targetColumn, sourceColumn);
//                            }
//                        }
//                        returnObj.put("theadList", theadList);
//                        for (int i = 0; i < dataArray.size(); i++){
//                            JSONObject dataObj = dataArray.getJSONObject(i);
//                            for(Entry<String, String> entry : targetAndSourceColumnMap.entrySet()) {
//                            	dataObj.put(entry.getKey(), dataObj.get(entry.getValue()));
//                            }
//                        }
//                        returnObj.put("tbodyList", dataArray);
//                    }
//                    if(externalObj.getBooleanValue("needPage")){
//                    	returnObj.put("rowNum", result.getString("rowNum"));
//                    	returnObj.put("pageCount", result.getString("pageCount"));
//                    	returnObj.put("currentPage", result.getString("currentPage"));
//                    	returnObj.put("pageSize", result.getString("pageSize"));
//                    }
//                }
//        	}            
//        }
        List<ProcessMatrixDispatcherVo> dispatcherVoList = matrixMapper.getMatrixDispatcherByMatrixUuid(matrixUuid);
        returnObj.put("dispatcherVoList", dispatcherVoList);
        List<ProcessMatrixFormComponentVo> componentVoList = matrixMapper.getMatrixFormComponentByMatrixUuid(matrixUuid);
        returnObj.put("componentVoList", componentVoList);
        returnObj.put("usedCount", dispatcherVoList.size() + componentVoList.size());
		return returnObj;
	}

}
