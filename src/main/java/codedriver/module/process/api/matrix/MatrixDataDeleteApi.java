package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixDataService;
import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.hdfs.security.token.block.DataEncryptionKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 16:29
 **/
@Service
public class MatrixDataDeleteApi extends ApiComponentBase {

    @Autowired
    private MatrixDataService dataService;

    @Override
    public String getToken() {
        return "matrix/data/delete";
    }

    @Override
    public String getName() {
        return "矩阵数据删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
             @Param( name = "uuid", desc = "矩阵数据uuid", type = ApiParamType.STRING, isRequired = true)})
    @Description(desc = "矩阵数据删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        dataService.deleteDynamicTableData(jsonObj.getString("uuid"), jsonObj.getString("matrixUuid"));
        return null;
    }
}
