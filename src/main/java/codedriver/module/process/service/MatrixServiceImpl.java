package codedriver.module.process.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixExternalMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixExternalNotFoundException;
import codedriver.framework.process.exception.matrix.MatrixNameRepeatException;
import codedriver.module.process.util.UUIDUtil;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-27 11:35
 **/
@Service
@Transactional
public class MatrixServiceImpl implements MatrixService {
	
	@Autowired
	private IntegrationMapper integrationMapper;

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixAttributeMapper matrixAttributeMapper;

    @Autowired
    private MatrixDataMapper matrixDataMapper;

    @Autowired
    private MatrixExternalMapper externalMapper;

    @Override
    public ProcessMatrixVo saveMatrix(ProcessMatrixVo matrixVo) {
        matrixVo.setLcu(UserContext.get().getUserId());
        if (StringUtils.isNotBlank(matrixVo.getUuid())){
        	if(matrixMapper.checkMatrixNameIsRepeat(matrixVo) > 0) {
        		throw new MatrixNameRepeatException(matrixVo.getName());
        	}
            matrixMapper.updateMatrixNameAndLcu(matrixVo);
        }else {
            matrixVo.setFcu(UserContext.get().getUserId());
            matrixVo.setUuid(UUIDUtil.getUUID());
            if(matrixMapper.checkMatrixNameIsRepeat(matrixVo) > 0) {
            	throw new MatrixNameRepeatException(matrixVo.getName());
        	}
            matrixMapper.insertMatrix(matrixVo);
        }
        
        return matrixVo;
    }

    @Override
    public int copyMatrix(String matrixUuid, String name) {
    	//判断name是否存在
    	ProcessMatrixVo processMatrixVo = new ProcessMatrixVo();
    	processMatrixVo.setKeyword(name);
    	if(matrixMapper.searchMatrixCount(processMatrixVo)>0){
    		throw new MatrixNameRepeatException(name);
    	}
        ProcessMatrixVo sourceMatrix = matrixMapper.getMatrixByUuid(matrixUuid);
        if (StringUtils.isNotBlank(name)){
            sourceMatrix.setName(name);
        }else {
            sourceMatrix.setName(sourceMatrix.getName() + "_copy");
        }
        sourceMatrix.setFcu(UserContext.get().getUserId());
        sourceMatrix.setLcu(UserContext.get().getUserId());
        String targetUuid = UUIDUtil.getUUID();
        sourceMatrix.setUuid(targetUuid);
        matrixMapper.insertMatrix(sourceMatrix);
        copyMatrixAttributeAndData(matrixUuid, targetUuid);
        return 0;
    }

    @Override
    public JSONObject getMatrixExternalData(String matrixUuid) {
        ProcessMatrixExternalVo externalVo = externalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
        if(externalVo == null) {
        	throw new MatrixExternalNotFoundException(matrixUuid);
        }
        IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
        IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
		if (handler == null) {
			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
		}

        JSONObject returnObj = new JSONObject();
        IntegrationResultVo resultVo = handler.sendRequest(integrationVo);
		if(resultVo != null && StringUtils.isNotBlank(resultVo.getTransformedResult())) {
			JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
			if(MapUtils.isNotEmpty(transformedResult)) {
				JSONArray theadList = transformedResult.getJSONArray("theadList");
				if (CollectionUtils.isNotEmpty(theadList)){
					List<String> headerList = new ArrayList<>();
					List<String> columnList = new ArrayList<>();
					for (int i = 0; i < theadList.size(); i++){
						JSONObject obj = theadList.getJSONObject(i);
						headerList.add(obj.getString("title"));
						columnList.add(obj.getString("key"));
					}
					returnObj.put("headerList", headerList);
					returnObj.put("columnList", columnList);
				}
				JSONArray tbodyList = transformedResult.getJSONArray("tbodyList");				
				if (CollectionUtils.isNotEmpty(tbodyList)){				
					returnObj.put("dataMapList", tbodyList);
				}
			}
		}
        return  returnObj;
    }

    public void copyMatrixAttributeAndData(String sourceMatrixUuid, String targetMatrixUuid){
        List<ProcessMatrixAttributeVo> attributeVoList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(sourceMatrixUuid);
        if (CollectionUtils.isNotEmpty(attributeVoList)){
            //属性拷贝
            List<String> sourceColumnList = new ArrayList<>();
            List<String> targetColumnList = new ArrayList<>();
            for (ProcessMatrixAttributeVo attributeVo : attributeVoList){
                String sourceUuid = attributeVo.getUuid();
                String targetUuid = UUIDUtil.getUUID();
                sourceColumnList.add(sourceUuid);
                targetColumnList.add(targetUuid);
                attributeVo.setMatrixUuid(targetMatrixUuid);
                attributeVo.setUuid(targetUuid);
                matrixAttributeMapper.insertMatrixAttribute(attributeVo);
            }

            if (matrixAttributeMapper.checkMatrixAttributeTableExist("matrix_" + targetMatrixUuid) == 0){
                matrixAttributeMapper.createMatrixDynamicTable(attributeVoList, targetMatrixUuid);
            }
            //数据拷贝
            matrixDataMapper.insertDynamicTableDataForCopy(sourceMatrixUuid, sourceColumnList, targetMatrixUuid, targetColumnList);
        }
    }

}
