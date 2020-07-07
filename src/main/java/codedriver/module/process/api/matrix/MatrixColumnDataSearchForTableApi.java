package codedriver.module.process.api.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.integration.core.IIntegrationHandler;
import codedriver.framework.integration.core.IntegrationHandlerFactory;
import codedriver.framework.integration.dao.mapper.IntegrationMapper;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixExternalMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixAttributeNotFoundException;
import codedriver.framework.process.exception.matrix.MatrixExternalException;
import codedriver.framework.process.exception.matrix.MatrixExternalNotFoundException;
import codedriver.framework.process.exception.matrix.MatrixNotFoundException;
import codedriver.framework.process.integration.handler.ProcessRequestFrom;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixService;

@Service
public class MatrixColumnDataSearchForTableApi extends ApiComponentBase {

	private final static Logger logger = LoggerFactory.getLogger(MatrixColumnDataSearchForTableApi.class);

	@Autowired
	private MatrixService matrixService;

	@Autowired
	private MatrixMapper matrixMapper;

	@Autowired
	private MatrixDataMapper matrixDataMapper;

	@Autowired
	private MatrixAttributeMapper matrixAttributeMapper;

	@Autowired
	private MatrixExternalMapper matrixExternalMapper;

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "matrix/column/data/search/fortable";
	}

	@Override
	public String getName() {
		return "矩阵属性数据查询-table接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ 
		@Param(name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true), 
		@Param(name = "columnList", desc = "目标属性集合，数据按这个字段顺序返回", type = ApiParamType.JSONARRAY, isRequired = true), 
		@Param(name = "searchColumnList ", desc = "搜索属性集合", type = ApiParamType.JSONARRAY), 
		@Param(name = "sourceColumnList", desc = "搜索过滤值集合", type = ApiParamType.JSONARRAY), 
		@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"), 
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"), 
		@Param(name = "arrayColumnList", desc = "需要将值转化成数组的属性集合", type = ApiParamType.JSONARRAY) 
	})
	@Description(desc = "矩阵属性数据查询-table接口")
	@Output({ 
		@Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "属性数据集合"), 
		@Param(name = "theadList", type = ApiParamType.JSONARRAY, desc = "属性列名集合"), 
		@Param(name = "searchColumnDetailList", type = ApiParamType.JSONARRAY, desc = "搜索属性详情集合"),
		@Param(name = "type", type = ApiParamType.STRING, desc = "矩阵类型"), 
		@Param(explode = BasePageVo.class) 
	})
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject returnObj = new JSONObject();
		ProcessMatrixDataVo dataVo = JSON.toJavaObject(jsonObj, ProcessMatrixDataVo.class);
		ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
		if (matrixVo == null) {
			throw new MatrixNotFoundException(dataVo.getMatrixUuid());
		}
		List<String> columnList = dataVo.getColumnList();
		if (CollectionUtils.isEmpty(columnList)) {
			throw new ParamIrregularException("参数“columnList”不符合格式要求");
		}
		List<String> searchColumnList = JSONObject.parseArray(jsonObj.getString("searchColumnList"), String.class);
		if (ProcessMatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
			returnObj.put("type", ProcessMatrixType.CUSTOM.getValue());
			Map<String, ProcessMatrixAttributeVo> attributeMap = new HashMap<>();
			List<ProcessMatrixAttributeVo> processMatrixAttributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
			for (ProcessMatrixAttributeVo attribute : processMatrixAttributeList) {
				attributeMap.put(attribute.getUuid(), attribute);
			}
			// theadList
			JSONArray theadList = new JSONArray();
			for (String column : dataVo.getColumnList()) {
				ProcessMatrixAttributeVo attribute = attributeMap.get(column);
				if (attribute != null) {
					JSONObject theadObj = new JSONObject();
					theadObj.put("key", attribute.getUuid());
					theadObj.put("title", attribute.getName());
					theadList.add(theadObj);
				} else {
					throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
				}
			}
			returnObj.put("theadList", theadList);
			List<Map<String, String>> dataMapList = matrixDataMapper.getDynamicTableDataByColumnList(dataVo);
			List<Map<String, Object>> tbodyList = matrixService.matrixTableDataValueHandle(processMatrixAttributeList, dataMapList);
			returnObj.put("tbodyList", tbodyList);

			if (CollectionUtils.isNotEmpty(searchColumnList)) {
				JSONArray searchColumnDetailList = new JSONArray();
				for (String column : searchColumnList) {
					ProcessMatrixAttributeVo attribute = attributeMap.get(column);
					if (attribute != null) {
						searchColumnDetailList.add(attribute);
					} else {
						throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
					}
				}
				returnObj.put("searchColumnDetailList", searchColumnDetailList);
			}

			if (dataVo.getNeedPage()) {
				int rowNum = matrixDataMapper.getDynamicTableDataByColumnCount(dataVo);
				int pageCount = PageUtil.getPageCount(rowNum, dataVo.getPageSize());
				returnObj.put("currentPage", dataVo.getCurrentPage());
				returnObj.put("pageSize", dataVo.getPageSize());
				returnObj.put("pageCount", pageCount);
				returnObj.put("rowNum", rowNum);
			}
		} else {
			returnObj.put("type", ProcessMatrixType.EXTERNAL.getValue());
			ProcessMatrixExternalVo externalVo = matrixExternalMapper.getMatrixExternalByMatrixUuid(dataVo.getMatrixUuid());
			if (externalVo == null) {
				throw new MatrixExternalNotFoundException(dataVo.getMatrixUuid());
			}
			IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
			IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
			if (handler == null) {
				throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
			}
			Map<String, ProcessMatrixAttributeVo> attributeMap = new HashMap<>();
			List<ProcessMatrixAttributeVo> processMatrixAttributeList = matrixService.getExternalMatrixAttributeList(dataVo.getMatrixUuid(), integrationVo);
			for (ProcessMatrixAttributeVo processMatrixAttributeVo : processMatrixAttributeList) {
				attributeMap.put(processMatrixAttributeVo.getUuid(), processMatrixAttributeVo);
			}

			// theadList
			JSONArray theadList = new JSONArray();
			for (String column : dataVo.getColumnList()) {
				ProcessMatrixAttributeVo attribute = attributeMap.get(column);
				if (attribute != null) {
					JSONObject theadObj = new JSONObject();
					theadObj.put("key", attribute.getUuid());
					theadObj.put("title", attribute.getName());
					theadList.add(theadObj);
				} else {
					throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
				}
			}

			if (CollectionUtils.isNotEmpty(searchColumnList)) {
				JSONArray searchColumnDetailList = new JSONArray();
				for (String column : searchColumnList) {
					ProcessMatrixAttributeVo attribute = attributeMap.get(column);
					if (attribute != null) {
						searchColumnDetailList.add(attribute);
					} else {
						throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
					}
				}
				returnObj.put("searchColumnDetailList", searchColumnDetailList);
			}
			integrationVo.getParamObj().putAll(jsonObj);
			IntegrationResultVo resultVo = handler.sendRequest(integrationVo, ProcessRequestFrom.MATRIX);
			if (StringUtils.isNotBlank(resultVo.getError())) {
				logger.error(resultVo.getError());
				throw new MatrixExternalException("外部接口访问异常");
			} else {
				matrixService.getExternalDataTbodyList(resultVo, dataVo.getColumnList(), dataVo.getPageSize(), returnObj);
				/** 将arrayColumnList包含的列值转成数组 **/
				List<String> arrayColumnList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("arrayColumnList")), String.class);
				if(CollectionUtils.isNotEmpty(arrayColumnList)) {
					JSONArray tbodyList = returnObj.getJSONArray("tbodyList");
					if(CollectionUtils.isNotEmpty(tbodyList)) {
						for(int i = 0; i < tbodyList.size(); i++) {
							JSONObject rowData = tbodyList.getJSONObject(i);
							for(Entry<String, Object> entry : rowData.entrySet()) {
								if(arrayColumnList.contains(entry.getKey())) {
									List<ValueTextVo> valueObjList = new ArrayList<>();
									JSONObject valueObj = (JSONObject) entry.getValue();
									String value = valueObj.getString("value");
									if(StringUtils.isNotBlank(value)) {
										if(value.startsWith("[") && value.endsWith("]")) {
											List<String> valueList = JSON.parseArray(valueObj.getJSONArray("value").toJSONString(), String.class);
											for(String valueStr : valueList) {
												valueObjList.add(new ValueTextVo(valueStr, valueStr));
											}
										}else {
											valueObjList.add(new ValueTextVo(value, value));
										}
									}
									valueObj.put("value", valueObjList);
								}
							}
						}
					}
				}
			}
			returnObj.put("theadList", theadList);
		}
		return returnObj;
	}
}
