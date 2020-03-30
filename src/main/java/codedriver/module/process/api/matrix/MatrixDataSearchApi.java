package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.MatrixDataService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
              @Param( name = "pageCount", desc = "页码数", type = ApiParamType.INTEGER),
              @Param( name = "rowNum", desc = "统计个数", type = ApiParamType.INTEGER),
              @Param( name = "pageSize", desc = "页最大条数", type = ApiParamType.INTEGER),
              @Param( name = "currentPage", desc = "当前页码", type = ApiParamType.INTEGER)})
    @Description( desc = "矩阵数据检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        ProcessMatrixDataVo dataVo = new ProcessMatrixDataVo();
        dataVo.setKeyword(jsonObj.getString("keyword"));
        dataVo.setMatrixUuid(jsonObj.getString("matrixUuid"));
        returnObj.put("tbodyList", dataService.searchDynamicTableData(dataVo));
        List<String> headList = new ArrayList<>();
        headList.add("id");
        headList.add("uuid");
        headList.addAll(dataVo.getColumnList());
        returnObj.put("theadList", headList);
        if (dataVo.getNeedPage()){
            returnObj.put("pageCount", dataVo.getPageCount());
            returnObj.put("rowNum", dataVo.getRowNum());
            returnObj.put("pageSize", dataVo.getPageSize());
            returnObj.put("currentPage", dataVo.getCurrentPage());
        }
        return returnObj;
    }
}
