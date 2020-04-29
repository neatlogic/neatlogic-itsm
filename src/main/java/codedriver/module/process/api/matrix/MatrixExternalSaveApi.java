package codedriver.module.process.api.matrix;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.MatrixExternalMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class MatrixExternalSaveApi extends ApiComponentBase {

    @Autowired
    private MatrixExternalMapper externalMapper;

    @Autowired
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/external/save";
    }

    @Override
    public String getName() {
        return "外部数据源矩阵保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
    	@Param( name = "matrixUuid", type = ApiParamType.STRING, isRequired = true, desc = "矩阵uuid"),
    	@Param( name = "plugin", type = ApiParamType.STRING, isRequired = true, desc = "插件"),
        @Param( name = "config", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "矩阵外部数据源配置")
    })
    @Description(desc = "外部数据源矩阵保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessMatrixExternalVo externalVo = JSON.toJavaObject(jsonObj, ProcessMatrixExternalVo.class);
        if(matrixMapper.checkMatrixIsExists(externalVo.getMatrixUuid()) == 0) {
    		throw new MatrixNotFoundException(externalVo.getMatrixUuid());
    	}
        if(externalMapper.getMatrixExternalIsExists(externalVo.getMatrixUuid()) == 0) {
        	externalMapper.insertMatrixExternal(externalVo);
        }else {
        	externalMapper.updateMatrixExternal(externalVo);
        }
        return null;
    }
}
