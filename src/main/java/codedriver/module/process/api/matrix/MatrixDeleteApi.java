package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:03
 **/
@Service
public class MatrixDeleteApi extends ApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;

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
    	matrixMapper.deleteMatrixByUuid(jsonObj.getString("uuid"));
        return null;
    }
}
