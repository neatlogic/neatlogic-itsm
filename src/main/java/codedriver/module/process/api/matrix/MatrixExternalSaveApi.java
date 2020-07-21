package codedriver.module.process.api.matrix;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixExternalMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixExternalException;
import codedriver.framework.process.exception.matrix.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class MatrixExternalSaveApi extends ApiComponentBase {

    @Autowired
    private MatrixExternalMapper externalMapper;

    @Autowired
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/external/save";
    }

    @Override
    public String getName() {
        return "外部数据源矩阵保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
    	@Param( name = "matrixUuid", type = ApiParamType.STRING, isRequired = true, desc = "矩阵uuid"),
    	@Param( name = "integrationUuid", type = ApiParamType.STRING, isRequired = true, desc = "集成设置uuid")
    })
    @Description(desc = "外部数据源矩阵保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessMatrixExternalVo externalVo = JSON.toJavaObject(jsonObj, ProcessMatrixExternalVo.class);
        ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(externalVo.getMatrixUuid());
        if(matrixVo == null) {
        	throw new MatrixNotFoundException(externalVo.getMatrixUuid());
        }
        
        if(ProcessMatrixType.EXTERNAL.getValue().equals(matrixVo.getType())) {
            if(externalMapper.getMatrixExternalIsExists(externalVo.getMatrixUuid()) == 0) {
            	externalMapper.insertMatrixExternal(externalVo);
            }else {
            	externalMapper.updateMatrixExternal(externalVo);
            }
        }else {
        	throw new MatrixExternalException("矩阵:'" + externalVo.getMatrixUuid() + "'不是外部数据源类型");
        }
        return null;
    }
}
