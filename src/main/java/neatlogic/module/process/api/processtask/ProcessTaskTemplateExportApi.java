package neatlogic.module.process.api.processtask;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeHandler;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormActiveVersionNotFoundExcepiton;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;
import neatlogic.framework.process.util.ProcessConfigUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.ExcelUtil;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings("deprecation")
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskTemplateExportApi extends PrivateBinaryStreamApiComponentBase {
    static Logger logger = LoggerFactory.getLogger(ProcessTaskTemplateExportApi.class);

    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private ProcessMapper processMapper;
    @Resource
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "processtask/template/export";
    }

    @Override
    public String getName() {
        return "导出工单表格模版";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "channelUuid", type = ApiParamType.STRING, isRequired = true, desc = "服务uuid")})
    @Output({})
    @Description(desc = "导出工单表格模版")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String channelUuid = paramObj.getString("channelUuid");
        ChannelVo channel = channelMapper.getChannelByUuid(channelUuid);
        if (channel == null) {
            throw new ChannelNotFoundException(channelUuid);
        }
        String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
        ProcessVo processVo = processMapper.getProcessBaseInfoByUuid(processUuid);
        if (processVo == null) {
            throw new ProcessNotFoundException(processUuid);
        }
        List<String> headerList = new ArrayList<>();
        headerList.add("标题(必填)");
        headerList.add("请求人(必填)");
        List<ChannelPriorityVo> priorityVos = channelMapper.getChannelPriorityListByChannelUuid(channelUuid);
        if (CollectionUtils.isNotEmpty(priorityVos)) {
            headerList.add("优先级(必填)");
        }
        List<FormAttributeVo> formAttributeVoList = new ArrayList<>();
        JSONObject configObj = processVo.getConfig();
        JSONObject processObj = configObj.getJSONObject("process");
        JSONObject formConfigObj = processObj.getJSONObject("formConfig");
        if (MapUtils.isNotEmpty(formConfigObj)) {
            String formUuid = formConfigObj.getString("uuid");
            if (StringUtils.isNotBlank(formUuid)) {
                FormVersionVo formVersionVo = formMapper.getActionFormVersionByFormUuid(formUuid);
                if (formVersionVo == null) {
                    FormVo formVo = formMapper.getFormByUuid(formUuid);
                    if (formVo == null) {
                        throw new FormNotFoundException(formUuid);
                    } else {
                        throw new FormActiveVersionNotFoundExcepiton(formVo.getName());
                    }
                }
                ProcessStepVo processStepVo = processMapper.getStartProcessStepByProcessUuid(processUuid);
                JSONArray stepList = processObj.getJSONArray("stepList");
                for (int i = 0; i < stepList.size(); i++) {
                    JSONObject stepObj = stepList.getJSONObject(i);
                    if (Objects.equals(stepObj.getString("uuid"), processStepVo.getUuid())) {
                        JSONObject stepConfig = stepObj.getJSONObject("stepConfig");
                        String formSceneUuid = stepConfig.getString("formSceneUuid");
                        if (StringUtils.isNotBlank(formSceneUuid)) {
                            formVersionVo.setSceneUuid(formSceneUuid);
                        }
                    }
                }
                formAttributeVoList = formVersionVo.getFormAttributeList();
            }
        }
        if (CollectionUtils.isNotEmpty(formAttributeVoList)) {
            for (FormAttributeVo formAttributeVo : formAttributeVoList) {
                IFormAttributeHandler formAttributeHandler = FormAttributeHandlerFactory.getHandler(formAttributeVo.getHandler());
                if (formAttributeHandler == null) {
                    continue;
                }
                if (formAttributeHandler.isProcessTaskBatchSubmissionTemplateParam()) {
                    if (formAttributeVo.isRequired()) {
                        headerList.add(formAttributeVo.getLabel() + "(必填)");
                    } else {
                        headerList.add(formAttributeVo.getLabel());
                    }
                }
            }
        }

        int isNeedContent = 0;
        /** 判断是否需要描述框 */
        isNeedContent = ProcessConfigUtil.getIsNeedContent(configObj);
        if (isNeedContent == 1) {
            headerList.add("描述");
        }
        List<String> channelData = new ArrayList<>();
        channelData.add("服务名称：");
        channelData.add(channel.getName());
        channelData.add("服务UUID(禁止修改)：");
        channelData.add(channelUuid);
        channelData.add("注意：不支持导入静态列表与动态列表，多个值之间用英文逗号\",\"隔开；单元格格式统一为文本");
        OutputStream os = null;
        Workbook workbook = new XSSFWorkbook();
        try {
            exportProcessTaskTemplate(workbook, headerList, null, null, channelData, 25);
            String fileNameEncode = channel.getName() + "-上报模版.xlsx";
            Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
            if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
                fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
            } else {
                fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
            }
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");
            os = response.getOutputStream();
            workbook.write(os);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
        }

        return null;
    }

    private Workbook exportProcessTaskTemplate(Workbook workbook, List<String> headerList, List<String> columnList, List<Map<String, Object>> dataMapList, List<String> channelData, Integer columnWidth) throws Exception {
        // 生成一个表格
        Sheet sheet = workbook.createSheet();
        // 设置sheet名字
        workbook.setSheetName(0, "sheet");
        Map<String, CellStyle> cellStyle = ExcelUtil.getRowCellStyle(workbook);
        CellStyle firstRowcellStyle = cellStyle.get("firstRowcellStyle");
        CellStyle rowcellStyle = cellStyle.get("rowcellStyle");

        /** 生成服务信息行 */
        Row channelRow = sheet.createRow(0);
        for (int i = 0; i < channelData.size(); i++) {
            Cell cell = channelRow.createCell(i);
            cell.setCellValue(channelData.get(i));
        }

        ExcelUtil.createRows(headerList, columnList, dataMapList, columnWidth, sheet, firstRowcellStyle, rowcellStyle, 1);
        return workbook;
    }
}
