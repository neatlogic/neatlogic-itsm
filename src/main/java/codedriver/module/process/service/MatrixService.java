package codedriver.module.process.service;

import codedriver.framework.process.dto.ProcessMatrixVo;
import com.alibaba.fastjson.JSONObject;

public interface MatrixService {

    public ProcessMatrixVo saveMatrix(ProcessMatrixVo matrixVo);

    public int copyMatrix(String matrixUuid, String name);

    public JSONObject getMatrixExternalData(String matrixUuid);

}
