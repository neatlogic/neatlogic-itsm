package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-27 17:49
 **/
@Service
public class MatrixNameUpdateApi extends ApiComponentBase {

    @Autowired
    private MatrixService matrixService;

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

    @Input({ @Param( name = "name", desc = "矩阵名称", type = ApiParamType.STRING, isRequired = true),
             @Param( name = "uuid", desc = "uuid", type = ApiParamType.STRING, isRequired = true)})
    @Description( desc = "矩阵名称变更接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessMatrixVo processMatrixVo = JSON.toJavaObject(jsonObj, ProcessMatrixVo.class);
        matrixService.updateMatrixName(processMatrixVo);
        return null;
    }
}
