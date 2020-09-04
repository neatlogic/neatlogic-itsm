package codedriver.module.process.api.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
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
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixDispatcherVo;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.dto.ProcessMatrixFormComponentVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixExternalException;
import codedriver.framework.process.exception.matrix.MatrixNotFoundException;
import codedriver.framework.process.integration.handler.ProcessRequestFrom;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.MatrixService;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 16:34
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixDataSearchApi extends PrivateApiComponentBase {

	private final static Logger logger = LoggerFactory.getLogger(MatrixDataSearchApi.class);
			
    @Autowired
    private MatrixService matrixService;

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixAttributeMapper attributeMapper;

    @Autowired
    private MatrixDataMapper matrixDataMapper;
    
	@Autowired
	private IntegrationMapper integrationMapper;

    @Autowired
    private MatrixExternalMapper externalMapper;
    
    @Override
    public String getToken() {
        return "matrix/data/search";
    }

    @Override
    public String getName() {
        return "矩阵数据检索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "keyword", desc = "关键字", type = ApiParamType.STRING),
             @Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
             @Param( name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN),
             @Param( name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
             @Param( name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER)
    })
    @Output({ @Param( name = "tbodyList", desc = "矩阵数据集合"),
              @Param( name = "theadList", desc = "矩阵属性集合"),
              @Param( explode = BasePageVo.class)})
    @Description( desc = "矩阵数据检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        ProcessMatrixDataVo dataVo = JSON.toJavaObject(jsonObj, ProcessMatrixDataVo.class);
        ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
    	if(matrixVo == null) {
    		throw new MatrixNotFoundException(dataVo.getMatrixUuid());
    	}
    	List<Map<String, Object>> tbodyList = new ArrayList<>();
    	if(ProcessMatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
    		List<ProcessMatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
            if (CollectionUtils.isNotEmpty(attributeVoList)){
            	List<String> columnList = new ArrayList<>();
            	JSONArray headList = new JSONArray();
                JSONObject selectionObj = new JSONObject();
                selectionObj.put("key", "selection");
                selectionObj.put("width", 60);
                headList.add(selectionObj);
                for (ProcessMatrixAttributeVo attributeVo : attributeVoList){
                    columnList.add(attributeVo.getUuid());
                    JSONObject columnObj = new JSONObject();
                	columnObj.put("title", attributeVo.getName());
                	columnObj.put("key", attributeVo.getUuid());
                    headList.add(columnObj);
                }
                JSONObject actionObj = new JSONObject();
                actionObj.put("title", "");
                actionObj.put("key", "action");
                actionObj.put("align", "right");
                actionObj.put("width", 10);
                headList.add(actionObj);
                
                returnObj.put("theadList", headList);
                
                dataVo.setColumnList(columnList);
                if (dataVo.getNeedPage()){
                    int rowNum = matrixDataMapper.getDynamicTableDataCount(dataVo);
                    returnObj.put("pageCount", PageUtil.getPageCount(rowNum, dataVo.getPageSize()));
                    returnObj.put("rowNum", rowNum);
                    returnObj.put("pageSize", dataVo.getPageSize());
                    returnObj.put("currentPage", dataVo.getCurrentPage());
                }
                
                List<Map<String, String>> dataList = matrixDataMapper.searchDynamicTableData(dataVo);
                tbodyList = matrixService.matrixTableDataValueHandle(attributeVoList, dataList);
            }
    	}else {
    		ProcessMatrixExternalVo externalVo = externalMapper.getMatrixExternalByMatrixUuid(dataVo.getMatrixUuid());
            if(externalVo != null) {
            	IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
                IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
        		if (handler == null) {
        			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
        		}
        		
            	integrationVo.getParamObj().putAll(jsonObj);
        		IntegrationResultVo resultVo = handler.sendRequest(integrationVo,ProcessRequestFrom.MATRIX);
        		if(StringUtils.isNotBlank(resultVo.getError())) {
        			logger.error(resultVo.getError());
            		throw new MatrixExternalException("外部接口访问异常");
            	}else if(StringUtils.isNotBlank(resultVo.getTransformedResult())) {
        			JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
        			if(MapUtils.isNotEmpty(transformedResult)) {
        				returnObj.putAll(transformedResult);
        				JSONArray tbodyArray = transformedResult.getJSONArray("tbodyList");
        				if(CollectionUtils.isNotEmpty(tbodyArray)) {
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
        				}
        			}
        		}
            }
    	}

		returnObj.put("tbodyList", tbodyList);
        List<ProcessMatrixDispatcherVo> dispatcherVoList = matrixMapper.getMatrixDispatcherByMatrixUuid(dataVo.getMatrixUuid());
        returnObj.put("dispatcherVoList", dispatcherVoList);
        List<ProcessMatrixFormComponentVo> componentVoList = matrixMapper.getMatrixFormComponentByMatrixUuid(dataVo.getMatrixUuid());
        returnObj.put("componentVoList", componentVoList);
        returnObj.put("usedCount", dispatcherVoList.size() + componentVoList.size());
        return returnObj;    	
    }
}
