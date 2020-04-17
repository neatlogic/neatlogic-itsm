package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
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
 * @create: 2020-03-26 19:06
 **/
@Service
public class MatrixAttributeSearchApi extends ApiComponentBase {

    @Autowired
    private MatrixAttributeMapper attributeMapper;

    @Autowired
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/attribute/search";
    }

    @Override
    public String getName() {
        return "矩阵属性检索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true)})
    @Output( {@Param( name = "Return", desc = "矩阵属性集合", explode = ProcessMatrixAttributeVo[].class)})
    @Description( desc = "矩阵属性检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String matrixUuid = jsonObj.getString("matrixUuid");
    	if(matrixMapper.checkMatrixIsExists(matrixUuid) == 0) {
    		throw new MatrixNotFoundException(matrixUuid);
    	}
        return attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
    }
}
