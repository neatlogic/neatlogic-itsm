package codedriver.module.process.service;

import codedriver.framework.exception.util.FreemarkerTransformException;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.util.FreemarkerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-27 18:16
 **/
@Transactional
@Service
public class MatrixAttributeServiceImpl implements MatrixAttributeService {

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
