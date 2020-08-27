package codedriver.module.process.api.matrix;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixExternalMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:03
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class MatrixDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;
    
    @Autowired
    private MatrixExternalMapper matrixExternalMapper;
    
    @Autowired
    private MatrixAttributeMapper matrixAttributeMapper;

    @Override
    public String getToken() {
        return "matrix/delete";
    }

    @Override
    public String getName() {
        return "矩阵删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "uuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true)})
    @Description(desc = "矩阵删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	String uuid = jsonObj.getString("uuid");
    	ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(uuid);
    	if(matrixVo != null) {
        	matrixMapper.deleteMatrixByUuid(uuid);
        	if(ProcessMatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
        		matrixAttributeMapper.deleteAttributeByMatrixUuid(uuid);
        		matrixAttributeMapper.dropMatrixDynamicTable(uuid);
        	}else {
        		matrixExternalMapper.deleteMatrixExternalByMatrixUuid(uuid);
        	}
    	}
        return null;
    }
}
