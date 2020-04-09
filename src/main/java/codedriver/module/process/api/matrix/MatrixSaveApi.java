package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import codedriver.framework.process.dto.ProcessMatrixVo;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:02
 **/
@Service
public class MatrixSaveApi extends ApiComponentBase {

    @Autowired
    private MatrixService matrixService;

    @Override
    public String getToken() {
        return "matrix/save";
    }

    @Override
    public String getName() {
        return "数据源矩阵保存";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "name", type = ApiParamType.STRING, desc = "矩阵名称", isRequired = true, xss = true),
             @Param( name = "type", type = ApiParamType.STRING, desc = "矩阵类型", isRequired = true),
             @Param( name = "uuid", type = ApiParamType.STRING, desc = "矩阵uuid"),
             @Param( name = "externalConfig", type = ApiParamType.STRING, desc = "矩阵外部数据源配置")})
    @Output({
            @Param( name = "matrix", explode = ProcessMatrixVo.class, desc = "矩阵数据源")
    })
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        ProcessMatrixVo matrixVo = JSON.toJavaObject(jsonObj, ProcessMatrixVo.class);
        ProcessMatrixVo matrix = matrixService.saveMatrix(matrixVo);
        returnObj.put("matrix", matrix);
        return returnObj;
    }
}
