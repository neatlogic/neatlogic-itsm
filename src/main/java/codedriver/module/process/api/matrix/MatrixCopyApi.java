package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:03
 **/
@Service
@Transactional
public class MatrixCopyApi extends ApiComponentBase {

    @Autowired
    private MatrixService matrixService;

    @Autowired
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/copy";
    }

    @Override
    public String getName() {
        return "矩阵数据源复制接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param(name = "uuid", desc = "矩阵数据源uuid", isRequired = true, type = ApiParamType.STRING),
             @Param(name = "name", desc = "矩阵名称", type = ApiParamType.STRING)})
    @Description(desc = "矩阵数据源复制接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	String uuid = jsonObj.getString("uuid");
    	if(matrixMapper.checkMatrixIsExists(uuid) == 0) {
    		throw new MatrixNotFoundException(uuid);
    	}
        matrixService.copyMatrix(uuid, jsonObj.getString("name"));
        return null;
    }
}
