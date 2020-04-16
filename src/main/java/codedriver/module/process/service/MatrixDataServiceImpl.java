package codedriver.module.process.service;

import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.module.process.util.UUIDUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 15:36
 **/
@Transactional
@Service
public class MatrixDataServiceImpl implements MatrixDataService {

    @Autowired
    private MatrixAttributeMapper attributeMapper;

    @Autowired
    private MatrixDataMapper matrixDataMapper;

    @Override
    public void saveDynamicTableData(JSONArray collection, String matrixUuid) {
        for (int i = 0; i < collection.size(); i++){
            JSONArray array = collection.getJSONArray(i);
            List<String> columnList = new ArrayList<>();
            List<String> dataList = new ArrayList<>();
            for (int j = 0; j < array.size(); j++){
                JSONObject dataObj = array.getJSONObject(j);
                String column = dataObj.getString("key");
                String data = dataObj.getString("value");
                if (("uuid").equals(column)){
                    if (StringUtils.isBlank(data)){
                        data = UUIDUtil.getUUID();
                    }else {
                        matrixDataMapper.deleteDynamicTableDataByUuid(matrixUuid, data);
                    }
                }
                columnList.add(column);
                dataList.add(data);
            }
            matrixDataMapper.insertDynamicTableData(columnList, dataList, matrixUuid);
        }
    }

    @Override
    public void deleteDynamicTableData(String uuid, String matrixUuid) {
        matrixDataMapper.deleteDynamicTableDataByUuid(matrixUuid, uuid);
    }

    @Override
    public List<Map<String, String>> searchDynamicTableData(ProcessMatrixDataVo dataVo) {
        List<ProcessMatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
        if (CollectionUtils.isNotEmpty(attributeVoList)){
        	List<String> columnList = new ArrayList<>();
            List<ProcessMatrixColumnVo> sourceColumnList = new ArrayList<>();
            for (ProcessMatrixAttributeVo attributeVo : attributeVoList){
                columnList.add(attributeVo.getUuid());
                sourceColumnList.add(new ProcessMatrixColumnVo(attributeVo.getUuid(), attributeVo.getName()));
            }
            dataVo.setColumnList(columnList);
            dataVo.setSourceColumnList(sourceColumnList);
            if (dataVo.getNeedPage()){
                int rowNum = matrixDataMapper.getDynamicTableDataCount(dataVo);
                dataVo.setRowNum(rowNum);
                dataVo.setPageCount(PageUtil.getPageCount(rowNum, dataVo.getPageSize()));
            }
            return matrixDataMapper.searchDynamicTableData(dataVo);
        }
        return null;
    }

    @Override
    public List<Map<String, String>> getDynamicTableDataByColumnList(ProcessMatrixDataVo dataVo) {
        return matrixDataMapper.getDynamicTableDataByColumnList(dataVo);
    }

    @Override
    public List<String> getExternalMatrixColumnData() {
        return null;
    }
}
