package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-27 16:52
 **/
@Service
public class MatrixTypeApi extends ApiComponentBase {

    @Override
    public String getToken() {
        return "matrix/type";
    }

    @Override
    public String getName() {
        return "矩阵类型返回接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({ @Param( name = "typeList", desc = "矩阵类型返回列表", type = ApiParamType.JSONARRAY)})
    @Description(desc = "矩阵类型返回接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        JSONArray typeArray = new JSONArray();
        for (ProcessMatrixType type : ProcessMatrixType.values()){
            JSONObject typeObj = new JSONObject();
            typeObj.put("text", type.getName());
            typeObj.put("value", type.getValue());
            typeArray.add(typeObj);
        }
        returnObj.put("typeList", typeArray);
        return returnObj;
    }
}
