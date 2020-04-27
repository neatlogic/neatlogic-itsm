package codedriver.module.process.service;

import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.matrixattribute.core.MatrixAttributeHandlerFactory;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
    public List<String> getExternalMatrixColumnData() {
        return null;
    }

	@Override
	public List<Map<String, Object>> matrixValueHandle(List<ProcessMatrixAttributeVo> ProcessMatrixAttributeList, List<Map<String, String>> valueList) {
		if(CollectionUtils.isNotEmpty(ProcessMatrixAttributeList)) {
			Map<String, ProcessMatrixAttributeVo> processMatrixAttributeMap = new HashMap<>();
			for(ProcessMatrixAttributeVo processMatrixAttributeVo : ProcessMatrixAttributeList) {
				processMatrixAttributeMap.put(processMatrixAttributeVo.getUuid(), processMatrixAttributeVo);
			}
			if(CollectionUtils.isNotEmpty(valueList)) {
				List<Map<String, Object>> resultList = new ArrayList<>(valueList.size());
				for(Map<String, String> valueMap : valueList) {
					Map<String, Object> resultMap = new HashMap<>();
					for(Entry<String, String> entry : valueMap.entrySet()) {
						String attributeUuid = entry.getKey();
						ProcessMatrixAttributeVo processMatrixAttribute = processMatrixAttributeMap.get(attributeUuid);
						if(processMatrixAttribute != null) {
							resultMap.put(attributeUuid, MatrixAttributeHandlerFactory.getHandler(processMatrixAttribute.getType()).getData(processMatrixAttribute, entry.getValue()));
						}else {
							resultMap.put(attributeUuid, MatrixAttributeHandlerFactory.getHandler(ProcessMatrixAttributeType.INPUT.getValue()).getData(processMatrixAttribute, entry.getValue()));
						}
					}
					resultList.add(resultMap);
				}
				return resultList;
			}
		}
		return null;
	}
}
