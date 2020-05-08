package codedriver.module.process.api.matrix;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
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
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixExternalNotFoundException;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixDataService;

@Service
public class MatrixColumnDataInitForTableApi extends ApiComponentBase {

	@Autowired
	private MatrixDataService matrixDataService;
	
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
    	@Param( name = "columnList", desc = "目标属性集合，数据按这个字段顺序返回", type = ApiParamType.JSONARRAY, isRequired = true),
        @Param( name = "uuidList", desc = "需要回显的数据uuid集合", type = ApiParamType.JSONARRAY),
    	@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Description(desc = "矩阵属性数据回显-table接口")
    @Output({
    	@Param( name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "属性数据集合"),
    	@Param( name = "theadList", type = ApiParamType.JSONARRAY, desc = "属性列名集合"),
    	@Param(explode = BasePageVo.class)
    	})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        ProcessMatrixDataVo dataVo = JSON.toJavaObject(jsonObj, ProcessMatrixDataVo.class);
        ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
        if(matrixVo == null) {
        	throw new MatrixNotFoundException(dataVo.getMatrixUuid());
        }
              
        if (matrixVo.getType().equals(ProcessMatrixType.CUSTOM.getValue())){
        	//theadList
        	JSONArray theadList = new JSONArray();
        	List<ProcessMatrixAttributeVo> matrixAttributeTheadList =  matrixAttributeMapper.getMatrixAttributeByMatrixUuidList(dataVo.getColumnList(), dataVo.getMatrixUuid());
        	for(String column :dataVo.getColumnList()) {
        		for(ProcessMatrixAttributeVo matrixAttributeSearch:matrixAttributeTheadList) {
            		if(matrixAttributeSearch.getUuid().equals(column)) {
            			theadList.add(matrixAttributeSearch);
            		}
            	}
        	}
        	returnObj.put("theadList", theadList);
        	//tbodyList
            List<Map<String, String>> dataMapList = matrixDataMapper.getDynamicTableDataByUuidList(dataVo);
            List<Map<String, Object>> tbodyList = matrixDataService.matrixTableDataValueHandle(matrixAttributeTheadList, dataMapList);
            returnObj.put("tbodyList", tbodyList);
            if(dataVo.getNeedPage()) {
    			int rowNum = matrixDataMapper.getDynamicTableDataByUuidCount(dataVo);
    			int pageCount = PageUtil.getPageCount(rowNum, dataVo.getPageSize());
    			returnObj.put("currentPage", dataVo.getCurrentPage());
    			returnObj.put("pageSize", dataVo.getPageSize());
    			returnObj.put("pageCount", pageCount);
    			returnObj.put("rowNum", rowNum);
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
    		IntegrationResultVo resultVo = handler.sendRequest(integrationVo);
    		if(resultVo != null && StringUtils.isNotBlank(resultVo.getTransformedResult())) {
    			JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
    			if(MapUtils.isNotEmpty(transformedResult)) {
    				returnObj.putAll(transformedResult);
    			}
    		}
        }
    	
        return returnObj;
    }
}
