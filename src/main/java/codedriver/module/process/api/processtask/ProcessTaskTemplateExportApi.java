package codedriver.module.process.api.processtask;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.process.formattribute.core.FormAttributeHandlerFactory;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.ExcelUtil;
import codedriver.module.process.formattribute.handler.DivideHandler;
import codedriver.module.process.formattribute.handler.LinkHandler;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
@Service
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
        boolean isShowAllAttr = false;
        Set<String> showAttrs = new HashSet<>();
        Set<Integer> showAttrRows = new HashSet<>();
        int isNeedContent = 0;
        ProcessVo process = processMapper.getProcessBaseInfoByUuid(processUuid);
        JSONObject configObj = process.getConfigObj();
        if(MapUtils.isNotEmpty(configObj)){
            /** 判断要不要生成“描述”列
             * 1、从stepList获取开始节点
             * 2、从connectionList获取开始节点后的第一个节点
             * 3、从stepList获取开始节点后的第一个节点是否启用描述框
             */
            JSONObject processConfig = configObj.getJSONObject("process");
            JSONArray stepList = processConfig.getJSONArray("stepList");
            if(MapUtils.isNotEmpty(processConfig) && CollectionUtils.isNotEmpty(stepList)){
                /** 获取开始节点UUID */
                String startUuid = "";
                for(Object obj : stepList){
                    JSONObject jsonObject = JSONObject.parseObject(obj.toString());
                    if(ProcessStepHandlerType.START.getHandler().equals(jsonObject.getString("handler"))){
                        startUuid = jsonObject.getString("uuid");
                        break;
                    }
                }
                JSONArray connectionList = processConfig.getJSONArray("connectionList");
                /** 获取开始节点后的第一个节点UUID */
                String firstStepUuid = "";
                if(CollectionUtils.isNotEmpty(connectionList)){
                    for(Object obj : connectionList){
                        JSONObject jsonObject = JSONObject.parseObject(obj.toString());
                        if(jsonObject.getString("fromStepUuid").equals(startUuid)){
                            firstStepUuid = jsonObject.getString("toStepUuid");
                            break;
                        }
                    }
                }
                /** 获取开始节点后的第一个节点是否启用描述框 */
                for(Object obj : stepList){
                    JSONObject jsonObject = JSONObject.parseObject(obj.toString());
                    if(jsonObject.getString("uuid").equals(firstStepUuid)){
                        isNeedContent = jsonObject.getJSONObject("stepConfig").getJSONObject("workerPolicyConfig").getIntValue("isNeedContent");
                        break;
                    }
                }

                /** 获取可编辑的表单属性UUID */
                JSONArray authorityList = (JSONArray) JSONPath.read(processConfig.toJSONString(), "formConfig.authorityList");
                if(CollectionUtils.isNotEmpty(authorityList)){
                    for(Object o : authorityList){
                        JSONObject object = JSONObject.parseObject(o.toString());
                        String action = object.getString("action");
                        JSONArray attributeUuidList = object.getJSONArray("attributeUuidList");
                        JSONArray processStepUuidList = object.getJSONArray("processStepUuidList");
                        String type = object.getString("type");
                        /** authorityList中存在可编辑与隐藏的表单属性配置
                         * 取可编辑的配置，如果以组件为单位，则直接记录属性UUID
                         * 如果以行为单位，则记录下可编辑的行
                         * 如果发现有attributeUuidList为"all"的配置项，则退出循环
                         */
                        if(CollectionUtils.isNotEmpty(processStepUuidList) && processStepUuidList.contains(firstStepUuid)
                        && StringUtils.isNotBlank(action) && "edit".equals(action) && CollectionUtils.isNotEmpty(attributeUuidList)
                        && StringUtils.isNotBlank(type)){
                            if("component".equals(type)){
                                if("all".equals(attributeUuidList.get(0).toString())){
                                    isShowAllAttr = true;
                                    showAttrs.clear();
                                    break;
                                }else{
                                    showAttrs.addAll(attributeUuidList.toJavaList(String.class));
                                }
                            }else if("row".equals(type)){
                                showAttrRows.addAll(attributeUuidList.toJavaList(Integer.class));
                            }
                        }
                    }
                }
            }
        }

        ProcessFormVo processForm = processMapper.getProcessFormByProcessUuid(processUuid);
        List<FormAttributeVo> formAttributeList = null;
        List<String> headerList = new ArrayList<>();
        if(processForm != null && formMapper.checkFormIsExists(processForm.getFormUuid()) > 0){
            FormVersionVo formVersionVo = formMapper.getActionFormVersionByFormUuid(processForm.getFormUuid());
            if (formVersionVo != null && StringUtils.isNotBlank(formVersionVo.getFormConfig())) {
                formAttributeList = formVersionVo.getFormAttributeList();
                /** 如果不是所有属性都可编辑，那么就根据行号查找可编辑的属性 */
                String formConfig = formVersionVo.getFormConfig();
                JSONArray tableList = (JSONArray)JSONPath.read(formConfig, "sheetsConfig.tableList");
                if(!isShowAllAttr && CollectionUtils.isNotEmpty(tableList) && CollectionUtils.isNotEmpty(showAttrRows)){
                    List<Integer> list = showAttrRows.stream().sorted().collect(Collectors.toList());
                    for(Integer i : list){
                        JSONArray array = JSONArray.parseArray(tableList.get(i-1).toString());
                        for(Object o : array){
                            if(JSONObject.parseObject(o.toString()) != null && JSONObject.parseObject(o.toString()).getJSONObject("component") != null){
                                String handler = JSONPath.read(o.toString(),"component.handler").toString();
                                /** 过滤掉分割线与链接 */
                                if(!(FormAttributeHandlerFactory.getHandler(handler) instanceof DivideHandler)
                                        && !(FormAttributeHandlerFactory.getHandler(handler) instanceof LinkHandler))
                                showAttrs.add(JSONObject.parseObject(o.toString()).getJSONObject("component").getString("uuid"));
                            }
                        }
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(formAttributeList)){
            Iterator<FormAttributeVo> iterator = formAttributeList.iterator();
            while (iterator.hasNext()){
                FormAttributeVo next = iterator.next();
                /** 过滤掉分割线与链接 */
                if((FormAttributeHandlerFactory.getHandler(next.getHandler()) instanceof DivideHandler)
                        || (FormAttributeHandlerFactory.getHandler(next.getHandler()) instanceof LinkHandler)){
                    continue;
                }
                /**
                 * 默认所有表单属性都是只读的
                 * 如果有配置所有属性可编辑，或者发现某些属性可编辑，则列在表头上
                 */
                if(isShowAllAttr || (CollectionUtils.isNotEmpty(showAttrs) && showAttrs.contains(next.getUuid()))){
                    if(next.isRequired()){
                        next.setLabel(next.getLabel() + "(必填)");
                    }
                    headerList.add(next.getLabel());
                }
            }
        }
        headerList.add(0,"标题(必填)");
        headerList.add(1,"请求人(必填)");
        headerList.add(2,"优先级(必填)");
        if(isNeedContent == 1){
            headerList.add(headerList.size(),"描述");
        }
        List<String> channelData = new ArrayList<>();
        channelData.add("服务名称：");
        channelData.add(channel.getName());
        channelData.add("服务UUID(禁止修改)：");
        channelData.add(channelUuid);
        OutputStream os = null;
        Workbook workbook = new XSSFWorkbook();
        try{
            exportProcessTaskTemplate(workbook,headerList,null,null,channelData,25);
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

    private Workbook exportProcessTaskTemplate(Workbook workbook, List<String> headerList, List<String> columnList, List<Map<String,Object>> dataMapList, List<String> channelData, Integer columnWidth) throws Exception {
        // 生成一个表格
        Sheet sheet = workbook.createSheet();
        // 设置sheet名字
        workbook.setSheetName(0,"sheet");
        Map<String, CellStyle> cellStyle = ExcelUtil.getRowCellStyle(workbook);
        CellStyle firstRowcellStyle = cellStyle.get("firstRowcellStyle");
        CellStyle rowcellStyle = cellStyle.get("rowcellStyle");

        /** 生成服务信息行 */
        Row channelRow = sheet.createRow(0);
        for(int i = 0;i < channelData.size();i++){
            Cell cell = channelRow.createCell(i);
            cell.setCellValue(channelData.get(i));
        }

        ExcelUtil.createRows(headerList, columnList, dataMapList, columnWidth, sheet, firstRowcellStyle, rowcellStyle, 1);
        return workbook;
    }
}
