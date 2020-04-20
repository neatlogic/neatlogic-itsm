package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.util.UUIDUtil;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
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
    	boolean isNewRow = true;
    	ProcessMatrixColumnVo uuidColumn = null;
    	List<ProcessMatrixColumnVo> rowData = new ArrayList<>();
    	JSONObject rowDataObj = jsonObj.getJSONObject("rowData");
    	for(Entry<String, Object> entry : rowDataObj.entrySet()) {
    		Object value = entry.getValue();
    		if(value == null) {
    			continue;
    		}
    		String column = entry.getKey();
    		if("uuid".equals(column)) {
    			if(value != null && StringUtils.isNotBlank(value.toString())) {
    				uuidColumn = new ProcessMatrixColumnVo(column, value.toString());
    				isNewRow = false;
    				continue;
    			}
    		}else if("id".equals(column)) {
				continue;
    		}
    		rowData.add(new ProcessMatrixColumnVo(column, value.toString()));
    	}
//    	Iterator<ProcessMatrixColumnVo> iterator = rowData.iterator();
//    	while(iterator.hasNext()) {
//    		ProcessMatrixColumnVo processMatrixColumnVo = iterator.next();
//    		if("uuid".equals(processMatrixColumnVo.getColumn())) {
//    			if(StringUtils.isBlank(processMatrixColumnVo.getValue())) {
//    				processMatrixColumnVo.setValue(UUIDUtil.getUUID());
//    				isNewRow = true;
//    			}else {
//    				uuidColumn = processMatrixColumnVo;
//    				iterator.remove();
//    			}
//    		}else if("id".equals(processMatrixColumnVo.getColumn())) {
//    			iterator.remove();
//    		}
//    	}
    		
    	if(isNewRow) {
    		rowData.add(new ProcessMatrixColumnVo("uuid", UUIDUtil.getUUID()));
    		matrixDataMapper.insertDynamicTableData2(rowData, matrixUuid);
    	}else {
    		matrixDataMapper.updateDynamicTableDataByUuid(rowData, uuidColumn, matrixUuid);
    	}
        return null;
    }
}
