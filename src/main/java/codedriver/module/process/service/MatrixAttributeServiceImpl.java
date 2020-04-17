package codedriver.module.process.service;

import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.module.process.util.UUIDUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-27 18:16
 **/
@Transactional
@Service
public class MatrixAttributeServiceImpl implements MatrixAttributeService {

    @Autowired
    private MatrixAttributeMapper attributeMapper;

    @Override
    public void saveMatrixAttribute(List<ProcessMatrixAttributeVo> _attributeVoList, String matrixUuid) {

        List<ProcessMatrixAttributeVo> matrixAttributeList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
        boolean dataExist = CollectionUtils.isNotEmpty(matrixAttributeList);
        if (dataExist){
            attributeMapper.deleteAttributeByMatrixUuid(matrixUuid);
        }
        if (CollectionUtils.isNotEmpty(_attributeVoList)){
            //有数据
            if (dataExist){
                //数据对比
                //删除数据
                //调整表
                List<ProcessMatrixAttributeVo> addAttributeList = new ArrayList<>();
                Iterator<ProcessMatrixAttributeVo> iterator = _attributeVoList.iterator();
                while (iterator.hasNext()){
                    ProcessMatrixAttributeVo attributeVo = iterator.next();
                    if (StringUtils.isBlank(attributeVo.getUuid())){
                        //过滤新增属性
                        addAttributeList.add(attributeVo);
                        attributeVo.setUuid(UUIDUtil.getUUID());
                        attributeMapper.insertMatrixAttribute(attributeVo);
                        iterator.remove();
                    }else {
                        attributeMapper.insertMatrixAttribute(attributeVo);
                    }
                }
                List<ProcessMatrixAttributeVo> deleteAttributeList = matrixAttributeList.stream().filter(item -> !_attributeVoList.contains(item)).collect(toList());

                //添加新增字段
                if (CollectionUtils.isNotEmpty(addAttributeList)){
                    for (ProcessMatrixAttributeVo attributeVo : addAttributeList){
                        attributeMapper.addMatrixDynamicTableColumn(attributeVo.getUuid(), matrixUuid);
                    }
                }
                if (CollectionUtils.isNotEmpty(deleteAttributeList)){
                    for (ProcessMatrixAttributeVo attributeVo : deleteAttributeList){
                        attributeMapper.dropMatrixDynamicTableColumn(attributeVo.getUuid(), matrixUuid);
                    }
                }

            }else {
                for (ProcessMatrixAttributeVo attributeVo : _attributeVoList){
                    attributeVo.setUuid(UUIDUtil.getUUID());
                    attributeMapper.insertMatrixAttribute(attributeVo);
                }
                attributeMapper.createMatrixDynamicTable(_attributeVoList, matrixUuid);
            }
        }else {
            //无数据
            if (dataExist){
                // 删除动态表
                attributeMapper.dropMatrixDynamicTable(matrixUuid);
            }
        }
    }

}
