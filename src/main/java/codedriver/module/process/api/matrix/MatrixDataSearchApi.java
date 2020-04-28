package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.process.dao.mapper.MatrixAttributeMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixColumnVo;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixDispatcherVo;
import codedriver.framework.process.dto.ProcessMatrixFormComponentVo;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixDataService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-30 16:34
 **/
@Service
public class MatrixDataSearchApi extends ApiComponentBase {

    @Autowired
    private MatrixDataService dataService;

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixAttributeMapper attributeMapper;
    
    @Override
    public String getToken() {
        return "matrix/data/search";
    }

    @Override
    public String getName() {
        return "矩阵数据检索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "keyword", desc = "关键字", type = ApiParamType.STRING),
             @Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true),
             @Param( name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN),
             @Param( name = "pageSize", desc = "显示条目数", type = ApiParamType.INTEGER),
             @Param( name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER)})
    @Output({ @Param( name = "tbodyList", desc = "矩阵数据集合"),
              @Param( name = "theadList", desc = "矩阵属性集合"),
              @Param( explode = BasePageVo.class)})
    @Description( desc = "矩阵数据检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        ProcessMatrixDataVo dataVo = JSON.toJavaObject(jsonObj, ProcessMatrixDataVo.class);
    	if(matrixMapper.checkMatrixIsExists(dataVo.getMatrixUuid()) == 0) {
    		throw new MatrixNotFoundException(dataVo.getMatrixUuid());
    	}
    	List<ProcessMatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(dataVo.getMatrixUuid());
        if (CollectionUtils.isNotEmpty(attributeVoList)){
//        	List<Map<String, String>> tbodyList = dataService.searchDynamicTableData(dataVo);
//        	returnObj.put("tbodyList", dataService.matrixValueHandle(attributeVoList, tbodyList));
        	returnObj.put("tbodyList", dataService.searchDynamicTableData(dataVo));
            List<ProcessMatrixColumnVo> processMatrixColumnList = dataVo.getSourceColumnList();
            if(CollectionUtils.isNotEmpty(processMatrixColumnList)) {
            	JSONArray headList = new JSONArray();
                JSONObject selectionObj = new JSONObject();
                selectionObj.put("key", "selection");
                selectionObj.put("width", 60);
                headList.add(selectionObj);
                
                for(ProcessMatrixColumnVo processMatrixColumnVo : processMatrixColumnList) {
                	JSONObject columnObj = new JSONObject();
                	columnObj.put("title", processMatrixColumnVo.getValue());
                	columnObj.put("key", processMatrixColumnVo.getColumn());
                    headList.add(columnObj);
                }
                
                JSONObject actionObj = new JSONObject();
                actionObj.put("title", "");
                actionObj.put("key", "action");
                actionObj.put("align", "right");
                actionObj.put("width", 10);
                headList.add(actionObj);
                
                returnObj.put("theadList", headList);
            }
            
            if (dataVo.getNeedPage()){
                returnObj.put("pageCount", dataVo.getPageCount());
                returnObj.put("rowNum", dataVo.getRowNum());
                returnObj.put("pageSize", dataVo.getPageSize());
                returnObj.put("currentPage", dataVo.getCurrentPage());
            }
        }
        
        List<ProcessMatrixDispatcherVo> dispatcherVoList = matrixMapper.getMatrixDispatcherByMatrixUuid(dataVo.getMatrixUuid());
        returnObj.put("dispatcherVoList", dispatcherVoList);
        List<ProcessMatrixFormComponentVo> componentVoList = matrixMapper.getMatrixFormComponentByMatrixUuid(dataVo.getMatrixUuid());
        returnObj.put("componentVoList", componentVoList);
        returnObj.put("usedCount", dispatcherVoList.size() + componentVoList.size());
        return returnObj;
    }
}
