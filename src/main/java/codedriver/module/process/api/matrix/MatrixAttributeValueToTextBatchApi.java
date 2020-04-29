package codedriver.module.process.api.matrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dao.mapper.RoleMapper;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.RoleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessMatrixAttributeType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class MatrixAttributeValueToTextBatchApi extends ApiComponentBase {
	
	 @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixAttributeMapper attributeMapper;
    
    @Autowired
	private UserMapper userMapper;
    
    @Autowired
	private TeamMapper teamMapper;
    
    @Autowired
	private RoleMapper roleMapper;

	@Override
	public String getToken() {
		return "matrix/attribute/valuetotext/batch";
	}

	@Override
	public String getName() {
		return "批量将矩阵属性值转换成显示文本接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	
	@Input({ 
		@Param(name = "matrixUuid", type = ApiParamType.STRING, isRequired = true, desc = "矩阵uuid"),
		@Param(name = "attributeData", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "矩阵属性数据")
	})
    @Output({
    	@Param( name = "Return", type = ApiParamType.JSONOBJECT, desc = "矩阵属性数据")
    })
    @Description( desc = "批量将矩阵属性值转换成显示文本接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String matrixUuid = jsonObj.getString("matrixUuid");
		if(matrixMapper.checkMatrixIsExists(matrixUuid) == 0) {
			throw new MatrixNotFoundException(matrixUuid);
		}
    	JSONObject attributeData = jsonObj.getJSONObject("attributeData");
		List<ProcessMatrixAttributeVo> attributeList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
        if (MapUtils.isNotEmpty(attributeData) && CollectionUtils.isNotEmpty(attributeList)){
        	Map<String, ProcessMatrixAttributeVo> processMatrixAttributeMap = new HashMap<>();
        	for(ProcessMatrixAttributeVo processMatrixAttributeVo : attributeList) {
        		processMatrixAttributeMap.put(processMatrixAttributeVo.getUuid(), processMatrixAttributeVo);
        	}
        	Map<String, Object> resultMap = new HashMap<>(attributeData.size());
        	for(Entry<String, Object> entry : attributeData.entrySet()) {
        		String attributeUuid = entry.getKey();        		
        		List<String> attributeValueList = JSON.parseArray(entry.getValue().toString(), String.class);
        		JSONArray attributeArray = new JSONArray(attributeValueList.size());
        		for(String value : attributeValueList) {
        			String type = ProcessMatrixAttributeType.INPUT.getValue();
					ProcessMatrixAttributeVo processMatrixAttribute = processMatrixAttributeMap.get(attributeUuid);
					if(processMatrixAttribute != null) {
						type = processMatrixAttribute.getType();
					}

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
					attributeArray.add(resultObj);
        		}
        		resultMap.put(attributeUuid, attributeArray);
        	}
        	return resultMap;
        }
		return null;
	}

}
