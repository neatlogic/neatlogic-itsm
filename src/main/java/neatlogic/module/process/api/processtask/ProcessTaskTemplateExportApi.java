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
import neatlogic.framework.util.FileUtil;
import neatlogic.framework.util.excel.ExcelBuilder;
import neatlogic.framework.util.excel.SheetBuilder;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;
import neatlogic.framework.process.util.ProcessConfigUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.binarystream.PrivateBinaryStreamApiComponentBase;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
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
        return "nmpap.processtasktemplateexportapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "channelUuid", type = ApiParamType.STRING, isRequired = true, desc = "nmpap.processtasktemplateexportapi.input.param.desc.channeluuid")})
    @Output({})
    @Description(desc = "nmpap.processtasktemplateexportapi.getname")
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

        String fileNameEncode = channel.getName() + "-上报模版.xlsx";
        fileNameEncode = FileUtil.getEncodedFileName(fileNameEncode);
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");

        ExcelBuilder builder = new ExcelBuilder(SXSSFWorkbook.class);
        SheetBuilder sheetBuilder = builder.withBorderColor(HSSFColor.HSSFColorPredefined.GREY_40_PERCENT)
                .withHeadFontColor(HSSFColor.HSSFColorPredefined.WHITE)
                .withHeadBgColor(HSSFColor.HSSFColorPredefined.DARK_BLUE)
                .withColumnWidth(30)
                .addSheet("sheet")
                ;

        try (Workbook workbook = builder.build();
             OutputStream os = response.getOutputStream()) {
            Sheet sheet = workbook.getSheet("sheet");
            /** 生成服务信息行 */
            Row channelRow = sheet.createRow(0);
            for (int i = 0; i < channelData.size(); i++) {
                Cell cell = channelRow.createCell(i);
                cell.setCellValue(channelData.get(i));
            }
            int columnWidth = 25;
            //生成标题行
            Row headerRow = sheet.createRow(1);
            if (CollectionUtils.isNotEmpty(headerList)) {
                int i = 0;
                for (String header : headerList) {
                    //设置列宽
                    sheet.setColumnWidth(i, columnWidth * 256);
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(header);
                    i++;
                }
            }
            workbook.write(os);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
