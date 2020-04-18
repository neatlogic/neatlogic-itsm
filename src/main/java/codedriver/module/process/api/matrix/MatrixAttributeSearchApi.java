package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
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
    	if(matrixMapper.checkMatrixIsExists(matrixUuid) == 0) {
    		throw new MatrixNotFoundException(matrixUuid);
    	}
    	List<ProcessMatrixAttributeVo> processMatrixAttributeList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
    	if(CollectionUtils.isNotEmpty(processMatrixAttributeList)) {
    		List<String> columnList = processMatrixAttributeList.stream().map(ProcessMatrixAttributeVo :: getUuid).collect(Collectors.toList());
    		ProcessMatrixDataVo dataVo = new ProcessMatrixDataVo();
    		dataVo.setMatrixUuid(matrixUuid);
    		dataVo.setColumnList(columnList);
    		dataVo.setNeedPage(false);
    		
    		List<Map<String, String>> matrixRowDataList = matrixDataMapper.searchDynamicTableData(dataVo);
    		if(CollectionUtils.isNotEmpty(matrixRowDataList)) {
    			Map<String, List<String>> attributeDataListMap = new HashMap<>();
    			for(Map<String, String> rowData : matrixRowDataList) {
        			for(Entry<String, String> columnData : rowData.entrySet()) {
        				if("id".equals(columnData.getKey())) {
        					continue;
        				}
        				if(StringUtils.isNotBlank(columnData.getValue())) {
        					List<String> attributeDataList = attributeDataListMap.get(columnData.getKey());
        					if(attributeDataList == null) {
        						attributeDataList = new ArrayList<>();
        						attributeDataListMap.put(columnData.getKey(), attributeDataList);
        					}
        					attributeDataList.add(columnData.getValue());
        				}
        			}
        		}
    			for(ProcessMatrixAttributeVo processMatrixAttributeVo : processMatrixAttributeList) {
    				if(CollectionUtils.isNotEmpty(attributeDataListMap.get(processMatrixAttributeVo.getUuid()))) {
    					processMatrixAttributeVo.setIsDeletable(0);
    				}
    			}
    		}
    	}
        return processMatrixAttributeList;
    }
}
