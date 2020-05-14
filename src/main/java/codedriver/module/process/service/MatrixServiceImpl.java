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

import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.process.dao.mapper.MatrixExternalMapper;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.exception.matrix.MatrixExternalNotFoundException;

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
    private MatrixExternalMapper externalMapper;

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

}
