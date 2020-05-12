package codedriver.module.process.service;

import codedriver.framework.exception.util.FreemarkerTransformException;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.util.FreemarkerUtil;
import codedriver.module.process.util.UUIDUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        List<ProcessMatrixAttributeVo> oldMatrixAttributeList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
        boolean dataExist = CollectionUtils.isNotEmpty(oldMatrixAttributeList);
        if (dataExist){
            attributeMapper.deleteAttributeByMatrixUuid(matrixUuid);
        }
        if (CollectionUtils.isNotEmpty(_attributeVoList)){
            //有数据
            if (dataExist){
                //数据对比
                //删除数据
                //调整表
            	List<String> oldAttributeUuidList = oldMatrixAttributeList.stream().map(ProcessMatrixAttributeVo::getUuid).collect(Collectors.toList());
                List<String> addAttributeUuidList = new ArrayList<>();
                List<String> existedAttributeUuidList = new ArrayList<>();
                for(ProcessMatrixAttributeVo attributeVo : _attributeVoList) {
                	attributeVo.setMatrixUuid(matrixUuid);
                	if (oldAttributeUuidList.contains(attributeVo.getUuid())){
                        attributeMapper.insertMatrixAttribute(attributeVo);
                        existedAttributeUuidList.add(attributeVo.getUuid());
                    }else {
                    	//过滤新增属性uuid
                        attributeMapper.insertMatrixAttribute(attributeVo);
                        addAttributeUuidList.add(attributeVo.getUuid());
                    }
                }
                
                //添加新增字段
                for(String attributeUuid : addAttributeUuidList) {
                	attributeMapper.addMatrixDynamicTableColumn(attributeUuid, matrixUuid);
                }
                //找出需要删除的属性uuid列表
                oldAttributeUuidList.removeAll(existedAttributeUuidList);
                for(String attributeUuid : oldAttributeUuidList) {
                	attributeMapper.dropMatrixDynamicTableColumn(attributeUuid, matrixUuid);
                }
            }else {
                for (ProcessMatrixAttributeVo attributeVo : _attributeVoList){
                	attributeVo.setMatrixUuid(matrixUuid);
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

	@Override
	public List<ProcessMatrixAttributeVo> getExternalMatrixAttributeList(String matrixUuid, IntegrationVo integrationVo) throws FreemarkerTransformException {
		List<ProcessMatrixAttributeVo> processMatrixAttributeList = new ArrayList<>();
		JSONObject config = integrationVo.getConfig();
		if(MapUtils.isNotEmpty(config)) {
			JSONObject output = config.getJSONObject("output");
			if(MapUtils.isNotEmpty(output)) {
				String content = output.getString("content");
				content = FreemarkerUtil.transform(null, content);
				JSONObject contentObj = JSON.parseObject(content);
				if(MapUtils.isNotEmpty(contentObj)) {
					JSONArray theadList = contentObj.getJSONArray("theadList");
            		if(CollectionUtils.isNotEmpty(theadList)) {
            			for(int i = 0; i < theadList.size(); i++) {
            				JSONObject theadObj = theadList.getJSONObject(i);
            				ProcessMatrixAttributeVo processMatrixAttributeVo = new ProcessMatrixAttributeVo();
            				processMatrixAttributeVo.setMatrixUuid(matrixUuid);
            				processMatrixAttributeVo.setUuid(theadObj.getString("key"));
            				processMatrixAttributeVo.setName(theadObj.getString("title"));
            				processMatrixAttributeVo.setType(ProcessMatrixAttributeType.INPUT.getValue());
            				processMatrixAttributeVo.setIsDeletable(0);
            				processMatrixAttributeVo.setSort(i);
            				processMatrixAttributeVo.setIsRequired(0);
            				Integer isSearchable = theadObj.getInteger("isSearchable");
            				processMatrixAttributeVo.setIsSearchable((isSearchable == null || isSearchable.intValue() != 1) ? 0 : 1);
            				processMatrixAttributeList.add(processMatrixAttributeVo);
            			}
            		}
				}
			}
		}
		return processMatrixAttributeList;
	}

}
