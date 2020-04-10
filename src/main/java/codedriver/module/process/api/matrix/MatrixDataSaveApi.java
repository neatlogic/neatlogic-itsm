package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.auth.label.FORM_MODIFY;
import codedriver.module.process.service.MatrixDataService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 15:26
 **/
@Service
public class MatrixDataSaveApi extends ApiComponentBase {

    @Autowired
    private MatrixDataService matrixDataService;

    @Override
    public String getToken() {
        return "matrix/data/save";
    }

    @Override
    public String getName() {
        return "矩阵数据保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
             @Param( name = "dataList", desc = "矩阵数据集合", type = ApiParamType.JSONARRAY, isRequired = true)})
    @Description(desc = "矩阵数据保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        matrixDataService.saveDynamicTableData(jsonObj.getJSONArray("dataList"), jsonObj.getString("matrixUuid"));
        return null;
    }
}
