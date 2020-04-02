package codedriver.module.process.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-31 17:00
 **/
public class ExcelUtil {

    public static void exportExcel(List<String> headerList, List<String> columnList, List<List<String>> columnSelectValueList, List<Map<String, String>> dataMapList, OutputStream os)
    {
        HSSFWorkbook workbook = new HSSFWorkbook();
        workbook.createSheet("sheet01");

        // 声明一个工作薄   生成一个表格
        HSSFSheet sheet = workbook.getSheet("sheet01");
        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth((short) 15);
        // 生成一个样式
        HSSFCellStyle style = workbook.createCellStyle();
        // 设置这些样式
        style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        // 生成一个字体
        HSSFFont font = workbook.createFont();
        font.setColor(HSSFColor.VIOLET.index);
        font.setFontHeightInPoints((short) 12);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        // 把字体应用到当前的样式
        style.setFont(font);
        // 生成并设置另一个样式
        HSSFCellStyle style2 = workbook.createCellStyle();
        style2.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
        style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style2.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style2.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        style2.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        // 生成另一个字体
        HSSFFont font2 = workbook.createFont();
        font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
        // 把字体应用到当前的样式
        style2.setFont(font2);
//        // 声明一个画图的顶级管理器
//        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
//        // 定义注释的大小和位置,详见文档
//        HSSFComment comment = patriarch.createComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 6, 5));
//        // 设置注释内容
//        comment.setString(new HSSFRichTextString("可以在POI中添加注释！"));
//        // 设置注释作者，当鼠标移动到单元格上是可以在状态栏中看到该内容.
//        comment.setAuthor("leno");

        // 产生表格标题行
        HSSFRow row = sheet.createRow(0);
        int i = 0;
        String[] textlists = {"test1","test2"};
        for (String header : headerList) {
            HSSFCell cell = row.createCell(i);
            cell.setAsActiveCell();
            cell.setCellStyle(style);
            HSSFRichTextString text = new HSSFRichTextString(header);
            cell.setCellValue(text);
            List<String> defaultValueList = columnSelectValueList.get(i);
            //行添加下拉框
            if (CollectionUtils.isNotEmpty(defaultValueList)){
                String[] values = new String[defaultValueList.size()];
                defaultValueList.toArray(values);
                CellRangeAddressList region = new CellRangeAddressList();
                region.addCellRangeAddress(1, i, SpreadsheetVersion.EXCEL97.getLastRowIndex(), i);
                DVConstraint constraint = DVConstraint.createExplicitListConstraint(values);
                HSSFDataValidation data_validation_list = new HSSFDataValidation(region, constraint);
                //将有效性验证添加到表单
                sheet.addValidationData(data_validation_list);
            }
            i++;
        }

        if (CollectionUtils.isNotEmpty(dataMapList)){
            int index = 0;
            for (Map<String, String> dataMap : dataMapList){
                index++;
                row = sheet.createRow(index);
                int j = 0;
                for (String column :columnList){
                    HSSFCell cell = row.createCell(j);
                    cell.setCellStyle(style2);
                    HSSFRichTextString richString = new HSSFRichTextString(dataMap.get(column));
                    HSSFFont font3 = workbook.createFont();
                    font3.setColor(HSSFColor.BLUE.index);
                    richString.applyFont(font3);
                    cell.setCellValue(richString);
                    j++;
                }
            }
        }
        try {
            workbook.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void exportExcelHeaders(List<String> headerList, List<List<String>> columnSelectValueList, OutputStream os){
        exportExcel(headerList, null, columnSelectValueList, null, os);
    }
}
