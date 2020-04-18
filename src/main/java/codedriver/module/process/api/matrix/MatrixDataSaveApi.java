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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.Iterator;
import java.util.List;

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
        @Param( name = "rowData", desc = "矩阵数据中的一行数据", type = ApiParamType.JSONARRAY, isRequired = true),
        @Param( name = "rowData[x].column", desc = "列uuid", type = ApiParamType.STRING),
        @Param( name = "rowData[x].value", desc = "列数据", type = ApiParamType.STRING)
    	
    })
    @Description(desc = "矩阵数据保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	String matrixUuid = jsonObj.getString("matrixUuid");
    	if(matrixMapper.checkMatrixIsExists(matrixUuid) == 0) {
    		throw new MatrixNotFoundException(matrixUuid);
    	}
    	boolean isNewRow = false;
    	ProcessMatrixColumnVo uuidColumn = null;
    	List<ProcessMatrixColumnVo> rowData = JSON.parseArray(jsonObj.getString("rowData"), ProcessMatrixColumnVo.class);
    	Iterator<ProcessMatrixColumnVo> iterator = rowData.iterator();
    	while(iterator.hasNext()) {
    		ProcessMatrixColumnVo processMatrixColumnVo = iterator.next();
    		if("uuid".equals(processMatrixColumnVo.getColumn())) {
    			if(StringUtils.isBlank(processMatrixColumnVo.getValue())) {
    				processMatrixColumnVo.setValue(UUIDUtil.getUUID());
    				isNewRow = true;
    			}else {
    				uuidColumn = processMatrixColumnVo;
    				iterator.remove();
    			}
    		}else if("id".equals(processMatrixColumnVo.getColumn())) {
    			iterator.remove();
    		}
    	}
    		
    	if(isNewRow) {
    		matrixDataMapper.insertDynamicTableData2(rowData, matrixUuid);
    	}else {
    		matrixDataMapper.updateDynamicTableDataByUuid(rowData, uuidColumn, matrixUuid);
    	}
        return null;
    }
}
