package codedriver.module.process.service;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    
    @Autowired
	private UserMapper userMapper;
    
    @Autowired
	private TeamMapper teamMapper;
    
    @Autowired
	private RoleMapper roleMapper;

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
	public List<Map<String, Object>> matrixTableDataValueHandle(List<ProcessMatrixAttributeVo> ProcessMatrixAttributeList, List<Map<String, String>> valueList) {
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
						resultMap.put(attributeUuid, matrixAttributeValueHandle(processMatrixAttributeMap.get(attributeUuid), entry.getValue()));
					}
					resultList.add(resultMap);
				}
				return resultList;
			}
		}
		return null;
	}

	@Override
	public JSONObject matrixAttributeValueHandle(ProcessMatrixAttributeVo processMatrixAttribute, Object valueObj) {
		JSONObject resultObj = new JSONObject();String type = ProcessMatrixAttributeType.INPUT.getValue();
		if(processMatrixAttribute != null) {
			type = processMatrixAttribute.getType();
		}
		resultObj.put("type", type);
		if(valueObj == null) {
			resultObj.put("value", null);
			resultObj.put("text", null);
			return resultObj;
		}
		String value = valueObj.toString();
		resultObj.put("value", value);
		resultObj.put("text", value);		
		if(ProcessMatrixAttributeType.SELECT.getValue().equals(type)) {
			if(processMatrixAttribute != null) {
				String config = processMatrixAttribute.getConfig();
				if(StringUtils.isNotBlank(config)) {
					JSONObject configObj = JSON.parseObject(config);
					JSONArray dataList = configObj.getJSONArray("dataList");
					if(CollectionUtils.isNotEmpty(dataList)) {
						for(int i = 0; i < dataList.size(); i++) {
							JSONObject data = dataList.getJSONObject(i);
							if(Objects.equals(value, data.getString("value"))) {
								resultObj.put("text", data.getString("text"));
							}
						}
					}
				}
			}
		}else if(ProcessMatrixAttributeType.USER.getValue().equals(type)) {
			UserVo userVo = userMapper.getUserBaseInfoByUserId(value);
			if(userVo != null) {
				resultObj.put("text", userVo.getUserName());
			}
		}else if(ProcessMatrixAttributeType.TEAM.getValue().equals(type)) {
			TeamVo teamVo = teamMapper.getTeamByUuid(value);
			if(teamVo != null) {
				resultObj.put("text", teamVo.getName());
			}
		}else if(ProcessMatrixAttributeType.ROLE.getValue().equals(type)) {
			RoleVo roleVo = roleMapper.getRoleByRoleName(value);
			if(roleVo != null) {
				resultObj.put("text", roleVo.getDescription());
			}
		}
		return resultObj;
	}

	@Override
	public JSONObject matrixAttributeValueHandle(Object value) {
		return matrixAttributeValueHandle(null, value);
	}
	
	@Override
	public List<String> matrixAttributeValueKeyWordSearch(ProcessMatrixAttributeVo processMatrixAttribute, String keyword, int pageSize) {
		pageSize *= 10; 
		String type = processMatrixAttribute.getType();
		if(ProcessMatrixAttributeType.SELECT.getValue().equals(type)) {
			if(processMatrixAttribute != null) {
				String config = processMatrixAttribute.getConfig();
				if(StringUtils.isNotBlank(config)) {
					JSONObject configObj = JSON.parseObject(config);
					List<ValueTextVo> dataList = JSON.parseArray(configObj.getString("dataList"), ValueTextVo.class);
					if(CollectionUtils.isNotEmpty(dataList)) {
						List<String> valueList = new ArrayList<>();
						for(ValueTextVo data : dataList) {
							if(data.getText().contains(keyword)) {
								valueList.add(data.getValue());
							}
						}
						if(CollectionUtils.isNotEmpty(valueList)) {
							return matrixDataMapper.getUuidListByAttributeValueListForSelectType(processMatrixAttribute.getMatrixUuid(), processMatrixAttribute.getUuid(), valueList, pageSize);
						}
					}
				}
			}
		}else if(ProcessMatrixAttributeType.USER.getValue().equals(type)) {
			return matrixDataMapper.getUuidListByKeywordForUserType(processMatrixAttribute.getMatrixUuid(), processMatrixAttribute.getUuid(), keyword, pageSize);
		}else if(ProcessMatrixAttributeType.TEAM.getValue().equals(type)) {
			return matrixDataMapper.getUuidListByKeywordForTeamType(processMatrixAttribute.getMatrixUuid(), processMatrixAttribute.getUuid(), keyword, pageSize);
		}else if(ProcessMatrixAttributeType.ROLE.getValue().equals(type)) {
			return matrixDataMapper.getUuidListByKeywordForRoleType(processMatrixAttribute.getMatrixUuid(), processMatrixAttribute.getUuid(), keyword, pageSize);
		}else if(ProcessMatrixAttributeType.DATE.getValue().equals(type)) {
			return matrixDataMapper.getUuidListByKeywordForDateType(processMatrixAttribute.getMatrixUuid(), processMatrixAttribute.getUuid(), keyword, pageSize);
		}else {
			return matrixDataMapper.getUuidListByKeywordForInputType(processMatrixAttribute.getMatrixUuid(), processMatrixAttribute.getUuid(), keyword, pageSize);
		}
		return null;
	}

	@Override
	public List<Map<String, JSONObject>> getExternalDataTbodyList(IntegrationResultVo resultVo, List<String> columnList, int pageSize, JSONObject resultObj) {
		if(resultVo != null && StringUtils.isNotBlank(resultVo.getTransformedResult())) {
			JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
			if(MapUtils.isNotEmpty(transformedResult)) {
				if(resultObj != null) {
					resultObj.putAll(transformedResult);
				}
				JSONArray tbodyList = transformedResult.getJSONArray("tbodyList");
				if(CollectionUtils.isNotEmpty(tbodyList)) {
					List<Map<String, JSONObject>> resultList = new ArrayList<>();
					for(int i = 0; i < tbodyList.size(); i++) {
						JSONObject rowData = tbodyList.getJSONObject(i);
						Map<String, JSONObject> resultMap = new HashMap<>(columnList.size());
						for(String column : columnList) {
							String columnValue = rowData.getString(column);
							resultMap.put(column, matrixAttributeValueHandle(columnValue)); 							
						}
						resultList.add(resultMap);
						if(resultList.size() >= pageSize) {
							break;
						}
					}
					if(resultObj != null) {
						resultObj.put("tbodyList", resultList);
					}
					return resultList;
				}
			}
		}
		return null;
	}
}
