package codedriver.module.process.service;

import codedriver.framework.process.dto.ProcessMatrixDataVo;
import com.alibaba.fastjson.JSONArray;

import java.util.List;
import java.util.Map;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 15:36
 **/
public interface MatrixDataService {

    public List<Map<String, String>> searchDynamicTableData(ProcessMatrixDataVo dataVo);

    public List<Map<String, String>> getDynamicTableColumnData(ProcessMatrixDataVo dataVo);

    public List<Map<String, String>> getDynamicTableDataByColumnList(ProcessMatrixDataVo dataVo);

    public List<String> getExternalMatrixColumnData();

    public void saveDynamicTableData(JSONArray dataArray, String matrixUuid);

    public void deleteDynamicTableData(String uuid, String matrixUuid);
}
