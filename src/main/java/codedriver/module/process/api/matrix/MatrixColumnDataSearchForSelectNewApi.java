package codedriver.module.process.api.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixAttributeNotFoundException;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixDataService;

@Service
public class MatrixColumnDataSearchForSelectNewApi extends ApiComponentBase {
	
	@Autowired
	private MatrixDataService matrixDataService;

    @Autowired
    private MatrixMapper matrixMapper;
    
    @Autowired
    private MatrixAttributeMapper matrixAttributeMapper;

    @Autowired
    private MatrixDataMapper matrixDataMapper;

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
    	@Param( name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true), 
    	@Param(name = "keywordColumn", desc = "关键字属性uuid", type = ApiParamType.STRING),           
    	@Param( name = "columnList", desc = "属性uuid列表", type = ApiParamType.JSONARRAY, isRequired = true),
        @Param( name = "sourceColumnList", desc = "源属性集合", type = ApiParamType.JSONARRAY),
        @Param( name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER)
    	})
    @Description(desc = "矩阵属性数据查询-下拉级联接口")
    @Output({ @Param( name = "columnDataList", type = ApiParamType.JSONARRAY, desc = "属性数据集合")})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	ProcessMatrixDataVo dataVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessMatrixDataVo>() {});
        ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
        if(matrixVo == null) {
        	throw new MatrixNotFoundException(dataVo.getMatrixUuid());
        }      

        if (matrixVo.getType().equals(ProcessMatrixType.CUSTOM.getValue())){
            JSONObject returnObj = new JSONObject();
            List<ProcessMatrixAttributeVo> attributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
            if (CollectionUtils.isNotEmpty(attributeList)){
            	Map<String, ProcessMatrixAttributeVo> processMatrixAttributeMap = new HashMap<>();
            	for(ProcessMatrixAttributeVo processMatrixAttributeVo : attributeList) {
            		processMatrixAttributeMap.put(processMatrixAttributeVo.getUuid(), processMatrixAttributeVo);
            	}
            	List<String> columnList = dataVo.getColumnList();
            	for(String column : columnList) {
            		if(!processMatrixAttributeMap.containsKey(column)) {
            			throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
            		}
            	}
            	String keywordColumn = jsonObj.getString("keywordColumn");
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
            	List<Map<String, Object>> resultList = new ArrayList<>(dataMapList.size());
            	for(Map<String, String> dataMap : dataMapList) {
            		Map<String, Object> resultMap = new HashMap<>(dataMap.size());
            		for(Entry<String, String> entry : dataMap.entrySet()) {
            			String attributeUuid = entry.getKey();
            			resultMap.put(attributeUuid, matrixDataService.matrixAttributeValueHandle(processMatrixAttributeMap.get(attributeUuid), entry.getValue()));
            		}
            		resultList.add(resultMap);
            	}
                returnObj.put("columnDataList", resultList);
            }
            return returnObj;
            
        }else {
            //外部数据源矩阵  暂未实现
            return null;
        }
    }
}
