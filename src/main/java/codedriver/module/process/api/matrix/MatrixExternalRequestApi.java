package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.matrixrexternal.core.IMatrixExternalRequestHandler;
import codedriver.framework.process.matrixrexternal.core.MatrixExternalRequestFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-04-03 15:10
 **/
@Service
public class MatrixExternalRequestApi extends ApiComponentBase {

    @Override
    public String getToken() {
        return "matrix/external/request";
    }

    @Override
    public String getName() {
        return "矩阵外部数据源请求访问接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "url", desc = "访问地址", isRequired = true, type = ApiParamType.STRING),
             @Param( name = "pluginId", desc = "插件id", isRequired = true, type = ApiParamType.STRING),
             @Param( name = "root", desc = "根属性", type = ApiParamType.STRING, isRequired = true),
             @Param( name = "config", desc = "配置信息", type = ApiParamType.STRING)})
    @Description(desc = "矩阵外部数据源请求访问接口")
    @Output({ @Param(name = "attributeList", type = ApiParamType.JSONARRAY, desc = "属性集合")})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject configObj = JSON.parseObject(jsonObj.getString("config"));
        IMatrixExternalRequestHandler requestHandler = MatrixExternalRequestFactory.getHandler(jsonObj.getString("pluginId"));
        return requestHandler.attributeHandler(configObj.getString("url"), jsonObj.getString("root"), configObj);
    }
}
