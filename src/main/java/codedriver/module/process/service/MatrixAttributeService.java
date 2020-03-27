package codedriver.module.process.service;

import codedriver.framework.process.dto.ProcessMatrixAttributeVo;

import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-27 18:16
 **/
public interface MatrixAttributeService {

    public void saveMatrixAttribute(List<ProcessMatrixAttributeVo> attributeVoList);
}
