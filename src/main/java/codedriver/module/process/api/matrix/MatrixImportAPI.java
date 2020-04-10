package codedriver.module.process.api.matrix;

import codedriver.framework.process.dao.mapper.MatrixDataMapper;
import codedriver.framework.process.dao.mapper.MatrixMapper;
import codedriver.framework.process.dto.ProcessMatrixAttributeVo;
import codedriver.framework.process.dto.ProcessMatrixVo;
import codedriver.framework.process.exception.process.*;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.BinaryStreamApiComponentBase;
import codedriver.module.process.service.MatrixAttributeService;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-04-01 16:32
 **/
@Service
public class MatrixImportAPI extends BinaryStreamApiComponentBase {

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MatrixMapper matrixMapper;

    @Autowired
    private MatrixAttributeService attributeService;

    @Autowired
    private MatrixDataMapper dataMapper;

    @Override
    public String getToken() {
        return "matrix/import";
    }

    @Override
    public String getName() {
        return "矩阵导入接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Description(desc = "矩阵导入接口")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject returnObj = new JSONObject();
        int update = 0, insert = 0, unExist = 0;
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        //获取所有导入文件
        Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
        if(multipartFileMap == null || multipartFileMap.isEmpty()) {
            throw new MatrixFileNotFoundException();
        }
        MultipartFile multipartFile = null;
        InputStream is = null;
        for(Map.Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
            multipartFile = entry.getValue();
            is = multipartFile.getInputStream();
            String name = multipartFile.getName();
            if (StringUtils.isNotBlank(name)){
                String matrixName = name.substring(0, name.indexOf("."));
                ProcessMatrixVo matrixVo = matrixMapper.getMatrixByName(matrixName);
                if (matrixVo == null){
                    throw new MatrixNotFoundException(matrixName);
                }
                List<ProcessMatrixAttributeVo> attributeVoList = attributeService.searchMatrixAttribute(matrixVo.getUuid());
                if (CollectionUtils.isNotEmpty(attributeVoList)){
                    Map<String, String> headerMap = new HashMap<>();
                    for (ProcessMatrixAttributeVo attributeVo : attributeVoList){
                        headerMap.put(attributeVo.getName(), attributeVo.getUuid());
                    }
                    if (is != null){
                        Workbook wb = WorkbookFactory.create(is);
                        Sheet sheet = wb.getSheetAt(0);
                        int rowNum = sheet.getLastRowNum();
                        //获取头栏位
                        Row headerRow = sheet.getRow(0);
                        int colNum = headerRow.getLastCellNum();
                        //attributeList 缺少uuid
                        if (colNum != attributeVoList.size() + 1){
                            throw new MatrixHeaderMisMatchException(matrixName);
                        }
                        int count = 0;
                        int uuidIndex = 0;
                        List<String> columnList = new ArrayList<>();
                        //获取头name集合，排好顺序
                        for (int i = 0; i < colNum; i ++){
                            Cell cell = headerRow.getCell(i);
                            String columnName = cell.getStringCellValue();
                            if (("uuid").equals(columnName)){
                                columnList.add("uuid");
                                uuidIndex = count;
                                count++;
                            }
                            if (headerMap.containsKey(columnName)){
                                columnList.add(headerMap.get(columnName));
                                count++;
                            }
                        }
                        if (count != colNum){
                            throw new MatrixHeaderMisMatchException(matrixName);
                        }
                        //解析数据
                        for (int i = 1; i < rowNum + 1; i++){
                            Row row = sheet.getRow(i);
                            List<String> dataList = new ArrayList<>();
                            for (int j = 0; j < colNum; j++){
                                Cell cell = row.getCell(j);
                                String value = getCellValue(cell);
                                dataList.add(StringUtils.isBlank(value)?UUID.randomUUID().toString().replace("-", ""):value);
                            }
                            //获取数据uuid
                            String uuid = getCellValue(row.getCell(uuidIndex));
                            if(StringUtils.isNotBlank(uuid)){
                                if (dataMapper.getDynamicTableDataCountByUuid(uuid, matrixVo.getUuid()) == 1){
                                    dataMapper.deleteDynamicTableDataByUuid(matrixVo.getUuid(), uuid);
                                    dataMapper.insertDynamicTableData(columnList, dataList, matrixVo.getUuid());
                                    update++;
                                }else {
                                    unExist++;
                                }
                            }else {
                                dataMapper.insertDynamicTableData(columnList, dataList, matrixVo.getUuid());
                                insert++;
                            }
                        }
                    }
                }else {
                    throw new MatrixDataNotFoundException(matrixName);
                }
            }
        }
        returnObj.put("insert", insert);
        returnObj.put("update", update);
        returnObj.put("unExist", unExist);
        return returnObj;
    }

    private String getCellValue (Cell cell){
        String value = "";
        if (cell != null){
            if (cell.getCellType() != Cell.CELL_TYPE_BLANK){
                switch (cell.getCellType()){
                    case Cell.CELL_TYPE_NUMERIC :
                        if (DateUtil.isCellDateFormatted(cell)){
                            value = formatter.format(cell.getDateCellValue());
                        }else {
                            value = String.valueOf(cell.getNumericCellValue());
                        }
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        value = String.valueOf(cell.getBooleanCellValue());
                        break;
                    case Cell.CELL_TYPE_FORMULA:
                        value = cell.getCellFormula();
                        break;
                    default:
                        value = cell.getStringCellValue();
                        break;
                }
            }
        }
        return value;
    }
}
