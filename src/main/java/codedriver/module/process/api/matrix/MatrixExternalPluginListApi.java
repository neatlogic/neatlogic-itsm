package codedriver.module.process.api.matrix;

import codedriver.framework.process.dto.MatrixExternalRequestVo;
import codedriver.framework.process.matrixrexternal.core.MatrixExternalRequestFactory;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return "矩阵外部数据源请求插件集合";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        List<MatrixExternalRequestVo> requestVos = MatrixExternalRequestFactory.getRequestList();
        returnObj.put("pluginList", requestVos);
        return returnObj;
    }
}
