package codedriver.module.process.api.processtask.datamigration;

import codedriver.framework.process.exception.form.FormImportException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Map;

@SuppressWarnings("deprecation")
@Service
@Transactional
@OperationType(type = OperationTypeEnum.OPERATE)
public class ProcessTaskImportFromExcelApi extends PrivateBinaryStreamApiComponentBase {
    static Logger logger = LoggerFactory.getLogger(ProcessTaskImportFromExcelApi.class);

    @Override
    public String getToken() {
        return "processtask/import/fromexcel";
    }

    @Override
    public String getName() {
        return "导入工单数据(通过固定格式excel文件)";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({

    })
    @Output({

    })
    @Description(desc = "导入工单数据(通过固定格式excel文件)")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        /**
         * 根据服务ID寻找对应的流程和表单，以此判断导入的excel格式是否合法
         */
        String channelUuid = paramObj.getString("channelUuid");

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        //获取所有导入文件
        Map<String, MultipartFile> multipartFileMap = multipartRequest.getFileMap();
        //如果没有导入文件, 抛异常
        if(multipartFileMap == null || multipartFileMap.isEmpty()) {
            throw new FormImportException("没有导入文件");
        }
        MultipartFile multipartFile = null;
        for(Map.Entry<String, MultipartFile> entry : multipartFileMap.entrySet()) {
            multipartFile = entry.getValue();
            InputStream in = multipartFile.getInputStream();
            HSSFWorkbook wb = null;
            wb = new HSSFWorkbook(in);
            Sheet sheet = wb.getSheetAt(0);
            int numberOfCells = sheet.getRow(1).getPhysicalNumberOfCells();
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);
                for(int j = 0;j < numberOfCells;j++){
                    Cell cell = row.getCell(j);
                    String value = cell.getStringCellValue();
                    System.out.println(value);
                }
            }
        }
        return null;
    }
}
