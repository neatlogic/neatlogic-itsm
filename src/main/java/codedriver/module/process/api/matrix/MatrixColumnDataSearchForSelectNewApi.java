package codedriver.module.process.api.matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Objects;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.constvalue.Expression;
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
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixAttributeNotFoundException;
import codedriver.framework.process.exception.matrix.MatrixExternalException;
import codedriver.framework.process.exception.matrix.MatrixExternalNotFoundException;
import codedriver.framework.process.exception.matrix.MatrixNotFoundException;
import codedriver.framework.process.integration.handler.ProcessRequestFrom;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixService;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixColumnDataSearchForSelectNewApi extends ApiComponentBase {

	private final static Logger logger = LoggerFactory.getLogger(MatrixColumnDataSearchForSelectNewApi.class);

	@Autowired
	private MatrixService matrixService;

	@Autowired
	private MatrixMapper matrixMapper;

	@Autowired
	private MatrixAttributeMapper matrixAttributeMapper;

	@Autowired
	private MatrixDataMapper matrixDataMapper;

	@Autowired
	private MatrixExternalMapper matrixExternalMapper;

	@Autowired
	private IntegrationMapper integrationMapper;

	@Override
	public String getToken() {
		return "matrix/column/data/search/forselect/new";
	}

	@Override
	public String getName() {
		return "矩阵属性数据查询-下拉级联接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({ @Param(name = "keyword", desc = "关键字", type = ApiParamType.STRING, xss = true), 
		@Param(name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true), 
		@Param(name = "keywordColumn", desc = "关键字属性uuid", type = ApiParamType.STRING), 
		@Param(name = "columnList", desc = "属性uuid列表", type = ApiParamType.JSONARRAY, isRequired = true), 
		@Param(name = "sourceColumnList", desc = "源属性集合", type = ApiParamType.JSONARRAY),
		@Param(name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
		@Param(name = "valueList", desc = "精确匹配回显数据参数", type = ApiParamType.JSONARRAY) })
	@Description(desc = "矩阵属性数据查询-下拉级联接口")
	@Output({ @Param(name = "columnDataList", type = ApiParamType.JSONARRAY, desc = "属性数据集合") })
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessMatrixDataVo dataVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessMatrixDataVo>() {
		});
		ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
		if (matrixVo == null) {
			throw new MatrixNotFoundException(dataVo.getMatrixUuid());
		}

		List<String> valueList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("valueList")), String.class);
		List<String> columnList = dataVo.getColumnList();
		if (CollectionUtils.isEmpty(columnList)) {
			throw new ParamIrregularException("参数“columnList”不符合格式要求");
		}
		String keywordColumn = jsonObj.getString("keywordColumn");
		List<Map<String, JSONObject>> resultList = new ArrayList<>();
		JSONObject returnObj = new JSONObject();
		if (ProcessMatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
			List<ProcessMatrixAttributeVo> attributeList = matrixAttributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
			if (CollectionUtils.isNotEmpty(attributeList)) {
				Map<String, ProcessMatrixAttributeVo> processMatrixAttributeMap = new HashMap<>();
				for (ProcessMatrixAttributeVo processMatrixAttributeVo : attributeList) {
					processMatrixAttributeMap.put(processMatrixAttributeVo.getUuid(), processMatrixAttributeVo);
				}
				/** 属性集合去重 **/
				List<String> distinctColumList = new ArrayList<>();
				for (String column : columnList) {
					if (!processMatrixAttributeMap.containsKey(column)) {
						throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
					}
					if (!distinctColumList.contains(column)) {
						distinctColumList.add(column);
					}
				}
				dataVo.setColumnList(distinctColumList);
				if (CollectionUtils.isNotEmpty(valueList)) {
					for (String value : valueList) {
						if (value.contains("&=&")) {
							List<ProcessMatrixColumnVo> sourceColumnList = new ArrayList<>();
							String[] split = value.split("&=&");
							if (StringUtils.isNotBlank(columnList.get(0))) {
								ProcessMatrixColumnVo processMatrixColumnVo = new ProcessMatrixColumnVo(columnList.get(0), split[0]);
								processMatrixColumnVo.setExpression(Expression.EQUAL.getExpression());
								sourceColumnList.add(processMatrixColumnVo);
							}
							dataVo.setSourceColumnList(sourceColumnList);
							if (columnList.size() >= 2 && StringUtils.isNotBlank(columnList.get(1))) {
								ProcessMatrixAttributeVo processMatrixAttribute = processMatrixAttributeMap.get(columnList.get(1));
								if (processMatrixAttribute == null) {
									throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), columnList.get(1));
								}
								List<String> uuidList = matrixService.matrixAttributeValueKeyWordSearch(processMatrixAttribute, split[1], dataVo.getPageSize());
								if (CollectionUtils.isNotEmpty(uuidList)) {
									dataVo.setUuidList(uuidList);
									List<Map<String, String>> dataMapList = matrixDataMapper.getDynamicTableDataByColumnList2(dataVo);
									for (Map<String, String> dataMap : dataMapList) {
										Map<String, JSONObject> resultMap = new HashMap<>(dataMap.size());
										for (Entry<String, String> entry : dataMap.entrySet()) {
											String attributeUuid = entry.getKey();
											resultMap.put(attributeUuid, matrixService.matrixAttributeValueHandle(processMatrixAttributeMap.get(attributeUuid), entry.getValue()));
										}
										JSONObject textObj = resultMap.get(columnList.get(1));
										if (MapUtils.isNotEmpty(textObj) && Objects.equal(textObj.get("text"), split[1])) {
											resultList.add(resultMap);
											;
										}
									}
								} else {
									return returnObj;
								}

							} else {
								return returnObj;
							}
						}
					}
				} else {
					if (StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
						ProcessMatrixAttributeVo processMatrixAttribute = processMatrixAttributeMap.get(keywordColumn);
						if (processMatrixAttribute == null) {
							throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), keywordColumn);
						}
						List<String> uuidList = matrixService.matrixAttributeValueKeyWordSearch(processMatrixAttribute, dataVo.getKeyword(), dataVo.getPageSize());
						if (CollectionUtils.isNotEmpty(uuidList)) {
							dataVo.setUuidList(uuidList);
						}
					}
					List<Map<String, String>> dataMapList = matrixDataMapper.getDynamicTableDataByColumnList2(dataVo);
					for (Map<String, String> dataMap : dataMapList) {
						Map<String, JSONObject> resultMap = new HashMap<>(dataMap.size());
						for (Entry<String, String> entry : dataMap.entrySet()) {
							String attributeUuid = entry.getKey();
							resultMap.put(attributeUuid, matrixService.matrixAttributeValueHandle(processMatrixAttributeMap.get(attributeUuid), entry.getValue()));
						}
						resultList.add(resultMap);
					}
				}
			}

		} else {
			ProcessMatrixExternalVo externalVo = matrixExternalMapper.getMatrixExternalByMatrixUuid(dataVo.getMatrixUuid());
			if (externalVo == null) {
				throw new MatrixExternalNotFoundException(dataVo.getMatrixUuid());
			}
			IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
			IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
			if (handler == null) {
				throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
			}
			List<String> attributeList = new ArrayList<>();
			List<ProcessMatrixAttributeVo> processMatrixAttributeList = matrixService.getExternalMatrixAttributeList(dataVo.getMatrixUuid(), integrationVo);
			for (ProcessMatrixAttributeVo processMatrixAttributeVo : processMatrixAttributeList) {
				attributeList.add(processMatrixAttributeVo.getUuid());
			}

			for (String column : columnList) {
				if (!attributeList.contains(column)) {
					throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), column);
				}
			}
			if (CollectionUtils.isNotEmpty(valueList)) {
				for (String value : valueList) {
					if (value.contains("&=&")) {
						List<ProcessMatrixColumnVo> sourceColumnList = new ArrayList<>();
						String[] split = value.split("&=&");
						for (int i = 0; i < split.length; i++) {
							String column = columnList.get(i);
							if (StringUtils.isNotBlank(column)) {
								ProcessMatrixColumnVo processMatrixColumnVo = new ProcessMatrixColumnVo(column, split[i]);
								processMatrixColumnVo.setExpression(Expression.EQUAL.getExpression());
								sourceColumnList.add(processMatrixColumnVo);
							}
						}
						// dataVo.setSourceColumnList(sourceColumnList);
						jsonObj.put("sourceColumnList", sourceColumnList);
						integrationVo.getParamObj().putAll(jsonObj);
						IntegrationResultVo resultVo = handler.sendRequest(integrationVo, ProcessRequestFrom.MATRIX);
						if (StringUtils.isNotBlank(resultVo.getError())) {
							logger.error(resultVo.getError());
							throw new MatrixExternalException("外部接口访问异常");
						} else {
							resultList.addAll(matrixService.getExternalDataTbodyList(resultVo, columnList, dataVo.getPageSize(), null));
						}
					}
				}
			} else {
				if (StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
					if (!attributeList.contains(keywordColumn)) {
						throw new MatrixAttributeNotFoundException(dataVo.getMatrixUuid(), keywordColumn);
					}
				}
				if (StringUtils.isNotBlank(keywordColumn) && StringUtils.isNotBlank(dataVo.getKeyword())) {
					ProcessMatrixColumnVo processMatrixColumnVo = new ProcessMatrixColumnVo();
					processMatrixColumnVo.setColumn(keywordColumn);
					processMatrixColumnVo.setExpression(Expression.LIKE.getExpression());
					processMatrixColumnVo.setValue(dataVo.getKeyword());
					List<ProcessMatrixColumnVo> sourceColumnList = dataVo.getSourceColumnList();
					if (CollectionUtils.isEmpty(sourceColumnList)) {
						sourceColumnList = new ArrayList<>();
					}
					sourceColumnList.add(processMatrixColumnVo);
					jsonObj.put("sourceColumnList", sourceColumnList);
				}
				integrationVo.getParamObj().putAll(jsonObj);
				IntegrationResultVo resultVo = handler.sendRequest(integrationVo, ProcessRequestFrom.MATRIX);
				if (StringUtils.isNotBlank(resultVo.getError())) {
					logger.error(resultVo.getError());
					throw new MatrixExternalException("外部接口访问异常");
				} else {
					resultList = matrixService.getExternalDataTbodyList(resultVo, columnList, dataVo.getPageSize(), null);
				}
			}
		}
		returnObj.put("columnDataList", resultList);
		return returnObj;
	}
}
