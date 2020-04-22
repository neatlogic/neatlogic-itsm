package codedriver.module.process.service;

import codedriver.framework.process.dto.ProcessMatrixDataVo;

import java.util.List;
import java.util.Map;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 15:36
 **/
public interface MatrixDataService {

    public List<Map<String, String>> searchDynamicTableData(ProcessMatrixDataVo dataVo);

    public List<String> getExternalMatrixColumnData();
}
