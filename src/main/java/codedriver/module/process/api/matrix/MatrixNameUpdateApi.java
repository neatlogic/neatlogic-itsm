package codedriver.module.process.api.matrix;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixNameRepeatException;
import codedriver.framework.process.exception.matrix.MatrixNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-27 17:49
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class MatrixNameUpdateApi extends PrivateApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/name/update";
    }

    @Override
    public String getName() {
        return "矩阵名称变更接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
    	@Param( name = "name", desc = "矩阵名称", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired= true, maxLength = 50),
        @Param( name = "uuid", desc = "uuid", type = ApiParamType.STRING, isRequired = true)
    })
    @Description( desc = "矩阵名称变更接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessMatrixVo processMatrixVo = JSON.toJavaObject(jsonObj, ProcessMatrixVo.class);
    	if(matrixMapper.checkMatrixIsExists(processMatrixVo.getUuid()) == 0) {
    		throw new MatrixNotFoundException(processMatrixVo.getUuid());
    	}
    	if(matrixMapper.checkMatrixNameIsRepeat(processMatrixVo) > 0){
    		throw new MatrixNameRepeatException(processMatrixVo.getName());
    	}
        processMatrixVo.setLcu(UserContext.get().getUserUuid(true));
        matrixMapper.updateMatrixNameAndLcu(processMatrixVo);
        return null;
    }
}
