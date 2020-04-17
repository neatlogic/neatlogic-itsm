package codedriver.module.process.api.matrix;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.constvalue.ProcessMatrixType;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
public class MatrixColumnDataSearchForTableApi extends ApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;
    
    @Autowired
    private MatrixDataMapper matrixDataMapper;
    
    @Autowired
    private MatrixAttributeMapper matrixAttributeMapper;

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
    	@Param( name = "columnList", desc = "目标属性集合，数据按这个字段顺序返回，参数名由“targetColumnList”改为“columnList”", type = ApiParamType.JSONARRAY, isRequired = true),
    	@Param( name = "searchColumnList ", desc = "搜索属性集合", type = ApiParamType.JSONARRAY),
        @Param( name = "sourceColumnList", desc = "搜索过滤值集合，参数名由“searchValueList”改为“sourceColumnList”", type = ApiParamType.JSONARRAY),
    	@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
		@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
		@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Description(desc = "矩阵属性数据查询-table接口")
    @Output({ 
    	@Param( name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "属性数据集合"),
    	@Param( name = "theadList", type = ApiParamType.JSONARRAY, desc = "属性列名集合"),
    	@Param( name = "searchColumnDetailList", type = ApiParamType.JSONARRAY, desc = "搜索属性详情集合"),
    	@Param(explode = BasePageVo.class)
    	})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        ProcessMatrixDataVo dataVo = JSON.toJavaObject(jsonObj, ProcessMatrixDataVo.class);
//        String matrixUuid = jsonObj.getString("matrixUuid");
        ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(dataVo.getMatrixUuid());
        if(matrixVo == null) {
        	throw new MatrixNotFoundException(dataVo.getMatrixUuid());
        }
//        List<ProcessMatrixColumnVo> sourceColumnList = JSON.parseArray(jsonObj.getString("searchValueList"), ProcessMatrixColumnVo.class);
//        List<String> targetColumnList =  JSONObject.parseArray(jsonObj.getJSONArray("targetColumnList").toJSONString(), String.class);
//        dataVo.setSourceColumnList(sourceColumnList);
//        dataVo.setColumnList(targetColumnList);
        if (matrixVo.getType().equals(ProcessMatrixType.CUSTOM.getValue())){
            List<Map<String, String>> dataMapList = matrixDataMapper.getDynamicTableDataByColumnList(dataVo);
            returnObj.put("tbodyList", dataMapList);
        }else {
            //外部数据源矩阵  暂未实现
            return null;
        }

    	List<String> searchColumnList =JSONObject.parseArray(jsonObj.getString("searchColumnList"), String.class);
        if(CollectionUtils.isNotEmpty(searchColumnList)) {
        	JSONArray searchColumnDetailList = new JSONArray();
        	List<ProcessMatrixAttributeVo> matrixAttributeSearchList =  matrixAttributeMapper.getMatrixAttributeByMatrixUuidList(searchColumnList,dataVo.getMatrixUuid());
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
    	List<ProcessMatrixAttributeVo> matrixAttributeTheadList = matrixAttributeMapper.getMatrixAttributeByMatrixUuidList(dataVo.getColumnList(), dataVo.getMatrixUuid());
    	for(String column : dataVo.getColumnList()) {
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
