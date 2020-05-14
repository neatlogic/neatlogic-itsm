package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixExternalMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixExternalNotFoundException;
import codedriver.framework.process.exception.process.MatrixExternalException;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-04-03 19:06
 **/
@Service
public class MatrixExternalGetApi extends ApiComponentBase {

    @Autowired
    private MatrixExternalMapper externalMapper;

    @Autowired
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/external/get";
    }

    @Override
    public String getName() {
        return "外部数据源矩阵获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "matrixUuid", desc = "矩阵uuid", isRequired = true, type = ApiParamType.STRING)})
    @Description(desc = "外部数据源矩阵获取接口")
    @Output({ @Param( name = "Return", explode = ProcessMatrixExternalVo.class, desc = "外部矩阵数据源")})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	String matrixUuid = jsonObj.getString("matrixUuid");
    	ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if(matrixVo == null) {
        	throw new MatrixNotFoundException(matrixUuid);
        }
        
        if(ProcessMatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
        	ProcessMatrixExternalVo processMatrixExternalVo = externalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
        	if(processMatrixExternalVo == null) {
        		throw new MatrixExternalNotFoundException(matrixUuid);
        	}
            return processMatrixExternalVo;
        }else {
        	throw new MatrixExternalException("矩阵:'" + matrixUuid + "'不是外部数据源类型");
        }
    }
}
