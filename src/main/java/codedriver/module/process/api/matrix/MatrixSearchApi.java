package codedriver.module.process.api.matrix;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:06
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class MatrixSearchApi extends ApiComponentBase {

    @Autowired
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/search";
    }

    @Override
    public String getName() {
        return "数据源矩阵检索";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ 
    	@Param( name = "keyword", desc = "关键字", type = ApiParamType.STRING),
    	@Param( name = "type", desc = "类型", type = ApiParamType.ENUM, rule = "custom,external"),
        @Param( name = "currentPage", desc = "当前页码", type = ApiParamType.INTEGER),
        @Param( name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN),
        @Param( name = "pageSize", desc = "页面展示数", type = ApiParamType.INTEGER),
        @Param( name = "valueList", desc = "精确匹配回显数据参数", type = ApiParamType.JSONARRAY)
    })
    @Output({ 
    	@Param( name = "tbodyList", desc = "矩阵数据源列表", explode = ProcessMatrixVo[].class),
        @Param( explode = BasePageVo.class)
    })
    @Description(desc = "数据源矩阵检索")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        List<String> valueList = JSON.parseArray(jsonObj.getString("valueList"), String.class);
        if(CollectionUtils.isNotEmpty(valueList)) {
        	List<ProcessMatrixVo> tbodyList = new ArrayList<>();
        	for(String uuid : valueList) {
        		ProcessMatrixVo processMatrixVo = matrixMapper.getMatrixByUuid(uuid);
        		if(processMatrixVo != null) {
        			tbodyList.add(processMatrixVo);
        		}
        	}
        	returnObj.put("tbodyList", tbodyList);
        }else {
        	 ProcessMatrixVo matrix = JSON.toJavaObject(jsonObj, ProcessMatrixVo.class);
             if (matrix.getNeedPage()){
                 int rowNum = matrixMapper.searchMatrixCount(matrix);
                 matrix.setPageCount(PageUtil.getPageCount(rowNum, matrix.getPageSize()));
                 returnObj.put("pageCount", matrix.getPageCount());
                 returnObj.put("rowNum", rowNum);
                 returnObj.put("pageSize", matrix.getPageSize());
                 returnObj.put("currentPage", matrix.getCurrentPage());
             }
             returnObj.put("tbodyList", matrixMapper.searchMatrix(matrix));
        }
        return returnObj;
    }
}
