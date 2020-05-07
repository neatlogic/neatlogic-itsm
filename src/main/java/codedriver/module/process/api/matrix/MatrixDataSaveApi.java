package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.exception.matrix.MatrixAttributeNotFoundException;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.util.UUIDUtil;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 15:26
 **/
@Service
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
    	if(matrixMapper.checkMatrixIsExists(matrixUuid) == 0) {
    		throw new MatrixNotFoundException(matrixUuid);
    	}

    	List<ProcessMatrixAttributeVo> attributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
    	List<String> attributeUuidList = attributeList.stream().map(ProcessMatrixAttributeVo::getUuid).collect(Collectors.toList());
    	List<ProcessMatrixColumnVo> rowData = new ArrayList<>();
    	JSONObject rowDataObj = jsonObj.getJSONObject("rowData");
    	for(String columnUuid : rowDataObj.keySet()) {
    		if(!"uuid".equals(columnUuid) && !"id".equals(columnUuid) && !attributeUuidList.contains(columnUuid)) {
    			throw new MatrixAttributeNotFoundException(matrixUuid, columnUuid);
    		}
    	}

    	String uuidValue = rowDataObj.getString("uuid");
    	if(uuidValue == null) {
    		rowData.add(new ProcessMatrixColumnVo("uuid", UUIDUtil.getUUID()));
    		for(ProcessMatrixAttributeVo processMatrixAttributeVo : attributeList) {
    			String value = rowDataObj.getString(processMatrixAttributeVo.getUuid());
    			if(value != null ) {
    				if(ProcessMatrixAttributeType.USER.getValue().equals(processMatrixAttributeVo.getType())) {
        				value = value.split("#")[1];
        			}else if(ProcessMatrixAttributeType.TEAM.getValue().equals(processMatrixAttributeVo.getType())) {
        				value = value.split("#")[1];
        			}else if(ProcessMatrixAttributeType.ROLE.getValue().equals(processMatrixAttributeVo.getType())) {
        				value = value.split("#")[1];
        			}
            		rowData.add(new ProcessMatrixColumnVo(processMatrixAttributeVo.getUuid(), value));
    			}
    		}
    		matrixDataMapper.insertDynamicTableData2(rowData, matrixUuid);
    	}else {
    		ProcessMatrixColumnVo uuidColumn = new ProcessMatrixColumnVo("uuid", uuidValue);
    		for(ProcessMatrixAttributeVo processMatrixAttributeVo : attributeList) {
    			String value = rowDataObj.getString(processMatrixAttributeVo.getUuid());
    			if(value != null ) {
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
    		matrixDataMapper.updateDynamicTableDataByUuid(rowData, uuidColumn, matrixUuid);
    	}
        return null;
    }
}
