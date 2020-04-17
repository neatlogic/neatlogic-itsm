package codedriver.module.process.api.matrix;

import codedriver.framework.process.dto.MatrixExternalRequestVo;
import codedriver.framework.process.matrixrexternal.core.MatrixExternalRequestFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-04-03 18:43
 **/
@Service
public class MatrixExternalPluginListApi extends ApiComponentBase {

    @Override
    public String getToken() {
        return "matrix/external/plugin/list";
    }

    @Override
    public String getName() {
        return "矩阵外部数据源请求插件列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({
    	@Param(name = "Return", explode = MatrixExternalRequestVo[].class, desc = "矩阵外部数据源请求插件列表")
    })
    @Description(desc = "矩阵外部数据源请求插件列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return MatrixExternalRequestFactory.getRequestList();
    }
}
