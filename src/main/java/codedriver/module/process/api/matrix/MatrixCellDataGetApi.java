package codedriver.module.process.api.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.exception.matrix.MatrixAttributeNotFoundException;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class MatrixCellDataGetApi extends ApiComponentBase {

	@Autowired
	private MatrixMapper matrixMapper;
	
	@Autowired
	private MatrixDataMapper matrixDataMapper;
	
	@Autowired
	private MatrixAttributeMapper matrixAttributeMapper;
	
	@Override
	public String getToken() {
		return "matrix/celldata/get";
	}

	@Override
	public String getName() {
		return "矩阵单元格数据获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({
        @Param( name = "matrixUuid", type = ApiParamType.STRING, isRequired = true, desc = "矩阵uuid"),
        @Param( name = "sourceColumn", type = ApiParamType.STRING, isRequired = true, desc = "源列属性uuid"),
        @Param( name = "sourceColumnValueList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "源列属性值列表"),
        @Param( name = "targetColumn", type = ApiParamType.STRING, isRequired = true, desc = "目标列属性uuid")
    })
	@Output({ 
		@Param( name = "Return", type = ApiParamType.JSONARRAY, desc = "目标列属性值列表")
	})
	@Description( desc = "矩阵单元格数据获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String matrixUuid = jsonObj.getString("matrixUuid");
		if(matrixMapper.checkMatrixIsExists(matrixUuid) == 0) {
			throw new MatrixNotFoundException(matrixUuid);
		}
		List<ProcessMatrixAttributeVo> attributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
		List<String> attributeUuidList = attributeList.stream().map(ProcessMatrixAttributeVo::getUuid).collect(Collectors.toList());
		String sourceColumn = jsonObj.getString("sourceColumn");
		if(!attributeUuidList.contains(sourceColumn)) {
			throw new MatrixAttributeNotFoundException(matrixUuid, sourceColumn);
		}
		String targetColumn = jsonObj.getString("targetColumn");
		if(!attributeUuidList.contains(targetColumn)) {
			throw new MatrixAttributeNotFoundException(matrixUuid, targetColumn);
		}
		
		List<String> resultObj = new ArrayList<>();
		ProcessMatrixColumnVo sourceColumnVo = new ProcessMatrixColumnVo();
		sourceColumnVo.setColumn(sourceColumn);
		List<String> sourceColumnValueList = JSON.parseArray(jsonObj.getString("sourceColumnValueList"), String.class);
		for(String sourceColumnValue : sourceColumnValueList) {
			sourceColumnVo.setValue(sourceColumnValue);
			resultObj.add(matrixDataMapper.getDynamicTableCellData(matrixUuid, sourceColumnVo, targetColumn));
		}	
		return resultObj;
	}

}
