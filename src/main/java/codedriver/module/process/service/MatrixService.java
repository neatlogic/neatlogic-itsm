package codedriver.module.process.service;

import codedriver.framework.process.dto.ProcessMatrixVo;

import java.util.List;

public interface MatrixService {

    public ProcessMatrixVo saveMatrix(ProcessMatrixVo matrixVo);

    public List<ProcessMatrixVo> searchMatrix(ProcessMatrixVo matrixVo);

    public int deleteMatrix(String uuid);

    public int updateMatrixName(ProcessMatrixVo matrixVo);

    public int copyMatrix(String matrixUuid);
}
