package codedriver.module.process.api.processtask;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.FormAttributeVo;
import codedriver.framework.process.dto.FormVersionVo;
import codedriver.framework.process.dto.ProcessFormVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.form.FormHasNoAttributeException;
import codedriver.framework.process.exception.form.FormNotFoundException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.ExcelUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
@Service
@Transactional
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskTemplateExportApi extends PrivateBinaryStreamApiComponentBase {
    static Logger logger = LoggerFactory.getLogger(ProcessTaskTemplateExportApi.class);

    @Autowired
    private ChannelMapper channelMapper;
    @Autowired
    private ProcessMapper processMapper;
    @Autowired
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

    @Input({@Param(name="channelUuid", type= ApiParamType.STRING, isRequired=true, desc="服务uuid")})
    @Output({})
    @Description(desc = "导出工单表格模版")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String channelUuid = paramObj.getString("channelUuid");
        ChannelVo channel = channelMapper.getChannelByUuid(channelUuid);
        if(channel == null){
            throw new ChannelNotFoundException(channelUuid);
        }
        String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
        if(processMapper.checkProcessIsExists(processUuid) == 0) {
            throw new ProcessNotFoundException(processUuid);
        }
        ProcessFormVo processForm = processMapper.getProcessFormByProcessUuid(processUuid);
        if(processForm == null || formMapper.checkFormIsExists(processForm.getFormUuid()) == 0){
            throw new FormNotFoundException(processForm.getFormUuid());
        }
        FormVersionVo formVersionVo = formMapper.getActionFormVersionByFormUuid(processForm.getFormUuid());
        List<FormAttributeVo> formAttributeList = null;
        if (formVersionVo != null && StringUtils.isNotBlank(formVersionVo.getFormConfig())) {
            formAttributeList = formVersionVo.getFormAttributeList();
        }
        if(CollectionUtils.isEmpty(formAttributeList)){
            throw new FormHasNoAttributeException(processForm.getFormUuid());
        }
        List<String> headerList = formAttributeList.stream().map(FormAttributeVo::getLabel).collect(Collectors.toList());
        headerList.add(0,"标题");
        headerList.add(1,"请求人");
        headerList.add(2,"优先级");
        headerList.add(headerList.size(),"描述");
        List<String> channelData = new ArrayList<>();
        channelData.add("服务名称：");
        channelData.add(channel.getName());
        channelData.add("服务UUID(禁止修改)：");
        channelData.add(channelUuid);
        OutputStream os = null;
        Workbook workbook = new XSSFWorkbook();
        try{
            ExcelUtil.exportProcessTaskTemplate(workbook,headerList,null,null,channelData,25);
            String fileNameEncode = channel.getName() + "-上报模版.xlsx";
            Boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
            if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
                fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
            } else {
                fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
            }
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;fileName=\"" + fileNameEncode + "\"");
            os = response.getOutputStream();
            workbook.write(os);
        }catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }finally {
            if (os != null) {
                os.flush();
                os.close();
            }
        }

        return null;
    }
}
