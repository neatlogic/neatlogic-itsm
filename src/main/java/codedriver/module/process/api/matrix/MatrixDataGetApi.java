package codedriver.module.process.api.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixExternalException;
import codedriver.framework.process.exception.matrix.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class MatrixDataGetApi extends ApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;
    
    @Autowired
    private MatrixDataMapper matrixDataMapper;

    @Autowired
    private MatrixAttributeMapper attributeMapper;

	@Override
	public String getToken() {
		return "matrix/data/get";
	}

	@Override
	public String getName() {
		return "矩阵一行数据获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
		@Param( name = "matrixUuid", type = ApiParamType.STRING, isRequired = true, desc = "矩阵uuid"),
		@Param( name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "行uuid")	
	})
	@Output({
		@Param(name = "Return", type = ApiParamType.JSONOBJECT, desc = "一行数据")
	})
	@Description( desc = "矩阵一行数据获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessMatrixDataVo dataVo = JSON.toJavaObject(jsonObj, ProcessMatrixDataVo.class);
		ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
		if(matrixVo == null) {
			throw new MatrixNotFoundException(dataVo.getMatrixUuid());
		}
		if(ProcessMatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
			List<ProcessMatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
	        if (CollectionUtils.isNotEmpty(attributeVoList)){
	        	List<String> columnList = new ArrayList<>();
	            for (ProcessMatrixAttributeVo attributeVo : attributeVoList){
	                columnList.add(attributeVo.getUuid());
	            }
	    		dataVo.setColumnList(columnList);
	        	Map<String, String> rowData = matrixDataMapper.getDynamicRowDataByUuid(dataVo);
	        	for (ProcessMatrixAttributeVo attributeVo : attributeVoList){
	        		if(ProcessMatrixAttributeType.USER.getValue().equals(attributeVo.getType())) {
	        			String value = rowData.get(attributeVo.getUuid());
	        			if(value != null) {
	        				value = GroupSearch.USER.getValuePlugin() + value;
	        				rowData.put(attributeVo.getUuid(), value);
	        			}
	        		}else if(ProcessMatrixAttributeType.TEAM.getValue().equals(attributeVo.getType())) {
	        			String value = rowData.get(attributeVo.getUuid());
	        			if(value != null) {
	        				value = GroupSearch.TEAM.getValuePlugin() + value;
	        				rowData.put(attributeVo.getUuid(), value);
	        			}
	        		}else if(ProcessMatrixAttributeType.ROLE.getValue().equals(attributeVo.getType())) {
	        			String value = rowData.get(attributeVo.getUuid());
	        			if(value != null) {
	        				value = GroupSearch.ROLE.getValuePlugin() + value;
	        				rowData.put(attributeVo.getUuid(), value);
	        			}
	        		}
	        	}
	        	return rowData;
	        }
		}else {
			throw new MatrixExternalException("矩阵外部数据源没有获取一行数据操作");
		}

    	
		return null;
	}

}
