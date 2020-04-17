package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.process.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
import codedriver.module.process.service.MatrixService;
import codedriver.module.process.util.ExcelUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-04-07 10:33
 **/
@Service
public class MatrixExternalExportApi extends BinaryStreamApiComponentBase {

    @Autowired
    private MatrixService matrixService;

    @Autowired
    private MatrixMapper matrixMapper;

    @Override
    public String getToken() {
        return "matrix/external/export";
    }

    @Override
    public String getName() {
        return "外部数据源导出接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true)})
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(paramObj.getString("matrixUuid"));
        if(matrixVo == null) {
        	throw new MatrixNotFoundException(paramObj.getString("matrixUuid"));
        }
        JSONObject dataObj = matrixService.getMatrixExternalData(paramObj.getString("matrixUuid"));
        List<String> headerList = dataObj.getJSONArray("headerList").toJavaList(String.class);
        List<String> columnList = dataObj.getJSONArray("columnList").toJavaList(String.class);
        List<Map<String, String>> dataMapList= (List<Map<String,String>>) dataObj.get("dataMapList");
        String fileNameEncode = matrixVo.getName() + ".xls";
        Boolean flag = request.getHeader("User-Agent").indexOf("like Gecko") > 0;
        if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
            fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
        } else {
            fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
        }
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;fileName=\"" + fileNameEncode + "\"");
        ExcelUtil.exportExcel( headerList, columnList, dataMapList, response.getOutputStream());
        return null;
    }
}
