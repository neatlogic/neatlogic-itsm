package codedriver.module.process.service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-27 11:35
 **/
public class MatrixServiceImpl implements MatrixService {

    @Autowired
    private MatrixMapper matrixMapper;

    @Override
    public ProcessMatrixVo saveMatrix(ProcessMatrixVo matrixVo) {
        matrixVo.setLcu(UserContext.get().getUserId());
        if (StringUtils.isNotBlank(matrixVo.getUuid())){
            matrixMapper.updateMatrixNameAndLcu(matrixVo);
        }else {
            matrixVo.setFcu(UserContext.get().getUserId());
            matrixVo.setUuid(UUID.randomUUID().toString().replace("-", ""));
            matrixMapper.insertMatrix(matrixVo);
        }
        return matrixVo;
    }

    @Override
    public List<ProcessMatrixVo> searchMatrix(ProcessMatrixVo matrixVo) {
        if (matrixVo.getNeedPage()){
            int rowNum = matrixMapper.searchMatrixCount(matrixVo);
            matrixVo.setRowNum(rowNum);
            matrixVo.setPageCount(PageUtil.getPageCount(rowNum, matrixVo.getPageSize()));
        }
        return matrixMapper.searchMatrix(matrixVo);
    }

    @Override
    public int deleteMatrix(String uuid) {
        matrixMapper.deleteMatrixByUuid(uuid);
        return 0;
    }

    @Override
    public int updateMatrixName(ProcessMatrixVo matrixVo) {
        matrixVo.setLcu(UserContext.get().getUserId());
        matrixMapper.updateMatrixNameAndLcu(matrixVo);
        return 0;
    }

    @Override
    public int copyMatrix(String matrixUuid) {
        ProcessMatrixVo sourceMatrix = matrixMapper.getMatrixByUuid(matrixUuid);
        sourceMatrix.setFcu(UserContext.get().getUserId());
        sourceMatrix.setLcu(UserContext.get().getUserId());
        sourceMatrix.setUuid(UUID.randomUUID().toString().replace("-", ""));
        matrixMapper.insertMatrix(sourceMatrix);
        return 0;
    }
}
