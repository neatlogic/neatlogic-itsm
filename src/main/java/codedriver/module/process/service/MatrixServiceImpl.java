package codedriver.module.process.service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-27 11:35
 **/
@Service
@Transactional
public class MatrixServiceImpl implements MatrixService {

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixAttributeMapper matrixAttributeMapper;

    @Autowired
    private MatrixDataMapper matrixDataMapper;

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
    public int copyMatrix(String matrixUuid, String name) {
        ProcessMatrixVo sourceMatrix = matrixMapper.getMatrixByUuid(matrixUuid);
        if (StringUtils.isNotBlank(name)){
            sourceMatrix.setName(name);
        }else {
            sourceMatrix.setName(sourceMatrix.getName() + "_copy");
        }
        sourceMatrix.setFcu(UserContext.get().getUserId());
        sourceMatrix.setLcu(UserContext.get().getUserId());
        String targetUuid = UUID.randomUUID().toString().replace("-", "");
        sourceMatrix.setUuid(targetUuid);
        matrixMapper.insertMatrix(sourceMatrix);
        copyMatrixAttributeAndData(matrixUuid, targetUuid);
        return 0;
    }

    public void copyMatrixAttributeAndData(String sourceMatrixUuid, String targetMatrixUuid){
        List<ProcessMatrixAttributeVo> attributeVoList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(sourceMatrixUuid);
        if (CollectionUtils.isNotEmpty(attributeVoList)){
            //属性拷贝
            List<String> sourceColumnList = new ArrayList<>();
            Map<String, String> compareMap = new HashMap<>();
            for (ProcessMatrixAttributeVo attributeVo : attributeVoList){
                String sourceUuid = attributeVo.getUuid();
                String targetUuid = UUID.randomUUID().toString().replace("-", "");
                sourceColumnList.add(sourceUuid);
                compareMap.put(sourceUuid, targetUuid);
                attributeVo.setMatrixUuid(targetMatrixUuid);
                attributeVo.setUuid(targetUuid);
                matrixAttributeMapper.insertMatrixAttribute(attributeVo);
            }

            if (matrixAttributeMapper.checkMatrixAttributeTableExist("matrix_" + targetMatrixUuid) == 0){
                matrixAttributeMapper.createMatrixDynamicTable(attributeVoList, targetMatrixUuid);
            }
            //数据拷贝
            ProcessMatrixDataVo sourceDataVo = new ProcessMatrixDataVo();
            sourceDataVo.setMatrixUuid(sourceMatrixUuid);
            sourceDataVo.setColumnList(sourceColumnList);
            List<Map<String, String>> sourceMatrixDataMapList = matrixDataMapper.searchDynamicTableData(sourceDataVo);
            if (CollectionUtils.isNotEmpty(sourceMatrixDataMapList)){
                for (Map sourceDataMap : sourceMatrixDataMapList){
                    List<String> targetColumnList = new ArrayList<>();
                    List<String> targetDataList = new ArrayList<>();
                    targetColumnList.add("uuid");
                    targetDataList.add(UUID.randomUUID().toString().replace("-", ""));
                    Set set = sourceDataMap.entrySet();
                    Iterator iterator = set.iterator();
                    while (iterator.hasNext()){
                        Map.Entry<String, String> entry = (Map.Entry)iterator.next();
                        String key = entry.getKey();
                        if (compareMap.containsKey(key)){
                            targetColumnList.add(compareMap.get(key));
                            targetDataList.add(entry.getValue());
                        }
                    }
                    matrixDataMapper.insertDynamicTableData(targetColumnList, targetDataList, targetMatrixUuid);
                }
            }
        }
    }
}
