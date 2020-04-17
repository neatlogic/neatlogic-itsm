package codedriver.module.process.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixExternalMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixRepeatException;
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
            matrixMapper.updateMatrixNameAndLcu(matrixVo);
        }else {
            matrixVo.setFcu(UserContext.get().getUserId());
            matrixVo.setUuid(UUIDUtil.getUUID());
            matrixMapper.insertMatrix(matrixVo);
        }

//        if (matrixVo.getType().equals(ProcessMatrixType.EXTERNAL.getValue())){
//            saveExternalMatrix(matrixVo);
//        }
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
    	//判断name是否存在
    	ProcessMatrixVo processMatrixVo = new ProcessMatrixVo();
    	processMatrixVo.setKeyword(name);
    	if(matrixMapper.searchMatrixCount(processMatrixVo)>0){
    		throw new MatrixRepeatException(name);
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
        JSONObject externalObj = JSONObject.parseObject(externalVo.getConfig());
        String plugin = externalVo.getPlugin();
        String root = externalObj.getString("root");
        String url = externalObj.getString("url");
        IMatrixExternalRequestHandler requestHandler = MatrixExternalRequestFactory.getHandler(plugin);
        JSONArray dataArray = requestHandler.dataHandler(url, root, externalObj);
        if (CollectionUtils.isNotEmpty(dataArray)){
            String columnConfig = externalObj.getString("columnConfig");
            JSONArray columnArray = JSONArray.parseArray(columnConfig);
            List<String> headerList = new ArrayList<>();
            List<String> attributeList = new ArrayList<>();
            for (int i = 0; i < columnArray.size(); i++){
                JSONObject obj = columnArray.getJSONObject(i);
                headerList.add(obj.getString("text"));
                attributeList.add(obj.getString("attribute"));
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
            List<Map<String, String>> sourceMatrixDataMapList = matrixDataMapper.searchDynamicTableData(sourceDataVo);
            if (CollectionUtils.isNotEmpty(sourceMatrixDataMapList)){
                for (Map<String,String> sourceDataMap : sourceMatrixDataMapList){
                    List<String> targetColumnList = new ArrayList<>();
                    List<String> targetDataList = new ArrayList<>();
                    targetColumnList.add("uuid");
                    targetDataList.add(UUIDUtil.getUUID());
                    Set<Entry<String, String>> set = sourceDataMap.entrySet();
                    Iterator<Entry<String, String>> iterator = set.iterator();
                    while (iterator.hasNext()){
                        Map.Entry<String, String> entry = iterator.next();
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

    public void saveExternalMatrix(ProcessMatrixVo matrixVo){
        ProcessMatrixExternalVo externalVo = new ProcessMatrixExternalVo();
        JSONObject externalObj = JSONObject.parseObject(matrixVo.getExternalConfig());
        externalVo.setMatrixUuid(matrixVo.getUuid());
        externalVo.setPlugin(externalObj.getString("plugin"));
        externalVo.setConfig(externalObj.getJSONObject("config").toString());
        if (StringUtils.isNotBlank(matrixVo.getUuid())){
            externalMapper.deleteMatrixExternalByMatrixUuid(matrixVo.getUuid());
        }
        externalMapper.insertMatrixExternal(externalVo);
    }
}
