package codedriver.module.process.integration.handler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.matrix.exception.MatrixExternalException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.integration.core.IntegrationHandlerBase;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.integration.dto.PatternVo;
@Component
public class MatrixIntegrationHandler extends IntegrationHandlerBase {

	@Override
	public String getName() {
		return "矩阵外部数据源查询";
	}

	@Override
	public Integer hasPattern() {
		return 1;
	}

	@Override
	public List<PatternVo> getInputPattern() {
		List<PatternVo> jsonList = new ArrayList<>();
		jsonList.add(new PatternVo("keyword", "input", ApiParamType.STRING, 0, "关键字"));
		jsonList.add(new PatternVo("currentPage", "input", ApiParamType.INTEGER, 0, "当前页"));
		jsonList.add(new PatternVo("pageSize", "input", ApiParamType.INTEGER, 0, "每页大小"));
		jsonList.add(new PatternVo("needPage", "input", ApiParamType.BOOLEAN, 0, "是否分页"));
		PatternVo sourceColumnList = new PatternVo("sourceColumnList", "input", ApiParamType.JSONARRAY, 0, "过滤参数列表");
		sourceColumnList.addChild(new PatternVo("column", "input", ApiParamType.STRING, 0, "过滤参数名称"));
		sourceColumnList.addChild(new PatternVo("expression", "input", ApiParamType.STRING, 0, "表达式"));
		sourceColumnList.addChild(new PatternVo("value", "input", ApiParamType.STRING, 0, "过滤参数值"));
		jsonList.add(sourceColumnList);
		return jsonList;
	}

	@Override
	public List<PatternVo> getOutputPattern() {
		List<PatternVo> jsonList = new ArrayList<>();
		PatternVo theadList = new PatternVo("theadList", "output", ApiParamType.JSONARRAY, 0, "表头列表");
		theadList.addChild(new PatternVo("key", "output", ApiParamType.STRING, 0, "表头键值"));
		theadList.addChild(new PatternVo("title", "output", ApiParamType.STRING, 0, "表头名称"));
		jsonList.add(theadList);
		PatternVo tbodyList = new PatternVo("tbodyList", "output", ApiParamType.JSONARRAY, 0, "数据列表");
		jsonList.add(tbodyList);
		jsonList.add(new PatternVo("currentPage", "output", ApiParamType.INTEGER, 0, "当前页"));
		jsonList.add(new PatternVo("rowNum", "output", ApiParamType.INTEGER, 0, "条目数量"));
		jsonList.add(new PatternVo("pageSize", "output", ApiParamType.INTEGER, 0, "每页大小"));
		jsonList.add(new PatternVo("pageCount", "output", ApiParamType.INTEGER, 0, "页数"));
		return jsonList;
	}

	@Override
	protected void beforeSend(IntegrationVo integrationVo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void afterReturn(IntegrationVo integrationVo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void validate(IntegrationResultVo resultVo) throws ApiRuntimeException {
		if(resultVo != null && StringUtils.isBlank(resultVo.getError())
				&& StringUtils.isNotBlank(resultVo.getTransformedResult())){
			JSONObject transformedResult = null;
			try {
				transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
			}catch (Exception ex){
				throw new MatrixExternalException("返回结果不是JSON格式");
			}
			if(MapUtils.isNotEmpty(transformedResult)){
				Set<String> keys = transformedResult.keySet();
				Set<String> keySet = new HashSet<>();
				getOutputPattern().stream().forEach(o -> keySet.add(o.getName()));
				if(!CollectionUtils.containsAll(keys,keySet)){
					throw new MatrixExternalException("返回结果不符合格式，缺少" + JSON.toJSONString(CollectionUtils.removeAll(keySet, keys)));
				}
				JSONArray theadList = transformedResult.getJSONArray("theadList");
				if(CollectionUtils.isNotEmpty(theadList)){
					for(int i = 0; i < theadList.size();i++){
						if(!theadList.getJSONObject(i).containsKey("key") || !theadList.getJSONObject(i).containsKey("title")){
							throw new MatrixExternalException("返回结果不符合格式,theadList缺少key或title");
						}
					}
				}else{
					throw new MatrixExternalException("返回结果不符合格式,缺少theadList");
				}
			}
		}
	}
}
