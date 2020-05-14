package codedriver.module.process.api.matrix;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.exception.integration.IntegrationHandlerNotFoundException;
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
import codedriver.framework.process.dto.ProcessMatrixDataVo;
import codedriver.framework.process.dto.ProcessMatrixExternalVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.matrix.MatrixExternalException;
import codedriver.framework.process.exception.matrix.MatrixExternalNotFoundException;
import codedriver.framework.process.exception.matrix.MatrixNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
import codedriver.module.process.util.ExcelUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-26 19:04
 **/
@Service
public class MatrixExportApi extends BinaryStreamApiComponentBase {

    @Autowired
    private MatrixAttributeMapper attributeMapper;

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixDataMapper matrixDataMapper;
	
	@Autowired
	private IntegrationMapper integrationMapper;

    @Autowired
    private MatrixExternalMapper externalMapper;
    
    @Override
    public String getToken() {
        return "matrix/export";
    }

    @Override
    public String getName() {
        return "矩阵导出接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "matrixUuid", desc = "矩阵uuid", type = ApiParamType.STRING, isRequired = true)})
    @Description( desc = "矩阵导出接口")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	String matrixUuid = paramObj.getString("matrixUuid");
        ProcessMatrixVo matrixVo = matrixMapper.getMatrixByUuid(matrixUuid);
        if(matrixVo == null) {
        	throw new MatrixNotFoundException(matrixUuid);
        }

        HSSFWorkbook workbook = null;
        if(ProcessMatrixType.CUSTOM.getValue().equals(matrixVo.getType())) {
        	List<ProcessMatrixAttributeVo> attributeVoList = attributeMapper.getMatrixAttributeByMatrixUuid(matrixUuid);
            if (CollectionUtils.isNotEmpty(attributeVoList)){
                List<String> headerList = new ArrayList<>();
                List<String> columnList = new ArrayList<>();
                List<List<String>> columnSelectValueList = new ArrayList<>();
                headerList.add("uuid");
                columnList.add("uuid");
                columnSelectValueList.add(new ArrayList<>());
                for (ProcessMatrixAttributeVo attributeVo : attributeVoList){
                    headerList.add(attributeVo.getName());
                    columnList.add(attributeVo.getUuid());
                    List<String> selectValueList = new ArrayList<>();
                    decodeDataConfig(attributeVo, selectValueList);
                    columnSelectValueList.add(selectValueList);
                }
                ProcessMatrixDataVo dataVo = new ProcessMatrixDataVo();
                dataVo.setMatrixUuid(paramObj.getString("matrixUuid"));
                dataVo.setColumnList(columnList);
                
                int currentPage = 1;
                dataVo.setPageSize(1000);
                int rowNum = matrixDataMapper.getDynamicTableDataCount(dataVo);
                System.out.println("rowNum:" + rowNum);
                int pageCount = PageUtil.getPageCount(rowNum, dataVo.getPageSize());
                System.out.println("pageCount:" + pageCount);
                while(currentPage <= pageCount) {
                    dataVo.setCurrentPage(currentPage);
                    dataVo.setStartNum(null);
                    System.out.println("currentPage:" + currentPage);
                	List<Map<String, String>> dataMapList = matrixDataMapper.searchDynamicTableData(dataVo);
                	workbook = ExcelUtil.createExcel(workbook, headerList, columnList, columnSelectValueList, dataMapList);
                	currentPage++;
                }              
            }
        }else {
        	ProcessMatrixExternalVo externalVo = externalMapper.getMatrixExternalByMatrixUuid(matrixUuid);
            if(externalVo == null) {
            	throw new MatrixExternalNotFoundException(matrixUuid);
            }
            IntegrationVo integrationVo = integrationMapper.getIntegrationByUuid(externalVo.getIntegrationUuid());
            IIntegrationHandler handler = IntegrationHandlerFactory.getHandler(integrationVo.getHandler());
    		if (handler == null) {
    			throw new IntegrationHandlerNotFoundException(integrationVo.getHandler());
    		}

            IntegrationResultVo resultVo = handler.sendRequest(integrationVo);
            if(StringUtils.isNotBlank(resultVo.getError())) {
        		throw new MatrixExternalException(resultVo.getError());
        	}else if(StringUtils.isNotBlank(resultVo.getTransformedResult())) {
    			JSONObject transformedResult = JSONObject.parseObject(resultVo.getTransformedResult());
    			if(MapUtils.isNotEmpty(transformedResult)) {
    				List<String> headerList = new ArrayList<>();
    				List<String> columnList = new ArrayList<>();
    				JSONArray theadList = transformedResult.getJSONArray("theadList");
    				if (CollectionUtils.isNotEmpty(theadList)){
    					for (int i = 0; i < theadList.size(); i++){
    						JSONObject obj = theadList.getJSONObject(i);
    						headerList.add(obj.getString("title"));
    						columnList.add(obj.getString("key"));
    					}
    				}
    				List<Map<String, String>> dataMapList = (List<Map<String, String>>) transformedResult.get("tbodyList");
    				workbook = ExcelUtil.createExcel(workbook, headerList, columnList, null, dataMapList);
    			}
    		}
        }
        
        if(workbook == null) {
        	workbook = new HSSFWorkbook();
        }
        String fileNameEncode = matrixVo.getName() + ".xls";
        Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
        if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
            fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
        } else {
            fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
        }
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;fileName=\"" + fileNameEncode + "\"");

        try (OutputStream os = response.getOutputStream();){               	
            workbook.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }      
        return null;
    }

    //解析config，抽取属性下拉框值
    private void decodeDataConfig(ProcessMatrixAttributeVo attributeVo, List<String> selectValueList){
        if (StringUtils.isNotBlank(attributeVo.getConfig())){
            String config = attributeVo.getConfig();
            JSONObject configObj = JSONObject.parseObject(config);
            JSONArray dataList = configObj.getJSONArray("dataList");
            if(CollectionUtils.isNotEmpty(dataList)) {
            	for(int i = 0; i < dataList.size(); i++) {
            		JSONObject dataObj = dataList.getJSONObject(i);
            		if(MapUtils.isNotEmpty(dataObj)) {
            			String value = dataObj.getString("value");
            			if(StringUtils.isNotBlank(value)) {
                    		selectValueList.add(value);
            			}
            		}
            	}
            }
//            if (AttributeHandler.SELECT.getValue().equals(configObj.getString("handler"))){
//                if (configObj.containsKey("config")){
//                    JSONArray configArray = configObj.getJSONArray("config");
//                    for (int i = 0; i < configArray.size(); i++){
//                        JSONObject param = configArray.getJSONObject(i);
//                        selectValueList.add(param.getString("value"));
//                    }
//                }
//            }
        }
    }
}
