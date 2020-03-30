package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:06
 **/
@Service
public class MatrixSearchApi extends ApiComponentBase {

    @Autowired
    private MatrixService matrixService;

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

    @Input({ @Param( name = "keyword", desc = "关键字", type = ApiParamType.STRING, isRequired = true),
             @Param( name = "currentPage", desc = "当前页码", type = ApiParamType.INTEGER),
             @Param( name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN),
             @Param( name = "pageSize", desc = "页面展示数", type = ApiParamType.INTEGER)})
    @Output({ @Param( name = "tbodyList", desc = "矩阵数据源列表", explode = ProcessMatrixVo[].class),
            @Param( name = "pageCount", desc = "页码数", type = ApiParamType.INTEGER),
            @Param( name = "rowNum", desc = "统计个数", type = ApiParamType.INTEGER),
            @Param( name = "pageSize", desc = "页最大条数", type = ApiParamType.INTEGER),
            @Param( name = "currentPage", desc = "当前页码", type = ApiParamType.INTEGER)})
    @Description(desc = "数据源矩阵检索")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        ProcessMatrixVo matrix = new ProcessMatrixVo();
        matrix.setKeyword(jsonObj.getString("keyword"));
        if (jsonObj.containsKey("currentPage")){
            matrix.setCurrentPage(jsonObj.getInteger("currentPage"));
        }
        if (jsonObj.containsKey("needPage")){
            matrix.setNeedPage(jsonObj.getBoolean("needPage"));
        }
        if (jsonObj.containsKey("pageSize")){
            matrix.setPageSize(jsonObj.getInteger("pageSize"));
        }
        returnObj.put("tbodyList", matrixService.searchMatrix(matrix));
        if (matrix.getNeedPage()){
            returnObj.put("pageCount", matrix.getPageCount());
            returnObj.put("rowNum", matrix.getRowNum());
            returnObj.put("pageSize", matrix.getPageSize());
            returnObj.put("currentPage", matrix.getCurrentPage());
        }
        return returnObj;
    }
}
