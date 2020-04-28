package codedriver.module.process.service;

import codedriver.framework.common.util.PageUtil;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;

import org.apache.commons.collections4.CollectionUtils;
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
						String type = ProcessMatrixAttributeType.INPUT.getValue();
						ProcessMatrixAttributeVo processMatrixAttribute = processMatrixAttributeMap.get(attributeUuid);
						if(processMatrixAttribute != null) {
							type = processMatrixAttribute.getType();
						}
						String value = entry.getValue();
						JSONObject resultObj = new JSONObject();
						resultObj.put("type", type);
						if(ProcessMatrixAttributeType.SELECT.getValue().equals(type)) {
							resultObj.put("value", value);
							resultObj.put("text", value);
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
							String[] split = value.split("#");
							resultObj.put("value", split[1]);
							UserVo userVo = userMapper.getUserBaseInfoByUserId(split[1]);
							if(userVo != null) {
								resultObj.put("text", userVo.getUserName());
							}else {
								resultObj.put("text", split[1]);
							}
						}else if(ProcessMatrixAttributeType.TEAM.getValue().equals(type)) {
							String[] split = value.split("#");
							resultObj.put("value", split[1]);
							TeamVo teamVo = teamMapper.getTeamByUuid(split[1]);
							if(teamVo != null) {
								resultObj.put("text", teamVo.getName());
							}else {
								resultObj.put("text", split[1]);
							}
						}else if(ProcessMatrixAttributeType.ROLE.getValue().equals(type)) {
							String[] split = value.split("#");
							resultObj.put("value", split[1]);
							RoleVo roleVo = roleMapper.getRoleByRoleName(split[1]);
							if(roleVo != null) {
								resultObj.put("text", roleVo.getDescription());
							}else {
								resultObj.put("text", split[1]);
							}
						}else if(ProcessMatrixAttributeType.DATE.getValue().equals(type)) {
							resultObj.put("value", value);
							resultObj.put("text", value);
						}else {
							resultObj.put("value", value);
							resultObj.put("text", value);
						}
						resultMap.put(attributeUuid, resultObj);
					}
					resultList.add(resultMap);
				}
				return resultList;
			}
		}
		return null;
	}
}
