package codedriver.module.process.integration.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.integration.core.IntegrationHandlerBase;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.integration.dto.PatternVo;
@Component
public class MatrixIntegrationHandler extends IntegrationHandlerBase {

	@Override
	public String getName() {
		return Type.MATRIX.getText();
	}

	@Override
	public String getType() {
		return Type.MATRIX.getValue();
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

}
