package codedriver.module.process.api.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixDataService;

@Service
public class MatrixColumnDataSearchForTableApi extends ApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;
    
    @Autowired
    private MatrixDataMapper matrixDataMapper;
    
    @Autowired
    private MatrixAttributeMapper matrixAttributeMapper;

    @Autowired
    private MatrixDataService matrixDataService;

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
    	@Param( name = "matrixUuid", desc = "矩阵Uuid", type = ApiParamType.STRING, isRequired = true),
    	@Param( name = "targetColumnList", desc = "目标属性集合，数据按这个字段顺序返回", type = ApiParamType.JSONARRAY, isRequired = true),
    	@Param( name = "searchColumnList ", desc = "搜索属性集合", type = ApiParamType.JSONARRAY),
        @Param( name = "searchValueList", desc = "搜索过滤值集合", type = ApiParamType.JSONARRAY),
    	@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Description(desc = "矩阵属性数据查询-table接口")
    @Output({ @Param( name = "columnDataList", type = ApiParamType.JSONARRAY, desc = "属性数据集合")})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        String matrixUuid = jsonObj.getString("matrixUuid");
        ProcessMatrixDataVo dataVo = new ProcessMatrixDataVo();
        if (jsonObj.containsKey("searchValueList")){
        	List<ProcessMatrixColumnVo> sourceColumnVoList = new ArrayList<>();
        	JSONArray sourceArray = jsonObj.getJSONArray("searchValueList");
            for (int i = 0; i < sourceArray.size(); i++){
                JSONObject sourceObj = sourceArray.getJSONObject(i);
                ProcessMatrixColumnVo sourceColumn = JSON.toJavaObject(sourceObj, ProcessMatrixColumnVo.class);
                sourceColumnVoList.add(sourceColumn);
            }
            dataVo.setSourceColumnList(sourceColumnVoList);
        }
        List<String> targetColumnList =  JSONObject.parseArray(jsonObj.getJSONArray("targetColumnList").toJSONString(), String.class);
        dataVo.setColumnList(targetColumnList);
        dataVo.setMatrixUuid(matrixUuid);
        ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if (matrixVo.getType().equals(ProcessMatrixType.CUSTOM.getValue())){
            List<Map<String, String>> dataMapList = matrixDataService.getDynamicTableDataByColumnList(dataVo);
            returnObj.put("tbodyList", dataMapList);
        }else {
            //外部数据源矩阵  暂未实现
            return null;
        }
        if(jsonObj.containsKey("searchColumnList")) {
        	JSONArray searchColumnDetailList = new JSONArray();
        	List<String> searchColumnList =JSONObject.parseArray(jsonObj.getJSONArray("searchColumnList").toJSONString(), String.class);
        	List<ProcessMatrixAttributeVo> matrixAttributeSearchList =  matrixAttributeMapper.getMatrixAttributeByMatrixUuidList(searchColumnList,matrixUuid);
        	for(String column :searchColumnList) {
        		for(ProcessMatrixAttributeVo matrixAttributeSearch:matrixAttributeSearchList) {
            		if(matrixAttributeSearch.getUuid().equals(column)) {
            			searchColumnDetailList.add(matrixAttributeSearch);
            		}
            	}
        	}
        	 returnObj.put("searchColumnDetailList", searchColumnDetailList);
        }
        //theadList
    	JSONArray theadList = new JSONArray();
    	List<ProcessMatrixAttributeVo> matrixAttributeTheadList =  matrixAttributeMapper.getMatrixAttributeByMatrixUuidList(targetColumnList,matrixUuid);
    	for(String column :targetColumnList) {
    		for(ProcessMatrixAttributeVo matrixAttributeSearch:matrixAttributeTheadList) {
        		if(matrixAttributeSearch.getUuid().equals(column)) {
        			theadList.add(matrixAttributeSearch);
        		}
        	}
    	}
    	returnObj.put("theadList", theadList);
        
        if(dataVo.getNeedPage()) {
			int rowNum = matrixDataMapper.getDynamicTableDataByColumnCount(dataVo);
			int pageCount = PageUtil.getPageCount(rowNum, dataVo.getPageSize());
			returnObj.put("currentPage", dataVo.getCurrentPage());
			returnObj.put("pageSize", dataVo.getPageSize());
			returnObj.put("pageCount", pageCount);
			returnObj.put("rowNum", rowNum);
		}
        
        return returnObj;
    }
}
