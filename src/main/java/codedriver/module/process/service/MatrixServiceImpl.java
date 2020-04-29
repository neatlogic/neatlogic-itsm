package codedriver.module.process.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixExternalMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixExternalNotFoundException;
import codedriver.framework.process.exception.matrix.MatrixExternalRequestHandlerNotFoundException;
import codedriver.framework.process.exception.matrix.MatrixNameRepeatException;
import codedriver.framework.process.matrixrexternal.core.IMatrixExternalRequestHandler;
import codedriver.framework.process.matrixrexternal.core.MatrixExternalRequestFactory;
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
        JSONObject returnObj = new JSONObject();
        ProcessMatrixExternalVo externalVo = externalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
        if(externalVo == null) {
        	throw new MatrixExternalNotFoundException(matrixUuid);
        }
        String plugin = externalVo.getPlugin();
        IMatrixExternalRequestHandler requestHandler = MatrixExternalRequestFactory.getHandler(plugin);
        if(requestHandler == null) {
        	throw new MatrixExternalRequestHandlerNotFoundException(plugin);
        }
        JSONObject externalObj = JSONObject.parseObject(externalVo.getConfig());
        if(MapUtils.isNotEmpty(externalObj)) {
            String url = externalObj.getString("url");
            if(StringUtils.isNotBlank(url)) {
            	String rootName = externalObj.getString("rootName");
                JSONObject dataObj = requestHandler.dataHandler(url, rootName, externalObj);
                JSONArray dataArray = dataObj.getJSONArray("tbodyList");
                if (CollectionUtils.isNotEmpty(dataArray)){
                	JSONArray columnList = externalObj.getJSONArray("columnList");
                    List<String> headerList = new ArrayList<>();
                    List<String> attributeList = new ArrayList<>();
                    for (int i = 0; i < columnList.size(); i++){
                        JSONObject obj = columnList.getJSONObject(i);
                        headerList.add(obj.getString("title"));
                        attributeList.add(obj.getString("targetColumn"));
                    }
                    List<Map<String, String>> dataMapList = new ArrayList<>();
                    for (int i = 0; i < dataArray.size(); i++){
                        JSONObject obj = dataArray.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        for (String attribute : attributeList){
                            map.put(attribute, obj.getString(attribute));
                        }
                        dataMapList.add(map);
                    }
                    returnObj.put("headerList", headerList);
                    returnObj.put("columnList", attributeList);
                    returnObj.put("dataMapList", dataMapList);
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
            Map<String, String> compareMap = new HashMap<>();
            for (ProcessMatrixAttributeVo attributeVo : attributeVoList){
                String sourceUuid = attributeVo.getUuid();
                String targetUuid = UUIDUtil.getUUID();
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
            sourceDataVo.setNeedPage(false);
            List<Map<String, String>> sourceMatrixDataMapList = matrixDataMapper.searchDynamicTableData(sourceDataVo);
            if (CollectionUtils.isNotEmpty(sourceMatrixDataMapList)){
                for (Map<String,String> sourceDataMap : sourceMatrixDataMapList){
                	List<ProcessMatrixColumnVo> rowData = new ArrayList<>();
                	rowData.add(new ProcessMatrixColumnVo("uuid", UUIDUtil.getUUID()));
                	for(Entry<String, String> entry : sourceDataMap.entrySet()) {
                		String column = compareMap.get(entry.getKey());
                		if(StringUtils.isNotBlank(column)) {
                			rowData.add(new ProcessMatrixColumnVo(column, entry.getValue()));
                		}
                	}
                	matrixDataMapper.insertDynamicTableData2(rowData, targetMatrixUuid);
                }
            }
        }
    }

}
