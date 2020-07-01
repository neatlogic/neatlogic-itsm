package codedriver.module.process.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixAttributeNotFoundException;
import codedriver.framework.process.exception.matrix.MatrixExternalException;
import codedriver.framework.process.exception.matrix.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.util.UUIDUtil;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 15:26
 **/
@Service
@Transactional
public class MatrixDataSaveApi extends ApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;
    
    @Autowired
    private MatrixAttributeMapper matrixAttributeMapper;

    @Autowired
    private MatrixDataMapper matrixDataMapper;

    @Override
    public String getToken() {
        return "matrix/data/save";
    }

    @Override
    public String getName() {
        return "矩阵数据保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ 
    	@Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
        @Param( name = "rowData", desc = "矩阵数据中的一行数据", type = ApiParamType.JSONOBJECT, isRequired = true)
    	
    })
    @Description(desc = "矩阵数据保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	String matrixUuid = jsonObj.getString("matrixUuid");
    	ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
		if(matrixVo == null) {
			throw new MatrixNotFoundException(matrixUuid);
		}
		if(ProcessMatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
			List<ProcessMatrixAttributeVo> attributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
	    	List<String> attributeUuidList = attributeList.stream().map(ProcessMatrixAttributeVo::getUuid).collect(Collectors.toList());
	    	JSONObject rowDataObj = jsonObj.getJSONObject("rowData");
	    	for(String columnUuid : rowDataObj.keySet()) {
	    		if(!"uuid".equals(columnUuid) && !"id".equals(columnUuid) && !attributeUuidList.contains(columnUuid)) {
	    			throw new MatrixAttributeNotFoundException(matrixUuid, columnUuid);
	    		}
	    	}

	    	boolean hasData = false;
	    	List<ProcessMatrixColumnVo> rowData = new ArrayList<>();
	    	for(ProcessMatrixAttributeVo processMatrixAttributeVo : attributeList) {
    			String value = rowDataObj.getString(processMatrixAttributeVo.getUuid());
    			if(StringUtils.isNotBlank(value)) {
    				hasData = true;
    				if(ProcessMatrixAttributeType.USER.getValue().equals(processMatrixAttributeVo.getType())) {
        				value = value.split("#")[1];
        			}else if(ProcessMatrixAttributeType.TEAM.getValue().equals(processMatrixAttributeVo.getType())) {
        				value = value.split("#")[1];
        			}else if(ProcessMatrixAttributeType.ROLE.getValue().equals(processMatrixAttributeVo.getType())) {
        				value = value.split("#")[1];
        			}
    			}
        		rowData.add(new ProcessMatrixColumnVo(processMatrixAttributeVo.getUuid(), value));
    		}
	    	String uuidValue = rowDataObj.getString("uuid");
	    	if(uuidValue == null) {
	    		if(hasData) {
		    		rowData.add(new ProcessMatrixColumnVo("uuid", UUIDUtil.getUUID()));    		
		    		matrixDataMapper.insertDynamicTableData(rowData, matrixUuid);    			
	    		}
	    	}else {
	    		if(hasData) {
		    		ProcessMatrixColumnVo uuidColumn = new ProcessMatrixColumnVo("uuid", uuidValue);
		    		matrixDataMapper.updateDynamicTableDataByUuid(rowData, uuidColumn, matrixUuid);    			
	    		}else {
	    			matrixDataMapper.deleteDynamicTableDataByUuid(matrixUuid, uuidValue);
	    		}
	    	}
		}else {
			throw new MatrixExternalException("矩阵外部数据源没有保存一行数据操作");
		}
    	
        return null;
    }
}
