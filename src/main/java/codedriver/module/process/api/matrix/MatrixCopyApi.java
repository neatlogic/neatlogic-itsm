package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixExternalException;
import codedriver.framework.process.exception.matrix.MatrixNameRepeatException;
import codedriver.framework.process.exception.matrix.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.util.UUIDUtil;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:03
 **/
@Service
@Transactional
public class MatrixCopyApi extends ApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixAttributeMapper matrixAttributeMapper;

    @Autowired
    private MatrixDataMapper matrixDataMapper;

    @Override
    public String getToken() {
        return "matrix/copy";
    }

    @Override
    public String getName() {
        return "矩阵数据源复制接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ 
    	@Param(name = "uuid", desc = "矩阵数据源uuid", isRequired = true, type = ApiParamType.STRING),
        @Param(name = "name", desc = "矩阵名称", isRequired = true, type = ApiParamType.STRING)
    })
    @Description(desc = "矩阵数据源复制接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	String uuid = jsonObj.getString("uuid");
        ProcessMatrixVo sourceMatrix = matrixMapper.getMatrixByUuid(uuid);
    	if(sourceMatrix == null) {
    		throw new MatrixNotFoundException(uuid);
    	}
    	if(ProcessMatrixType.CUSTOM.getValue().equals(sourceMatrix.getType())) {
    		String name = jsonObj.getString("name");
            //判断name是否存在
            String targetMatrixUuid = UUIDUtil.getUUID();
            sourceMatrix.setUuid(targetMatrixUuid);
            sourceMatrix.setName(name);
        	if(matrixMapper.checkMatrixNameIsRepeat(sourceMatrix) > 0){
        		throw new MatrixNameRepeatException(name);
        	}
            sourceMatrix.setFcu(UserContext.get().getUserId());
            sourceMatrix.setLcu(UserContext.get().getUserId());
            matrixMapper.insertMatrix(sourceMatrix);

            List<ProcessMatrixAttributeVo> attributeVoList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(uuid);
            if (CollectionUtils.isNotEmpty(attributeVoList)){
                //属性拷贝
                List<String> sourceColumnList = new ArrayList<>();
                List<String> targetColumnList = new ArrayList<>();
                for (ProcessMatrixAttributeVo attributeVo : attributeVoList){
                    String sourceAttributeUuid = attributeVo.getUuid();
                    String targetAttributeUuid = UUIDUtil.getUUID();
                    sourceColumnList.add(sourceAttributeUuid);
                    targetColumnList.add(targetAttributeUuid);
                    attributeVo.setMatrixUuid(targetMatrixUuid);
                    attributeVo.setUuid(targetAttributeUuid);
                    matrixAttributeMapper.insertMatrixAttribute(attributeVo);
                }

                if (matrixAttributeMapper.checkMatrixAttributeTableExist("matrix_" + targetMatrixUuid) == 0){
                    matrixAttributeMapper.createMatrixDynamicTable(attributeVoList, targetMatrixUuid);
                }
                //数据拷贝
                matrixDataMapper.insertDynamicTableDataForCopy(uuid, sourceColumnList, targetMatrixUuid, targetColumnList);
            }
    	}else {
    		throw new MatrixExternalException("矩阵外部数据源没有复制操作");
    	}
    	
        return null;
    }
}
